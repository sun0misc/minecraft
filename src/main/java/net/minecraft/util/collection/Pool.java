package net.minecraft.util.collection;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.random.Random;

public class Pool {
   private final int totalWeight;
   private final ImmutableList entries;

   Pool(List entries) {
      this.entries = ImmutableList.copyOf(entries);
      this.totalWeight = Weighting.getWeightSum(entries);
   }

   public static Pool empty() {
      return new Pool(ImmutableList.of());
   }

   @SafeVarargs
   public static Pool of(Weighted... entries) {
      return new Pool(ImmutableList.copyOf(entries));
   }

   public static Pool of(List entries) {
      return new Pool(entries);
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   public Optional getOrEmpty(Random random) {
      if (this.totalWeight == 0) {
         return Optional.empty();
      } else {
         int i = random.nextInt(this.totalWeight);
         return Weighting.getAt(this.entries, i);
      }
   }

   public List getEntries() {
      return this.entries;
   }

   public static Codec createCodec(Codec entryCodec) {
      return entryCodec.listOf().xmap(Pool::of, Pool::getEntries);
   }
}
