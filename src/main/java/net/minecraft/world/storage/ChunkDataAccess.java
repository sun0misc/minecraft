/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.storage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.ChunkDataList;

public interface ChunkDataAccess<T>
extends AutoCloseable {
    public CompletableFuture<ChunkDataList<T>> readChunkData(ChunkPos var1);

    public void writeChunkData(ChunkDataList<T> var1);

    public void awaitAll(boolean var1);

    @Override
    default public void close() throws IOException {
    }
}

