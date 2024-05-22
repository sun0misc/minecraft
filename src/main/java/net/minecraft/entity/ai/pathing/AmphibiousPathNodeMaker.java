/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class AmphibiousPathNodeMaker
extends LandPathNodeMaker {
    private final boolean penalizeDeepWater;
    private float oldWalkablePenalty;
    private float oldWaterBorderPenalty;

    public AmphibiousPathNodeMaker(boolean penalizeDeepWater) {
        this.penalizeDeepWater = penalizeDeepWater;
    }

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        entity.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.oldWalkablePenalty = entity.getPathfindingPenalty(PathNodeType.WALKABLE);
        entity.setPathfindingPenalty(PathNodeType.WALKABLE, 6.0f);
        this.oldWaterBorderPenalty = entity.getPathfindingPenalty(PathNodeType.WATER_BORDER);
        entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, 4.0f);
    }

    @Override
    public void clear() {
        this.entity.setPathfindingPenalty(PathNodeType.WALKABLE, this.oldWalkablePenalty);
        this.entity.setPathfindingPenalty(PathNodeType.WATER_BORDER, this.oldWaterBorderPenalty);
        super.clear();
    }

    @Override
    public PathNode getStart() {
        if (!this.entity.isTouchingWater()) {
            return super.getStart();
        }
        return this.getStart(new BlockPos(MathHelper.floor(this.entity.getBoundingBox().minX), MathHelper.floor(this.entity.getBoundingBox().minY + 0.5), MathHelper.floor(this.entity.getBoundingBox().minZ)));
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return this.createNode(x, y + 0.5, z);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        int i = super.getSuccessors(successors, node);
        PathNodeType lv = this.getNodeType(node.x, node.y + 1, node.z);
        PathNodeType lv2 = this.getNodeType(node.x, node.y, node.z);
        int j = this.entity.getPathfindingPenalty(lv) >= 0.0f && lv2 != PathNodeType.STICKY_HONEY ? MathHelper.floor(Math.max(1.0f, this.entity.getStepHeight())) : 0;
        double d = this.getFeetY(new BlockPos(node.x, node.y, node.z));
        PathNode lv3 = this.getPathNode(node.x, node.y + 1, node.z, Math.max(0, j - 1), d, Direction.UP, lv2);
        PathNode lv4 = this.getPathNode(node.x, node.y - 1, node.z, j, d, Direction.DOWN, lv2);
        if (this.isValidAquaticAdjacentSuccessor(lv3, node)) {
            successors[i++] = lv3;
        }
        if (this.isValidAquaticAdjacentSuccessor(lv4, node) && lv2 != PathNodeType.TRAPDOOR) {
            successors[i++] = lv4;
        }
        for (int k = 0; k < i; ++k) {
            PathNode lv5 = successors[k];
            if (lv5.type != PathNodeType.WATER || !this.penalizeDeepWater || lv5.y >= this.entity.getWorld().getSeaLevel() - 10) continue;
            lv5.penalty += 1.0f;
        }
        return i;
    }

    private boolean isValidAquaticAdjacentSuccessor(@Nullable PathNode node, PathNode successor) {
        return this.isValidAdjacentSuccessor(node, successor) && node.type == PathNodeType.WATER;
    }

    @Override
    protected boolean isAmphibious() {
        return true;
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        PathNodeType lv = context.getNodeType(x, y, z);
        if (lv == PathNodeType.WATER) {
            BlockPos.Mutable lv2 = new BlockPos.Mutable();
            for (Direction lv3 : Direction.values()) {
                lv2.set(x, y, z).move(lv3);
                PathNodeType lv4 = context.getNodeType(lv2.getX(), lv2.getY(), lv2.getZ());
                if (lv4 != PathNodeType.BLOCKED) continue;
                return PathNodeType.WATER_BORDER;
            }
            return PathNodeType.WATER;
        }
        return super.getDefaultNodeType(context, x, y, z);
    }
}

