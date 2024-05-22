/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.network;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class PendingUpdateManager
implements AutoCloseable {
    private final Long2ObjectOpenHashMap<PendingUpdate> blockPosToPendingUpdate = new Long2ObjectOpenHashMap();
    private int sequence;
    private boolean pendingSequence;

    public void addPendingUpdate(BlockPos pos, BlockState state, ClientPlayerEntity player) {
        this.blockPosToPendingUpdate.compute(pos.asLong(), (posLong, pendingUpdate) -> {
            if (pendingUpdate != null) {
                return pendingUpdate.withSequence(this.sequence);
            }
            return new PendingUpdate(this.sequence, state, player.getPos());
        });
    }

    public boolean hasPendingUpdate(BlockPos pos, BlockState state) {
        PendingUpdate lv = this.blockPosToPendingUpdate.get(pos.asLong());
        if (lv == null) {
            return false;
        }
        lv.setBlockState(state);
        return true;
    }

    public void processPendingUpdates(int maxProcessableSequence, ClientWorld world) {
        Iterator objectIterator = this.blockPosToPendingUpdate.long2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            PendingUpdate lv = (PendingUpdate)entry.getValue();
            if (lv.sequence > maxProcessableSequence) continue;
            BlockPos lv2 = BlockPos.fromLong(entry.getLongKey());
            objectIterator.remove();
            world.processPendingUpdate(lv2, lv.blockState, lv.playerPos);
        }
    }

    public PendingUpdateManager incrementSequence() {
        ++this.sequence;
        this.pendingSequence = true;
        return this;
    }

    @Override
    public void close() {
        this.pendingSequence = false;
    }

    public int getSequence() {
        return this.sequence;
    }

    public boolean hasPendingSequence() {
        return this.pendingSequence;
    }

    @Environment(value=EnvType.CLIENT)
    static class PendingUpdate {
        final Vec3d playerPos;
        int sequence;
        BlockState blockState;

        PendingUpdate(int sequence, BlockState blockState, Vec3d playerPos) {
            this.sequence = sequence;
            this.blockState = blockState;
            this.playerPos = playerPos;
        }

        PendingUpdate withSequence(int sequence) {
            this.sequence = sequence;
            return this;
        }

        void setBlockState(BlockState state) {
            this.blockState = state;
        }
    }
}

