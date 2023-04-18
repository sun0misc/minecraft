package net.minecraft.util.collection;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

public class DataPool extends Pool {
   public static Codec createEmptyAllowedCodec(Codec dataCodec) {
      return Weighted.Present.createCodec(dataCodec).listOf().xmap(DataPool::new, Pool::getEntries);
   }

   public static Codec createCodec(Codec dataCodec) {
      return Codecs.nonEmptyList(Weighted.Present.createCodec(dataCodec).listOf()).xmap(DataPool::new, Pool::getEntries);
   }

   DataPool(List list) {
      super(list);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static DataPool empty() {
      return new DataPool(List.of());
   }

   public static DataPool of(Object object) {
      return new DataPool(List.of(Weighted.of(object, 1)));
   }

   public Optional getDataOrEmpty(Random random) {
      return this.getOrEmpty(random).map(Weighted.Present::getData);
   }

   public static class Builder {
      private final ImmutableList.Builder entries = ImmutableList.builder();

      public Builder add(Object object, int weight) {
         this.entries.add(Weighted.of(object, weight));
         return this;
      }

      public DataPool build() {
         return new DataPool(this.entries.build());
      }
   }
}
