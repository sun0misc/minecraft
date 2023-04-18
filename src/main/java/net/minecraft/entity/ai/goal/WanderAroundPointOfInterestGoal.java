package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WanderAroundPointOfInterestGoal extends WanderAroundGoal {
   private static final int HORIZONTAL_RANGE = 10;
   private static final int VERTICAL_RANGE = 7;

   public WanderAroundPointOfInterestGoal(PathAwareEntity entity, double speed, boolean canDespawn) {
      super(entity, speed, 10, canDespawn);
   }

   public boolean canStart() {
      ServerWorld lv = (ServerWorld)this.mob.world;
      BlockPos lv2 = this.mob.getBlockPos();
      return lv.isNearOccupiedPointOfInterest(lv2) ? false : super.canStart();
   }

   @Nullable
   protected Vec3d getWanderTarget() {
      ServerWorld lv = (ServerWorld)this.mob.world;
      BlockPos lv2 = this.mob.getBlockPos();
      ChunkSectionPos lv3 = ChunkSectionPos.from(lv2);
      ChunkSectionPos lv4 = LookTargetUtil.getPosClosestToOccupiedPointOfInterest(lv, lv3, 2);
      return lv4 != lv3 ? NoPenaltyTargeting.findTo(this.mob, 10, 7, Vec3d.ofBottomCenter(lv4.getCenterPos()), 1.5707963705062866) : null;
   }
}
