package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class TakeoffPhase extends AbstractPhase {
   private boolean shouldFindNewPath;
   @Nullable
   private Path path;
   @Nullable
   private Vec3d pathTarget;

   public TakeoffPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public void serverTick() {
      if (!this.shouldFindNewPath && this.path != null) {
         BlockPos lv = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
         if (!lv.isWithinDistance(this.dragon.getPos(), 10.0)) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
         }
      } else {
         this.shouldFindNewPath = false;
         this.updatePath();
      }

   }

   public void beginPhase() {
      this.shouldFindNewPath = true;
      this.path = null;
      this.pathTarget = null;
   }

   private void updatePath() {
      int i = this.dragon.getNearestPathNodeIndex();
      Vec3d lv = this.dragon.getRotationVectorFromPhase(1.0F);
      int j = this.dragon.getNearestPathNodeIndex(-lv.x * 40.0, 105.0, -lv.z * 40.0);
      if (this.dragon.getFight() != null && this.dragon.getFight().getAliveEndCrystals() > 0) {
         j %= 12;
         if (j < 0) {
            j += 12;
         }
      } else {
         j -= 12;
         j &= 7;
         j += 12;
      }

      this.path = this.dragon.findPath(i, j, (PathNode)null);
      this.followPath();
   }

   private void followPath() {
      if (this.path != null) {
         this.path.next();
         if (!this.path.isFinished()) {
            Vec3i lv = this.path.getCurrentNodePos();
            this.path.next();

            double d;
            do {
               d = (double)((float)lv.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
            } while(d < (double)lv.getY());

            this.pathTarget = new Vec3d((double)lv.getX(), d, (double)lv.getZ());
         }
      }

   }

   @Nullable
   public Vec3d getPathTarget() {
      return this.pathTarget;
   }

   public PhaseType getType() {
      return PhaseType.TAKEOFF;
   }
}
