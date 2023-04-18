package net.minecraft.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public abstract class SpecialCraftingRecipe implements CraftingRecipe {
   private final Identifier id;
   private final CraftingRecipeCategory category;

   public SpecialCraftingRecipe(Identifier id, CraftingRecipeCategory category) {
      this.id = id;
      this.category = category;
   }

   public Identifier getId() {
      return this.id;
   }

   public boolean isIgnoredInRecipeBook() {
      return true;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      return ItemStack.EMPTY;
   }

   public CraftingRecipeCategory getCategory() {
      return this.category;
   }
}
