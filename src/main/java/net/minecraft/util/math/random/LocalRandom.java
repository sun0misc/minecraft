package net.minecraft.util.math.random;

public class LocalRandom implements BaseRandom {
   private static final int INT_BITS = 48;
   private static final long SEED_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private long seed;
   private final GaussianGenerator gaussianGenerator = new GaussianGenerator(this);

   public LocalRandom(long seed) {
      this.setSeed(seed);
   }

   public Random split() {
      return new LocalRandom(this.nextLong());
   }

   public RandomSplitter nextSplitter() {
      return new CheckedRandom.Splitter(this.nextLong());
   }

   public void setSeed(long seed) {
      this.seed = (seed ^ 25214903917L) & 281474976710655L;
      this.gaussianGenerator.reset();
   }

   public int next(int bits) {
      long l = this.seed * 25214903917L + 11L & 281474976710655L;
      this.seed = l;
      return (int)(l >> 48 - bits);
   }

   public double nextGaussian() {
      return this.gaussianGenerator.next();
   }
}
