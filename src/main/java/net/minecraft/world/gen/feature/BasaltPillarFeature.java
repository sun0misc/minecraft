package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BasaltPillarFeature extends Feature {
   public BasaltPillarFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      if (lv2.isAir(lv) && !lv2.isAir(lv.up())) {
         BlockPos.Mutable lv4 = lv.mutableCopy();
         BlockPos.Mutable lv5 = lv.mutableCopy();
         boolean bl = true;
         boolean bl2 = true;
         boolean bl3 = true;
         boolean bl4 = true;

         while(lv2.isAir(lv4)) {
            if (lv2.isOutOfHeightLimit(lv4)) {
               return true;
            }

            lv2.setBlockState(lv4, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
            bl = bl && this.stopOrPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.NORTH));
            bl2 = bl2 && this.stopOrPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.SOUTH));
            bl3 = bl3 && this.stopOrPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.WEST));
            bl4 = bl4 && this.stopOrPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.EAST));
            lv4.move(Direction.DOWN);
         }

         lv4.move(Direction.UP);
         this.tryPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.NORTH));
         this.tryPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.SOUTH));
         this.tryPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.WEST));
         this.tryPlaceBasalt(lv2, lv3, lv5.set(lv4, (Direction)Direction.EAST));
         lv4.move(Direction.DOWN);
         BlockPos.Mutable lv6 = new BlockPos.Mutable();

         for(int i = -3; i < 4; ++i) {
            for(int j = -3; j < 4; ++j) {
               int k = MathHelper.abs(i) * MathHelper.abs(j);
               if (lv3.nextInt(10) < 10 - k) {
                  lv6.set(lv4.add(i, 0, j));
                  int l = 3;

                  while(lv2.isAir(lv5.set(lv6, (Direction)Direction.DOWN))) {
                     lv6.move(Direction.DOWN);
                     --l;
                     if (l <= 0) {
                        break;
                     }
                  }

                  if (!lv2.isAir(lv5.set(lv6, (Direction)Direction.DOWN))) {
                     lv2.setBlockState(lv6, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void tryPlaceBasalt(WorldAccess world, Random random, BlockPos pos) {
      if (random.nextBoolean()) {
         world.setBlockState(pos, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
      }

   }

   private boolean stopOrPlaceBasalt(WorldAccess world, Random random, BlockPos pos) {
      if (random.nextInt(10) != 0) {
         world.setBlockState(pos, Blocks.BASALT.getDefaultState(), Block.NOTIFY_LISTENERS);
         return true;
      } else {
         return false;
      }
   }
}
