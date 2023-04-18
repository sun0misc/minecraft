package net.minecraft.recipe;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ArmorDyeRecipe extends SpecialCraftingRecipe {
   public ArmorDyeRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      ItemStack lv = ItemStack.EMPTY;
      List list = Lists.newArrayList();

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv2 = arg.getStack(i);
         if (!lv2.isEmpty()) {
            if (lv2.getItem() instanceof DyeableItem) {
               if (!lv.isEmpty()) {
                  return false;
               }

               lv = lv2;
            } else {
               if (!(lv2.getItem() instanceof DyeItem)) {
                  return false;
               }

               list.add(lv2);
            }
         }
      }

      return !lv.isEmpty() && !list.isEmpty();
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      List list = Lists.newArrayList();
      ItemStack lv = ItemStack.EMPTY;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv2 = arg.getStack(i);
         if (!lv2.isEmpty()) {
            Item lv3 = lv2.getItem();
            if (lv3 instanceof DyeableItem) {
               if (!lv.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               lv = lv2.copy();
            } else {
               if (!(lv3 instanceof DyeItem)) {
                  return ItemStack.EMPTY;
               }

               list.add((DyeItem)lv3);
            }
         }
      }

      if (!lv.isEmpty() && !list.isEmpty()) {
         return DyeableItem.blendAndSetColor(lv, list);
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.ARMOR_DYE;
   }
}
