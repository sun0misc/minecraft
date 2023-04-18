package net.minecraft.util.shape;

import java.util.BitSet;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Direction;

public final class BitSetVoxelSet extends VoxelSet {
   private final BitSet storage;
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;

   public BitSetVoxelSet(int i, int j, int k) {
      super(i, j, k);
      this.storage = new BitSet(i * j * k);
      this.minX = i;
      this.minY = j;
      this.minZ = k;
   }

   public static BitSetVoxelSet create(int sizeX, int sizeY, int sizeZ, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      BitSetVoxelSet lv = new BitSetVoxelSet(sizeX, sizeY, sizeZ);
      lv.minX = minX;
      lv.minY = minY;
      lv.minZ = minZ;
      lv.maxX = maxX;
      lv.maxY = maxY;
      lv.maxZ = maxZ;

      for(int r = minX; r < maxX; ++r) {
         for(int s = minY; s < maxY; ++s) {
            for(int t = minZ; t < maxZ; ++t) {
               lv.set(r, s, t, false);
            }
         }
      }

      return lv;
   }

   public BitSetVoxelSet(VoxelSet other) {
      super(other.sizeX, other.sizeY, other.sizeZ);
      if (other instanceof BitSetVoxelSet) {
         this.storage = (BitSet)((BitSetVoxelSet)other).storage.clone();
      } else {
         this.storage = new BitSet(this.sizeX * this.sizeY * this.sizeZ);

         for(int i = 0; i < this.sizeX; ++i) {
            for(int j = 0; j < this.sizeY; ++j) {
               for(int k = 0; k < this.sizeZ; ++k) {
                  if (other.contains(i, j, k)) {
                     this.storage.set(this.getIndex(i, j, k));
                  }
               }
            }
         }
      }

      this.minX = other.getMin(Direction.Axis.X);
      this.minY = other.getMin(Direction.Axis.Y);
      this.minZ = other.getMin(Direction.Axis.Z);
      this.maxX = other.getMax(Direction.Axis.X);
      this.maxY = other.getMax(Direction.Axis.Y);
      this.maxZ = other.getMax(Direction.Axis.Z);
   }

   protected int getIndex(int x, int y, int z) {
      return (x * this.sizeY + y) * this.sizeZ + z;
   }

   public boolean contains(int x, int y, int z) {
      return this.storage.get(this.getIndex(x, y, z));
   }

   private void set(int x, int y, int z, boolean updateBounds) {
      this.storage.set(this.getIndex(x, y, z));
      if (updateBounds) {
         this.minX = Math.min(this.minX, x);
         this.minY = Math.min(this.minY, y);
         this.minZ = Math.min(this.minZ, z);
         this.maxX = Math.max(this.maxX, x + 1);
         this.maxY = Math.max(this.maxY, y + 1);
         this.maxZ = Math.max(this.maxZ, z + 1);
      }

   }

   public void set(int x, int y, int z) {
      this.set(x, y, z, true);
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public int getMin(Direction.Axis axis) {
      return axis.choose(this.minX, this.minY, this.minZ);
   }

   public int getMax(Direction.Axis axis) {
      return axis.choose(this.maxX, this.maxY, this.maxZ);
   }

   static BitSetVoxelSet combine(VoxelSet first, VoxelSet second, PairList xPoints, PairList yPoints, PairList zPoints, BooleanBiFunction function) {
      BitSetVoxelSet lv = new BitSetVoxelSet(xPoints.size() - 1, yPoints.size() - 1, zPoints.size() - 1);
      int[] is = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
      xPoints.forEachPair((x1, x2, xIndex) -> {
         boolean[] bls = new boolean[]{false};
         yPoints.forEachPair((y1, y2, yIndex) -> {
            boolean[] bls2 = new boolean[]{false};
            zPoints.forEachPair((z1, z2, zIndex) -> {
               if (function.apply(first.inBoundsAndContains(x1, y1, z1), second.inBoundsAndContains(x2, y2, z2))) {
                  lv.storage.set(lv.getIndex(xIndex, yIndex, zIndex));
                  is[2] = Math.min(is[2], zIndex);
                  is[5] = Math.max(is[5], zIndex);
                  bls2[0] = true;
               }

               return true;
            });
            if (bls2[0]) {
               is[1] = Math.min(is[1], yIndex);
               is[4] = Math.max(is[4], yIndex);
               bls[0] = true;
            }

            return true;
         });
         if (bls[0]) {
            is[0] = Math.min(is[0], xIndex);
            is[3] = Math.max(is[3], xIndex);
         }

         return true;
      });
      lv.minX = is[0];
      lv.minY = is[1];
      lv.minZ = is[2];
      lv.maxX = is[3] + 1;
      lv.maxY = is[4] + 1;
      lv.maxZ = is[5] + 1;
      return lv;
   }

   protected static void forEachBox(VoxelSet voxelSet, VoxelSet.PositionBiConsumer callback, boolean coalesce) {
      BitSetVoxelSet lv = new BitSetVoxelSet(voxelSet);

      for(int i = 0; i < lv.sizeY; ++i) {
         for(int j = 0; j < lv.sizeX; ++j) {
            int k = -1;

            for(int l = 0; l <= lv.sizeZ; ++l) {
               if (lv.inBoundsAndContains(j, i, l)) {
                  if (coalesce) {
                     if (k == -1) {
                        k = l;
                     }
                  } else {
                     callback.consume(j, i, l, j + 1, i + 1, l + 1);
                  }
               } else if (k != -1) {
                  int m = j;
                  int n = i;
                  lv.clearColumn(k, l, j, i);

                  while(lv.isColumnFull(k, l, m + 1, i)) {
                     lv.clearColumn(k, l, m + 1, i);
                     ++m;
                  }

                  while(lv.isXzSquareFull(j, m + 1, k, l, n + 1)) {
                     for(int o = j; o <= m; ++o) {
                        lv.clearColumn(k, l, o, n + 1);
                     }

                     ++n;
                  }

                  callback.consume(j, i, k, m + 1, n + 1, l);
                  k = -1;
               }
            }
         }
      }

   }

   private boolean isColumnFull(int z1, int z2, int x, int y) {
      if (x < this.sizeX && y < this.sizeY) {
         return this.storage.nextClearBit(this.getIndex(x, y, z1)) >= this.getIndex(x, y, z2);
      } else {
         return false;
      }
   }

   private boolean isXzSquareFull(int x1, int x2, int z1, int z2, int y) {
      for(int n = x1; n < x2; ++n) {
         if (!this.isColumnFull(z1, z2, n, y)) {
            return false;
         }
      }

      return true;
   }

   private void clearColumn(int z1, int z2, int x, int y) {
      this.storage.clear(this.getIndex(x, y, z1), this.getIndex(x, y, z2));
   }
}
