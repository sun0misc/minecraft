package net.minecraft.entity.ai.brain.task;

import java.util.function.BiPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.world.GameRules;

public class DefeatTargetTask {
   public static Task create(int celebrationDuration, BiPredicate predicate) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.ANGRY_AT), context.queryMemoryAbsent(MemoryModuleType.CELEBRATE_LOCATION), context.queryMemoryOptional(MemoryModuleType.DANCING)).apply(context, (attackTarget, angryAt, celebrateLocation, dancing) -> {
            return (world, entity, time) -> {
               LivingEntity lv = (LivingEntity)context.getValue(attackTarget);
               if (!lv.isDead()) {
                  return false;
               } else {
                  if (predicate.test(entity, lv)) {
                     dancing.remember(true, (long)celebrationDuration);
                  }

                  celebrateLocation.remember(lv.getBlockPos(), (long)celebrationDuration);
                  if (lv.getType() != EntityType.PLAYER || world.getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) {
                     attackTarget.forget();
                     angryAt.forget();
                  }

                  return true;
               }
            };
         });
      });
   }
}
