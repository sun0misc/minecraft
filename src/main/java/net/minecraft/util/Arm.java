package net.minecraft.util;

public enum Arm implements TranslatableOption {
   LEFT(0, "options.mainHand.left"),
   RIGHT(1, "options.mainHand.right");

   private final int id;
   private final String translationKey;

   private Arm(int id, String translationKey) {
      this.id = id;
      this.translationKey = translationKey;
   }

   public Arm getOpposite() {
      return this == LEFT ? RIGHT : LEFT;
   }

   public int getId() {
      return this.id;
   }

   public String getTranslationKey() {
      return this.translationKey;
   }

   // $FF: synthetic method
   private static Arm[] method_36606() {
      return new Arm[]{LEFT, RIGHT};
   }
}
