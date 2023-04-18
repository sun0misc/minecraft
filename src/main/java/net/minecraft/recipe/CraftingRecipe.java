package net.minecraft.recipe;

import net.minecraft.recipe.book.CraftingRecipeCategory;

public interface CraftingRecipe extends Recipe {
   default RecipeType getType() {
      return RecipeType.CRAFTING;
   }

   CraftingRecipeCategory getCategory();
}
