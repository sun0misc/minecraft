/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.Nullable;

public class ChunkSectionCache
implements AutoCloseable {
    private final WorldAccess world;
    private final Long2ObjectMap<ChunkSection> cache = new Long2ObjectOpenHashMap<ChunkSection>();
    @Nullable
    private ChunkSection cachedSection;
    private long sectionPos;

    public ChunkSectionCache(WorldAccess world) {
        this.world = world;
    }

    @Nullable
    public ChunkSection getSection(BlockPos pos) {
        int i = this.world.getSectionIndex(pos.getY());
        if (i < 0 || i >= this.world.countVerticalSections()) {
            return null;
        }
        long l2 = ChunkSectionPos.toLong(pos);
        if (this.cachedSection == null || this.sectionPos != l2) {
            this.cachedSection = this.cache.computeIfAbsent(l2, l -> {
                Chunk lv = this.world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
                ChunkSection lv2 = lv.getSection(i);
                lv2.lock();
                return lv2;
            });
            this.sectionPos = l2;
        }
        return this.cachedSection;
    }

    public BlockState getBlockState(BlockPos pos) {
        ChunkSection lv = this.getSection(pos);
        if (lv == null) {
            return Blocks.AIR.getDefaultState();
        }
        int i = ChunkSectionPos.getLocalCoord(pos.getX());
        int j = ChunkSectionPos.getLocalCoord(pos.getY());
        int k = ChunkSectionPos.getLocalCoord(pos.getZ());
        return lv.getBlockState(i, j, k);
    }

    @Override
    public void close() {
        for (ChunkSection lv : this.cache.values()) {
            lv.unlock();
        }
    }
}

