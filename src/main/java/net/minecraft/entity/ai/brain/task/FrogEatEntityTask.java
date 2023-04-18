package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class FrogEatEntityTask extends MultiTickTask {
   public static final int RUN_TIME = 100;
   public static final int CATCH_DURATION = 6;
   public static final int EAT_DURATION = 10;
   private static final float MAX_DISTANCE = 1.75F;
   private static final float VELOCITY_MULTIPLIER = 0.75F;
   public static final int UNREACHABLE_TONGUE_TARGETS_START_TIME = 100;
   public static final int MAX_UNREACHABLE_TONGUE_TARGETS = 5;
   private int eatTick;
   private int moveToTargetTick;
   private final SoundEvent tongueSound;
   private final SoundEvent eatSound;
   private Vec3d targetPos;
   private Phase phase;

   public FrogEatEntityTask(SoundEvent tongueSound, SoundEvent eatSound) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.IS_PANICKING, MemoryModuleState.VALUE_ABSENT), 100);
      this.phase = FrogEatEntityTask.Phase.DONE;
      this.tongueSound = tongueSound;
      this.eatSound = eatSound;
   }

   protected boolean shouldRun(ServerWorld arg, FrogEntity arg2) {
      LivingEntity lv = (LivingEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
      boolean bl = this.isTargetReachable(arg2, lv);
      if (!bl) {
         arg2.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
         this.markTargetAsUnreachable(arg2, lv);
      }

      return bl && arg2.getPose() != EntityPose.CROAKING && FrogEntity.isValidFrogFood(lv);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, FrogEntity arg2, long l) {
      return arg2.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET) && this.phase != FrogEatEntityTask.Phase.DONE && !arg2.getBrain().hasMemoryModule(MemoryModuleType.IS_PANICKING);
   }

   protected void run(ServerWorld arg, FrogEntity arg2, long l) {
      LivingEntity lv = (LivingEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
      LookTargetUtil.lookAt(arg2, lv);
      arg2.setFrogTarget(lv);
      arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(lv.getPos(), 2.0F, 0)));
      this.moveToTargetTick = 10;
      this.phase = FrogEatEntityTask.Phase.MOVE_TO_TARGET;
   }

   protected void finishRunning(ServerWorld arg, FrogEntity arg2, long l) {
      arg2.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
      arg2.clearFrogTarget();
      arg2.setPose(EntityPose.STANDING);
   }

   private void eat(ServerWorld world, FrogEntity frog) {
      world.playSoundFromEntity((PlayerEntity)null, frog, this.eatSound, SoundCategory.NEUTRAL, 2.0F, 1.0F);
      Optional optional = frog.getFrogTarget();
      if (optional.isPresent()) {
         Entity lv = (Entity)optional.get();
         if (lv.isAlive()) {
            frog.tryAttack(lv);
            if (!lv.isAlive()) {
               lv.remove(Entity.RemovalReason.KILLED);
            }
         }
      }

   }

   protected void keepRunning(ServerWorld arg, FrogEntity arg2, long l) {
      LivingEntity lv = (LivingEntity)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
      arg2.setFrogTarget(lv);
      switch (this.phase) {
         case MOVE_TO_TARGET:
            if (lv.distanceTo(arg2) < 1.75F) {
               arg.playSoundFromEntity((PlayerEntity)null, arg2, this.tongueSound, SoundCategory.NEUTRAL, 2.0F, 1.0F);
               arg2.setPose(EntityPose.USING_TONGUE);
               lv.setVelocity(lv.getPos().relativize(arg2.getPos()).normalize().multiply(0.75));
               this.targetPos = lv.getPos();
               this.eatTick = 0;
               this.phase = FrogEatEntityTask.Phase.CATCH_ANIMATION;
            } else if (this.moveToTargetTick <= 0) {
               arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(lv.getPos(), 2.0F, 0)));
               this.moveToTargetTick = 10;
            } else {
               --this.moveToTargetTick;
            }
            break;
         case CATCH_ANIMATION:
            if (this.eatTick++ >= 6) {
               this.phase = FrogEatEntityTask.Phase.EAT_ANIMATION;
               this.eat(arg, arg2);
            }
            break;
         case EAT_ANIMATION:
            if (this.eatTick >= 10) {
               this.phase = FrogEatEntityTask.Phase.DONE;
            } else {
               ++this.eatTick;
            }
         case DONE:
      }

   }

   private boolean isTargetReachable(FrogEntity entity, LivingEntity target) {
      Path lv = entity.getNavigation().findPathTo((Entity)target, 0);
      return lv != null && lv.getManhattanDistanceFromTarget() < 1.75F;
   }

   private void markTargetAsUnreachable(FrogEntity entity, LivingEntity target) {
      List list = (List)entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      boolean bl = !list.contains(target.getUuid());
      if (list.size() == 5 && bl) {
         list.remove(0);
      }

      if (bl) {
         list.add(target.getUuid());
      }

      entity.getBrain().remember(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS, list, 100L);
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (FrogEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (FrogEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (FrogEntity)entity, time);
   }

   private static enum Phase {
      MOVE_TO_TARGET,
      CATCH_ANIMATION,
      EAT_ANIMATION,
      DONE;

      // $FF: synthetic method
      private static Phase[] method_41390() {
         return new Phase[]{MOVE_TO_TARGET, CATCH_ANIMATION, EAT_ANIMATION, DONE};
      }
   }
}
