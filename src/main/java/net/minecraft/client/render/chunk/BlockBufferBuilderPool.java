/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render.chunk;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BlockBufferBuilderPool {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<BlockBufferBuilderStorage> availableBuilders;
    private volatile int availableBuilderCount;

    private BlockBufferBuilderPool(List<BlockBufferBuilderStorage> availableBuilders) {
        this.availableBuilders = Queues.newArrayDeque(availableBuilders);
        this.availableBuilderCount = this.availableBuilders.size();
    }

    public static BlockBufferBuilderPool allocate(int max) {
        int j = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / BlockBufferBuilderStorage.EXPECTED_TOTAL_SIZE);
        int k = Math.max(1, Math.min(max, j));
        ArrayList<BlockBufferBuilderStorage> list = new ArrayList<BlockBufferBuilderStorage>(k);
        try {
            for (int l = 0; l < k; ++l) {
                list.add(new BlockBufferBuilderStorage());
            }
        } catch (OutOfMemoryError outOfMemoryError) {
            LOGGER.warn("Allocated only {}/{} buffers", (Object)list.size(), (Object)k);
            int m = Math.min(list.size() * 2 / 3, list.size() - 1);
            for (int n = 0; n < m; ++n) {
                ((BlockBufferBuilderStorage)list.remove(list.size() - 1)).close();
            }
        }
        return new BlockBufferBuilderPool(list);
    }

    @Nullable
    public BlockBufferBuilderStorage acquire() {
        BlockBufferBuilderStorage lv = this.availableBuilders.poll();
        if (lv != null) {
            this.availableBuilderCount = this.availableBuilders.size();
            return lv;
        }
        return null;
    }

    public void release(BlockBufferBuilderStorage builders) {
        this.availableBuilders.add(builders);
        this.availableBuilderCount = this.availableBuilders.size();
    }

    public boolean hasNoAvailableBuilder() {
        return this.availableBuilders.isEmpty();
    }

    public int getAvailableBuilderCount() {
        return this.availableBuilderCount;
    }
}

