package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class LandingApproachPhase extends AbstractPhase {
   private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility();
   @Nullable
   private Path path;
   @Nullable
   private Vec3d pathTarget;

   public LandingApproachPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public PhaseType getType() {
      return PhaseType.LANDING_APPROACH;
   }

   public void beginPhase() {
      this.path = null;
      this.pathTarget = null;
   }

   public void serverTick() {
      double d = this.pathTarget == null ? 0.0 : this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.updatePath();
      }

   }

   @Nullable
   public Vec3d getPathTarget() {
      return this.pathTarget;
   }

   private void updatePath() {
      if (this.path == null || this.path.isFinished()) {
         int i = this.dragon.getNearestPathNodeIndex();
         BlockPos lv = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
         PlayerEntity lv2 = this.dragon.world.getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, this.dragon, (double)lv.getX(), (double)lv.getY(), (double)lv.getZ());
         int j;
         if (lv2 != null) {
            Vec3d lv3 = (new Vec3d(lv2.getX(), 0.0, lv2.getZ())).normalize();
            j = this.dragon.getNearestPathNodeIndex(-lv3.x * 40.0, 105.0, -lv3.z * 40.0);
         } else {
            j = this.dragon.getNearestPathNodeIndex(40.0, (double)lv.getY(), 0.0);
         }

         PathNode lv4 = new PathNode(lv.getX(), lv.getY(), lv.getZ());
         this.path = this.dragon.findPath(i, j, lv4);
         if (this.path != null) {
            this.path.next();
         }
      }

      this.followPath();
      if (this.path != null && this.path.isFinished()) {
         this.dragon.getPhaseManager().setPhase(PhaseType.LANDING);
      }

   }

   private void followPath() {
      if (this.path != null && !this.path.isFinished()) {
         Vec3i lv = this.path.getCurrentNodePos();
         this.path.next();
         double d = (double)lv.getX();
         double e = (double)lv.getZ();

         double f;
         do {
            f = (double)((float)lv.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(f < (double)lv.getY());

         this.pathTarget = new Vec3d(d, f, e);
      }

   }
}
