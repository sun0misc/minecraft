package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

public class NoiseBasedCountPlacementModifier extends AbstractCountPlacementModifier {
   public static final Codec MODIFIER_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("noise_to_count_ratio").forGetter((arg) -> {
         return arg.noiseToCountRatio;
      }), Codec.DOUBLE.fieldOf("noise_factor").forGetter((arg) -> {
         return arg.noiseFactor;
      }), Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0).forGetter((arg) -> {
         return arg.noiseOffset;
      })).apply(instance, NoiseBasedCountPlacementModifier::new);
   });
   private final int noiseToCountRatio;
   private final double noiseFactor;
   private final double noiseOffset;

   private NoiseBasedCountPlacementModifier(int noiseToCountRatio, double noiseFactor, double noiseOffset) {
      this.noiseToCountRatio = noiseToCountRatio;
      this.noiseFactor = noiseFactor;
      this.noiseOffset = noiseOffset;
   }

   public static NoiseBasedCountPlacementModifier of(int noiseToCountRatio, double noiseFactor, double noiseOffset) {
      return new NoiseBasedCountPlacementModifier(noiseToCountRatio, noiseFactor, noiseOffset);
   }

   protected int getCount(Random random, BlockPos pos) {
      double d = Biome.FOLIAGE_NOISE.sample((double)pos.getX() / this.noiseFactor, (double)pos.getZ() / this.noiseFactor, false);
      return (int)Math.ceil((d + this.noiseOffset) * (double)this.noiseToCountRatio);
   }

   public PlacementModifierType getType() {
      return PlacementModifierType.NOISE_BASED_COUNT;
   }
}
