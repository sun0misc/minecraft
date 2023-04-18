package net.minecraft.entity.ai.brain.task;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;

public class RingBellTask {
   private static final float RUN_CHANCE = 0.95F;
   public static final int MAX_DISTANCE = 3;

   public static Task create() {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.MEETING_POINT)).apply(context, (meetingPoint) -> {
            return (world, entity, time) -> {
               if (world.random.nextFloat() <= 0.95F) {
                  return false;
               } else {
                  BlockPos lv = ((GlobalPos)context.getValue(meetingPoint)).getPos();
                  if (lv.isWithinDistance(entity.getBlockPos(), 3.0)) {
                     BlockState lv2 = world.getBlockState(lv);
                     if (lv2.isOf(Blocks.BELL)) {
                        BellBlock lv3 = (BellBlock)lv2.getBlock();
                        lv3.ring(entity, world, lv, (Direction)null);
                     }
                  }

                  return true;
               }
            };
         });
      });
   }
}
