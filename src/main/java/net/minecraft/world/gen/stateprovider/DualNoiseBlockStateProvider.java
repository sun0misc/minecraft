package net.minecraft.world.gen.stateprovider;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;

public class DualNoiseBlockStateProvider extends NoiseBlockStateProvider {
   public static final Codec DUAL_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Range.createRangedCodec(Codec.INT, 1, 64).fieldOf("variety").forGetter((arg) -> {
         return arg.variety;
      }), DoublePerlinNoiseSampler.NoiseParameters.CODEC.fieldOf("slow_noise").forGetter((arg) -> {
         return arg.slowNoiseParameters;
      }), Codecs.POSITIVE_FLOAT.fieldOf("slow_scale").forGetter((arg) -> {
         return arg.slowScale;
      })).and(fillNoiseCodecFields(instance)).apply(instance, DualNoiseBlockStateProvider::new);
   });
   private final Range variety;
   private final DoublePerlinNoiseSampler.NoiseParameters slowNoiseParameters;
   private final float slowScale;
   private final DoublePerlinNoiseSampler slowNoiseSampler;

   public DualNoiseBlockStateProvider(Range variety, DoublePerlinNoiseSampler.NoiseParameters slowNoiseParameters, float slowScale, long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, List states) {
      super(seed, noiseParameters, scale, states);
      this.variety = variety;
      this.slowNoiseParameters = slowNoiseParameters;
      this.slowScale = slowScale;
      this.slowNoiseSampler = DoublePerlinNoiseSampler.create(new ChunkRandom(new CheckedRandom(seed)), slowNoiseParameters);
   }

   protected BlockStateProviderType getType() {
      return BlockStateProviderType.DUAL_NOISE_PROVIDER;
   }

   public BlockState get(Random random, BlockPos pos) {
      double d = this.getSlowNoiseValue(pos);
      int i = (int)MathHelper.clampedMap(d, -1.0, 1.0, (double)(Integer)this.variety.minInclusive(), (double)((Integer)this.variety.maxInclusive() + 1));
      List list = Lists.newArrayListWithCapacity(i);

      for(int j = 0; j < i; ++j) {
         list.add(this.getStateAtValue(this.states, this.getSlowNoiseValue(pos.add(j * '픑', 0, j * '薺'))));
      }

      return this.getStateFromList(list, pos, (double)this.scale);
   }

   protected double getSlowNoiseValue(BlockPos pos) {
      return this.slowNoiseSampler.sample((double)((float)pos.getX() * this.slowScale), (double)((float)pos.getY() * this.slowScale), (double)((float)pos.getZ() * this.slowScale));
   }
}
