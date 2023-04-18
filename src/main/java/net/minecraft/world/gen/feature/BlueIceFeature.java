package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BlueIceFeature extends Feature {
   public BlueIceFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      if (lv.getY() > lv2.getSeaLevel() - 1) {
         return false;
      } else if (!lv2.getBlockState(lv).isOf(Blocks.WATER) && !lv2.getBlockState(lv.down()).isOf(Blocks.WATER)) {
         return false;
      } else {
         boolean bl = false;
         Direction[] var6 = Direction.values();
         int j = var6.length;

         int k;
         for(k = 0; k < j; ++k) {
            Direction lv4 = var6[k];
            if (lv4 != Direction.DOWN && lv2.getBlockState(lv.offset(lv4)).isOf(Blocks.PACKED_ICE)) {
               bl = true;
               break;
            }
         }

         if (!bl) {
            return false;
         } else {
            lv2.setBlockState(lv, Blocks.BLUE_ICE.getDefaultState(), Block.NOTIFY_LISTENERS);

            for(int i = 0; i < 200; ++i) {
               j = lv3.nextInt(5) - lv3.nextInt(6);
               k = 3;
               if (j < 2) {
                  k += j / 2;
               }

               if (k >= 1) {
                  BlockPos lv5 = lv.add(lv3.nextInt(k) - lv3.nextInt(k), j, lv3.nextInt(k) - lv3.nextInt(k));
                  BlockState lv6 = lv2.getBlockState(lv5);
                  if (lv6.isAir() || lv6.isOf(Blocks.WATER) || lv6.isOf(Blocks.PACKED_ICE) || lv6.isOf(Blocks.ICE)) {
                     Direction[] var11 = Direction.values();
                     int var12 = var11.length;

                     for(int var13 = 0; var13 < var12; ++var13) {
                        Direction lv7 = var11[var13];
                        BlockState lv8 = lv2.getBlockState(lv5.offset(lv7));
                        if (lv8.isOf(Blocks.BLUE_ICE)) {
                           lv2.setBlockState(lv5, Blocks.BLUE_ICE.getDefaultState(), Block.NOTIFY_LISTENERS);
                           break;
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}
