package net.minecraft.entity.ai.brain.task;

import java.util.List;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableLong;

public class GoToSecondaryPositionTask {
   public static Task create(MemoryModuleType secondaryPositions, float speed, int completionRange, int primaryPositionActivationDistance, MemoryModuleType primaryPosition) {
      MutableLong mutableLong = new MutableLong(0L);
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(secondaryPositions), context.queryMemoryValue(primaryPosition)).apply(context, (walkTarget, secondary, primary) -> {
            return (world, entity, time) -> {
               List list = (List)context.getValue(secondary);
               GlobalPos lv = (GlobalPos)context.getValue(primary);
               if (list.isEmpty()) {
                  return false;
               } else {
                  GlobalPos lv2 = (GlobalPos)list.get(world.getRandom().nextInt(list.size()));
                  if (lv2 != null && world.getRegistryKey() == lv2.getDimension() && lv.getPos().isWithinDistance(entity.getPos(), (double)primaryPositionActivationDistance)) {
                     if (time > mutableLong.getValue()) {
                        walkTarget.remember((Object)(new WalkTarget(lv2.getPos(), speed, completionRange)));
                        mutableLong.setValue(time + 100L);
                     }

                     return true;
                  } else {
                     return false;
                  }
               }
            };
         });
      });
   }
}
