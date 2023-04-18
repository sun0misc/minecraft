package net.minecraft.util.math.intprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.math.random.Random;

public class BiasedToBottomIntProvider extends IntProvider {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("min_inclusive").forGetter((provider) -> {
         return provider.min;
      }), Codec.INT.fieldOf("max_inclusive").forGetter((provider) -> {
         return provider.max;
      })).apply(instance, BiasedToBottomIntProvider::new);
   }).comapFlatMap((provider) -> {
      return provider.max < provider.min ? DataResult.error(() -> {
         return "Max must be at least min, min_inclusive: " + provider.min + ", max_inclusive: " + provider.max;
      }) : DataResult.success(provider);
   }, Function.identity());
   private final int min;
   private final int max;

   private BiasedToBottomIntProvider(int min, int max) {
      this.min = min;
      this.max = max;
   }

   public static BiasedToBottomIntProvider create(int min, int max) {
      return new BiasedToBottomIntProvider(min, max);
   }

   public int get(Random random) {
      return this.min + random.nextInt(random.nextInt(this.max - this.min + 1) + 1);
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public IntProviderType getType() {
      return IntProviderType.BIASED_TO_BOTTOM;
   }

   public String toString() {
      return "[" + this.min + "-" + this.max + "]";
   }
}
