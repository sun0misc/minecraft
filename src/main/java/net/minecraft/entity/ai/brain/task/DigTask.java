package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class DigTask extends MultiTickTask {
   public DigTask(int duration) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT), duration);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, WardenEntity arg2, long l) {
      return arg2.getRemovalReason() == null;
   }

   protected boolean shouldRun(ServerWorld arg, WardenEntity arg2) {
      return arg2.isOnGround() || arg2.isTouchingWater() || arg2.isInLava();
   }

   protected void run(ServerWorld arg, WardenEntity arg2, long l) {
      if (arg2.isOnGround()) {
         arg2.setPose(EntityPose.DIGGING);
         arg2.playSound(SoundEvents.ENTITY_WARDEN_DIG, 5.0F, 1.0F);
      } else {
         arg2.playSound(SoundEvents.ENTITY_WARDEN_AGITATED, 5.0F, 1.0F);
         this.finishRunning(arg, arg2, l);
      }

   }

   protected void finishRunning(ServerWorld arg, WardenEntity arg2, long l) {
      if (arg2.getRemovalReason() == null) {
         arg2.remove(Entity.RemovalReason.DISCARDED);
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
