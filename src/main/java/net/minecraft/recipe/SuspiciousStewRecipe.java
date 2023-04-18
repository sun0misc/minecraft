package net.minecraft.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SuspiciousStewRecipe extends SpecialCraftingRecipe {
   public SuspiciousStewRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      boolean bl = false;
      boolean bl2 = false;
      boolean bl3 = false;
      boolean bl4 = false;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv = arg.getStack(i);
         if (!lv.isEmpty()) {
            if (lv.isOf(Blocks.BROWN_MUSHROOM.asItem()) && !bl3) {
               bl3 = true;
            } else if (lv.isOf(Blocks.RED_MUSHROOM.asItem()) && !bl2) {
               bl2 = true;
            } else if (lv.isIn(ItemTags.SMALL_FLOWERS) && !bl) {
               bl = true;
            } else {
               if (!lv.isOf(Items.BOWL) || bl4) {
                  return false;
               }

               bl4 = true;
            }
         }
      }

      return bl && bl3 && bl2 && bl4;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      ItemStack lv = new ItemStack(Items.SUSPICIOUS_STEW, 1);

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv2 = arg.getStack(i);
         if (!lv2.isEmpty()) {
            SuspiciousStewIngredient lv3 = SuspiciousStewIngredient.of(lv2.getItem());
            if (lv3 != null) {
               SuspiciousStewItem.addEffectToStew(lv, lv3.getEffectInStew(), lv3.getEffectInStewDuration());
               break;
            }
         }
      }

      return lv;
   }

   public boolean fits(int width, int height) {
      return width >= 2 && height >= 2;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.SUSPICIOUS_STEW;
   }
}
