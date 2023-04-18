package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.SectionDistanceLevelPropagator;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import org.jetbrains.annotations.Nullable;

public abstract class LightStorage extends SectionDistanceLevelPropagator {
   protected static final int field_31710 = 0;
   protected static final int field_31711 = 1;
   protected static final int field_31712 = 2;
   protected static final ChunkNibbleArray EMPTY = new ChunkNibbleArray();
   private static final Direction[] DIRECTIONS = Direction.values();
   private final LightType lightType;
   private final ChunkProvider chunkProvider;
   protected final LongSet readySections = new LongOpenHashSet();
   protected final LongSet markedNotReadySections = new LongOpenHashSet();
   protected final LongSet markedReadySections = new LongOpenHashSet();
   protected volatile ChunkToNibbleArrayMap uncachedStorage;
   protected final ChunkToNibbleArrayMap storage;
   protected final LongSet dirtySections = new LongOpenHashSet();
   protected final LongSet notifySections = new LongOpenHashSet();
   protected final Long2ObjectMap queuedSections = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
   private final LongSet queuedEdgeSections = new LongOpenHashSet();
   private final LongSet columnsToRetain = new LongOpenHashSet();
   private final LongSet sectionsToRemove = new LongOpenHashSet();
   protected volatile boolean hasLightUpdates;

   protected LightStorage(LightType lightType, ChunkProvider chunkProvider, ChunkToNibbleArrayMap lightData) {
      super(3, 16, 256);
      this.lightType = lightType;
      this.chunkProvider = chunkProvider;
      this.storage = lightData;
      this.uncachedStorage = lightData.copy();
      this.uncachedStorage.disableCache();
   }

   protected boolean hasSection(long sectionPos) {
      return this.getLightSection(sectionPos, true) != null;
   }

   @Nullable
   protected ChunkNibbleArray getLightSection(long sectionPos, boolean cached) {
      return this.getLightSection(cached ? this.storage : this.uncachedStorage, sectionPos);
   }

   @Nullable
   protected ChunkNibbleArray getLightSection(ChunkToNibbleArrayMap storage, long sectionPos) {
      return storage.get(sectionPos);
   }

   @Nullable
   public ChunkNibbleArray getLightSection(long sectionPos) {
      ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      return lv != null ? lv : this.getLightSection(sectionPos, false);
   }

   protected abstract int getLight(long blockPos);

   protected int get(long blockPos) {
      long m = ChunkSectionPos.fromBlockPos(blockPos);
      ChunkNibbleArray lv = this.getLightSection(m, true);
      return lv.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
   }

