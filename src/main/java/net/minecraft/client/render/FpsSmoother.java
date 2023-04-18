package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FpsSmoother {
   private final long[] times;
   private int size;
   private int index;

   public FpsSmoother(int size) {
      this.times = new long[size];
   }

   public long getTargetUsedTime(long time) {
      if (this.size < this.times.length) {
         ++this.size;
      }

      this.times[this.index] = time;
      this.index = (this.index + 1) % this.times.length;
      long m = Long.MAX_VALUE;
      long n = Long.MIN_VALUE;
      long o = 0L;

      for(int i = 0; i < this.size; ++i) {
         long p = this.times[i];
         o += p;
         m = Math.min(m, p);
         n = Math.max(n, p);
      }

      if (this.size > 2) {
         o -= m + n;
         return o / (long)(this.size - 2);
      } else {
         return o > 0L ? (long)this.size / o : 0L;
      }
   }
}
