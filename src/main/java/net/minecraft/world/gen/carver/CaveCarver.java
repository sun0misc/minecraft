package net.minecraft.world.gen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.AquiferSampler;

public class CaveCarver extends Carver {
   public CaveCarver(Codec codec) {
      super(codec);
   }

   public boolean shouldCarve(CaveCarverConfig arg, Random arg2) {
      return arg2.nextFloat() <= arg.probability;
   }

   public boolean carve(CarverContext arg, CaveCarverConfig arg2, Chunk arg3, Function function, Random arg4, AquiferSampler arg5, ChunkPos arg6, CarvingMask arg7) {
      int i = ChunkSectionPos.getBlockCoord(this.getBranchFactor() * 2 - 1);
      int j = arg4.nextInt(arg4.nextInt(arg4.nextInt(this.getMaxCaveCount()) + 1) + 1);

      for(int k = 0; k < j; ++k) {
         double d = (double)arg6.getOffsetX(arg4.nextInt(16));
         double e = (double)arg2.y.get(arg4, arg);
         double f = (double)arg6.getOffsetZ(arg4.nextInt(16));
         double g = (double)arg2.horizontalRadiusMultiplier.get(arg4);
         double h = (double)arg2.verticalRadiusMultiplier.get(arg4);
         double l = (double)arg2.floorLevel.get(arg4);
         Carver.SkipPredicate lv = (context, scaledRelativeX, scaledRelativeY, scaledRelativeZ, y) -> {
            return isPositionExcluded(scaledRelativeX, scaledRelativeY, scaledRelativeZ, l);
         };
         int m = 1;
         float o;
         if (arg4.nextInt(4) == 0) {
            double n = (double)arg2.yScale.get(arg4);
            o = 1.0F + arg4.nextFloat() * 6.0F;
            this.carveCave(arg, arg2, arg3, function, arg5, d, e, f, o, n, arg7, lv);
            m += arg4.nextInt(4);
         }

         for(int p = 0; p < m; ++p) {
            float q = arg4.nextFloat() * 6.2831855F;
            o = (arg4.nextFloat() - 0.5F) / 4.0F;
            float r = this.getTunnelSystemWidth(arg4);
            int s = i - arg4.nextInt(i / 4);
            int t = false;
            this.carveTunnels(arg, arg2, arg3, function, arg4.nextLong(), arg5, d, e, f, g, h, r, q, o, 0, s, this.getTunnelSystemHeightWidthRatio(), arg7, lv);
         }
      }

      return true;
   }

   protected int getMaxCaveCount() {
      return 15;
   }

   protected float getTunnelSystemWidth(Random random) {
      float f = random.nextFloat() * 2.0F + random.nextFloat();
      if (random.nextInt(10) == 0) {
         f *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
      }

      return f;
   }

   protected double getTunnelSystemHeightWidthRatio() {
      return 1.0;
   }

   protected void carveCave(CarverContext context, CaveCarverConfig config, Chunk chunk, Function posToBiome, AquiferSampler aquiferSampler, double d, double e, double f, float g, double h, CarvingMask mask, Carver.SkipPredicate skipPredicate) {
      double i = 1.5 + (double)(MathHelper.sin(1.5707964F) * g);
      double j = i * h;
      this.carveRegion(context, config, chunk, posToBiome, aquiferSampler, d + 1.0, e, f, i, j, mask, skipPredicate);
   }

   protected void carveTunnels(CarverContext context, CaveCarverConfig config, Chunk chunk, Function posToBiome, long seed, AquiferSampler aquiferSampler, double x, double y, double z, double horizontalScale, double verticalScale, float width, float yaw, float pitch, int branchStartIndex, int branchCount, double yawPitchRatio, CarvingMask mask, Carver.SkipPredicate skipPredicate) {
      Random lv = Random.create(seed);
      int p = lv.nextInt(branchCount / 2) + branchCount / 4;
      boolean bl = lv.nextInt(6) == 0;
      float q = 0.0F;
      float r = 0.0F;

      for(int s = branchStartIndex; s < branchCount; ++s) {
         double t = 1.5 + (double)(MathHelper.sin(3.1415927F * (float)s / (float)branchCount) * width);
         double u = t * yawPitchRatio;
         float v = MathHelper.cos(pitch);
         x += (double)(MathHelper.cos(yaw) * v);
         y += (double)MathHelper.sin(pitch);
         z += (double)(MathHelper.sin(yaw) * v);
         pitch *= bl ? 0.92F : 0.7F;
         pitch += r * 0.1F;
         yaw += q * 0.1F;
         r *= 0.9F;
         q *= 0.75F;
         r += (lv.nextFloat() - lv.nextFloat()) * lv.nextFloat() * 2.0F;
         q += (lv.nextFloat() - lv.nextFloat()) * lv.nextFloat() * 4.0F;
         if (s == p && width > 1.0F) {
            this.carveTunnels(context, config, chunk, posToBiome, lv.nextLong(), aquiferSampler, x, y, z, horizontalScale, verticalScale, lv.nextFloat() * 0.5F + 0.5F, yaw - 1.5707964F, pitch / 3.0F, s, branchCount, 1.0, mask, skipPredicate);
            this.carveTunnels(context, config, chunk, posToBiome, lv.nextLong(), aquiferSampler, x, y, z, horizontalScale, verticalScale, lv.nextFloat() * 0.5F + 0.5F, yaw + 1.5707964F, pitch / 3.0F, s, branchCount, 1.0, mask, skipPredicate);
            return;
         }

         if (lv.nextInt(4) != 0) {
            if (!canCarveBranch(chunk.getPos(), x, z, s, branchCount, width)) {
               return;
            }

            this.carveRegion(context, config, chunk, posToBiome, aquiferSampler, x, y, z, t * horizontalScale, u * verticalScale, mask, skipPredicate);
         }
      }

   }

   private static boolean isPositionExcluded(double scaledRelativeX, double scaledRelativeY, double scaledRelativeZ, double floorY) {
      if (scaledRelativeY <= floorY) {
         return true;
      } else {
         return scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ >= 1.0;
      }
   }
}
