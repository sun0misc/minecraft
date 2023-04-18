package net.minecraft.util.collection;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.math.MathHelper;

public class LinkedBlockPosHashSet extends LongLinkedOpenHashSet {
   private final Storage buffer;

   public LinkedBlockPosHashSet(int expectedSize, float loadFactor) {
      super(expectedSize, loadFactor);
      this.buffer = new Storage(expectedSize / 64, loadFactor);
   }

   public boolean add(long posLong) {
      return this.buffer.add(posLong);
   }

   public boolean rem(long posLong) {
      return this.buffer.rem(posLong);
   }

   public long removeFirstLong() {
      return this.buffer.removeFirstLong();
   }

   public int size() {
      throw new UnsupportedOperationException();
   }

   public boolean isEmpty() {
      return this.buffer.isEmpty();
   }

   protected static class Storage extends Long2LongLinkedOpenHashMap {
      private static final int STARTING_OFFSET = MathHelper.floorLog2(60000000);
      private static final int HORIZONTAL_COLUMN_BIT_SEPARATION = MathHelper.floorLog2(60000000);
      private static final int FIELD_SPACING;
      private static final int Y_BIT_OFFSET = 0;
      private static final int X_BIT_OFFSET;
      private static final int Z_BIT_OFFSET;
      private static final long MAX_POSITION;
      private int lastWrittenIndex = -1;
      private long lastWrittenKey;
      private final int expectedSize;

      public Storage(int expectedSize, float loadFactor) {
         super(expectedSize, loadFactor);
         this.expectedSize = expectedSize;
      }

      static long getKey(long posLong) {
         return posLong & ~MAX_POSITION;
      }

      static int getBlockOffset(long posLong) {
         int i = (int)(posLong >>> Z_BIT_OFFSET & 3L);
         int j = (int)(posLong >>> 0 & 3L);
         int k = (int)(posLong >>> X_BIT_OFFSET & 3L);
         return i << 4 | k << 2 | j;
      }

      static long getBlockPosLong(long key, int valueLength) {
         key |= (long)(valueLength >>> 4 & 3) << Z_BIT_OFFSET;
         key |= (long)(valueLength >>> 2 & 3) << X_BIT_OFFSET;
         key |= (long)(valueLength >>> 0 & 3) << 0;
         return key;
      }

      public boolean add(long posLong) {
         long m = getKey(posLong);
         int i = getBlockOffset(posLong);
         long n = 1L << i;
         int j;
         if (m == 0L) {
            if (this.containsNullKey) {
               return this.setBits(this.n, n);
            }

            this.containsNullKey = true;
            j = this.n;
         } else {
            if (this.lastWrittenIndex != -1 && m == this.lastWrittenKey) {
               return this.setBits(this.lastWrittenIndex, n);
            }

            long[] ls = this.key;
            j = (int)HashCommon.mix(m) & this.mask;

            for(long o = ls[j]; o != 0L; o = ls[j]) {
               if (o == m) {
                  this.lastWrittenIndex = j;
                  this.lastWrittenKey = m;
                  return this.setBits(j, n);
               }

               j = j + 1 & this.mask;
            }
         }

         this.key[j] = m;
         this.value[j] = n;
         if (this.size == 0) {
            this.first = this.last = j;
            this.link[j] = -1L;
         } else {
            long[] var10000 = this.link;
            int var10001 = this.last;
            var10000[var10001] ^= (this.link[this.last] ^ (long)j & 4294967295L) & 4294967295L;
            this.link[j] = ((long)this.last & 4294967295L) << 32 | 4294967295L;
            this.last = j;
         }

         if (this.size++ >= this.maxFill) {
            this.rehash(HashCommon.arraySize(this.size + 1, this.f));
         }

         return false;
      }

      private boolean setBits(int index, long mask) {
         boolean bl = (this.value[index] & mask) != 0L;
         long[] var10000 = this.value;
         var10000[index] |= mask;
         return bl;
      }

      public boolean rem(long posLong) {
         long m = getKey(posLong);
         int i = getBlockOffset(posLong);
         long n = 1L << i;
         if (m == 0L) {
            return this.containsNullKey ? this.unsetBits(n) : false;
         } else if (this.lastWrittenIndex != -1 && m == this.lastWrittenKey) {
            return this.unsetBitsAt(this.lastWrittenIndex, n);
         } else {
            long[] ls = this.key;
            int j = (int)HashCommon.mix(m) & this.mask;

            for(long o = ls[j]; o != 0L; o = ls[j]) {
               if (m == o) {
                  this.lastWrittenIndex = j;
                  this.lastWrittenKey = m;
                  return this.unsetBitsAt(j, n);
               }

               j = j + 1 & this.mask;
            }

            return false;
         }
      }

      private boolean unsetBits(long mask) {
         if ((this.value[this.n] & mask) == 0L) {
            return false;
         } else {
            long[] var10000 = this.value;
            int var10001 = this.n;
            var10000[var10001] &= ~mask;
            if (this.value[this.n] != 0L) {
               return true;
            } else {
               this.containsNullKey = false;
               --this.size;
               this.fixPointers(this.n);
               if (this.size < this.maxFill / 4 && this.n > 16) {
                  this.rehash(this.n / 2);
               }

               return true;
            }
         }
      }

      private boolean unsetBitsAt(int index, long mask) {
         if ((this.value[index] & mask) == 0L) {
            return false;
         } else {
            long[] var10000 = this.value;
            var10000[index] &= ~mask;
            if (this.value[index] != 0L) {
               return true;
            } else {
               this.lastWrittenIndex = -1;
               --this.size;
               this.fixPointers(index);
               this.shiftKeys(index);
               if (this.size < this.maxFill / 4 && this.n > 16) {
                  this.rehash(this.n / 2);
               }

               return true;
            }
         }
      }

      public long removeFirstLong() {
         if (this.size == 0) {
            throw new NoSuchElementException();
         } else {
            int i = this.first;
            long l = this.key[i];
            int j = Long.numberOfTrailingZeros(this.value[i]);
            long[] var10000 = this.value;
            var10000[i] &= ~(1L << j);
            if (this.value[i] == 0L) {
               this.removeFirstLong();
               this.lastWrittenIndex = -1;
            }

            return getBlockPosLong(l, j);
         }
      }

      protected void rehash(int newN) {
         if (newN > this.expectedSize) {
            super.rehash(newN);
         }

      }

      static {
         FIELD_SPACING = 64 - STARTING_OFFSET - HORIZONTAL_COLUMN_BIT_SEPARATION;
         X_BIT_OFFSET = FIELD_SPACING;
         Z_BIT_OFFSET = FIELD_SPACING + HORIZONTAL_COLUMN_BIT_SEPARATION;
         MAX_POSITION = 3L << Z_BIT_OFFSET | 3L | 3L << X_BIT_OFFSET;
      }
   }
}
