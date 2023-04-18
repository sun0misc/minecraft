package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.WardenBrain;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public class SniffTask extends MultiTickTask {
   private static final double HORIZONTAL_RADIUS = 6.0;
   private static final double VERTICAL_RADIUS = 20.0;

   public SniffTask(int runTime) {
      super(ImmutableMap.of(MemoryModuleType.IS_SNIFFING, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleState.REGISTERED, MemoryModuleType.DISTURBANCE_LOCATION, MemoryModuleState.REGISTERED, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleState.REGISTERED), runTime);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, WardenEntity arg2, long l) {
      return true;
   }

   protected void run(ServerWorld arg, WardenEntity arg2, long l) {
      arg2.playSound(SoundEvents.ENTITY_WARDEN_SNIFF, 5.0F, 1.0F);
   }

   protected void finishRunning(ServerWorld arg, WardenEntity arg2, long l) {
      if (arg2.isInPose(EntityPose.SNIFFING)) {
         arg2.setPose(EntityPose.STANDING);
      }

      arg2.getBrain().forget(MemoryModuleType.IS_SNIFFING);
      Optional var10000 = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_ATTACKABLE);
      Objects.requireNonNull(arg2);
      var10000.filter(arg2::isValidTarget).ifPresent((target) -> {
         if (arg2.isInRange(target, 6.0, 20.0)) {
            arg2.increaseAngerAt(target);
         }

         if (!arg2.getBrain().hasMemoryModule(MemoryModuleType.DISTURBANCE_LOCATION)) {
            WardenBrain.lookAtDisturbance(arg2, target.getBlockPos());
         }

      });
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
