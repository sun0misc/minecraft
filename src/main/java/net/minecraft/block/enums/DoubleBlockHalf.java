package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum DoubleBlockHalf implements StringIdentifiable {
   UPPER,
   LOWER;

   public String toString() {
      return this.asString();
   }

   public String asString() {
      return this == UPPER ? "upper" : "lower";
   }

   // $FF: synthetic method
   private static DoubleBlockHalf[] method_36727() {
      return new DoubleBlockHalf[]{UPPER, LOWER};
   }
}
