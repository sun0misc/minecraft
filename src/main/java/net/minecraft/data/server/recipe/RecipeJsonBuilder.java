package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;

public abstract class RecipeJsonBuilder {
   protected static CraftingRecipeCategory getCraftingCategory(RecipeCategory category) {
      CraftingRecipeCategory var10000;
      switch (category) {
         case BUILDING_BLOCKS:
            var10000 = CraftingRecipeCategory.BUILDING;
            break;
         case TOOLS:
         case COMBAT:
            var10000 = CraftingRecipeCategory.EQUIPMENT;
            break;
         case REDSTONE:
            var10000 = CraftingRecipeCategory.REDSTONE;
            break;
         default:
            var10000 = CraftingRecipeCategory.MISC;
      }

      return var10000;
   }

   protected abstract static class CraftingRecipeJsonProvider implements RecipeJsonProvider {
      private final CraftingRecipeCategory craftingCategory;

      protected CraftingRecipeJsonProvider(CraftingRecipeCategory craftingCategory) {
         this.craftingCategory = craftingCategory;
      }

      public void serialize(JsonObject json) {
         json.addProperty("category", this.craftingCategory.asString());
      }
   }
}
