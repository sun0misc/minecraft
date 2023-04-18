package net.minecraft.world.gen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.AquiferSampler;

public class RavineCarver extends Carver {
   public RavineCarver(Codec codec) {
      super(codec);
   }

   public boolean shouldCarve(RavineCarverConfig arg, Random arg2) {
      return arg2.nextFloat() <= arg.probability;
   }

   public boolean carve(CarverContext arg, RavineCarverConfig arg2, Chunk arg3, Function function, Random arg4, AquiferSampler arg5, ChunkPos arg6, CarvingMask arg7) {
      int i = (this.getBranchFactor() * 2 - 1) * 16;
      double d = (double)arg6.getOffsetX(arg4.nextInt(16));
      int j = arg2.y.get(arg4, arg);
      double e = (double)arg6.getOffsetZ(arg4.nextInt(16));
      float f = arg4.nextFloat() * 6.2831855F;
      float g = arg2.verticalRotation.get(arg4);
      double h = (double)arg2.yScale.get(arg4);
      float k = arg2.shape.thickness.get(arg4);
      int l = (int)((float)i * arg2.shape.distanceFactor.get(arg4));
      int m = false;
      this.carveRavine(arg, arg2, arg3, function, arg4.nextLong(), arg5, d, (double)j, e, k, f, g, 0, l, h, arg7);
      return true;
   }

   private void carveRavine(CarverContext context, RavineCarverConfig config, Chunk chunk, Function posToBiome, long seed, AquiferSampler aquiferSampler, double x, double y, double z, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, CarvingMask mask) {
      Random lv = Random.create(seed);
      float[] fs = this.createHorizontalStretchFactors(context, config, lv);
      float n = 0.0F;
      float o = 0.0F;

      for(int p = branchStartIndex; p < branchCount; ++p) {
         double q = 1.5 + (double)(MathHelper.sin((float)p * 3.1415927F / (float)branchCount) * width);
         double r = q * yawPitchRatio;
         q *= (double)config.shape.horizontalRadiusFactor.get(lv);
         r = this.getVerticalScale(config, lv, r, (float)branchCount, (float)p);
         float s = MathHelper.cos(pitch);
         float t = MathHelper.sin(pitch);
         x += (double)(MathHelper.cos(yaw) * s);
         y += (double)t;
         z += (double)(MathHelper.sin(yaw) * s);
         pitch *= 0.7F;
         pitch += o * 0.05F;
         yaw += n * 0.05F;
         o *= 0.8F;
         n *= 0.5F;
         o += (lv.nextFloat() - lv.nextFloat()) * lv.nextFloat() * 2.0F;
         n += (lv.nextFloat() - lv.nextFloat()) * lv.nextFloat() * 4.0F;
         if (lv.nextInt(4) != 0) {
            if (!canCarveBranch(chunk.getPos(), x, z, p, branchCount, width)) {
               return;
            }

            this.carveRegion(context, config, chunk, posToBiome, aquiferSampler, x, y, z, q, r, mask, (contextx, scaledRelativeX, scaledRelativeY, scaledRelativeZ, yx) -> {
               return this.isPositionExcluded(contextx, fs, scaledRelativeX, scaledRelativeY, scaledRelativeZ, yx);
            });
         }
      }

   }

   private float[] createHorizontalStretchFactors(CarverContext context, RavineCarverConfig config, Random random) {
      int i = context.getHeight();
      float[] fs = new float[i];
      float f = 1.0F;

      for(int j = 0; j < i; ++j) {
         if (j == 0 || random.nextInt(config.shape.widthSmoothness) == 0) {
            f = 1.0F + random.nextFloat() * random.nextFloat();
         }

         fs[j] = f * f;
      }

      return fs;
   }

   private double getVerticalScale(RavineCarverConfig config, Random random, double pitch, float branchCount, float branchIndex) {
      float h = 1.0F - MathHelper.abs(0.5F - branchIndex / branchCount) * 2.0F;
      float i = config.shape.verticalRadiusDefaultFactor + config.shape.verticalRadiusCenterFactor * h;
      return (double)i * pitch * (double)MathHelper.nextBetween(random, 0.75F, 1.0F);
   }

   private boolean isPositionExcluded(CarverContext context, float[] horizontalStretchFactors, double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, int y) {
      int j = y - context.getMinY();
      return (scaledRelativeX * scaledRelativeX + scaledRelativeZ * scaledRelativeZ) * (double)horizontalStretchFactors[j - 1] + scaledRelativeY * scaledRelativeY / 6.0 >= 1.0;
   }
}
