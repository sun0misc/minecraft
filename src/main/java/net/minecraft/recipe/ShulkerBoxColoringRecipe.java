package net.minecraft.recipe;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ShulkerBoxColoringRecipe extends SpecialCraftingRecipe {
   public ShulkerBoxColoringRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      int i = 0;
      int j = 0;

      for(int k = 0; k < arg.size(); ++k) {
         ItemStack lv = arg.getStack(k);
         if (!lv.isEmpty()) {
            if (Block.getBlockFromItem(lv.getItem()) instanceof ShulkerBoxBlock) {
               ++i;
            } else {
               if (!(lv.getItem() instanceof DyeItem)) {
                  return false;
               }

               ++j;
            }

            if (j > 1 || i > 1) {
               return false;
            }
         }
      }

      return i == 1 && j == 1;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      ItemStack lv = ItemStack.EMPTY;
      DyeItem lv2 = (DyeItem)Items.WHITE_DYE;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv3 = arg.getStack(i);
         if (!lv3.isEmpty()) {
            Item lv4 = lv3.getItem();
            if (Block.getBlockFromItem(lv4) instanceof ShulkerBoxBlock) {
               lv = lv3;
            } else if (lv4 instanceof DyeItem) {
               lv2 = (DyeItem)lv4;
            }
         }
      }

      ItemStack lv5 = ShulkerBoxBlock.getItemStack(lv2.getColor());
      if (lv.hasNbt()) {
         lv5.setNbt(lv.getNbt().copy());
      }

      return lv5;
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.SHULKER_BOX;
   }
}
