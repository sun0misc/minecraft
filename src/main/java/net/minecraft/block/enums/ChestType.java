package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum ChestType implements StringIdentifiable {
   SINGLE("single"),
   LEFT("left"),
   RIGHT("right");

   private final String name;

   private ChestType(String name) {
      this.name = name;
   }

   public String asString() {
      return this.name;
   }

   public ChestType getOpposite() {
      ChestType var10000;
      switch (this) {
         case SINGLE:
            var10000 = SINGLE;
            break;
         case LEFT:
            var10000 = RIGHT;
            break;
         case RIGHT:
            var10000 = LEFT;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   // $FF: synthetic method
   private static ChestType[] method_36724() {
      return new ChestType[]{SINGLE, LEFT, RIGHT};
   }
}
