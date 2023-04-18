package net.minecraft.entity.ai;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NoPenaltySolidTargeting {
   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double rangeAngle) {
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         return tryMake(entity, horizontalRange, verticalRange, startHeight, directionX, directionZ, rangeAngle, bl);
      });
   }

   @Nullable
   public static BlockPos tryMake(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double rangeAngle, boolean posTargetInRange) {
      BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, startHeight, directionX, directionZ, rangeAngle);
      if (lv == null) {
         return null;
      } else {
         BlockPos lv2 = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), lv);
         if (!NavigationConditions.isHeightInvalid(lv2, entity) && !NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, lv2)) {
            lv2 = FuzzyPositions.upWhile(lv2, entity.world.getTopY(), (pos) -> {
               return NavigationConditions.isSolidAt(entity, pos);
            });
            return NavigationConditions.hasPathfindingPenalty(entity, lv2) ? null : lv2;
         } else {
            return null;
         }
      }
   }
}
