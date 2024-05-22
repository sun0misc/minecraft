/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public abstract class EntityNavigation {
    private static final int RECALCULATE_COOLDOWN = 20;
    private static final int field_41545 = 100;
    private static final float field_41546 = 0.25f;
    protected final MobEntity entity;
    protected final World world;
    @Nullable
    protected Path currentPath;
    protected double speed;
    protected int tickCount;
    protected int pathStartTime;
    protected Vec3d pathStartPos = Vec3d.ZERO;
    protected Vec3i lastNodePosition = Vec3i.ZERO;
    protected long currentNodeMs;
    protected long lastActiveTickMs;
    protected double currentNodeTimeout;
    protected float nodeReachProximity = 0.5f;
    protected boolean inRecalculationCooldown;
    protected long lastRecalculateTime;
    protected PathNodeMaker nodeMaker;
    @Nullable
    private BlockPos currentTarget;
    private int currentDistance;
    private float rangeMultiplier = 1.0f;
    private final PathNodeNavigator pathNodeNavigator;
    private boolean nearPathStartPos;

    public EntityNavigation(MobEntity entity, World world) {
        this.entity = entity;
        this.world = world;
        int i = MathHelper.floor(entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE) * 16.0);
        this.pathNodeNavigator = this.createPathNodeNavigator(i);
    }

    public void resetRangeMultiplier() {
        this.rangeMultiplier = 1.0f;
    }

    public void setRangeMultiplier(float rangeMultiplier) {
        this.rangeMultiplier = rangeMultiplier;
    }

    @Nullable
    public BlockPos getTargetPos() {
        return this.currentTarget;
    }

    protected abstract PathNodeNavigator createPathNodeNavigator(int var1);

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void recalculatePath() {
        if (this.world.getTime() - this.lastRecalculateTime > 20L) {
            if (this.currentTarget != null) {
                this.currentPath = null;
                this.currentPath = this.findPathTo(this.currentTarget, this.currentDistance);
                this.lastRecalculateTime = this.world.getTime();
                this.inRecalculationCooldown = false;
            }
        } else {
            this.inRecalculationCooldown = true;
        }
    }

    @Nullable
    public final Path findPathTo(double x, double y, double z, int distance) {
        return this.findPathTo(BlockPos.ofFloored(x, y, z), distance);
    }

    @Nullable
    public Path findPathToAny(Stream<BlockPos> positions, int distance) {
        return this.findPathTo(positions.collect(Collectors.toSet()), 8, false, distance);
    }

    @Nullable
    public Path findPathTo(Set<BlockPos> positions, int distance) {
        return this.findPathTo(positions, 8, false, distance);
    }

    @Nullable
    public Path findPathTo(BlockPos target, int distance) {
        return this.findPathTo(ImmutableSet.of(target), 8, false, distance);
    }

    @Nullable
    public Path findPathTo(BlockPos target, int minDistance, int maxDistance) {
        return this.findPathToAny(ImmutableSet.of(target), 8, false, minDistance, maxDistance);
    }

    @Nullable
    public Path findPathTo(Entity entity, int distance) {
        return this.findPathTo(ImmutableSet.of(entity.getBlockPos()), 16, true, distance);
    }

    @Nullable
    protected Path findPathTo(Set<BlockPos> positions, int range, boolean useHeadPos, int distance) {
        return this.findPathToAny(positions, range, useHeadPos, distance, (float)this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
    }

    @Nullable
    protected Path findPathToAny(Set<BlockPos> positions, int range, boolean useHeadPos, int distance, float followRange) {
        if (positions.isEmpty()) {
            return null;
        }
        if (this.entity.getY() < (double)this.world.getBottomY()) {
            return null;
        }
        if (!this.isAtValidPosition()) {
            return null;
        }
        if (this.currentPath != null && !this.currentPath.isFinished() && positions.contains(this.currentTarget)) {
            return this.currentPath;
        }
        this.world.getProfiler().push("pathfind");
        BlockPos lv = useHeadPos ? this.entity.getBlockPos().up() : this.entity.getBlockPos();
        int k = (int)(followRange + (float)range);
        ChunkCache lv2 = new ChunkCache(this.world, lv.add(-k, -k, -k), lv.add(k, k, k));
        Path lv3 = this.pathNodeNavigator.findPathToAny(lv2, this.entity, positions, followRange, distance, this.rangeMultiplier);
        this.world.getProfiler().pop();
        if (lv3 != null && lv3.getTarget() != null) {
            this.currentTarget = lv3.getTarget();
            this.currentDistance = distance;
            this.resetNode();
        }
        return lv3;
    }

    public boolean startMovingTo(double x, double y, double z, double speed) {
        return this.startMovingAlong(this.findPathTo(x, y, z, 1), speed);
    }

    public boolean startMovingTo(double x, double y, double z, int distance, double speed) {
        return this.startMovingAlong(this.findPathTo(x, y, z, distance), speed);
    }

    public boolean startMovingTo(Entity entity, double speed) {
        Path lv = this.findPathTo(entity, 1);
        return lv != null && this.startMovingAlong(lv, speed);
    }

    public boolean startMovingAlong(@Nullable Path path, double speed) {
        if (path == null) {
            this.currentPath = null;
            return false;
        }
        if (!path.equalsPath(this.currentPath)) {
            this.currentPath = path;
        }
        if (this.isIdle()) {
            return false;
        }
        this.adjustPath();
        if (this.currentPath.getLength() <= 0) {
            return false;
        }
        this.speed = speed;
        Vec3d lv = this.getPos();
        this.pathStartTime = this.tickCount;
        this.pathStartPos = lv;
        return true;
    }

    @Nullable
    public Path getCurrentPath() {
        return this.currentPath;
    }

    public void tick() {
        Vec3d lv;
        ++this.tickCount;
        if (this.inRecalculationCooldown) {
            this.recalculatePath();
        }
        if (this.isIdle()) {
            return;
        }
        if (this.isAtValidPosition()) {
            this.continueFollowingPath();
        } else if (this.currentPath != null && !this.currentPath.isFinished()) {
            lv = this.getPos();
            Vec3d lv2 = this.currentPath.getNodePosition(this.entity);
            if (lv.y > lv2.y && !this.entity.isOnGround() && MathHelper.floor(lv.x) == MathHelper.floor(lv2.x) && MathHelper.floor(lv.z) == MathHelper.floor(lv2.z)) {
                this.currentPath.next();
            }
        }
        DebugInfoSender.sendPathfindingData(this.world, this.entity, this.currentPath, this.nodeReachProximity);
        if (this.isIdle()) {
            return;
        }
        lv = this.currentPath.getNodePosition(this.entity);
        this.entity.getMoveControl().moveTo(lv.x, this.adjustTargetY(lv), lv.z, this.speed);
    }

    protected double adjustTargetY(Vec3d pos) {
        BlockPos lv = BlockPos.ofFloored(pos);
        return this.world.getBlockState(lv.down()).isAir() ? pos.y : LandPathNodeMaker.getFeetY(this.world, lv);
    }

    protected void continueFollowingPath() {
        boolean bl;
        Vec3d lv = this.getPos();
        this.nodeReachProximity = this.entity.getWidth() > 0.75f ? this.entity.getWidth() / 2.0f : 0.75f - this.entity.getWidth() / 2.0f;
        BlockPos lv2 = this.currentPath.getCurrentNodePos();
        double d = Math.abs(this.entity.getX() - ((double)lv2.getX() + 0.5));
        double e = Math.abs(this.entity.getY() - (double)lv2.getY());
        double f = Math.abs(this.entity.getZ() - ((double)lv2.getZ() + 0.5));
        boolean bl2 = bl = d < (double)this.nodeReachProximity && f < (double)this.nodeReachProximity && e < 1.0;
        if (bl || this.canJumpToNext(this.currentPath.getCurrentNode().type) && this.shouldJumpToNextNode(lv)) {
            this.currentPath.next();
        }
        this.checkTimeouts(lv);
    }

    private boolean shouldJumpToNextNode(Vec3d currentPos) {
        boolean bl2;
        if (this.currentPath.getCurrentNodeIndex() + 1 >= this.currentPath.getLength()) {
            return false;
        }
        Vec3d lv = Vec3d.ofBottomCenter(this.currentPath.getCurrentNodePos());
        if (!currentPos.isInRange(lv, 2.0)) {
            return false;
        }
        if (this.canPathDirectlyThrough(currentPos, this.currentPath.getNodePosition(this.entity))) {
            return true;
        }
        Vec3d lv2 = Vec3d.ofBottomCenter(this.currentPath.getNodePos(this.currentPath.getCurrentNodeIndex() + 1));
        Vec3d lv3 = lv.subtract(currentPos);
        Vec3d lv4 = lv2.subtract(currentPos);
        double d = lv3.lengthSquared();
        double e = lv4.lengthSquared();
        boolean bl = e < d;
        boolean bl3 = bl2 = d < 0.5;
        if (bl || bl2) {
            Vec3d lv5 = lv3.normalize();
            Vec3d lv6 = lv4.normalize();
            return lv6.dotProduct(lv5) < 0.0;
        }
        return false;
    }

    protected void checkTimeouts(Vec3d currentPos) {
        if (this.tickCount - this.pathStartTime > 100) {
            float f = this.entity.getMovementSpeed() >= 1.0f ? this.entity.getMovementSpeed() : this.entity.getMovementSpeed() * this.entity.getMovementSpeed();
            float g = f * 100.0f * 0.25f;
            if (currentPos.squaredDistanceTo(this.pathStartPos) < (double)(g * g)) {
                this.nearPathStartPos = true;
                this.stop();
            } else {
                this.nearPathStartPos = false;
            }
            this.pathStartTime = this.tickCount;
            this.pathStartPos = currentPos;
        }
        if (this.currentPath != null && !this.currentPath.isFinished()) {
            BlockPos lv = this.currentPath.getCurrentNodePos();
            long l = this.world.getTime();
            if (lv.equals(this.lastNodePosition)) {
                this.currentNodeMs += l - this.lastActiveTickMs;
            } else {
                this.lastNodePosition = lv;
                double d = currentPos.distanceTo(Vec3d.ofBottomCenter(this.lastNodePosition));
                double d2 = this.currentNodeTimeout = this.entity.getMovementSpeed() > 0.0f ? d / (double)this.entity.getMovementSpeed() * 20.0 : 0.0;
            }
            if (this.currentNodeTimeout > 0.0 && (double)this.currentNodeMs > this.currentNodeTimeout * 3.0) {
                this.resetNodeAndStop();
            }
            this.lastActiveTickMs = l;
        }
    }

    private void resetNodeAndStop() {
        this.resetNode();
        this.stop();
    }

    private void resetNode() {
        this.lastNodePosition = Vec3i.ZERO;
        this.currentNodeMs = 0L;
        this.currentNodeTimeout = 0.0;
        this.nearPathStartPos = false;
    }

    public boolean isIdle() {
        return this.currentPath == null || this.currentPath.isFinished();
    }

    public boolean isFollowingPath() {
        return !this.isIdle();
    }

    public void stop() {
        this.currentPath = null;
    }

    protected abstract Vec3d getPos();

    protected abstract boolean isAtValidPosition();

    protected void adjustPath() {
        if (this.currentPath == null) {
            return;
        }
        for (int i = 0; i < this.currentPath.getLength(); ++i) {
            PathNode lv = this.currentPath.getNode(i);
            PathNode lv2 = i + 1 < this.currentPath.getLength() ? this.currentPath.getNode(i + 1) : null;
            BlockState lv3 = this.world.getBlockState(new BlockPos(lv.x, lv.y, lv.z));
            if (!lv3.isIn(BlockTags.CAULDRONS)) continue;
            this.currentPath.setNode(i, lv.copyWithNewPosition(lv.x, lv.y + 1, lv.z));
            if (lv2 == null || lv.y < lv2.y) continue;
            this.currentPath.setNode(i + 1, lv.copyWithNewPosition(lv2.x, lv.y + 1, lv2.z));
        }
    }

    protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target) {
        return false;
    }

    public boolean canJumpToNext(PathNodeType nodeType) {
        return nodeType != PathNodeType.DANGER_FIRE && nodeType != PathNodeType.DANGER_OTHER && nodeType != PathNodeType.WALKABLE_DOOR;
    }

    protected static boolean doesNotCollide(MobEntity entity, Vec3d startPos, Vec3d entityPos, boolean includeFluids) {
        Vec3d lv = new Vec3d(entityPos.x, entityPos.y + (double)entity.getHeight() * 0.5, entityPos.z);
        return entity.getWorld().raycast(new RaycastContext(startPos, lv, RaycastContext.ShapeType.COLLIDER, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    public boolean isValidPosition(BlockPos pos) {
        BlockPos lv = pos.down();
        return this.world.getBlockState(lv).isOpaqueFullCube(this.world, lv);
    }

    public PathNodeMaker getNodeMaker() {
        return this.nodeMaker;
    }

    public void setCanSwim(boolean canSwim) {
        this.nodeMaker.setCanSwim(canSwim);
    }

    public boolean canSwim() {
        return this.nodeMaker.canSwim();
    }

    public boolean shouldRecalculatePath(BlockPos pos) {
        if (this.inRecalculationCooldown) {
            return false;
        }
        if (this.currentPath == null || this.currentPath.isFinished() || this.currentPath.getLength() == 0) {
            return false;
        }
        PathNode lv = this.currentPath.getEnd();
        Vec3d lv2 = new Vec3d(((double)lv.x + this.entity.getX()) / 2.0, ((double)lv.y + this.entity.getY()) / 2.0, ((double)lv.z + this.entity.getZ()) / 2.0);
        return pos.isWithinDistance(lv2, (double)(this.currentPath.getLength() - this.currentPath.getCurrentNodeIndex()));
    }

    public float getNodeReachProximity() {
        return this.nodeReachProximity;
    }

    public boolean isNearPathStartPos() {
        return this.nearPathStartPos;
    }
}

