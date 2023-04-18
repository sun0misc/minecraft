package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class NetherForestVegetationFeatureConfig extends BlockPileFeatureConfig {
   public static final Codec VEGETATION_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BlockStateProvider.TYPE_CODEC.fieldOf("state_provider").forGetter((config) -> {
         return config.stateProvider;
      }), Codecs.POSITIVE_INT.fieldOf("spread_width").forGetter((config) -> {
         return config.spreadWidth;
      }), Codecs.POSITIVE_INT.fieldOf("spread_height").forGetter((config) -> {
         return config.spreadHeight;
      })).apply(instance, NetherForestVegetationFeatureConfig::new);
   });
   public final int spreadWidth;
   public final int spreadHeight;

   public NetherForestVegetationFeatureConfig(BlockStateProvider stateProvider, int spreadWidth, int spreadHeight) {
      super(stateProvider);
      this.spreadWidth = spreadWidth;
      this.spreadHeight = spreadHeight;
   }
}
