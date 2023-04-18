package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum WallMountLocation implements StringIdentifiable {
   FLOOR("floor"),
   WALL("wall"),
   CEILING("ceiling");

   private final String name;

   private WallMountLocation(String name) {
      this.name = name;
   }

   public String asString() {
      return this.name;
   }

   // $FF: synthetic method
   private static WallMountLocation[] method_36720() {
      return new WallMountLocation[]{FLOOR, WALL, CEILING};
   }
}
