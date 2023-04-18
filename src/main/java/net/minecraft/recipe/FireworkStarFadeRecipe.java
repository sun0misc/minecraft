package net.minecraft.recipe;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class FireworkStarFadeRecipe extends SpecialCraftingRecipe {
   private static final Ingredient INPUT_STAR;

   public FireworkStarFadeRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      boolean bl = false;
      boolean bl2 = false;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv = arg.getStack(i);
         if (!lv.isEmpty()) {
            if (lv.getItem() instanceof DyeItem) {
               bl = true;
            } else {
               if (!INPUT_STAR.test(lv)) {
                  return false;
               }

               if (bl2) {
                  return false;
               }

               bl2 = true;
            }
         }
      }

      return bl2 && bl;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      List list = Lists.newArrayList();
      ItemStack lv = null;

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv2 = arg.getStack(i);
         Item lv3 = lv2.getItem();
         if (lv3 instanceof DyeItem) {
            list.add(((DyeItem)lv3).getColor().getFireworkColor());
         } else if (INPUT_STAR.test(lv2)) {
            lv = lv2.copyWithCount(1);
         }
      }

      if (lv != null && !list.isEmpty()) {
         lv.getOrCreateSubNbt("Explosion").putIntArray("FadeColors", (List)list);
         return lv;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.FIREWORK_STAR_FADE;
   }

   static {
      INPUT_STAR = Ingredient.ofItems(Items.FIREWORK_STAR);
   }
}
