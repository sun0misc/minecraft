package net.minecraft.util.math.noise;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;

public class OctaveSimplexNoiseSampler {
   private final SimplexNoiseSampler[] octaveSamplers;
   private final double persistence;
   private final double lacunarity;

   public OctaveSimplexNoiseSampler(Random random, List octaves) {
      this(random, (IntSortedSet)(new IntRBTreeSet(octaves)));
   }

   private OctaveSimplexNoiseSampler(Random random, IntSortedSet octaves) {
      if (octaves.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int i = -octaves.firstInt();
         int j = octaves.lastInt();
         int k = i + j + 1;
         if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            SimplexNoiseSampler lv = new SimplexNoiseSampler(random);
            int l = j;
            this.octaveSamplers = new SimplexNoiseSampler[k];
            if (j >= 0 && j < k && octaves.contains(0)) {
               this.octaveSamplers[j] = lv;
            }

            for(int m = j + 1; m < k; ++m) {
               if (m >= 0 && octaves.contains(l - m)) {
                  this.octaveSamplers[m] = new SimplexNoiseSampler(random);
               } else {
                  random.skip(262);
               }
            }

            if (j > 0) {
               long n = (long)(lv.sample(lv.originX, lv.originY, lv.originZ) * 9.223372036854776E18);
               Random lv2 = new ChunkRandom(new CheckedRandom(n));

               for(int o = l - 1; o >= 0; --o) {
                  if (o < k && octaves.contains(l - o)) {
                     this.octaveSamplers[o] = new SimplexNoiseSampler(lv2);
                  } else {
                     lv2.skip(262);
                  }
               }
            }

            this.lacunarity = Math.pow(2.0, (double)j);
            this.persistence = 1.0 / (Math.pow(2.0, (double)k) - 1.0);
         }
      }
   }

   public double sample(double x, double y, boolean useOrigin) {
      double f = 0.0;
      double g = this.lacunarity;
      double h = this.persistence;
      SimplexNoiseSampler[] var12 = this.octaveSamplers;
      int var13 = var12.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         SimplexNoiseSampler lv = var12[var14];
         if (lv != null) {
            f += lv.sample(x * g + (useOrigin ? lv.originX : 0.0), y * g + (useOrigin ? lv.originY : 0.0)) * h;
         }

         g /= 2.0;
         h *= 2.0;
      }

      return f;
   }
}
