package net.minecraft.recipe;

import java.util.List;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CraftingDecoratedPotRecipe extends SpecialCraftingRecipe {
   public CraftingDecoratedPotRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      if (!this.fits(arg.getWidth(), arg.getHeight())) {
         return false;
      } else {
         for(int i = 0; i < arg.size(); ++i) {
            ItemStack lv = arg.getStack(i);
            switch (i) {
               case 1:
               case 3:
               case 5:
               case 7:
                  if (!lv.isIn(ItemTags.DECORATED_POT_SHARDS)) {
                     return false;
                  }
                  break;
               case 2:
               case 4:
               case 6:
               default:
                  if (!lv.isOf(Items.AIR)) {
                     return false;
                  }
            }
         }

         return true;
      }
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      ItemStack lv = Items.DECORATED_POT.getDefaultStack();
      NbtCompound lv2 = new NbtCompound();
      DecoratedPotBlockEntity.writeShardsToNbt(List.of(arg.getStack(1).getItem(), arg.getStack(3).getItem(), arg.getStack(5).getItem(), arg.getStack(7).getItem()), lv2);
      BlockItem.setBlockEntityNbt(lv, BlockEntityType.DECORATED_POT, lv2);
      return lv;
   }

   public boolean fits(int width, int height) {
      return width == 3 && height == 3;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.CRAFTING_DECORATED_POT;
   }
}
