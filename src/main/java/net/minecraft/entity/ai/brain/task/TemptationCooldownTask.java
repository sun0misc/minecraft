package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;

public class TemptationCooldownTask extends MultiTickTask {
   private final MemoryModuleType moduleType;

   public TemptationCooldownTask(MemoryModuleType moduleType) {
      super(ImmutableMap.of(moduleType, MemoryModuleState.VALUE_PRESENT));
      this.moduleType = moduleType;
   }

   private Optional getTemptationCooldownTicks(LivingEntity entity) {
      return entity.getBrain().getOptionalRegisteredMemory(this.moduleType);
   }

   protected boolean isTimeLimitExceeded(long time) {
      return false;
   }

   protected boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
      Optional optional = this.getTemptationCooldownTicks(entity);
      return optional.isPresent() && (Integer)optional.get() > 0;
   }

   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      Optional optional = this.getTemptationCooldownTicks(entity);
      entity.getBrain().remember(this.moduleType, (Object)((Integer)optional.get() - 1));
   }

   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      entity.getBrain().forget(this.moduleType);
   }
}
