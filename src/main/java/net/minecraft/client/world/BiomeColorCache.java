/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToIntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BiomeColorCache {
    private static final int MAX_ENTRY_SIZE = 256;
    private final ThreadLocal<Last> last = ThreadLocal.withInitial(Last::new);
    private final Long2ObjectLinkedOpenHashMap<Colors> colors = new Long2ObjectLinkedOpenHashMap(256, 0.25f);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ToIntFunction<BlockPos> colorFactory;

    public BiomeColorCache(ToIntFunction<BlockPos> colorFactory) {
        this.colorFactory = colorFactory;
    }

    public int getBiomeColor(BlockPos pos) {
        int o;
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        int j = ChunkSectionPos.getSectionCoord(pos.getZ());
        Last lv = this.last.get();
        if (lv.x != i || lv.z != j || lv.colors == null || lv.colors.needsCacheRefresh()) {
            lv.x = i;
            lv.z = j;
            lv.colors = this.getColorArray(i, j);
        }
        int[] is = lv.colors.get(pos.getY());
        int k = pos.getX() & 0xF;
        int l = pos.getZ() & 0xF;
        int m = l << 4 | k;
        int n = is[m];
        if (n != -1) {
            return n;
        }
        is[m] = o = this.colorFactory.applyAsInt(pos);
        return o;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void reset(int chunkX, int chunkZ) {
        try {
            this.lock.writeLock().lock();
            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    long m = ChunkPos.toLong(chunkX + k, chunkZ + l);
                    Colors lv = this.colors.remove(m);
                    if (lv == null) continue;
                    lv.setNeedsCacheRefresh();
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void reset() {
        try {
            this.lock.writeLock().lock();
            this.colors.values().forEach(Colors::setNeedsCacheRefresh);
            this.colors.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Colors getColorArray(int chunkX, int chunkZ) {
        Colors lv;
        long l = ChunkPos.toLong(chunkX, chunkZ);
        this.lock.readLock().lock();
        try {
            lv = this.colors.get(l);
            if (lv != null) {
                Colors colors = lv;
                return colors;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        this.lock.writeLock().lock();
        try {
            Colors lv3;
            lv = this.colors.get(l);
            if (lv != null) {
                Colors colors = lv;
                return colors;
            }
            Colors lv2 = new Colors();
            if (this.colors.size() >= 256 && (lv3 = this.colors.removeFirst()) != null) {
                lv3.setNeedsCacheRefresh();
            }
            this.colors.put(l, lv2);
            Colors colors = lv2;
            return colors;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Last {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        @Nullable
        Colors colors;

        private Last() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Colors {
        private final Int2ObjectArrayMap<int[]> colors = new Int2ObjectArrayMap(16);
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private static final int XZ_COLORS_SIZE = MathHelper.square(16);
        private volatile boolean needsCacheRefresh;

        Colors() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public int[] get(int y2) {
            this.lock.readLock().lock();
            try {
                int[] is = this.colors.get(y2);
                if (is != null) {
                    int[] nArray = is;
                    return nArray;
                }
            } finally {
                this.lock.readLock().unlock();
            }
            this.lock.writeLock().lock();
            try {
                int[] nArray = this.colors.computeIfAbsent(y2, y -> this.createDefault());
                return nArray;
            } finally {
                this.lock.writeLock().unlock();
            }
        }

        private int[] createDefault() {
            int[] is = new int[XZ_COLORS_SIZE];
            Arrays.fill(is, -1);
            return is;
        }

        public boolean needsCacheRefresh() {
            return this.needsCacheRefresh;
        }

        public void setNeedsCacheRefresh() {
            this.needsCacheRefresh = true;
        }
    }
}

