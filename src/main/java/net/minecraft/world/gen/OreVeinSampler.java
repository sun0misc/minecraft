package net.minecraft.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public final class OreVeinSampler {
   private static final float field_36620 = 0.4F;
   private static final int field_36621 = 20;
   private static final double field_36622 = 0.2;
   private static final float field_36623 = 0.7F;
   private static final float field_36624 = 0.1F;
   private static final float field_36625 = 0.3F;
   private static final float field_36626 = 0.6F;
   private static final float RAW_ORE_BLOCK_CHANCE = 0.02F;
   private static final float field_36628 = -0.3F;

   private OreVeinSampler() {
   }

   protected static ChunkNoiseSampler.BlockStateSampler create(DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap, RandomSplitter randomDeriver) {
      BlockState lv = null;
      return (pos) -> {
         double d = veinToggle.sample(pos);
         int i = pos.blockY();
         VeinType lvx = d > 0.0 ? OreVeinSampler.VeinType.COPPER : OreVeinSampler.VeinType.IRON;
         double e = Math.abs(d);
         int j = lvx.maxY - i;
         int k = i - lvx.minY;
         if (k >= 0 && j >= 0) {
            int l = Math.min(j, k);
            double f = MathHelper.clampedMap((double)l, 0.0, 20.0, -0.2, 0.0);
            if (e + f < 0.4000000059604645) {
               return lv;
            } else {
               Random lv2 = randomDeriver.split(pos.blockX(), i, pos.blockZ());
               if (lv2.nextFloat() > 0.7F) {
                  return lv;
               } else if (veinRidged.sample(pos) >= 0.0) {
                  return lv;
               } else {
                  double g = MathHelper.clampedMap(e, 0.4000000059604645, 0.6000000238418579, 0.10000000149011612, 0.30000001192092896);
                  if ((double)lv2.nextFloat() < g && veinGap.sample(pos) > -0.30000001192092896) {
                     return lv2.nextFloat() < 0.02F ? lvx.rawOreBlock : lvx.ore;
                  } else {
                     return lvx.stone;
                  }
               }
            }
         } else {
            return lv;
         }
      };
   }

   protected static enum VeinType {
      COPPER(Blocks.COPPER_ORE.getDefaultState(), Blocks.RAW_COPPER_BLOCK.getDefaultState(), Blocks.GRANITE.getDefaultState(), 0, 50),
      IRON(Blocks.DEEPSLATE_IRON_ORE.getDefaultState(), Blocks.RAW_IRON_BLOCK.getDefaultState(), Blocks.TUFF.getDefaultState(), -60, -8);

      final BlockState ore;
      final BlockState rawOreBlock;
      final BlockState stone;
      protected final int minY;
      protected final int maxY;

      private VeinType(BlockState ore, BlockState rawOreBlock, BlockState stone, int minY, int maxY) {
         this.ore = ore;
         this.rawOreBlock = rawOreBlock;
         this.stone = stone;
         this.minY = minY;
         this.maxY = maxY;
      }

      // $FF: synthetic method
      private static VeinType[] method_36754() {
         return new VeinType[]{COPPER, IRON};
      }
   }
}
