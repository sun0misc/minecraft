/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;

public class LandingApproachPhase
extends AbstractPhase {
    private static final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility();
    @Nullable
    private Path path;
    @Nullable
    private Vec3d pathTarget;

    public LandingApproachPhase(EnderDragonEntity arg) {
        super(arg);
    }

    public PhaseType<LandingApproachPhase> getType() {
        return PhaseType.LANDING_APPROACH;
    }

    @Override
    public void beginPhase() {
        this.path = null;
        this.pathTarget = null;
    }

    @Override
    public void serverTick() {
        double d;
        double d2 = d = this.pathTarget == null ? 0.0 : this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.updatePath();
        }
    }

    @Override
    @Nullable
    public Vec3d getPathTarget() {
        return this.pathTarget;
    }

    private void updatePath() {
        if (this.path == null || this.path.isFinished()) {
            int j;
            int i = this.dragon.getNearestPathNodeIndex();
            BlockPos lv = this.dragon.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin()));
            PlayerEntity lv2 = this.dragon.getWorld().getClosestPlayer(PLAYERS_IN_RANGE_PREDICATE, this.dragon, (double)lv.getX(), (double)lv.getY(), lv.getZ());
            if (lv2 != null) {
                Vec3d lv3 = new Vec3d(lv2.getX(), 0.0, lv2.getZ()).normalize();
                j = this.dragon.getNearestPathNodeIndex(-lv3.x * 40.0, 105.0, -lv3.z * 40.0);
            } else {
                j = this.dragon.getNearestPathNodeIndex(40.0, lv.getY(), 0.0);
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
            double f;
            BlockPos lv = this.path.getCurrentNodePos();
            this.path.next();
            double d = lv.getX();
            double e = lv.getZ();
            while ((f = (double)((float)lv.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)lv.getY()) {
            }
            this.pathTarget = new Vec3d(d, f, e);
        }
    }
}

