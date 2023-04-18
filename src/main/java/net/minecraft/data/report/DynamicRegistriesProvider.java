package net.minecraft.data.report;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;

public class DynamicRegistriesProvider implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DataOutput output;
   private final CompletableFuture registryLookupFuture;

   public DynamicRegistriesProvider(DataOutput output, CompletableFuture registryLookupFuture) {
      this.registryLookupFuture = registryLookupFuture;
      this.output = output;
   }

   public CompletableFuture run(DataWriter writer) {
      return this.registryLookupFuture.thenCompose((lookup) -> {
         DynamicOps dynamicOps = RegistryOps.of(JsonOps.INSTANCE, (RegistryWrapper.WrapperLookup)lookup);
         return CompletableFuture.allOf((CompletableFuture[])RegistryLoader.DYNAMIC_REGISTRIES.stream().flatMap((entry) -> {
            return this.writeRegistryEntries(writer, lookup, dynamicOps, entry).stream();
         }).toArray((i) -> {
            return new CompletableFuture[i];
         }));
      });
   }

   private Optional writeRegistryEntries(DataWriter writer, RegistryWrapper.WrapperLookup lookup, DynamicOps ops, RegistryLoader.Entry registry) {
      RegistryKey lv = registry.key();
      return lookup.getOptionalWrapper(lv).map((wrapper) -> {
         DataOutput.PathResolver lvx = this.output.getResolver(DataOutput.OutputType.DATA_PACK, lv.getValue().getPath());
         return CompletableFuture.allOf((CompletableFuture[])wrapper.streamEntries().map((entry) -> {
            return writeToPath(lvx.resolveJson(entry.registryKey().getValue()), writer, ops, registry.elementCodec(), entry.value());
         }).toArray((i) -> {
            return new CompletableFuture[i];
         }));
      });
   }

   private static CompletableFuture writeToPath(Path path, DataWriter cache, DynamicOps json, Encoder encoder, Object value) {
      Optional optional = encoder.encodeStart(json, value).resultOrPartial((error) -> {
         LOGGER.error("Couldn't serialize element {}: {}", path, error);
      });
      return optional.isPresent() ? DataProvider.writeToPath(cache, (JsonElement)optional.get(), path) : CompletableFuture.completedFuture((Object)null);
   }

   public final String getName() {
      return "Registries";
   }
}
