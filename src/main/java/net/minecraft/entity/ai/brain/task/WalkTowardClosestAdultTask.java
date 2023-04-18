package net.minecraft.entity.ai.brain.task;

import java.util.function.Function;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class WalkTowardClosestAdultTask {
   public static SingleTickTask create(UniformIntProvider executionRange, float speed) {
      return create(executionRange, (entity) -> {
         return speed;
      });
   }

   public static SingleTickTask create(UniformIntProvider executionRange, Function speed) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ADULT), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply(context, (nearestVisibleAdult, lookTarget, walkTarget) -> {
            return (world, entity, time) -> {
               if (!entity.isBaby()) {
                  return false;
               } else {
                  PassiveEntity lv = (PassiveEntity)context.getValue(nearestVisibleAdult);
                  if (entity.isInRange(lv, (double)(executionRange.getMax() + 1)) && !entity.isInRange(lv, (double)executionRange.getMin())) {
                     WalkTarget lv2 = new WalkTarget(new EntityLookTarget(lv, false), (Float)speed.apply(entity), executionRange.getMin() - 1);
                     lookTarget.remember((Object)(new EntityLookTarget(lv, true)));
                     walkTarget.remember((Object)lv2);
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
