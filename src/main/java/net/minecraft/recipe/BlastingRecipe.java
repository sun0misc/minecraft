package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.util.Identifier;

public class BlastingRecipe extends AbstractCookingRecipe {
   public BlastingRecipe(Identifier id, String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
      super(RecipeType.BLASTING, id, group, category, input, output, experience, cookTime);
   }

   public ItemStack createIcon() {
      return new ItemStack(Blocks.BLAST_FURNACE);
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.BLASTING;
   }
}
