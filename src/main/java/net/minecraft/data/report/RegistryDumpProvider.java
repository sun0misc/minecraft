package net.minecraft.data.report;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RegistryDumpProvider implements DataProvider {
   private final DataOutput output;

   public RegistryDumpProvider(DataOutput output) {
      this.output = output;
   }

   public CompletableFuture run(DataWriter writer) {
      JsonObject jsonObject = new JsonObject();
      Registries.REGISTRIES.streamEntries().forEach((entry) -> {
         jsonObject.add(entry.registryKey().getValue().toString(), toJson((Registry)entry.value()));
      });
      Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("registries.json");
      return DataProvider.writeToPath(writer, jsonObject, path);
   }

   private static JsonElement toJson(Registry registry) {
      JsonObject jsonObject = new JsonObject();
      if (registry instanceof DefaultedRegistry) {
         Identifier lv = ((DefaultedRegistry)registry).getDefaultId();
         jsonObject.addProperty("default", lv.toString());
      }

      int i = Registries.REGISTRIES.getRawId(registry);
      jsonObject.addProperty("protocol_id", i);
      JsonObject jsonObject2 = new JsonObject();
      registry.streamEntries().forEach((entry) -> {
         Object object = entry.value();
         int i = registry.getRawId(object);
         JsonObject jsonObject2x = new JsonObject();
         jsonObject2x.addProperty("protocol_id", i);
         jsonObject2.add(entry.registryKey().getValue().toString(), jsonObject2x);
      });
      jsonObject.add("entries", jsonObject2);
      return jsonObject;
   }

   public final String getName() {
      return "Registry Dump";
   }
}
