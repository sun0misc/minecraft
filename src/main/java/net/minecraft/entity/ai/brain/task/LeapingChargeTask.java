package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class LeapingChargeTask extends MultiTickTask {
   public static final int RUN_TIME = 100;
   private final UniformIntProvider cooldownRange;
   private final SoundEvent sound;

   public LeapingChargeTask(UniformIntProvider cooldownRange, SoundEvent sound) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleState.VALUE_PRESENT), 100);
      this.cooldownRange = cooldownRange;
      this.sound = sound;
   }

   protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
      return !arg2.isOnGround();
   }

   protected void run(ServerWorld arg, MobEntity arg2, long l) {
      arg2.setNoDrag(true);
      arg2.setPose(EntityPose.LONG_JUMPING);
   }

   protected void finishRunning(ServerWorld arg, MobEntity arg2, long l) {
      if (arg2.isOnGround()) {
         arg2.setVelocity(arg2.getVelocity().multiply(0.10000000149011612, 1.0, 0.10000000149011612));
         arg.playSoundFromEntity((PlayerEntity)null, arg2, this.sound, SoundCategory.NEUTRAL, 2.0F, 1.0F);
      }

      arg2.setNoDrag(false);
      arg2.setPose(EntityPose.STANDING);
      arg2.getBrain().forget(MemoryModuleType.LONG_JUMP_MID_JUMP);
      arg2.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, (Object)this.cooldownRange.get(arg.random));
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (MobEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (MobEntity)entity, time);
   }
}
