package net.minecraft.recipe;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ShieldDecorationRecipe extends SpecialCraftingRecipe {
   public ShieldDecorationRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      ItemStack lv = ItemStack.EMPTY;
      ItemStack lv2 = ItemStack.EMPTY;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv3 = arg.getStack(i);
         if (!lv3.isEmpty()) {
            if (lv3.getItem() instanceof BannerItem) {
               if (!lv2.isEmpty()) {
                  return false;
               }

               lv2 = lv3;
            } else {
               if (!lv3.isOf(Items.SHIELD)) {
                  return false;
               }

               if (!lv.isEmpty()) {
                  return false;
               }

               if (BlockItem.getBlockEntityNbt(lv3) != null) {
                  return false;
               }

               lv = lv3;
            }
         }
      }

      if (!lv.isEmpty() && !lv2.isEmpty()) {
         return true;
      } else {
         return false;
      }
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      ItemStack lv = ItemStack.EMPTY;
      ItemStack lv2 = ItemStack.EMPTY;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv3 = arg.getStack(i);
         if (!lv3.isEmpty()) {
            if (lv3.getItem() instanceof BannerItem) {
               lv = lv3;
            } else if (lv3.isOf(Items.SHIELD)) {
               lv2 = lv3.copy();
            }
         }
      }

      if (lv2.isEmpty()) {
         return lv2;
      } else {
         NbtCompound lv4 = BlockItem.getBlockEntityNbt(lv);
         NbtCompound lv5 = lv4 == null ? new NbtCompound() : lv4.copy();
         lv5.putInt("Base", ((BannerItem)lv.getItem()).getColor().getId());
         BlockItem.setBlockEntityNbt(lv2, BlockEntityType.BANNER, lv5);
         return lv2;
      }
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.SHIELD_DECORATION;
   }
}
