package net.minecraft.entity.ai;

import java.util.Objects;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FuzzyTargeting {
   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
      Objects.requireNonNull(entity);
      return find(entity, horizontalRange, verticalRange, entity::getPathfindingFavor);
   }

   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, ToDoubleFunction scorer) {
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBest(() -> {
         BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange);
         BlockPos lv2 = towardTarget(entity, horizontalRange, bl, lv);
         return lv2 == null ? null : validate(entity, lv2);
      }, scorer);
   }

   @Nullable
   public static Vec3d findTo(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d end) {
      Vec3d lv = end.subtract(entity.getX(), entity.getY(), entity.getZ());
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return findValid(entity, horizontalRange, verticalRange, lv, bl);
   }

   @Nullable
   public static Vec3d findFrom(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d start) {
      Vec3d lv = entity.getPos().subtract(start);
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return findValid(entity, horizontalRange, verticalRange, lv, bl);
   }

   @Nullable
   private static Vec3d findValid(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d direction, boolean posTargetInRange) {
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, direction.x, direction.z, 1.5707963705062866);
         if (lv == null) {
            return null;
         } else {
            BlockPos lv2 = towardTarget(entity, horizontalRange, posTargetInRange, lv);
            return lv2 == null ? null : validate(entity, lv2);
         }
      });
   }

   @Nullable
   public static BlockPos validate(PathAwareEntity entity, BlockPos pos) {
      pos = FuzzyPositions.upWhile(pos, entity.world.getTopY(), (currentPos) -> {
         return NavigationConditions.isSolidAt(entity, currentPos);
      });
      return !NavigationConditions.isWaterAt(entity, pos) && !NavigationConditions.hasPathfindingPenalty(entity, pos) ? pos : null;
   }

   @Nullable
   public static BlockPos towardTarget(PathAwareEntity entity, int horizontalRange, boolean posTargetInRange, BlockPos relativeInRangePos) {
      BlockPos lv = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), relativeInRangePos);
      return !NavigationConditions.isHeightInvalid(lv, entity) && !NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, lv) && !NavigationConditions.isInvalidPosition(entity.getNavigation(), lv) ? lv : null;
   }
}
