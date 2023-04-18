package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WanderAroundTask extends MultiTickTask {
   private static final int MAX_UPDATE_COUNTDOWN = 40;
   private int pathUpdateCountdownTicks;
   @Nullable
   private Path path;
   @Nullable
   private BlockPos lookTargetPos;
   private float speed;

   public WanderAroundTask() {
      this(150, 250);
   }

   public WanderAroundTask(int minRunTime, int maxRunTime) {
      super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED, MemoryModuleType.PATH, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT), minRunTime, maxRunTime);
   }

   protected boolean shouldRun(ServerWorld arg, MobEntity arg2) {
      if (this.pathUpdateCountdownTicks > 0) {
         --this.pathUpdateCountdownTicks;
         return false;
      } else {
         Brain lv = arg2.getBrain();
         WalkTarget lv2 = (WalkTarget)lv.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).get();
         boolean bl = this.hasReached(arg2, lv2);
         if (!bl && this.hasFinishedPath(arg2, lv2, arg.getTime())) {
            this.lookTargetPos = lv2.getLookTarget().getBlockPos();
            return true;
         } else {
            lv.forget(MemoryModuleType.WALK_TARGET);
            if (bl) {
               lv.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }

            return false;
         }
      }
   }

   protected boolean shouldKeepRunning(ServerWorld arg, MobEntity arg2, long l) {
      if (this.path != null && this.lookTargetPos != null) {
         Optional optional = arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET);
         boolean bl = (Boolean)optional.map(WanderAroundTask::isTargetSpectator).orElse(false);
         EntityNavigation lv = arg2.getNavigation();
         return !lv.isIdle() && optional.isPresent() && !this.hasReached(arg2, (WalkTarget)optional.get()) && !bl;
      } else {
         return false;
      }
   }

   protected void finishRunning(ServerWorld arg, MobEntity arg2, long l) {
      if (arg2.getBrain().hasMemoryModule(MemoryModuleType.WALK_TARGET) && !this.hasReached(arg2, (WalkTarget)arg2.getBrain().getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).get()) && arg2.getNavigation().isNearPathStartPos()) {
         this.pathUpdateCountdownTicks = arg.getRandom().nextInt(40);
      }

      arg2.getNavigation().stop();
      arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
      arg2.getBrain().forget(MemoryModuleType.PATH);
      this.path = null;
   }

   protected void run(ServerWorld arg, MobEntity arg2, long l) {
      arg2.getBrain().remember(MemoryModuleType.PATH, (Object)this.path);
      arg2.getNavigation().startMovingAlong(this.path, (double)this.speed);
   }

   protected void keepRunning(ServerWorld arg, MobEntity arg2, long l) {
      Path lv = arg2.getNavigation().getCurrentPath();
      Brain lv2 = arg2.getBrain();
      if (this.path != lv) {
         this.path = lv;
         lv2.remember(MemoryModuleType.PATH, (Object)lv);
      }

      if (lv != null && this.lookTargetPos != null) {
         WalkTarget lv3 = (WalkTarget)lv2.getOptionalRegisteredMemory(MemoryModuleType.WALK_TARGET).get();
         if (lv3.getLookTarget().getBlockPos().getSquaredDistance(this.lookTargetPos) > 4.0 && this.hasFinishedPath(arg2, lv3, arg.getTime())) {
            this.lookTargetPos = lv3.getLookTarget().getBlockPos();
            this.run(arg, arg2, l);
         }

      }
   }

   private boolean hasFinishedPath(MobEntity entity, WalkTarget walkTarget, long time) {
      BlockPos lv = walkTarget.getLookTarget().getBlockPos();
      this.path = entity.getNavigation().findPathTo((BlockPos)lv, 0);
      this.speed = walkTarget.getSpeed();
      Brain lv2 = entity.getBrain();
      if (this.hasReached(entity, walkTarget)) {
         lv2.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      } else {
         boolean bl = this.path != null && this.path.reachesTarget();
         if (bl) {
            lv2.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         } else if (!lv2.hasMemoryModule(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            lv2.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, (Object)time);
         }

         if (this.path != null) {
            return true;
         }

         Vec3d lv3 = NoPenaltyTargeting.findTo((PathAwareEntity)entity, 10, 7, Vec3d.ofBottomCenter(lv), 1.5707963705062866);
         if (lv3 != null) {
            this.path = entity.getNavigation().findPathTo(lv3.x, lv3.y, lv3.z, 0);
            return this.path != null;
         }
      }

      return false;
   }

   private boolean hasReached(MobEntity entity, WalkTarget walkTarget) {
      return walkTarget.getLookTarget().getBlockPos().getManhattanDistance(entity.getBlockPos()) <= walkTarget.getCompletionRange();
   }

   private static boolean isTargetSpectator(WalkTarget target) {
      LookTarget lv = target.getLookTarget();
      if (lv instanceof EntityLookTarget lv2) {
         return lv2.getEntity().isSpectator();
      } else {
         return false;
      }
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (MobEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (MobEntity)entity, time);
   }
}
