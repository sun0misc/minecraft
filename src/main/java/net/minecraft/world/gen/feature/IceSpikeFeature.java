package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class IceSpikeFeature extends Feature {
   public IceSpikeFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      Random lv2 = context.getRandom();

      StructureWorldAccess lv3;
      for(lv3 = context.getWorld(); lv3.isAir(lv) && lv.getY() > lv3.getBottomY() + 2; lv = lv.down()) {
      }

      if (!lv3.getBlockState(lv).isOf(Blocks.SNOW_BLOCK)) {
         return false;
      } else {
         lv = lv.up(lv2.nextInt(4));
         int i = lv2.nextInt(4) + 7;
         int j = i / 4 + lv2.nextInt(2);
         if (j > 1 && lv2.nextInt(60) == 0) {
            lv = lv.up(10 + lv2.nextInt(30));
         }

         int k;
         int l;
         for(k = 0; k < i; ++k) {
            float f = (1.0F - (float)k / (float)i) * (float)j;
            l = MathHelper.ceil(f);

            for(int m = -l; m <= l; ++m) {
               float g = (float)MathHelper.abs(m) - 0.25F;

               for(int n = -l; n <= l; ++n) {
                  float h = (float)MathHelper.abs(n) - 0.25F;
                  if ((m == 0 && n == 0 || !(g * g + h * h > f * f)) && (m != -l && m != l && n != -l && n != l || !(lv2.nextFloat() > 0.75F))) {
                     BlockState lv4 = lv3.getBlockState(lv.add(m, k, n));
                     if (lv4.isAir() || isSoil(lv4) || lv4.isOf(Blocks.SNOW_BLOCK) || lv4.isOf(Blocks.ICE)) {
                        this.setBlockState(lv3, lv.add(m, k, n), Blocks.PACKED_ICE.getDefaultState());
                     }

                     if (k != 0 && l > 1) {
                        lv4 = lv3.getBlockState(lv.add(m, -k, n));
                        if (lv4.isAir() || isSoil(lv4) || lv4.isOf(Blocks.SNOW_BLOCK) || lv4.isOf(Blocks.ICE)) {
                           this.setBlockState(lv3, lv.add(m, -k, n), Blocks.PACKED_ICE.getDefaultState());
                        }
                     }
                  }
               }
            }
         }

         k = j - 1;
         if (k < 0) {
            k = 0;
         } else if (k > 1) {
            k = 1;
         }

         for(int o = -k; o <= k; ++o) {
            for(l = -k; l <= k; ++l) {
               BlockPos lv5 = lv.add(o, -1, l);
               int p = 50;
               if (Math.abs(o) == 1 && Math.abs(l) == 1) {
                  p = lv2.nextInt(5);
               }

               while(lv5.getY() > 50) {
                  BlockState lv6 = lv3.getBlockState(lv5);
                  if (!lv6.isAir() && !isSoil(lv6) && !lv6.isOf(Blocks.SNOW_BLOCK) && !lv6.isOf(Blocks.ICE) && !lv6.isOf(Blocks.PACKED_ICE)) {
                     break;
                  }

                  this.setBlockState(lv3, lv5, Blocks.PACKED_ICE.getDefaultState());
                  lv5 = lv5.down();
                  --p;
                  if (p <= 0) {
                     lv5 = lv5.down(lv2.nextInt(5) + 1);
                     p = lv2.nextInt(5);
                  }
               }
            }
         }

         return true;
      }
   }
}
