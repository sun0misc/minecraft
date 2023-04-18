package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;

public class NoiseThresholdBlockStateProvider extends AbstractNoiseBlockStateProvider {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillCodecFields(instance).and(instance.group(Codec.floatRange(-1.0F, 1.0F).fieldOf("threshold").forGetter((arg) -> {
         return arg.threshold;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("high_chance").forGetter((arg) -> {
         return arg.highChance;
      }), BlockState.CODEC.fieldOf("default_state").forGetter((arg) -> {
         return arg.defaultState;
      }), Codec.list(BlockState.CODEC).fieldOf("low_states").forGetter((arg) -> {
         return arg.lowStates;
      }), Codec.list(BlockState.CODEC).fieldOf("high_states").forGetter((arg) -> {
         return arg.highStates;
      }))).apply(instance, NoiseThresholdBlockStateProvider::new);
   });
   private final float threshold;
   private final float highChance;
   private final BlockState defaultState;
   private final List lowStates;
   private final List highStates;

   public NoiseThresholdBlockStateProvider(long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, float threshold, float highChance, BlockState defaultState, List lowStates, List highStates) {
      super(seed, noiseParameters, scale);
      this.threshold = threshold;
      this.highChance = highChance;
      this.defaultState = defaultState;
      this.lowStates = lowStates;
      this.highStates = highStates;
   }

   protected BlockStateProviderType getType() {
      return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
   }

   public BlockState get(Random random, BlockPos pos) {
      double d = this.getNoiseValue(pos, (double)this.scale);
      if (d < (double)this.threshold) {
         return (BlockState)Util.getRandom(this.lowStates, random);
      } else {
         return random.nextFloat() < this.highChance ? (BlockState)Util.getRandom(this.highStates, random) : this.defaultState;
      }
   }
}
