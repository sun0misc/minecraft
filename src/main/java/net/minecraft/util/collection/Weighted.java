package net.minecraft.util.collection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface Weighted {
   Weight getWeight();

   static Present of(Object data, int weight) {
      return new Present(data, Weight.of(weight));
   }

   public static class Present implements Weighted {
      private final Object data;
      private final Weight weight;

      Present(Object data, Weight weight) {
         this.data = data;
         this.weight = weight;
      }

      public Object getData() {
         return this.data;
      }

      public Weight getWeight() {
         return this.weight;
      }

      public static Codec createCodec(Codec dataCodec) {
         return RecordCodecBuilder.create((instance) -> {
            return instance.group(dataCodec.fieldOf("data").forGetter(Present::getData), Weight.CODEC.fieldOf("weight").forGetter(Present::getWeight)).apply(instance, Present::new);
         });
      }
   }

   public static class Absent implements Weighted {
      private final Weight weight;

      public Absent(int weight) {
         this.weight = Weight.of(weight);
      }

      public Absent(Weight weight) {
         this.weight = weight;
      }

      public Weight getWeight() {
         return this.weight;
      }
   }
}
