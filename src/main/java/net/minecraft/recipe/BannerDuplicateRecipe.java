package net.minecraft.recipe;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BannerDuplicateRecipe extends SpecialCraftingRecipe {
   public BannerDuplicateRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      DyeColor lv = null;
      ItemStack lv2 = null;
      ItemStack lv3 = null;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv4 = arg.getStack(i);
         if (!lv4.isEmpty()) {
            Item lv5 = lv4.getItem();
            if (!(lv5 instanceof BannerItem)) {
               return false;
            }

            BannerItem lv6 = (BannerItem)lv5;
            if (lv == null) {
               lv = lv6.getColor();
            } else if (lv != lv6.getColor()) {
               return false;
            }

            int j = BannerBlockEntity.getPatternCount(lv4);
            if (j > 6) {
               return false;
            }

            if (j > 0) {
               if (lv2 != null) {
                  return false;
               }

               lv2 = lv4;
            } else {
               if (lv3 != null) {
                  return false;
               }

               lv3 = lv4;
            }
         }
      }

      return lv2 != null && lv3 != null;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv = arg.getStack(i);
         if (!lv.isEmpty()) {
            int j = BannerBlockEntity.getPatternCount(lv);
            if (j > 0 && j <= 6) {
               return lv.copyWithCount(1);
            }
         }
      }

      return ItemStack.EMPTY;
   }

   public DefaultedList getRemainder(CraftingInventory arg) {
      DefaultedList lv = DefaultedList.ofSize(arg.size(), ItemStack.EMPTY);

      for(int i = 0; i < lv.size(); ++i) {
         ItemStack lv2 = arg.getStack(i);
         if (!lv2.isEmpty()) {
            if (lv2.getItem().hasRecipeRemainder()) {
               lv.set(i, new ItemStack(lv2.getItem().getRecipeRemainder()));
            } else if (lv2.hasNbt() && BannerBlockEntity.getPatternCount(lv2) > 0) {
               lv.set(i, lv2.copyWithCount(1));
            }
         }
      }

      return lv;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.BANNER_DUPLICATE;
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }
}
