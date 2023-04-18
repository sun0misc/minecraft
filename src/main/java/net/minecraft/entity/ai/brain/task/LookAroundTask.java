package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public class LookAroundTask extends MultiTickTask {
   public LookAroundTask(int minRunTime, int maxRunTime) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT), minRunTime, maxRunTime);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
      return arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LOOK_TARGET).filter((lookTarget) -> {
         return lookTarget.isSeenBy(arg2);
      }).isPresent();
   }

   protected void finishRunning(ServerWorld arg, MobEntity arg2, long l) {
      arg2.getBrain().forget(MemoryModuleType.LOOK_TARGET);
   }

   protected void keepRunning(ServerWorld arg, MobEntity arg2, long l) {
      arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LOOK_TARGET).ifPresent((lookTarget) -> {
         arg2.getLookControl().lookAt(lookTarget.getPos());
      });
   }
}
