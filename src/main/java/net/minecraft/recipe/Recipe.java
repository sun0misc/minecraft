package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public interface Recipe {
   boolean matches(Inventory inventory, World world);

   ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager);

   boolean fits(int width, int height);

   ItemStack getOutput(DynamicRegistryManager registryManager);

   default DefaultedList getRemainder(Inventory inventory) {
      DefaultedList lv = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

      for(int i = 0; i < lv.size(); ++i) {
         Item lv2 = inventory.getStack(i).getItem();
         if (lv2.hasRecipeRemainder()) {
            lv.set(i, new ItemStack(lv2.getRecipeRemainder()));
         }
      }

      return lv;
   }

   default DefaultedList getIngredients() {
      return DefaultedList.of();
   }

   default boolean isIgnoredInRecipeBook() {
      return false;
   }

   default boolean showNotification() {
      return true;
   }

   default String getGroup() {
      return "";
   }

   default ItemStack createIcon() {
      return new ItemStack(Blocks.CRAFTING_TABLE);
   }

   Identifier getId();

   RecipeSerializer getSerializer();

   RecipeType getType();

   default boolean isEmpty() {
      DefaultedList lv = this.getIngredients();
      return lv.isEmpty() || lv.stream().anyMatch((ingredient) -> {
         return ingredient.getMatchingStacks().length == 0;
      });
   }
}
