package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.network.message.MessageType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

public class SerializableRegistries {
   private static final Map REGISTRIES = (Map)Util.make(() -> {
      ImmutableMap.Builder builder = ImmutableMap.builder();
      add(builder, RegistryKeys.BIOME, Biome.NETWORK_CODEC);
      add(builder, RegistryKeys.MESSAGE_TYPE, MessageType.CODEC);
      add(builder, RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC);
      add(builder, RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC);
      add(builder, RegistryKeys.DIMENSION_TYPE, DimensionType.CODEC);
      add(builder, RegistryKeys.DAMAGE_TYPE, DamageType.CODEC);
      return builder.build();
   });
   public static final Codec CODEC = createCodec();

   private static void add(ImmutableMap.Builder builder, RegistryKey key, Codec networkCodec) {
      builder.put(key, new Info(key, networkCodec));
   }

   private static Stream stream(DynamicRegistryManager dynamicRegistryManager) {
      return dynamicRegistryManager.streamAllRegistries().filter((entry) -> {
         return REGISTRIES.containsKey(entry.key());
      });
   }

   private static DataResult getNetworkCodec(RegistryKey registryRef) {
      return (DataResult)Optional.ofNullable((Info)REGISTRIES.get(registryRef)).map((info) -> {
         return info.networkCodec();
      }).map(DataResult::success).orElseGet(() -> {
         return DataResult.error(() -> {
            return "Unknown or not serializable registry: " + registryRef;
         });
      });
   }

   private static Codec createCodec() {
      Codec codec = Identifier.CODEC.xmap(RegistryKey::ofRegistry, RegistryKey::getValue);
      Codec codec2 = codec.partialDispatch("type", (registry) -> {
         return DataResult.success(registry.getKey());
      }, (registryRef) -> {
         return getNetworkCodec(registryRef).map((codec) -> {
            return RegistryCodecs.createRegistryCodec(registryRef, Lifecycle.experimental(), codec);
         });
      });
      UnboundedMapCodec unboundedMapCodec = Codec.unboundedMap(codec, codec2);
      return createDynamicRegistryManagerCodec(unboundedMapCodec);
   }

   private static Codec createDynamicRegistryManagerCodec(UnboundedMapCodec networkCodec) {
      return networkCodec.xmap(DynamicRegistryManager.ImmutableImpl::new, (registryManager) -> {
         return (Map)stream(registryManager).collect(ImmutableMap.toImmutableMap((entry) -> {
            return entry.key();
         }, (entry) -> {
            return entry.value();
         }));
      });
   }

   public static Stream streamDynamicEntries(CombinedDynamicRegistries combinedRegistries) {
      return stream(combinedRegistries.getSucceedingRegistryManagers(ServerDynamicRegistryType.WORLDGEN));
   }

   public static Stream streamRegistryManagerEntries(CombinedDynamicRegistries combinedRegistries) {
      Stream stream = combinedRegistries.get(ServerDynamicRegistryType.STATIC).streamAllRegistries();
      Stream stream2 = streamDynamicEntries(combinedRegistries);
      return Stream.concat(stream2, stream);
   }

   private static record Info(RegistryKey key, Codec networkCodec) {
      Info(RegistryKey arg, Codec codec) {
         this.key = arg;
         this.networkCodec = codec;
      }

      public RegistryKey key() {
         return this.key;
      }

      public Codec networkCodec() {
         return this.networkCodec;
      }
   }
}
