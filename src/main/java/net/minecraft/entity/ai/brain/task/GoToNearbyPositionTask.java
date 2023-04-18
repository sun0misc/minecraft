package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableLong;

public class GoToNearbyPositionTask {
   public static Task create(MemoryModuleType posModule, float walkSpeed, int completionRange, int maxDistance) {
      MutableLong mutableLong = new MutableLong(0L);
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(posModule)).apply(context, (walkTarget, pos) -> {
            return (world, entity, time) -> {
               GlobalPos lv = (GlobalPos)context.getValue(pos);
               if (world.getRegistryKey() == lv.getDimension() && lv.getPos().isWithinDistance(entity.getPos(), (double)maxDistance)) {
                  if (time <= mutableLong.getValue()) {
                     return true;
                  } else {
                     walkTarget.remember((Object)(new WalkTarget(lv.getPos(), walkSpeed, completionRange)));
                     mutableLong.setValue(time + 80L);
                     return true;
                  }
               } else {
                  return false;
               }
            };
         });
      });
   }
}
