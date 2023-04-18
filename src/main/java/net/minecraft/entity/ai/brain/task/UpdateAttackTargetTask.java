package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;

public class UpdateAttackTargetTask {
   public static Task create(Function targetGetter) {
      return create((entity) -> {
         return true;
      }, targetGetter);
   }

   public static Task create(Predicate startCondition, Function targetGetter) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryAbsent(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, cantReachWalkTargetSince) -> {
            return (world, entity, time) -> {
               if (!startCondition.test(entity)) {
                  return false;
               } else {
                  Optional optional = (Optional)targetGetter.apply(entity);
                  if (optional.isEmpty()) {
                     return false;
                  } else {
                     LivingEntity lv = (LivingEntity)optional.get();
                     if (!entity.canTarget(lv)) {
                        return false;
                     } else {
                        attackTarget.remember((Object)lv);
                        cantReachWalkTargetSince.forget();
                        return true;
                     }
                  }
               }
            };
         });
      });
   }
}
