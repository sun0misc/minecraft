package net.minecraft.util.math.random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import net.minecraft.util.math.MathHelper;

public class Xoroshiro128PlusPlusRandom implements Random {
   private static final float FLOAT_MULTIPLIER = 5.9604645E-8F;
   private static final double DOUBLE_MULTIPLIER = 1.1102230246251565E-16;
   private Xoroshiro128PlusPlusRandomImpl implementation;
   private final GaussianGenerator gaussianGenerator = new GaussianGenerator(this);

   public Xoroshiro128PlusPlusRandom(long seed) {
      this.implementation = new Xoroshiro128PlusPlusRandomImpl(RandomSeed.createXoroshiroSeed(seed));
   }

   public Xoroshiro128PlusPlusRandom(long seedLo, long seedHi) {
      this.implementation = new Xoroshiro128PlusPlusRandomImpl(seedLo, seedHi);
   }

   public Random split() {
      return new Xoroshiro128PlusPlusRandom(this.implementation.next(), this.implementation.next());
   }

   public RandomSplitter nextSplitter() {
      return new Splitter(this.implementation.next(), this.implementation.next());
   }

   public void setSeed(long seed) {
      this.implementation = new Xoroshiro128PlusPlusRandomImpl(RandomSeed.createXoroshiroSeed(seed));
      this.gaussianGenerator.reset();
   }

   public int nextInt() {
      return (int)this.implementation.next();
   }

   public int nextInt(int bound) {
      if (bound <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else {
         long l = Integer.toUnsignedLong(this.nextInt());
         long m = l * (long)bound;
         long n = m & 4294967295L;
         if (n < (long)bound) {
            for(int j = Integer.remainderUnsigned(~bound + 1, bound); n < (long)j; n = m & 4294967295L) {
               l = Integer.toUnsignedLong(this.nextInt());
               m = l * (long)bound;
            }
         }

         long o = m >> 32;
         return (int)o;
      }
   }

   public long nextLong() {
      return this.implementation.next();
   }

   public boolean nextBoolean() {
      return (this.implementation.next() & 1L) != 0L;
   }

   public float nextFloat() {
      return (float)this.next(24) * 5.9604645E-8F;
   }

   public double nextDouble() {
      return (double)this.next(53) * 1.1102230246251565E-16;
   }

   public double nextGaussian() {
      return this.gaussianGenerator.next();
   }

   public void skip(int count) {
      for(int j = 0; j < count; ++j) {
         this.implementation.next();
      }

   }

   private long next(int bits) {
      return this.implementation.next() >>> 64 - bits;
   }

   public static class Splitter implements RandomSplitter {
      private static final HashFunction MD5_HASHER = Hashing.md5();
      private final long seedLo;
      private final long seedHi;

      public Splitter(long seedLo, long seedHi) {
         this.seedLo = seedLo;
         this.seedHi = seedHi;
      }

      public Random split(int x, int y, int z) {
         long l = MathHelper.hashCode(x, y, z);
         long m = l ^ this.seedLo;
         return new Xoroshiro128PlusPlusRandom(m, this.seedHi);
      }

      public Random split(String seed) {
         byte[] bs = MD5_HASHER.hashString(seed, Charsets.UTF_8).asBytes();
         long l = Longs.fromBytes(bs[0], bs[1], bs[2], bs[3], bs[4], bs[5], bs[6], bs[7]);
         long m = Longs.fromBytes(bs[8], bs[9], bs[10], bs[11], bs[12], bs[13], bs[14], bs[15]);
         return new Xoroshiro128PlusPlusRandom(l ^ this.seedLo, m ^ this.seedHi);
      }

      @VisibleForTesting
      public void addDebugInfo(StringBuilder info) {
         info.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
      }
   }
}
