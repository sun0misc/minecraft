package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;

public class HurtBySensor extends Sensor {
   public Set getOutputMemoryModules() {
      return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      Brain lv = entity.getBrain();
      DamageSource lv2 = entity.getRecentDamageSource();
      if (lv2 != null) {
         lv.remember(MemoryModuleType.HURT_BY, (Object)entity.getRecentDamageSource());
         Entity lv3 = lv2.getAttacker();
         if (lv3 instanceof LivingEntity) {
            lv.remember(MemoryModuleType.HURT_BY_ENTITY, (Object)((LivingEntity)lv3));
         }
      } else {
         lv.forget(MemoryModuleType.HURT_BY);
      }

      lv.getOptionalRegisteredMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent((arg3) -> {
         if (!arg3.isAlive() || arg3.world != world) {
            lv.forget(MemoryModuleType.HURT_BY_ENTITY);
         }

      });
   }
}
