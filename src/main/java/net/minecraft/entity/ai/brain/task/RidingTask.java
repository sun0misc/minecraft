package net.minecraft.entity.ai.brain.task;

import java.util.function.BiPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;

public class RidingTask {
   public static Task create(int range, BiPredicate alternativeRideCondition) {
      return TaskTriggerer.task((context) -> {
         return context.group(context.queryMemoryOptional(MemoryModuleType.RIDE_TARGET)).apply(context, (rideTarget) -> {
            return (world, entity, time) -> {
               Entity lv = entity.getVehicle();
               Entity lv2 = (Entity)context.getOptionalValue(rideTarget).orElse((Object)null);
               if (lv == null && lv2 == null) {
                  return false;
               } else {
                  Entity lv3 = lv == null ? lv2 : lv;
                  if (canRideTarget(entity, lv3, range) && !alternativeRideCondition.test(entity, lv3)) {
                     return false;
                  } else {
                     entity.stopRiding();
                     rideTarget.forget();
                     return true;
                  }
               }
            };
         });
      });
   }

   private static boolean canRideTarget(LivingEntity entity, Entity vehicle, int range) {
      return vehicle.isAlive() && vehicle.isInRange(entity, (double)range) && vehicle.world == entity.world;
   }
}
