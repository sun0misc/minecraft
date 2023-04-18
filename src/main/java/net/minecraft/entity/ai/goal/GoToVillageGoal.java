package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class GoToVillageGoal extends Goal {
   private static final int field_30228 = 10;
   private final PathAwareEntity mob;
   private final int searchRange;
   @Nullable
   private BlockPos targetPosition;

   public GoToVillageGoal(PathAwareEntity mob, int searchRange) {
      this.mob = mob;
      this.searchRange = toGoalTicks(searchRange);
      this.setControls(EnumSet.of(Goal.Control.MOVE));
   }

   public boolean canStart() {
      if (this.mob.hasPassengers()) {
         return false;
      } else if (this.mob.world.isDay()) {
         return false;
      } else if (this.mob.getRandom().nextInt(this.searchRange) != 0) {
         return false;
      } else {
         ServerWorld lv = (ServerWorld)this.mob.world;
         BlockPos lv2 = this.mob.getBlockPos();
         if (!lv.isNearOccupiedPointOfInterest(lv2, 6)) {
            return false;
         } else {
            Vec3d lv3 = FuzzyTargeting.find(this.mob, 15, 7, (arg2) -> {
               return (double)(-lv.getOccupiedPointOfInterestDistance(ChunkSectionPos.from(arg2)));
            });
            this.targetPosition = lv3 == null ? null : BlockPos.ofFloored(lv3);
            return this.targetPosition != null;
         }
      }
   }

   public boolean shouldContinue() {
      return this.targetPosition != null && !this.mob.getNavigation().isIdle() && this.mob.getNavigation().getTargetPos().equals(this.targetPosition);
   }

   public void tick() {
      if (this.targetPosition != null) {
         EntityNavigation lv = this.mob.getNavigation();
         if (lv.isIdle() && !this.targetPosition.isWithinDistance(this.mob.getPos(), 10.0)) {
            Vec3d lv2 = Vec3d.ofBottomCenter(this.targetPosition);
            Vec3d lv3 = this.mob.getPos();
            Vec3d lv4 = lv3.subtract(lv2);
            lv2 = lv4.multiply(0.4).add(lv2);
            Vec3d lv5 = lv2.subtract(lv3).normalize().multiply(10.0).add(lv3);
            BlockPos lv6 = BlockPos.ofFloored(lv5);
            lv6 = this.mob.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, lv6);
            if (!lv.startMovingTo((double)lv6.getX(), (double)lv6.getY(), (double)lv6.getZ(), 1.0)) {
               this.findOtherWaypoint();
            }
         }

      }
   }

   private void findOtherWaypoint() {
      Random lv = this.mob.getRandom();
      BlockPos lv2 = this.mob.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, this.mob.getBlockPos().add(-8 + lv.nextInt(16), 0, -8 + lv.nextInt(16)));
      this.mob.getNavigation().startMovingTo((double)lv2.getX(), (double)lv2.getY(), (double)lv2.getZ(), 1.0);
   }
}
