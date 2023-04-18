package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableInt;

public class ForgetBellRingTask {
   private static final int MIN_HEARD_BELL_TIME = 300;

   public static Task create(int maxHiddenSeconds, int distance) {
      int k = maxHiddenSeconds * 20;
      MutableInt mutableInt = new MutableInt(0);
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.HIDING_PLACE), context.queryMemoryValue(MemoryModuleType.HEARD_BELL_TIME)).apply(context, (hidingPlace, heardBellTime) -> {
            return (world, entity, time) -> {
               long m = (Long)context.getValue(heardBellTime);
               boolean bl = m + 300L <= time;
               if (mutableInt.getValue() <= k && !bl) {
                  BlockPos lv = ((GlobalPos)context.getValue(hidingPlace)).getPos();
                  if (lv.isWithinDistance(entity.getBlockPos(), (double)distance)) {
                     mutableInt.increment();
                  }

                  return true;
               } else {
                  heardBellTime.forget();
                  hidingPlace.forget();
                  entity.getBrain().refreshActivities(world.getTimeOfDay(), world.getTime());
                  mutableInt.setValue(0);
                  return true;
               }
            };
         });
      });
   }
}
