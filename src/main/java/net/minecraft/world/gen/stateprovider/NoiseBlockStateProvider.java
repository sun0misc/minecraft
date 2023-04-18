package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;

public class NoiseBlockStateProvider extends AbstractNoiseBlockStateProvider {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillNoiseCodecFields(instance).apply(instance, NoiseBlockStateProvider::new);
   });
   protected final List states;

   protected static Products.P4 fillNoiseCodecFields(RecordCodecBuilder.Instance instance) {
      return fillCodecFields(instance).and(Codec.list(BlockState.CODEC).fieldOf("states").forGetter((arg) -> {
         return arg.states;
      }));
   }

   public NoiseBlockStateProvider(long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, List states) {
      super(seed, noiseParameters, scale);
      this.states = states;
   }

   protected BlockStateProviderType getType() {
      return BlockStateProviderType.NOISE_PROVIDER;
   }

   public BlockState get(Random random, BlockPos pos) {
      return this.getStateFromList(this.states, pos, (double)this.scale);
   }

   protected BlockState getStateFromList(List states, BlockPos pos, double scale) {
      double e = this.getNoiseValue(pos, scale);
      return this.getStateAtValue(states, e);
   }

   protected BlockState getStateAtValue(List states, double value) {
      double e = MathHelper.clamp((1.0 + value) / 2.0, 0.0, 0.9999);
      return (BlockState)states.get((int)(e * (double)states.size()));
   }
}
