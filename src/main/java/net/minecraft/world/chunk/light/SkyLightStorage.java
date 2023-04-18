package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;

public class SkyLightStorage extends LightStorage {
   private static final Direction[] LIGHT_REDUCTION_DIRECTIONS;
   private final LongSet field_15820 = new LongOpenHashSet();
   private final LongSet sectionsToUpdate = new LongOpenHashSet();
   private final LongSet sectionsToRemove = new LongOpenHashSet();
   private final LongSet enabledColumns = new LongOpenHashSet();
   private volatile boolean hasUpdates;

   protected SkyLightStorage(ChunkProvider chunkProvider) {
      super(LightType.SKY, chunkProvider, new Data(new Long2ObjectOpenHashMap(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
   }

   protected int getLight(long blockPos) {
      return this.getLight(blockPos, false);
   }

   protected int getLight(long blockPos, boolean cached) {
      long m = ChunkSectionPos.fromBlockPos(blockPos);
      int i = ChunkSectionPos.unpackY(m);
      Data lv = cached ? (Data)this.storage : (Data)this.uncachedStorage;
      int j = lv.columnToTopSection.get(ChunkSectionPos.withZeroY(m));
      if (j != lv.minSectionY && i < j) {
         ChunkNibbleArray lv2 = this.getLightSection(lv, m);
         if (lv2 == null) {
            for(blockPos = BlockPos.removeChunkSectionLocalY(blockPos); lv2 == null; lv2 = this.getLightSection(lv, m)) {
               ++i;
               if (i >= j) {
                  return 15;
               }

               m = ChunkSectionPos.offset(m, Direction.UP);
            }
         }

         return lv2.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
      } else {
         return cached && !this.isSectionEnabled(m) ? 0 : 15;
      }
   }

   protected void onLoadSection(long sectionPos) {
      int i = ChunkSectionPos.unpackY(sectionPos);
      if (((Data)this.storage).minSectionY > i) {
         ((Data)this.storage).minSectionY = i;
         ((Data)this.storage).columnToTopSection.defaultReturnValue(((Data)this.storage).minSectionY);
      }

      long m = ChunkSectionPos.withZeroY(sectionPos);
      int j = ((Data)this.storage).columnToTopSection.get(m);
      if (j < i + 1) {
         ((Data)this.storage).columnToTopSection.put(m, i + 1);
         if (this.enabledColumns.contains(m)) {
            this.enqueueAddSection(sectionPos);
            if (j > ((Data)this.storage).minSectionY) {
               long n = ChunkSectionPos.asLong(ChunkSectionPos.unpackX(sectionPos), j - 1, ChunkSectionPos.unpackZ(sectionPos));
               this.enqueueRemoveSection(n);
            }

            this.checkForUpdates();
         }
      }

   }

   private void enqueueRemoveSection(long sectionPos) {
      this.sectionsToRemove.add(sectionPos);
      this.sectionsToUpdate.remove(sectionPos);
   }

   private void enqueueAddSection(long sectionPos) {
      this.sectionsToUpdate.add(sectionPos);
      this.sectionsToRemove.remove(sectionPos);
   }

   private void checkForUpdates() {
      this.hasUpdates = !this.sectionsToUpdate.isEmpty() || !this.sectionsToRemove.isEmpty();
   }

   protected void onUnloadSection(long sectionPos) {
      long m = ChunkSectionPos.withZeroY(sectionPos);
      boolean bl = this.enabledColumns.contains(m);
      if (bl) {
         this.enqueueRemoveSection(sectionPos);
      }

      int i = ChunkSectionPos.unpackY(sectionPos);
      if (((Data)this.storage).columnToTopSection.get(m) == i + 1) {
         long n;
         for(n = sectionPos; !this.hasSection(n) && this.isAboveMinHeight(i); n = ChunkSectionPos.offset(n, Direction.DOWN)) {
            --i;
         }

         if (this.hasSection(n)) {
            ((Data)this.storage).columnToTopSection.put(m, i + 1);
            if (bl) {
               this.enqueueAddSection(n);
            }
         } else {
            ((Data)this.storage).columnToTopSection.remove(m);
         }
      }

      if (bl) {
         this.checkForUpdates();
      }

   }

   protected void setColumnEnabled(long columnPos, boolean enabled) {
      this.updateAll();
      if (enabled && this.enabledColumns.add(columnPos)) {
         int i = ((Data)this.storage).columnToTopSection.get(columnPos);
         if (i != ((Data)this.storage).minSectionY) {
            long m = ChunkSectionPos.asLong(ChunkSectionPos.unpackX(columnPos), i - 1, ChunkSectionPos.unpackZ(columnPos));
            this.enqueueAddSection(m);
            this.checkForUpdates();
         }
      } else if (!enabled) {
         this.enabledColumns.remove(columnPos);
      }

   }

   protected boolean hasLightUpdates() {
      return super.hasLightUpdates() || this.hasUpdates;
   }

   protected ChunkNibbleArray createSection(long sectionPos) {
      ChunkNibbleArray lv = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
      if (lv != null) {
         return lv;
      } else {
         long m = ChunkSectionPos.offset(sectionPos, Direction.UP);
         int i = ((Data)this.storage).columnToTopSection.get(ChunkSectionPos.withZeroY(sectionPos));
         if (i != ((Data)this.storage).minSectionY && ChunkSectionPos.unpackY(m) < i) {
            ChunkNibbleArray lv2;
            while((lv2 = this.getLightSection(m, true)) == null) {
               m = ChunkSectionPos.offset(m, Direction.UP);
            }

            return copy(lv2);
         } else {
            return new ChunkNibbleArray();
         }
      }
   }

   private static ChunkNibbleArray copy(ChunkNibbleArray source) {
      if (source.isUninitialized()) {
         return new ChunkNibbleArray();
      } else {
         byte[] bs = source.asByteArray();
         byte[] cs = new byte[2048];

         for(int i = 0; i < 16; ++i) {
            System.arraycopy(bs, 0, cs, i * 128, 128);
         }

         return new ChunkNibbleArray(cs);
      }
   }

   protected void updateLight(ChunkLightProvider lightProvider, boolean doSkylight, boolean skipEdgeLightPropagation) {
      super.updateLight(lightProvider, doSkylight, skipEdgeLightPropagation);
      if (doSkylight) {
         LongIterator var4;
         long l;
         int i;
         int j;
         if (!this.sectionsToUpdate.isEmpty()) {
            var4 = this.sectionsToUpdate.iterator();

            label160:
            while(true) {
               while(true) {
                  do {
                     do {
                        do {
                           if (!var4.hasNext()) {
                              break label160;
                           }

                           l = (Long)var4.next();
                           i = this.getLevel(l);
                        } while(i == 2);
                     } while(this.sectionsToRemove.contains(l));
                  } while(!this.field_15820.add(l));

                  int k;
                  if (i == 1) {
                     this.removeSection(lightProvider, l);
                     if (this.dirtySections.add(l)) {
                        ((Data)this.storage).replaceWithCopy(l);
                     }

                     Arrays.fill(this.getLightSection(l, true).asByteArray(), (byte)-1);
                     j = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackX(l));
                     k = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l));
                     int m = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackZ(l));
                     Direction[] var11 = LIGHT_REDUCTION_DIRECTIONS;
                     int t = var11.length;

                     long n;
                     for(int var13 = 0; var13 < t; ++var13) {
                        Direction lv = var11[var13];
                        n = ChunkSectionPos.offset(l, lv);
                        if ((this.sectionsToRemove.contains(n) || !this.field_15820.contains(n) && !this.sectionsToUpdate.contains(n)) && this.hasSection(n)) {
                           for(int o = 0; o < 16; ++o) {
                              for(int p = 0; p < 16; ++p) {
                                 long q;
                                 long r;
                                 switch (lv) {
                                    case NORTH:
                                       q = BlockPos.asLong(j + o, k + p, m);
                                       r = BlockPos.asLong(j + o, k + p, m - 1);
                                       break;
                                    case SOUTH:
                                       q = BlockPos.asLong(j + o, k + p, m + 16 - 1);
                                       r = BlockPos.asLong(j + o, k + p, m + 16);
                                       break;
                                    case WEST:
                                       q = BlockPos.asLong(j, k + o, m + p);
                                       r = BlockPos.asLong(j - 1, k + o, m + p);
                                       break;
                                    default:
                                       q = BlockPos.asLong(j + 16 - 1, k + o, m + p);
                                       r = BlockPos.asLong(j + 16, k + o, m + p);
                                 }

                                 lightProvider.updateLevel(q, r, lightProvider.getPropagatedLevel(q, r, 0), true);
                              }
                           }
                        }
                     }

                     for(int s = 0; s < 16; ++s) {
                        for(t = 0; t < 16; ++t) {
                           long u = BlockPos.asLong(ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(l), s), ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l)), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(l), t));
                           n = BlockPos.asLong(ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(l), s), ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(l)) - 1, ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(l), t));
                           lightProvider.updateLevel(u, n, lightProvider.getPropagatedLevel(u, n, 0), true);
                        }
                     }
                  } else {
                     for(j = 0; j < 16; ++j) {
                        for(k = 0; k < 16; ++k) {
                           long v = BlockPos.asLong(ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(l), j), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackY(l), 15), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(l), k));
                           lightProvider.updateLevel(Long.MAX_VALUE, v, 0, true);
                        }
                     }
                  }
               }
            }
         }

         this.sectionsToUpdate.clear();
         if (!this.sectionsToRemove.isEmpty()) {
            var4 = this.sectionsToRemove.iterator();

            label90:
            while(true) {
               do {
                  do {
                     if (!var4.hasNext()) {
                        break label90;
                     }

                     l = (Long)var4.next();
                  } while(!this.field_15820.remove(l));
               } while(!this.hasSection(l));

               for(i = 0; i < 16; ++i) {
                  for(j = 0; j < 16; ++j) {
                     long w = BlockPos.asLong(ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackX(l), i), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackY(l), 15), ChunkSectionPos.getOffsetPos(ChunkSectionPos.unpackZ(l), j));
                     lightProvider.updateLevel(Long.MAX_VALUE, w, 15, false);
                  }
               }
            }
         }

         this.sectionsToRemove.clear();
         this.hasUpdates = false;
      }
   }

   protected boolean isAboveMinHeight(int sectionY) {
      return sectionY >= ((Data)this.storage).minSectionY;
   }

   protected boolean isAtOrAboveTopmostSection(long sectionPos) {
      long m = ChunkSectionPos.withZeroY(sectionPos);
      int i = ((Data)this.storage).columnToTopSection.get(m);
      return i == ((Data)this.storage).minSectionY || ChunkSectionPos.unpackY(sectionPos) >= i;
   }

   protected boolean isSectionEnabled(long sectionPos) {
      long m = ChunkSectionPos.withZeroY(sectionPos);
      return this.enabledColumns.contains(m);
   }

   static {
      LIGHT_REDUCTION_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   }

   protected static final class Data extends ChunkToNibbleArrayMap {
      int minSectionY;
      final Long2IntOpenHashMap columnToTopSection;

      public Data(Long2ObjectOpenHashMap arrays, Long2IntOpenHashMap columnToTopSection, int minSectionY) {
         super(arrays);
         this.columnToTopSection = columnToTopSection;
         columnToTopSection.defaultReturnValue(minSectionY);
         this.minSectionY = minSectionY;
      }

      public Data copy() {
         return new Data(this.arrays.clone(), this.columnToTopSection.clone(), this.minSectionY);
      }

      // $FF: synthetic method
      public ChunkToNibbleArrayMap copy() {
         return this.copy();
      }
   }
}
