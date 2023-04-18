package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;

public abstract class NearestVisibleLivingEntitySensor extends Sensor {
   protected abstract boolean matches(LivingEntity entity, LivingEntity target);

   protected abstract MemoryModuleType getOutputMemoryModule();

   public Set getOutputMemoryModules() {
      return ImmutableSet.of(this.getOutputMemoryModule());
   }

   protected void sense(ServerWorld world, LivingEntity entity) {
      entity.getBrain().remember(this.getOutputMemoryModule(), this.getNearestVisibleLivingEntity(entity));
   }

   private Optional getNearestVisibleLivingEntity(LivingEntity entity) {
      return this.getVisibleLivingEntities(entity).flatMap((arg2) -> {
         return arg2.findFirst((arg2x) -> {
            return this.matches(entity, arg2x);
         });
      });
   }

   protected Optional getVisibleLivingEntities(LivingEntity entity) {
      return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS);
   }
}
