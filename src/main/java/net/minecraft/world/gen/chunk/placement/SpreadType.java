package net.minecraft.world.gen.chunk.placement;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.random.Random;

public enum SpreadType implements StringIdentifiable {
   LINEAR("linear"),
   TRIANGULAR("triangular");

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(SpreadType::values);
   private final String name;

   private SpreadType(String name) {
      this.name = name;
   }

   public String asString() {
      return this.name;
   }

   public int get(Random random, int bound) {
      int var10000;
      switch (this) {
         case LINEAR:
            var10000 = random.nextInt(bound);
            break;
         case TRIANGULAR:
            var10000 = (random.nextInt(bound) + random.nextInt(bound)) / 2;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   // $FF: synthetic method
   private static SpreadType[] method_40175() {
      return new SpreadType[]{LINEAR, TRIANGULAR};
   }
}
