package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;

public class WorldPreset {
   public static final Codec CODEC = Codecs.validate(RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.DIMENSION), DimensionOptions.CODEC).fieldOf("dimensions").forGetter((preset) -> {
         return preset.dimensions;
      })).apply(instance, WorldPreset::new);
   }), WorldPreset::validate);
   public static final Codec ENTRY_CODEC;
   private final Map dimensions;

   public WorldPreset(Map dimensions) {
      this.dimensions = dimensions;
   }

   private Registry createDimensionOptionsRegistry() {
      MutableRegistry lv = new SimpleRegistry(RegistryKeys.DIMENSION, Lifecycle.experimental());
      DimensionOptionsRegistryHolder.streamAll(this.dimensions.keySet().stream()).forEach((registryKey) -> {
         DimensionOptions lvx = (DimensionOptions)this.dimensions.get(registryKey);
         if (lvx != null) {
            lv.add(registryKey, lvx, Lifecycle.stable());
         }

      });
      return lv.freeze();
   }

   public DimensionOptionsRegistryHolder createDimensionsRegistryHolder() {
      return new DimensionOptionsRegistryHolder(this.createDimensionOptionsRegistry());
   }

   public Optional getOverworld() {
      return Optional.ofNullable((DimensionOptions)this.dimensions.get(DimensionOptions.OVERWORLD));
   }

   private static DataResult validate(WorldPreset preset) {
      return preset.getOverworld().isEmpty() ? DataResult.error(() -> {
         return "Missing overworld dimension";
      }) : DataResult.success(preset, Lifecycle.stable());
   }

   static {
      ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.WORLD_PRESET, CODEC);
   }
}
