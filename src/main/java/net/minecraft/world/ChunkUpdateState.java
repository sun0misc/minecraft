/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class ChunkUpdateState
extends PersistentState {
    private static final String REMAINING_KEY = "Remaining";
    private static final String ALL_KEY = "All";
    private final LongSet all;
    private final LongSet remaining;

    public static PersistentState.Type<ChunkUpdateState> getPersistentStateType() {
        return new PersistentState.Type<ChunkUpdateState>(ChunkUpdateState::new, ChunkUpdateState::fromNbt, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
    }

    private ChunkUpdateState(LongSet all, LongSet remaining) {
        this.all = all;
        this.remaining = remaining;
    }

    public ChunkUpdateState() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public static ChunkUpdateState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return new ChunkUpdateState(new LongOpenHashSet(nbt.getLongArray(ALL_KEY)), new LongOpenHashSet(nbt.getLongArray(REMAINING_KEY)));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLongArray(ALL_KEY, this.all.toLongArray());
        nbt.putLongArray(REMAINING_KEY, this.remaining.toLongArray());
        return nbt;
    }

    public void add(long pos) {
        this.all.add(pos);
        this.remaining.add(pos);
    }

    public boolean contains(long pos) {
        return this.all.contains(pos);
    }

    public boolean isRemaining(long pos) {
        return this.remaining.contains(pos);
    }

    public void markResolved(long pos) {
        this.remaining.remove(pos);
    }

    public LongSet getAll() {
        return this.all;
    }
}

