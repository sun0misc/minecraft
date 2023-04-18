package net.minecraft.world.chunk;

import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.Nullable;

public class ChunkNibbleArray {
   public static final int COPY_TIMES = 16;
   public static final int COPY_BLOCK_SIZE = 128;
   public static final int BYTES_LENGTH = 2048;
   private static final int NIBBLE_BITS = 4;
   @Nullable
   protected byte[] bytes;

   public ChunkNibbleArray() {
   }

   public ChunkNibbleArray(byte[] bytes) {
      this.bytes = bytes;
      if (bytes.length != 2048) {
         throw (IllegalArgumentException)Util.throwOrPause(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + bytes.length));
      }
   }

   protected ChunkNibbleArray(int size) {
      this.bytes = new byte[size];
   }

   public int get(int x, int y, int z) {
      return this.get(getIndex(x, y, z));
   }

   public void set(int x, int y, int z, int value) {
      this.set(getIndex(x, y, z), value);
   }

   private static int getIndex(int x, int y, int z) {
      return y << 8 | z << 4 | x;
   }

   private int get(int index) {
      if (this.bytes == null) {
         return 0;
      } else {
         int j = getArrayIndex(index);
         int k = occupiesSmallerBits(index);
         return this.bytes[j] >> 4 * k & 15;
      }
   }

   private void set(int index, int value) {
      if (this.bytes == null) {
         this.bytes = new byte[2048];
      }

      int k = getArrayIndex(index);
      int l = occupiesSmallerBits(index);
      int m = ~(15 << 4 * l);
      int n = (value & 15) << 4 * l;
      this.bytes[k] = (byte)(this.bytes[k] & m | n);
   }

   private static int occupiesSmallerBits(int i) {
      return i & 1;
   }

   private static int getArrayIndex(int i) {
      return i >> 1;
   }

   public byte[] asByteArray() {
      if (this.bytes == null) {
         this.bytes = new byte[2048];
      }

      return this.bytes;
   }

   public ChunkNibbleArray copy() {
      return this.bytes == null ? new ChunkNibbleArray() : new ChunkNibbleArray((byte[])this.bytes.clone());
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();

      for(int i = 0; i < 4096; ++i) {
         stringBuilder.append(Integer.toHexString(this.get(i)));
         if ((i & 15) == 15) {
            stringBuilder.append("\n");
         }

         if ((i & 255) == 255) {
            stringBuilder.append("\n");
         }
      }

      return stringBuilder.toString();
   }

   @Debug
   public String bottomToString(int unused) {
      StringBuilder stringBuilder = new StringBuilder();

      for(int j = 0; j < 256; ++j) {
         stringBuilder.append(Integer.toHexString(this.get(j)));
         if ((j & 15) == 15) {
            stringBuilder.append("\n");
         }
      }

      return stringBuilder.toString();
   }

   public boolean isUninitialized() {
      return this.bytes == null;
   }
}
