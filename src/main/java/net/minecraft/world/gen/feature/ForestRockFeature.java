package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class ForestRockFeature extends Feature {
   public ForestRockFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();

      SingleStateFeatureConfig lv4;
      for(lv4 = (SingleStateFeatureConfig)context.getConfig(); lv.getY() > lv2.getBottomY() + 3; lv = lv.down()) {
         if (!lv2.isAir(lv.down())) {
            BlockState lv5 = lv2.getBlockState(lv.down());
            if (isSoil(lv5) || isStone(lv5)) {
               break;
            }
         }
      }

      if (lv.getY() <= lv2.getBottomY() + 3) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            int j = lv3.nextInt(2);
            int k = lv3.nextInt(2);
            int l = lv3.nextInt(2);
            float f = (float)(j + k + l) * 0.333F + 0.5F;
            Iterator var11 = BlockPos.iterate(lv.add(-j, -k, -l), lv.add(j, k, l)).iterator();

            while(var11.hasNext()) {
               BlockPos lv6 = (BlockPos)var11.next();
               if (lv6.getSquaredDistance(lv) <= (double)(f * f)) {
                  lv2.setBlockState(lv6, lv4.state, Block.NOTIFY_ALL);
               }
            }

            lv = lv.add(-1 + lv3.nextInt(2), -lv3.nextInt(2), -1 + lv3.nextInt(2));
         }

         return true;
      }
   }
}
