package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class FleeTask extends MultiTickTask {
   private static final int MIN_RUN_TIME = 100;
   private static final int MAX_RUN_TIME = 120;
   private static final int HORIZONTAL_RANGE = 5;
   private static final int VERTICAL_RANGE = 4;
   private static final Predicate PANIC_PREDICATE = (entity) -> {
      return entity.getAttacker() != null || entity.shouldEscapePowderSnow() || entity.isOnFire();
   };
   private final float speed;
   private final Predicate predicate;

   public FleeTask(float speed) {
      this(speed, PANIC_PREDICATE);
   }

   public FleeTask(float speed, Predicate predicate) {
      super(ImmutableMap.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.REGISTERED, MemoryModuleType.HURT_BY, MemoryModuleState.VALUE_PRESENT), 100, 120);
      this.speed = speed;
      this.predicate = predicate;
   }

   protected boolean shouldRun(ServerWorld arg, PathAwareEntity arg2) {
      return this.predicate.test(arg2);
   }

   protected boolean shouldKeepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      return true;
   }

   protected void run(ServerWorld arg, PathAwareEntity arg2, long l) {
      arg2.getBrain().remember(MemoryModuleType.IS_PANICKING, (Object)true);
      arg2.getBrain().forget(MemoryModuleType.WALK_TARGET);
   }

   protected void finishRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      Brain lv = arg2.getBrain();
      lv.forget(MemoryModuleType.IS_PANICKING);
   }

   protected void keepRunning(ServerWorld arg, PathAwareEntity arg2, long l) {
      if (arg2.getNavigation().isIdle()) {
         Vec3d lv = this.findTarget(arg2, arg);
         if (lv != null) {
            arg2.getBrain().remember(MemoryModuleType.WALK_TARGET, (Object)(new WalkTarget(lv, this.speed, 0)));
         }
      }

   }

   @Nullable
   private Vec3d findTarget(PathAwareEntity entity, ServerWorld world) {
      if (entity.isOnFire()) {
         Optional optional = this.findClosestWater(world, entity).map(Vec3d::ofBottomCenter);
         if (optional.isPresent()) {
            return (Vec3d)optional.get();
         }
      }

      return FuzzyTargeting.find(entity, 5, 4);
   }

   private Optional findClosestWater(BlockView world, Entity entity) {
      BlockPos lv = entity.getBlockPos();
      return !world.getBlockState(lv).getCollisionShape(world, lv).isEmpty() ? Optional.empty() : BlockPos.findClosest(lv, 5, 1, (pos) -> {
         return world.getFluidState(pos).isIn(FluidTags.WATER);
      });
   }

   // $FF: synthetic method
   protected void finishRunning(ServerWorld world, LivingEntity entity, long time) {
      this.finishRunning(world, (PathAwareEntity)entity, time);
   }

   // $FF: synthetic method
   protected void keepRunning(ServerWorld world, LivingEntity entity, long time) {
      this.keepRunning(world, (PathAwareEntity)entity, time);
   }

   // $FF: synthetic method
   protected void run(ServerWorld world, LivingEntity entity, long time) {
      this.run(world, (PathAwareEntity)entity, time);
   }
}
