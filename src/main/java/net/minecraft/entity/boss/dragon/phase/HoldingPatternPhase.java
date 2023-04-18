package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class HoldingPatternPhase extends AbstractPhase {
   private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility();
   @Nullable
   private Path path;
   @Nullable
   private Vec3d pathTarget;
   private boolean shouldFindNewPath;

   public HoldingPatternPhase(EnderDragonEntity arg) {
      super(arg);
   }

   public PhaseType getType() {
      return PhaseType.HOLDING_PATTERN;
   }

   public void serverTick() {
      double d = this.pathTarget == null ? 0.0 : this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.tickInRange();
      }

   }

   public void beginPhase() {
      this.path = null;
      this.pathTarget = null;
   }

   @Nullable
   public Vec3d getPathTarget() {
      return this.pathTarget;
   }

   private void tickInRange() {
      int i;
      if (this.path != null && this.path.isFinished()) {
         BlockPos lv = this.dragon.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPortalFeature.ORIGIN));
         i = this.dragon.getFight() == null ? 0 : this.dragon.getFight().getAliveEndCrystals();
         if (this.dragon.getRandom().nextInt(i + 3) == 0) {
            this.dragon.getPhaseManager().setPhase(PhaseType.LANDING_APPROACH);
            return;
         }

         PlayerEntity lv2 = this.dragon.world.getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, this.dragon, (double)lv.getX(), (double)lv.getY(), (double)lv.getZ());
         double d;
         if (lv2 != null) {
            d = lv.getSquaredDistance(lv2.getPos()) / 512.0;
         } else {
            d = 64.0;
         }

         if (lv2 != null && (this.dragon.getRandom().nextInt((int)(d + 2.0)) == 0 || this.dragon.getRandom().nextInt(i + 2) == 0)) {
            this.strafePlayer(lv2);
            return;
         }
      }

      if (this.path == null || this.path.isFinished()) {
         int j = this.dragon.getNearestPathNodeIndex();
         i = j;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.shouldFindNewPath = !this.shouldFindNewPath;
            i = j + 6;
         }

         if (this.shouldFindNewPath) {
            ++i;
         } else {
            --i;
         }

         if (this.dragon.getFight() != null && this.dragon.getFight().getAliveEndCrystals() >= 0) {
            i %= 12;
            if (i < 0) {
               i += 12;
            }
         } else {
            i -= 12;
            i &= 7;
            i += 12;
         }

         this.path = this.dragon.findPath(j, i, (PathNode)null);
         if (this.path != null) {
            this.path.next();
         }
      }

      this.followPath();
   }

   private void strafePlayer(PlayerEntity player) {
      this.dragon.getPhaseManager().setPhase(PhaseType.STRAFE_PLAYER);
      ((StrafePlayerPhase)this.dragon.getPhaseManager().create(PhaseType.STRAFE_PLAYER)).setTargetEntity(player);
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

   public void crystalDestroyed(EndCrystalEntity crystal, BlockPos pos, DamageSource source, @Nullable PlayerEntity player) {
      if (player != null && this.dragon.canTarget(player)) {
         this.strafePlayer(player);
      }

   }
}
