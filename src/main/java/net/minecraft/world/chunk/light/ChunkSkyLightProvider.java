package net.minecraft.world.chunk.light;

import java.util.Locale;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;

public final class ChunkSkyLightProvider extends ChunkLightProvider {
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final Direction[] HORIZONTAL_DIRECTIONS;

   public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
      super(chunkProvider, new SkyLightStorage(chunkProvider));
   }

   protected int getPropagatedLevel(long sourceId, long targetId, int level) {
      if (!this.isMarker(targetId) && !this.isMarker(sourceId)) {
         if (level >= 14) {
            return 15;
         } else {
            this.reusableBlockPos.set(targetId);
            BlockState lv = this.getStateForLighting(this.reusableBlockPos);
            int j = this.getOpacity(lv, this.reusableBlockPos);
            if (j >= 15) {
               return 15;
            } else {
               Direction lv2 = getDirection(sourceId, targetId);
               if (lv2 == null) {
                  throw new IllegalStateException(String.format(Locale.ROOT, "Light was spread in illegal direction. From %d, %d, %d to %d, %d, %d", BlockPos.unpackLongX(sourceId), BlockPos.unpackLongY(sourceId), BlockPos.unpackLongZ(sourceId), BlockPos.unpackLongX(targetId), BlockPos.unpackLongY(targetId), BlockPos.unpackLongZ(targetId)));
               } else {
                  this.reusableBlockPos.set(sourceId);
                  BlockState lv3 = this.getStateForLighting(this.reusableBlockPos);
                  if (this.shapesCoverFullCube(sourceId, lv3, targetId, lv, lv2)) {
                     return 15;
                  } else {
                     boolean bl = lv2 == Direction.DOWN;
                     return bl && level == 0 && j == 0 ? 0 : level + Math.max(1, j);
                  }
               }
            }
         }
      } else {
         return 15;
      }
   }

   protected void propagateLevel(long id, int level, boolean decrease) {
      if (!decrease || level < this.levelCount - 2) {
         long m = ChunkSectionPos.fromBlockPos(id);
         int j = BlockPos.unpackLongY(id);
         int k = ChunkSectionPos.getLocalCoord(j);
         int n = ChunkSectionPos.getSectionCoord(j);
         int o;
         if (k != 0) {
            o = 0;
         } else {
            int p;
            for(p = 0; !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.offset(m, 0, -p - 1, 0)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(n - p - 1); ++p) {
            }

            o = p;
         }

         long q = BlockPos.add(id, 0, -1 - o * 16, 0);
         long r = ChunkSectionPos.fromBlockPos(q);
         if (m == r || ((SkyLightStorage)this.lightStorage).hasSection(r)) {
            this.propagateLevel(id, q, level, decrease);
         }

         long s = BlockPos.offset(id, Direction.UP);
         long t = ChunkSectionPos.fromBlockPos(s);
         if (m == t || ((SkyLightStorage)this.lightStorage).hasSection(t)) {
            this.propagateLevel(id, s, level, decrease);
         }

         Direction[] var19 = HORIZONTAL_DIRECTIONS;
         int var20 = var19.length;

         for(int var21 = 0; var21 < var20; ++var21) {
            Direction lv = var19[var21];
            int u = 0;

            while(true) {
               long v = BlockPos.add(id, lv.getOffsetX(), -u, lv.getOffsetZ());
               long w = ChunkSectionPos.fromBlockPos(v);
               if (m == w) {
                  this.propagateLevel(id, v, level, decrease);
                  break;
               }

               if (((SkyLightStorage)this.lightStorage).hasSection(w)) {
                  long x = BlockPos.add(id, 0, -u, 0);
                  this.propagateLevel(x, v, level, decrease);
               }

               ++u;
               if (u > o * 16) {
                  break;
               }
            }
         }

      }
   }

   protected int recalculateLevel(long id, long excludedId, int maxLevel) {
      int j = maxLevel;
      long n = ChunkSectionPos.fromBlockPos(id);
      ChunkNibbleArray lv = ((SkyLightStorage)this.lightStorage).getLightSection(n, true);
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
               lv3 = ((SkyLightStorage)this.lightStorage).getLightSection(p, true);
            }

            int k;
            if (lv3 != null) {
               k = this.getCurrentLevelFromSection(lv3, o);
            } else {
               if (lv2 == Direction.DOWN) {
                  continue;
               }

               k = 15 - ((SkyLightStorage)this.lightStorage).getLight(o, true);
            }

            if (k + 1 < j || k == 0 && lv2 == Direction.UP) {
               int q = this.getPropagatedLevel(o, id, k);
               if (j > q) {
                  j = q;
               }

               if (j == 0) {
                  return j;
               }
            }
         }
      }

      return j;
   }

   protected void resetLevel(long id) {
      ((SkyLightStorage)this.lightStorage).updateAll();
      long m = ChunkSectionPos.fromBlockPos(id);
      if (((SkyLightStorage)this.lightStorage).hasSection(m)) {
         super.resetLevel(id);
      } else {
         for(id = BlockPos.removeChunkSectionLocalY(id); !((SkyLightStorage)this.lightStorage).hasSection(m) && !((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(m); id = BlockPos.add(id, 0, 16, 0)) {
            m = ChunkSectionPos.offset(m, Direction.UP);
         }

         if (((SkyLightStorage)this.lightStorage).hasSection(m)) {
            super.resetLevel(id);
         }
      }

   }

   public String displaySectionLevel(long sectionPos) {
      String var10000 = super.displaySectionLevel(sectionPos);
      return var10000 + (((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(sectionPos) ? "*" : "");
   }

   static {
      HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   }
}
