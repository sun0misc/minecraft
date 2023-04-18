package net.minecraft.entity.ai;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NoPenaltyTargeting {
   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange);
         return tryMake(entity, horizontalRange, bl, lv);
      });
   }

   @Nullable
   public static Vec3d findTo(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d end, double angleRange) {
      Vec3d lv = end.subtract(entity.getX(), entity.getY(), entity.getZ());
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         BlockPos lvx = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, lv.x, lv.z, angleRange);
         return lvx == null ? null : tryMake(entity, horizontalRange, bl, lvx);
      });
   }

   @Nullable
   public static Vec3d findFrom(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d start) {
      Vec3d lv = entity.getPos().subtract(start);
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         BlockPos lvx = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, lv.x, lv.z, 1.5707963705062866);
         return lvx == null ? null : tryMake(entity, horizontalRange, bl, lvx);
      });
   }

   @Nullable
   private static BlockPos tryMake(PathAwareEntity entity, int horizontalRange, boolean posTargetInRange, BlockPos fuzz) {
      BlockPos lv = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), fuzz);
      return !NavigationConditions.isHeightInvalid(lv, entity) && !NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, lv) && !NavigationConditions.isInvalidPosition(entity.getNavigation(), lv) && !NavigationConditions.hasPathfindingPenalty(entity, lv) ? lv : null;
   }
}
