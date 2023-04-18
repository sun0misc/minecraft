package net.minecraft.entity.ai;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class AboveGroundTargeting {
   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, double x, double z, float angle, int maxAboveSolid, int minAboveSolid) {
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, x, z, (double)angle);
         if (lv == null) {
            return null;
         } else {
            BlockPos lv2 = FuzzyTargeting.towardTarget(entity, horizontalRange, bl, lv);
            if (lv2 == null) {
               return null;
            } else {
               lv2 = FuzzyPositions.upWhile(lv2, entity.getRandom().nextInt(maxAboveSolid - minAboveSolid + 1) + minAboveSolid, entity.world.getTopY(), (pos) -> {
                  return NavigationConditions.isSolidAt(entity, pos);
               });
               return !NavigationConditions.isWaterAt(entity, lv2) && !NavigationConditions.hasPathfindingPenalty(entity, lv2) ? lv2 : null;
            }
         }
      });
   }
}
