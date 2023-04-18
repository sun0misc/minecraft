package net.minecraft.entity.ai.brain.task;

import java.util.function.Predicate;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;

public class WalkToNearestVisibleWantedItemTask {
   public static Task create(float speed, boolean requiresWalkTarget, int radius) {
      return create((entity) -> {
         return true;
      }, speed, requiresWalkTarget, radius);
   }

   public static Task create(Predicate startCondition, float speed, boolean requiresWalkTarget, int radius) {
      return TaskTriggerer.task((context) -> {
         TaskTriggerer lv = requiresWalkTarget ? context.queryMemoryOptional(MemoryModuleType.WALK_TARGET) : context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET);
         return context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), lv, context.queryMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), context.queryMemoryOptional(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)).apply(context, (lookTarget, walkTarget, nearestVisibleWantedItem, itemPickupCooldownTicks) -> {
            return (world, entity, time) -> {
               ItemEntity lv = (ItemEntity)context.getValue(nearestVisibleWantedItem);
               if (context.getOptionalValue(itemPickupCooldownTicks).isEmpty() && startCondition.test(entity) && lv.isInRange(entity, (double)radius) && entity.world.getWorldBorder().contains(lv.getBlockPos())) {
                  WalkTarget lv2 = new WalkTarget(new EntityLookTarget(lv, false), speed, 0);
                  lookTarget.remember((Object)(new EntityLookTarget(lv, true)));
                  walkTarget.remember((Object)lv2);
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }
}
