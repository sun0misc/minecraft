package net.minecraft.world.chunk.light;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;

public final class ChunkBlockLightProvider extends ChunkLightProvider {
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BlockPos.Mutable mutablePos;

   public ChunkBlockLightProvider(ChunkProvider chunkProvider) {
      this(chunkProvider, new BlockLightStorage(chunkProvider));
   }

   @VisibleForTesting
   public ChunkBlockLightProvider(ChunkProvider chunkProvider, BlockLightStorage blockLightStorage) {
      super(chunkProvider, blockLightStorage);
      this.mutablePos = new BlockPos.Mutable();
   }

   private int getLightSourceLuminance(long blockPos) {
      return this.getStateForLighting(this.mutablePos.set(blockPos)).getLuminance();
   }

   protected int getPropagatedLevel(long sourceId, long targetId, int level) {
      if (this.isMarker(targetId)) {
         return 15;
      } else if (this.isMarker(sourceId)) {
         return level + 15 - this.getLightSourceLuminance(targetId);
      } else if (level >= 14) {
         return 15;
      } else {
         this.mutablePos.set(targetId);
         BlockState lv = this.getStateForLighting(this.mutablePos);
         int j = this.getOpacity(lv, this.mutablePos);
         if (j >= 15) {
            return 15;
         } else {
            Direction lv2 = getDirection(sourceId, targetId);
            if (lv2 == null) {
               return 15;
            } else {
               this.mutablePos.set(sourceId);
               BlockState lv3 = this.getStateForLighting(this.mutablePos);
               return this.shapesCoverFullCube(sourceId, lv3, targetId, lv, lv2) ? 15 : level + Math.max(1, j);
            }
         }
      }
   }

   protected void propagateLevel(long id, int level, boolean decrease) {
      if (!decrease || level < this.levelCount - 2) {
         long m = ChunkSectionPos.fromBlockPos(id);
         Direction[] var7 = DIRECTIONS;
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction lv = var7[var9];
            long n = BlockPos.offset(id, lv);
            long o = ChunkSectionPos.fromBlockPos(n);
            if (m == o || ((BlockLightStorage)this.lightStorage).hasSection(o)) {
               this.propagateLevel(id, n, level, decrease);
            }
         }

      }
   }

   protected int recalculateLevel(long id, long excludedId, int maxLevel) {
      int j = maxLevel;
      if (!this.isMarker(excludedId)) {
         int k = this.getPropagatedLevel(Long.MAX_VALUE, id, 0);
         if (maxLevel > k) {
            j = k;
         }

         if (j == 0) {
            return j;
         }
      }

      long n = ChunkSectionPos.fromBlockPos(id);
      ChunkNibbleArray lv = ((BlockLightStorage)this.lightStorage).getLightSection(n, true);
      Direction[] var10 = DIRECTIONS;
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction lv2 = var10[var12];
         long o = BlockPos.offset(id, lv2);
         if (o != excludedId) {
            long p = ChunkSectionPos.fromBlockPos(o);
            ChunkNibbleArray lv3;
            if (n == p) {
               lv3 = lv;
            } else {
               lv3 = ((BlockLightStorage)this.lightStorage).getLightSection(p, true);
            }

            if (lv3 != null) {
               int q = this.getCurrentLevelFromSection(lv3, o);
               if (q + 1 < j) {
                  int r = this.getPropagatedLevel(o, id, q);
                  if (j > r) {
                     j = r;
                  }

                  if (j == 0) {
                     return j;
                  }
               }
            }
         }
      }

      return j;
   }

   public void addLightSource(BlockPos pos, int level) {
      ((BlockLightStorage)this.lightStorage).updateAll();
      this.updateLevel(Long.MAX_VALUE, pos.asLong(), 15 - level, true);
   }
}
