package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class NetherForestVegetationFeature extends Feature {
   public NetherForestVegetationFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      BlockState lv3 = lv.getBlockState(lv2.down());
      NetherForestVegetationFeatureConfig lv4 = (NetherForestVegetationFeatureConfig)context.getConfig();
      Random lv5 = context.getRandom();
      if (!lv3.isIn(BlockTags.NYLIUM)) {
         return false;
      } else {
         int i = lv2.getY();
         if (i >= lv.getBottomY() + 1 && i + 1 < lv.getTopY()) {
            int j = 0;

            for(int k = 0; k < lv4.spreadWidth * lv4.spreadWidth; ++k) {
               BlockPos lv6 = lv2.add(lv5.nextInt(lv4.spreadWidth) - lv5.nextInt(lv4.spreadWidth), lv5.nextInt(lv4.spreadHeight) - lv5.nextInt(lv4.spreadHeight), lv5.nextInt(lv4.spreadWidth) - lv5.nextInt(lv4.spreadWidth));
               BlockState lv7 = lv4.stateProvider.get(lv5, lv6);
               if (lv.isAir(lv6) && lv6.getY() > lv.getBottomY() && lv7.canPlaceAt(lv, lv6)) {
                  lv.setBlockState(lv6, lv7, Block.NOTIFY_LISTENERS);
                  ++j;
               }
            }

            return j > 0;
         } else {
            return false;
         }
      }
   }
}
