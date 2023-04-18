package net.minecraft.entity.ai.brain.task;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.Vec3d;

public class GoToRememberedPositionTask {
   public static Task createPosBased(MemoryModuleType posModule, float speed, int range, boolean requiresWalkTarget) {
      return create(posModule, speed, range, requiresWalkTarget, Vec3d::ofBottomCenter);
   }

   public static SingleTickTask createEntityBased(MemoryModuleType entityModule, float speed, int range, boolean requiresWalkTarget) {
      return create(entityModule, speed, range, requiresWalkTarget, Entity::getPos);
   }

   private static SingleTickTask create(MemoryModuleType posSource, float speed, int range, boolean requiresWalkTarget, Function posGetter) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(posSource)).apply(context, (walkTarget, posSourcex) -> {
            return (world, entity, time) -> {
               Optional optional = context.getOptionalValue(walkTarget);
               if (optional.isPresent() && !requiresWalkTarget) {
                  return false;
               } else {
                  Vec3d lv = entity.getPos();
                  Vec3d lv2 = (Vec3d)posGetter.apply(context.getValue(posSourcex));
                  if (!lv.isInRange(lv2, (double)range)) {
                     return false;
                  } else {
                     Vec3d lv4;
                     if (optional.isPresent() && ((WalkTarget)optional.get()).getSpeed() == speed) {
                        Vec3d lv3 = ((WalkTarget)optional.get()).getLookTarget().getPos().subtract(lv);
                        lv4 = lv2.subtract(lv);
                        if (lv3.dotProduct(lv4) < 0.0) {
                           return false;
                        }
                     }

                     for(int j = 0; j < 10; ++j) {
                        lv4 = FuzzyTargeting.findFrom(entity, 16, 7, lv2);
                        if (lv4 != null) {
                           walkTarget.remember((Object)(new WalkTarget(lv4, speed, 0)));
                           break;
                        }
                     }

                     return true;
                  }
               }
            };
         });
      });
   }
}
