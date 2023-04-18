package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.random.Random;

public class ConstantIntProvider extends IntProvider {
   public static final ConstantIntProvider ZERO = new ConstantIntProvider(0);
   public static final Codec CODEC;
   private final int value;

   public static ConstantIntProvider create(int value) {
      return value == 0 ? ZERO : new ConstantIntProvider(value);
   }

   private ConstantIntProvider(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public int get(Random random) {
      return this.value;
   }

   public int getMin() {
      return this.value;
   }

   public int getMax() {
      return this.value;
   }

   public IntProviderType getType() {
      return IntProviderType.CONSTANT;
   }

   public String toString() {
      return Integer.toString(this.value);
   }

   static {
      CODEC = Codec.either(Codec.INT, RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("value").forGetter((provider) -> {
            return provider.value;
         })).apply(instance, ConstantIntProvider::new);
      })).xmap((either) -> {
         return (ConstantIntProvider)either.map(ConstantIntProvider::create, (provider) -> {
            return provider;
         });
      }, (provider) -> {
         return Either.left(provider.value);
      });
   }
}