   protected void set(long blockPos, int value) {
      long m = ChunkSectionPos.fromBlockPos(blockPos);
      if (this.dirtySections.add(m)) {
         this.storage.replaceWithCopy(m);
      }

      ChunkNibbleArray lv = this.getLightSection(m, true);
      lv.set(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)), value);
      LongSet var10001 = this.notifySections;
      Objects.requireNonNull(var10001);
      ChunkSectionPos.forEachChunkSectionAround(blockPos, var10001::add);
   }

   protected int getLevel(long id) {
      if (this.isMarker(id)) {
         return 2;
      } else if (this.readySections.contains(id)) {
         return 0;
      } else {
         return !this.sectionsToRemove.contains(id) && this.storage.containsKey(id) ? 1 : 2;
      }
   }

   protected int getInitialLevel(long id) {
      if (this.markedNotReadySections.contains(id)) {
         return 2;
      } else {
         return !this.readySections.contains(id) && !this.markedReadySections.contains(id) ? 2 : 0;
      }
   }

   protected void setLevel(long id, int level) {
      int j = this.getLevel(id);
      if (j != 0 && level == 0) {
         this.readySections.add(id);
         this.markedReadySections.remove(id);
      }

      if (j == 0 && level != 0) {
         this.readySections.remove(id);
         this.markedNotReadySections.remove(id);
      }

      if (j >= 2 && level != 2) {
         if (this.sectionsToRemove.contains(id)) {
            this.sectionsToRemove.remove(id);
         } else {
            this.storage.put(id, this.createSection(id));
            this.dirtySections.add(id);
            this.onLoadSection(id);
            int k = ChunkSectionPos.unpackX(id);
            int m = ChunkSectionPos.unpackY(id);
            int n = ChunkSectionPos.unpackZ(id);

            for(int o = -1; o <= 1; ++o) {
               for(int p = -1; p <= 1; ++p) {
                  for(int q = -1; q <= 1; ++q) {
                     this.notifySections.add(ChunkSectionPos.asLong(k + p, m + q, n + o));
                  }
               }
            }
         }
      }

      if (j != 2 && level >= 2) {
         this.sectionsToRemove.add(id);
      }

      this.hasLightUpdates = !this.sectionsToRemove.isEmpty();
   }

   protected ChunkNibbleArray createSection(long sectionPos) {
      ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      return lv != null ? lv : new ChunkNibbleArray();
   }

   protected void removeSection(ChunkLightProvider storage, long sectionPos) {
      if (storage.getPendingUpdateCount() != 0) {
         if (storage.getPendingUpdateCount() < 8192) {
            storage.removePendingUpdateIf((mx) -> {
               return ChunkSectionPos.fromBlockPos(mx) == sectionPos;
            });
         } else {
            int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
            int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
            int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));

            for(int m = 0; m < 16; ++m) {
               for(int n = 0; n < 16; ++n) {
                  for(int o = 0; o < 16; ++o) {
                     long p = BlockPos.asLong(i + m, j + n, k + o);
                     storage.removePendingUpdate(p);
                  }
               }
            }

         }
      }
   }

   protected boolean hasLightUpdates() {
      return this.hasLightUpdates;
   }

   protected void updateLight(ChunkLightProvider lightProvider, boolean doSkylight, boolean skipEdgeLightPropagation) {
      if (this.hasLightUpdates() || !this.queuedSections.isEmpty()) {
         LongIterator var4 = this.sectionsToRemove.iterator();

         long l;
         ChunkNibbleArray lv2;
         while(var4.hasNext()) {
            l = (Long)var4.next();
            this.removeSection(lightProvider, l);
            ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.remove(l);
            lv2 = this.storage.removeChunk(l);
            if (this.columnsToRetain.contains(ChunkSectionPos.withZeroY(l))) {
               if (lv != null) {
                  this.queuedSections.put(l, lv);
               } else if (lv2 != null) {
                  this.queuedSections.put(l, lv2);
               }
            }
         }

         this.storage.clearCache();
         var4 = this.sectionsToRemove.iterator();

         while(var4.hasNext()) {
            l = (Long)var4.next();
            this.onUnloadSection(l);
         }

         this.sectionsToRemove.clear();
         this.hasLightUpdates = false;
         ObjectIterator objectIterator = this.queuedSections.long2ObjectEntrySet().iterator();

         long m;
         Long2ObjectMap.Entry entry;
         while(objectIterator.hasNext()) {
            entry = (Long2ObjectMap.Entry)objectIterator.next();
            m = entry.getLongKey();
            if (this.hasSection(m)) {
               lv2 = (ChunkNibbleArray)entry.getValue();
               if (this.storage.get(m) != lv2) {
                  this.removeSection(lightProvider, m);
                  this.storage.put(m, lv2);
                  this.dirtySections.add(m);
               }
            }
         }

         this.storage.clearCache();
         if (!skipEdgeLightPropagation) {
            var4 = this.queuedSections.keySet().iterator();

            while(var4.hasNext()) {
               l = (Long)var4.next();
               this.updateSection(lightProvider, l);
            }
         } else {
            var4 = this.queuedEdgeSections.iterator();

            while(var4.hasNext()) {
               l = (Long)var4.next();
               this.updateSection(lightProvider, l);
            }
         }

         this.queuedEdgeSections.clear();
         objectIterator = this.queuedSections.long2ObjectEntrySet().iterator();

         while(objectIterator.hasNext()) {
            entry = (Long2ObjectMap.Entry)objectIterator.next();
            m = entry.getLongKey();
            if (this.hasSection(m)) {
               objectIterator.remove();
            }
         }

      }
   }

   private void updateSection(ChunkLightProvider lightProvider, long sectionPos) {
      if (this.hasSection(sectionPos)) {
         int i = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(sectionPos));
         int j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(sectionPos));
         int k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(sectionPos));
         Direction[] var7 = DIRECTIONS;
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction lv = var7[var9];
            long m = ChunkSectionPos.offset(sectionPos, lv);
            if (!this.queuedSections.containsKey(m) && this.hasSection(m)) {
               for(int n = 0; n < 16; ++n) {
                  for(int o = 0; o < 16; ++o) {
                     long p;
                     long q;
                     switch (lv) {
                        case DOWN:
                           p = BlockPos.asLong(i + o, j, k + n);
                           q = BlockPos.asLong(i + o, j - 1, k + n);
                           break;
                        case UP:
                           p = BlockPos.asLong(i + o, j + 16 - 1, k + n);
                           q = BlockPos.asLong(i + o, j + 16, k + n);
                           break;
                        case NORTH:
                           p = BlockPos.asLong(i + n, j + o, k);
                           q = BlockPos.asLong(i + n, j + o, k - 1);
                           break;
                        case SOUTH:
                           p = BlockPos.asLong(i + n, j + o, k + 16 - 1);
                           q = BlockPos.asLong(i + n, j + o, k + 16);
                           break;
                        case WEST:
                           p = BlockPos.asLong(i, j + n, k + o);
                           q = BlockPos.asLong(i - 1, j + n, k + o);
                           break;
                        default:
                           p = BlockPos.asLong(i + 16 - 1, j + n, k + o);
                           q = BlockPos.asLong(i + 16, j + n, k + o);
                     }

                     lightProvider.updateLevel(p, q, lightProvider.getPropagatedLevel(p, q, lightProvider.getLevel(p)), false);
                     lightProvider.updateLevel(q, p, lightProvider.getPropagatedLevel(q, p, lightProvider.getLevel(q)), false);
                  }
               }
            }
         }

      }
   }

   protected void onLoadSection(long sectionPos) {
   }

   protected void onUnloadSection(long sectionPos) {
   }

   protected void setColumnEnabled(long columnPos, boolean enabled) {
   }

   public void setRetainColumn(long sectionPos, boolean retain) {
      if (retain) {
         this.columnsToRetain.add(sectionPos);
      } else {
         this.columnsToRetain.remove(sectionPos);
      }

   }

   protected void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray array, boolean nonEdge) {
      if (array != null) {
         this.queuedSections.put(sectionPos, array);
         if (!nonEdge) {
            this.queuedEdgeSections.add(sectionPos);
         }
      } else {
         this.queuedSections.remove(sectionPos);
      }

   }

   protected void setSectionStatus(long sectionPos, boolean notReady) {
      boolean bl2 = this.readySections.contains(sectionPos);
      if (!bl2 && !notReady) {
         this.markedReadySections.add(sectionPos);
         this.updateLevel(Long.MAX_VALUE, sectionPos, 0, true);
      }

      if (bl2 && notReady) {
         this.markedNotReadySections.add(sectionPos);
         this.updateLevel(Long.MAX_VALUE, sectionPos, 2, false);
      }

   }

   protected void updateAll() {
      if (this.hasPendingUpdates()) {
         this.applyPendingUpdates(Integer.MAX_VALUE);
      }

   }

   protected void notifyChanges() {
      if (!this.dirtySections.isEmpty()) {
         ChunkToNibbleArrayMap lv = this.storage.copy();
         lv.disableCache();
         this.uncachedStorage = lv;
         this.dirtySections.clear();
      }

      if (!this.notifySections.isEmpty()) {
         LongIterator longIterator = this.notifySections.iterator();

         while(longIterator.hasNext()) {
            long l = longIterator.nextLong();
            this.chunkProvider.onLightUpdate(this.lightType, ChunkSectionPos.from(l));
         }

         this.notifySections.clear();
      }

   }
}
