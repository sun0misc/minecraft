package net.minecraft.data.client;

import com.google.gson.JsonPrimitive;

public class VariantSettings {
   public static final VariantSetting X = new VariantSetting("x", (rotation) -> {
      return new JsonPrimitive(rotation.degrees);
   });
   public static final VariantSetting Y = new VariantSetting("y", (rotation) -> {
      return new JsonPrimitive(rotation.degrees);
   });
   public static final VariantSetting MODEL = new VariantSetting("model", (id) -> {
      return new JsonPrimitive(id.toString());
   });
   public static final VariantSetting UVLOCK = new VariantSetting("uvlock", JsonPrimitive::new);
   public static final VariantSetting WEIGHT = new VariantSetting("weight", JsonPrimitive::new);

   public static enum Rotation {
      R0(0),
      R90(90),
      R180(180),
      R270(270);

      final int degrees;

      private Rotation(int degrees) {
         this.degrees = degrees;
      }

      // $FF: synthetic method
      private static Rotation[] method_36941() {
         return new Rotation[]{R0, R90, R180, R270};
      }
   }
}
