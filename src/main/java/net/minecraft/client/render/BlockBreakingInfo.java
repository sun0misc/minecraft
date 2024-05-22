/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import net.minecraft.util.math.BlockPos;

public class BlockBreakingInfo
implements Comparable<BlockBreakingInfo> {
    private final int actorNetworkId;
    private final BlockPos pos;
    private int stage;
    private int lastUpdateTick;

    public BlockBreakingInfo(int breakingEntityId, BlockPos pos) {
        this.actorNetworkId = breakingEntityId;
        this.pos = pos;
    }

    public int getActorId() {
        return this.actorNetworkId;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setStage(int stage) {
        if (stage > 10) {
            stage = 10;
        }
        this.stage = stage;
    }

    public int getStage() {
        return this.stage;
    }

    public void setLastUpdateTick(int lastUpdateTick) {
        this.lastUpdateTick = lastUpdateTick;
    }

    public int getLastUpdateTick() {
        return this.lastUpdateTick;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BlockBreakingInfo lv = (BlockBreakingInfo)o;
        return this.actorNetworkId == lv.actorNetworkId;
    }

    public int hashCode() {
        return Integer.hashCode(this.actorNetworkId);
    }

    @Override
    public int compareTo(BlockBreakingInfo arg) {
        if (this.stage != arg.stage) {
            return Integer.compare(this.stage, arg.stage);
        }
        return Integer.compare(this.actorNetworkId, arg.actorNetworkId);
    }

    @Override
    public /* synthetic */ int compareTo(Object other) {
        return this.compareTo((BlockBreakingInfo)other);
    }
}

