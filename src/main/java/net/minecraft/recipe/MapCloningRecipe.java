package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MapCloningRecipe extends SpecialCraftingRecipe {
   public MapCloningRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      int i = 0;
      ItemStack lv = ItemStack.EMPTY;

      for(int j = 0; j < arg.size(); ++j) {
         ItemStack lv2 = arg.getStack(j);
         if (!lv2.isEmpty()) {
            if (lv2.isOf(Items.FILLED_MAP)) {
               if (!lv.isEmpty()) {
                  return false;
               }

               lv = lv2;
            } else {
               if (!lv2.isOf(Items.MAP)) {
                  return false;
               }

               ++i;
            }
         }
      }

      return !lv.isEmpty() && i > 0;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      int i = 0;
      ItemStack lv = ItemStack.EMPTY;

      for(int j = 0; j < arg.size(); ++j) {
         ItemStack lv2 = arg.getStack(j);
         if (!lv2.isEmpty()) {
            if (lv2.isOf(Items.FILLED_MAP)) {
               if (!lv.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               lv = lv2;
            } else {
               if (!lv2.isOf(Items.MAP)) {
                  return ItemStack.EMPTY;
               }

               ++i;
            }
         }
      }

      if (!lv.isEmpty() && i >= 1) {
         return lv.copyWithCount(i + 1);
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean fits(int width, int height) {
      return width >= 3 && height >= 3;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.MAP_CLONING;
   }
}
