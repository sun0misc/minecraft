package net.minecraft.util.math.random;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.thread.LockHelper;

public class CheckedRandom implements BaseRandom {
   private static final int INT_BITS = 48;
   private static final long SEED_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private final AtomicLong seed = new AtomicLong();
   private final GaussianGenerator gaussianGenerator = new GaussianGenerator(this);

   public CheckedRandom(long seed) {
      this.setSeed(seed);
   }

   public Random split() {
      return new CheckedRandom(this.nextLong());
   }

   public RandomSplitter nextSplitter() {
      return new Splitter(this.nextLong());
   }

   public void setSeed(long seed) {
      if (!this.seed.compareAndSet(this.seed.get(), (seed ^ 25214903917L) & 281474976710655L)) {
         throw LockHelper.crash("LegacyRandomSource", (Thread)null);
      } else {
         this.gaussianGenerator.reset();
      }
   }

   public int next(int bits) {
      long l = this.seed.get();
      long m = l * 25214903917L + 11L & 281474976710655L;
      if (!this.seed.compareAndSet(l, m)) {
         throw LockHelper.crash("LegacyRandomSource", (Thread)null);
      } else {
         return (int)(m >> 48 - bits);
      }
   }

   public double nextGaussian() {
      return this.gaussianGenerator.next();
   }

   public static class Splitter implements RandomSplitter {
      private final long seed;

      public Splitter(long seed) {
         this.seed = seed;
      }

      public Random split(int x, int y, int z) {
         long l = MathHelper.hashCode(x, y, z);
         long m = l ^ this.seed;
         return new CheckedRandom(m);
      }

      public Random split(String seed) {
         int i = seed.hashCode();
         return new CheckedRandom((long)i ^ this.seed);
      }

      @VisibleForTesting
      public void addDebugInfo(StringBuilder info) {
         info.append("LegacyPositionalRandomFactory{").append(this.seed).append("}");
      }
   }
}
