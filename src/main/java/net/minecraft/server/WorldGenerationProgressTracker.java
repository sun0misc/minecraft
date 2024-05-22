/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public class WorldGenerationProgressTracker
implements WorldGenerationProgressListener {
    private final WorldGenerationProgressLogger progressLogger;
    private final Long2ObjectOpenHashMap<ChunkStatus> chunkStatuses = new Long2ObjectOpenHashMap();
    private ChunkPos spawnPos = new ChunkPos(0, 0);
    private final int centerSize;
    private final int radius;
    private final int size;
    private boolean running;

    private WorldGenerationProgressTracker(WorldGenerationProgressLogger progressLogger, int centerSize, int radius, int size) {
        this.progressLogger = progressLogger;
        this.centerSize = centerSize;
        this.radius = radius;
        this.size = size;
    }

    public static WorldGenerationProgressTracker create(int spawnChunkRadius) {
        return spawnChunkRadius > 0 ? WorldGenerationProgressTracker.forSpawnChunks(spawnChunkRadius + 1) : WorldGenerationProgressTracker.noSpawnChunks();
    }

    public static WorldGenerationProgressTracker forSpawnChunks(int spawnChunkRadius) {
        WorldGenerationProgressLogger lv = WorldGenerationProgressLogger.forSpawnChunks(spawnChunkRadius);
        int j = WorldGenerationProgressListener.getStartRegionSize(spawnChunkRadius);
        int k = spawnChunkRadius + ChunkLevels.FULL_GENERATION_REQUIRED_LEVEL;
        int l = WorldGenerationProgressListener.getStartRegionSize(k);
        return new WorldGenerationProgressTracker(lv, j, k, l);
    }

    public static WorldGenerationProgressTracker noSpawnChunks() {
        return new WorldGenerationProgressTracker(WorldGenerationProgressLogger.noSpawnChunks(), 0, 0, 0);
    }

    @Override
    public void start(ChunkPos spawnPos) {
        if (!this.running) {
            return;
        }
        this.progressLogger.start(spawnPos);
        this.spawnPos = spawnPos;
    }

    @Override
    public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
        if (!this.running) {
            return;
        }
        this.progressLogger.setChunkStatus(pos, status);
        if (status == null) {
            this.chunkStatuses.remove(pos.toLong());
        } else {
            this.chunkStatuses.put(pos.toLong(), status);
        }
    }

    @Override
    public void start() {
        this.running = true;
        this.chunkStatuses.clear();
        this.progressLogger.start();
    }

    @Override
    public void stop() {
        this.running = false;
        this.progressLogger.stop();
    }

    public int getCenterSize() {
        return this.centerSize;
    }

    public int getSize() {
        return this.size;
    }

    public int getProgressPercentage() {
        return this.progressLogger.getProgressPercentage();
    }

    @Nullable
    public ChunkStatus getChunkStatus(int x, int z) {
        return this.chunkStatuses.get(ChunkPos.toLong(x + this.spawnPos.x - this.radius, z + this.spawnPos.z - this.radius));
    }
}

