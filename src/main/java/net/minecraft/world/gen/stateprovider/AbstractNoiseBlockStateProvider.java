package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;

public abstract class AbstractNoiseBlockStateProvider extends BlockStateProvider {
   protected final long seed;
   protected final DoublePerlinNoiseSampler.NoiseParameters noiseParameters;
   protected final float scale;
   protected final DoublePerlinNoiseSampler noiseSampler;

   protected static Products.P3 fillCodecFields(RecordCodecBuilder.Instance instance) {
      return instance.group(Codec.LONG.fieldOf("seed").forGetter((arg) -> {
         return arg.seed;
      }), DoublePerlinNoiseSampler.NoiseParameters.CODEC.fieldOf("noise").forGetter((arg) -> {
         return arg.noiseParameters;
      }), Codecs.POSITIVE_FLOAT.fieldOf("scale").forGetter((arg) -> {
         return arg.scale;
      }));
   }

   protected AbstractNoiseBlockStateProvider(long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale) {
      this.seed = seed;
      this.noiseParameters = noiseParameters;
      this.scale = scale;
      this.noiseSampler = DoublePerlinNoiseSampler.create(new ChunkRandom(new CheckedRandom(seed)), noiseParameters);
   }

   protected double getNoiseValue(BlockPos pos, double scale) {
      return this.noiseSampler.sample((double)pos.getX() * scale, (double)pos.getY() * scale, (double)pos.getZ() * scale);
   }
}
