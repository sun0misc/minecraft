package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BlockColumnFeature extends Feature {
   public BlockColumnFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockColumnFeatureConfig lv2 = (BlockColumnFeatureConfig)context.getConfig();
      Random lv3 = context.getRandom();
      int i = lv2.layers().size();
      int[] is = new int[i];
      int j = 0;

      for(int k = 0; k < i; ++k) {
         is[k] = ((BlockColumnFeatureConfig.Layer)lv2.layers().get(k)).height().get(lv3);
         j += is[k];
      }

      if (j == 0) {
         return false;
      } else {
         BlockPos.Mutable lv4 = context.getOrigin().mutableCopy();
         BlockPos.Mutable lv5 = lv4.mutableCopy().move(lv2.direction());

         int l;
         for(l = 0; l < j; ++l) {
            if (!lv2.allowedPlacement().test(lv, lv5)) {
               method_38906(is, j, l, lv2.prioritizeTip());
               break;
            }

            lv5.move(lv2.direction());
         }

         for(l = 0; l < i; ++l) {
            int m = is[l];
            if (m != 0) {
               BlockColumnFeatureConfig.Layer lv6 = (BlockColumnFeatureConfig.Layer)lv2.layers().get(l);

               for(int n = 0; n < m; ++n) {
                  lv.setBlockState(lv4, lv6.state().get(lv3, lv4), Block.NOTIFY_LISTENERS);
                  lv4.move(lv2.direction());
               }
            }
         }

         return true;
      }
   }

   private static void method_38906(int[] is, int i, int j, boolean bl) {
      int k = i - j;
      int l = bl ? 1 : -1;
      int m = bl ? 0 : is.length - 1;
      int n = bl ? is.length : -1;

      for(int o = m; o != n && k > 0; o += l) {
         int p = is[o];
         int q = Math.min(p, k);
         k -= q;
         is[o] -= q;
      }

   }
}
