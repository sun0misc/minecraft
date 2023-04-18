package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class MapExtendingRecipe extends ShapedRecipe {
   public MapExtendingRecipe(Identifier id, CraftingRecipeCategory category) {
      super(id, "", category, 3, 3, DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.FILLED_MAP), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER), Ingredient.ofItems(Items.PAPER)), new ItemStack(Items.MAP));
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      if (!super.matches(arg, arg2)) {
         return false;
      } else {
         ItemStack lv = findFilledMap(arg);
         if (lv.isEmpty()) {
            return false;
         } else {
            MapState lv2 = FilledMapItem.getMapState(lv, arg2);
            if (lv2 == null) {
               return false;
            } else if (lv2.hasMonumentIcon()) {
               return false;
            } else {
               return lv2.scale < 4;
            }
         }
      }
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      ItemStack lv = findFilledMap(arg).copyWithCount(1);
      lv.getOrCreateNbt().putInt("map_scale_direction", 1);
      return lv;
   }

   private static ItemStack findFilledMap(CraftingInventory inventory) {
      for(int i = 0; i < inventory.size(); ++i) {
         ItemStack lv = inventory.getStack(i);
         if (lv.isOf(Items.FILLED_MAP)) {
            return lv;
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean isIgnoredInRecipeBook() {
      return true;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.MAP_EXTENDING;
   }
}
