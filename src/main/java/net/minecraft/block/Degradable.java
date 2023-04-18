package net.minecraft.block;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface Degradable {
   int DEGRADING_RANGE = 4;

   Optional getDegradationResult(BlockState state);

   float getDegradationChanceMultiplier();

   default void tickDegradation(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      float f = 0.05688889F;
      if (random.nextFloat() < 0.05688889F) {
         this.tryDegrade(state, world, pos, random);
      }

   }

   Enum getDegradationLevel();

   default void tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      int i = this.getDegradationLevel().ordinal();
      int j = 0;
      int k = 0;
      Iterator var8 = BlockPos.iterateOutwards(pos, 4, 4, 4).iterator();

      while(var8.hasNext()) {
         BlockPos lv = (BlockPos)var8.next();
         int l = lv.getManhattanDistance(pos);
         if (l > 4) {
            break;
         }

         if (!lv.equals(pos)) {
            BlockState lv2 = world.getBlockState(lv);
            Block lv3 = lv2.getBlock();
            if (lv3 instanceof Degradable) {
               Enum enum_ = ((Degradable)lv3).getDegradationLevel();
               if (this.getDegradationLevel().getClass() == enum_.getClass()) {
                  int m = enum_.ordinal();
                  if (m < i) {
                     return;
                  }

                  if (m > i) {
                     ++k;
                  } else {
                     ++j;
                  }
               }
            }
         }
      }

      float f = (float)(k + 1) / (float)(k + j + 1);
      float g = f * f * this.getDegradationChanceMultiplier();
      if (random.nextFloat() < g) {
         this.getDegradationResult(state).ifPresent((statex) -> {
            world.setBlockState(pos, statex);
         });
      }

   }
}
