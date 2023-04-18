package net.minecraft.entity.ai;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NoWaterTargeting {
   @Nullable
   public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, Vec3d direction, double angleRange) {
      Vec3d lv = direction.subtract(entity.getX(), entity.getY(), entity.getZ());
      boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
      return FuzzyPositions.guessBestPathTarget(entity, () -> {
         BlockPos lvx = NoPenaltySolidTargeting.tryMake(entity, horizontalRange, verticalRange, startHeight, lv.x, lv.z, angleRange, bl);
         return lvx != null && !NavigationConditions.isWaterAt(entity, lvx) ? lvx : null;
      });
   }
}
