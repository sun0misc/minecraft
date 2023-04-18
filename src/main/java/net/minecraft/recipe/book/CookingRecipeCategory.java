package net.minecraft.recipe.book;

import net.minecraft.util.StringIdentifiable;

public enum CookingRecipeCategory implements StringIdentifiable {
   FOOD("food"),
   BLOCKS("blocks"),
   MISC("misc");

   public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(CookingRecipeCategory::values);
   private final String id;

   private CookingRecipeCategory(String id) {
      this.id = id;
   }

   public String asString() {
      return this.id;
   }

   // $FF: synthetic method
   private static CookingRecipeCategory[] method_45439() {
      return new CookingRecipeCategory[]{FOOD, BLOCKS, MISC};
   }
}
