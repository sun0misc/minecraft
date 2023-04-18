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

@Environment(EnvType.CLIENT)
public class BiomeColorCache {
   private static final int MAX_ENTRY_SIZE = 256;
   private final ThreadLocal last = ThreadLocal.withInitial(Last::new);
   private final Long2ObjectLinkedOpenHashMap colors = new Long2ObjectLinkedOpenHashMap(256, 0.25F);
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private final ToIntFunction colorFactory;

   public BiomeColorCache(ToIntFunction colorFactory) {
      this.colorFactory = colorFactory;
   }

   public int getBiomeColor(BlockPos pos) {
      int i = ChunkSectionPos.getSectionCoord(pos.getX());
      int j = ChunkSectionPos.getSectionCoord(pos.getZ());
      Last lv = (Last)this.last.get();
      if (lv.x != i || lv.z != j || lv.colors == null || lv.colors.needsCacheRefresh()) {
         lv.x = i;
         lv.z = j;
         lv.colors = this.getColorArray(i, j);
      }

      int[] is = lv.colors.get(pos.getY());
      int k = pos.getX() & 15;
      int l = pos.getZ() & 15;
      int m = l << 4 | k;
      int n = is[m];
      if (n != -1) {
         return n;
      } else {
         int o = this.colorFactory.applyAsInt(pos);
         is[m] = o;
         return o;
      }
   }

   public void reset(int chunkX, int chunkZ) {
      try {
         this.lock.writeLock().lock();

         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               long m = ChunkPos.toLong(chunkX + k, chunkZ + l);
               Colors lv = (Colors)this.colors.remove(m);
               if (lv != null) {
                  lv.setNeedsCacheRefresh();
               }
            }
         }
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   public void reset() {
      try {
         this.lock.writeLock().lock();
         this.colors.clear();
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   private Colors getColorArray(int chunkX, int chunkZ) {
      long l = ChunkPos.toLong(chunkX, chunkZ);
      this.lock.readLock().lock();

      Colors lv;
      Colors lv2;
      label126: {
         try {
            lv = (Colors)this.colors.get(l);
            if (lv == null) {
               break label126;
            }

            lv2 = lv;
         } finally {
            this.lock.readLock().unlock();
         }

         return lv2;
      }

      this.lock.writeLock().lock();

      Colors var7;
      try {
         lv = (Colors)this.colors.get(l);
         if (lv != null) {
            lv2 = lv;
            return lv2;
         }

         lv2 = new Colors();
         if (this.colors.size() >= 256) {
            this.colors.removeFirst();
         }

         this.colors.put(l, lv2);
         var7 = lv2;
      } finally {
         this.lock.writeLock().unlock();
      }

      return var7;
   }

   @Environment(EnvType.CLIENT)
   static class Last {
      public int x = Integer.MIN_VALUE;
      public int z = Integer.MIN_VALUE;
      @Nullable
      Colors colors;

      private Last() {
      }
   }

   @Environment(EnvType.CLIENT)
   static class Colors {
      private final Int2ObjectArrayMap colors = new Int2ObjectArrayMap(16);
      private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
      private static final int XZ_COLORS_SIZE = MathHelper.square(16);
      private volatile boolean needsCacheRefresh;

      public int[] get(int y) {
         this.lock.readLock().lock();

         int[] is;
         try {
            is = (int[])this.colors.get(y);
            if (is != null) {
               int[] var3 = is;
               return var3;
            }
         } finally {
            this.lock.readLock().unlock();
         }

         this.lock.writeLock().lock();

         try {
            is = (int[])this.colors.computeIfAbsent(y, (yx) -> {
               return this.createDefault();
            });
         } finally {
            this.lock.writeLock().unlock();
         }

         return is;
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
