package net.minecraft.entity.damage;

import net.minecraft.util.StringIdentifiable;

public enum DeathMessageType implements StringIdentifiable {
   DEFAULT("default"),
   FALL_VARIANTS("fall_variants"),
   INTENTIONAL_GAME_DESIGN("intentional_game_design");

   public static final com.mojang.serialization.Codec CODEC = StringIdentifiable.createCodec(DeathMessageType::values);
   private final String id;

   private DeathMessageType(String id) {
      this.id = id;
   }

   public String asString() {
      return this.id;
   }

   // $FF: synthetic method
   private static DeathMessageType[] method_48840() {
      return new DeathMessageType[]{DEFAULT, FALL_VARIANTS, INTENTIONAL_GAME_DESIGN};
   }
}
