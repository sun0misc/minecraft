package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class EmergeTask extends MultiTickTask {
   public EmergeTask(int duration) {
      super(ImmutableMap.of(MemoryModuleType.IS_EMERGING, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), duration);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, WardenEntity arg2, long l) {
      return true;
   }

   protected void run(ServerWorld arg, WardenEntity arg2, long l) {
      arg2.setPose(EntityPose.EMERGING);
      arg2.playSound(SoundEvents.ENTITY_WARDEN_EMERGE, 5.0F, 1.0F);
   }

   protected void finishRunning(ServerWorld arg, WardenEntity arg2, long l) {
      if (arg2.isInPose(EntityPose.EMERGING)) {
         arg2.setPose(EntityPose.STANDING);
      }

   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (WardenEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (WardenEntity)entity, time);
   }
}
