package net.minecraft.util.math.random;

import java.util.function.LongFunction;

public class ChunkRandom extends CheckedRandom {
   private final Random baseRandom;
   private int sampleCount;

   public ChunkRandom(Random baseRandom) {
      super(0L);
      this.baseRandom = baseRandom;
   }

   public int getSampleCount() {
      return this.sampleCount;
   }

   public Random split() {
      return this.baseRandom.split();
   }

   public RandomSplitter nextSplitter() {
      return this.baseRandom.nextSplitter();
   }

   public int next(int count) {
      ++this.sampleCount;
      Random var3 = this.baseRandom;
      if (var3 instanceof CheckedRandom lv) {
         return lv.next(count);
      } else {
         return (int)(this.baseRandom.nextLong() >>> 64 - count);
      }
   }

   public synchronized void setSeed(long seed) {
      if (this.baseRandom != null) {
         this.baseRandom.setSeed(seed);
      }
   }

   public long setPopulationSeed(long worldSeed, int blockX, int blockZ) {
      this.setSeed(worldSeed);
      long m = this.nextLong() | 1L;
      long n = this.nextLong() | 1L;
      long o = (long)blockX * m + (long)blockZ * n ^ worldSeed;
      this.setSeed(o);
      return o;
   }

   public void setDecoratorSeed(long populationSeed, int index, int step) {
      long m = populationSeed + (long)index + (long)(10000 * step);
      this.setSeed(m);
   }

   public void setCarverSeed(long worldSeed, int chunkX, int chunkZ) {
      this.setSeed(worldSeed);
      long m = this.nextLong();
      long n = this.nextLong();
      long o = (long)chunkX * m ^ (long)chunkZ * n ^ worldSeed;
      this.setSeed(o);
   }

   public void setRegionSeed(long worldSeed, int regionX, int regionZ, int salt) {
      long m = (long)regionX * 341873128712L + (long)regionZ * 132897987541L + worldSeed + (long)salt;
      this.setSeed(m);
   }

   public static Random getSlimeRandom(int chunkX, int chunkZ, long worldSeed, long scrambler) {
      return Random.create(worldSeed + (long)(chunkX * chunkX * 4987142) + (long)(chunkX * 5947611) + (long)(chunkZ * chunkZ) * 4392871L + (long)(chunkZ * 389711) ^ scrambler);
   }

   public static enum RandomProvider {
      LEGACY(CheckedRandom::new),
      XOROSHIRO(Xoroshiro128PlusPlusRandom::new);

      private final LongFunction provider;

      private RandomProvider(LongFunction provider) {
         this.provider = provider;
      }

      public Random create(long seed) {
         return (Random)this.provider.apply(seed);
      }

      // $FF: synthetic method
      private static RandomProvider[] method_39005() {
         return new RandomProvider[]{LEGACY, XOROSHIRO};
      }
   }
}
