package net.minecraft.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public class Weighting {
   private Weighting() {
   }

   public static int getWeightSum(List pool) {
      long l = 0L;

      Weighted lv;
      for(Iterator var3 = pool.iterator(); var3.hasNext(); l += (long)lv.getWeight().getValue()) {
         lv = (Weighted)var3.next();
      }

      if (l > 2147483647L) {
         throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
      } else {
         return (int)l;
      }
   }

   public static Optional getRandom(Random random, List pool, int totalWeight) {
      if (totalWeight < 0) {
         throw (IllegalArgumentException)Util.throwOrPause(new IllegalArgumentException("Negative total weight in getRandomItem"));
      } else if (totalWeight == 0) {
         return Optional.empty();
      } else {
         int j = random.nextInt(totalWeight);
         return getAt(pool, j);
      }
   }

   public static Optional getAt(List pool, int totalWeight) {
      Iterator var2 = pool.iterator();

      Weighted lv;
      do {
         if (!var2.hasNext()) {
            return Optional.empty();
         }

         lv = (Weighted)var2.next();
         totalWeight -= lv.getWeight().getValue();
      } while(totalWeight >= 0);

      return Optional.of(lv);
   }

   public static Optional getRandom(Random random, List pool) {
      return getRandom(random, pool, getWeightSum(pool));
   }
}
