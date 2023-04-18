package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;

public class ForgetAttackTargetTask {
   private static final int REMEMBER_TIME = 200;

   public static Task create(BiConsumer forgetCallback) {
      return create((entity) -> {
         return false;
      }, forgetCallback, true);
   }

   public static Task create(Predicate alternativeCondition) {
      return create(alternativeCondition, (entity, target) -> {
      }, true);
   }

   public static Task create() {
      return create((entity) -> {
         return false;
      }, (entity, target) -> {
      }, true);
   }

   public static Task create(Predicate alternativeCondition, BiConsumer forgetCallback, boolean shouldForgetIfTargetUnreachable) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryValue(MemoryModuleType.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, cantReachWalkTargetSince) -> {
            return (world, entity, time) -> {
               LivingEntity lv = (LivingEntity)context.getValue(attackTarget);
               if (entity.canTarget(lv) && (!shouldForgetIfTargetUnreachable || !cannotReachTarget(entity, context.getOptionalValue(cantReachWalkTargetSince))) && lv.isAlive() && lv.world == entity.world && !alternativeCondition.test(lv)) {
                  return true;
               } else {
                  forgetCallback.accept(entity, lv);
                  attackTarget.forget();
                  return true;
               }
            };
         });
      });
   }

   private static boolean cannotReachTarget(LivingEntity arg, Optional optional) {
      return optional.isPresent() && arg.world.getTime() - (Long)optional.get() > 200L;
   }
}
