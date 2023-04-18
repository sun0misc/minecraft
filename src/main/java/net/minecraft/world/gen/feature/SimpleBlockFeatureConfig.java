package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record SimpleBlockFeatureConfig(BlockStateProvider toPlace) implements FeatureConfig {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockStateProvider.TYPE_CODEC.fieldOf("to_place").forGetter((config) -> {
         return config.toPlace;
      })).apply(instance, SimpleBlockFeatureConfig::new);
   });

   public SimpleBlockFeatureConfig(BlockStateProvider toPlace) {
      this.toPlace = toPlace;
   }

   public BlockStateProvider toPlace() {
      return this.toPlace;
   }
}
