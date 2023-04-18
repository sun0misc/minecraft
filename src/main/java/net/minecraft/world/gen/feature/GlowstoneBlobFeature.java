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

public class GlowstoneBlobFeature extends Feature {
   public GlowstoneBlobFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      Random lv3 = context.getRandom();
      if (!lv.isAir(lv2)) {
         return false;
      } else {
         BlockState lv4 = lv.getBlockState(lv2.up());
         if (!lv4.isOf(Blocks.NETHERRACK) && !lv4.isOf(Blocks.BASALT) && !lv4.isOf(Blocks.BLACKSTONE)) {
            return false;
         } else {
            lv.setBlockState(lv2, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);

            for(int i = 0; i < 1500; ++i) {
               BlockPos lv5 = lv2.add(lv3.nextInt(8) - lv3.nextInt(8), -lv3.nextInt(12), lv3.nextInt(8) - lv3.nextInt(8));
               if (lv.getBlockState(lv5).isAir()) {
                  int j = 0;
                  Direction[] var9 = Direction.values();
                  int var10 = var9.length;

                  for(int var11 = 0; var11 < var10; ++var11) {
                     Direction lv6 = var9[var11];
                     if (lv.getBlockState(lv5.offset(lv6)).isOf(Blocks.GLOWSTONE)) {
                        ++j;
                     }

                     if (j > 1) {
                        break;
                     }
                  }

                  if (j == 1) {
                     lv.setBlockState(lv5, Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
                  }
               }
            }

            return true;
         }
      }
   }
}
