package net.minecraft.entity.passive;

import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum HorseColor implements StringIdentifiable {
   WHITE(0, "white"),
   CREAMY(1, "creamy"),
   CHESTNUT(2, "chestnut"),
   BROWN(3, "brown"),
   BLACK(4, "black"),
   GRAY(5, "gray"),
   DARK_BROWN(6, "dark_brown");

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(HorseColor::values);
   private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(HorseColor::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.WRAP);
   private final int id;
   private final String name;

   private HorseColor(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public static HorseColor byId(int id) {
      return (HorseColor)BY_ID.apply(id);
   }

   public String asString() {
      return this.name;
   }

   // $FF: synthetic method
   private static HorseColor[] method_36646() {
      return new HorseColor[]{WHITE, CREAMY, CHESTNUT, BROWN, BLACK, GRAY, DARK_BROWN};
   }
}
