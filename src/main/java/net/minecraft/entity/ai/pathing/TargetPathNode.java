/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.network.PacketByteBuf;

public class TargetPathNode
extends PathNode {
    private float nearestNodeDistance = Float.MAX_VALUE;
    private PathNode nearestNode;
    private boolean reached;

    public TargetPathNode(PathNode node) {
        super(node.x, node.y, node.z);
    }

    public TargetPathNode(int i, int j, int k) {
        super(i, j, k);
    }

    public void updateNearestNode(float distance, PathNode node) {
        if (distance < this.nearestNodeDistance) {
            this.nearestNodeDistance = distance;
            this.nearestNode = node;
        }
    }

    public PathNode getNearestNode() {
        return this.nearestNode;
    }

    public void markReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }

    public static TargetPathNode fromBuffer(PacketByteBuf buffer) {
        TargetPathNode lv = new TargetPathNode(buffer.readInt(), buffer.readInt(), buffer.readInt());
        TargetPathNode.readFromBuf(buffer, lv);
        return lv;
    }
}

