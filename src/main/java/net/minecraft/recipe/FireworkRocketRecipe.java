package net.minecraft.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class FireworkRocketRecipe extends SpecialCraftingRecipe {
   private static final Ingredient PAPER;
   private static final Ingredient DURATION_MODIFIER;
   private static final Ingredient FIREWORK_STAR;

   public FireworkRocketRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      boolean bl = false;
      int i = 0;

      for(int j = 0; j < arg.size(); ++j) {
         ItemStack lv = arg.getStack(j);
         if (!lv.isEmpty()) {
            if (PAPER.test(lv)) {
               if (bl) {
                  return false;
               }

               bl = true;
            } else if (DURATION_MODIFIER.test(lv)) {
               ++i;
               if (i > 3) {
                  return false;
               }
            } else if (!FIREWORK_STAR.test(lv)) {
               return false;
            }
         }
      }

      return bl && i >= 1;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      ItemStack lv = new ItemStack(Items.FIREWORK_ROCKET, 3);
      NbtCompound lv2 = lv.getOrCreateSubNbt("Fireworks");
      NbtList lv3 = new NbtList();
      int i = 0;

      for(int j = 0; j < arg.size(); ++j) {
         ItemStack lv4 = arg.getStack(j);
         if (!lv4.isEmpty()) {
            if (DURATION_MODIFIER.test(lv4)) {
               ++i;
            } else if (FIREWORK_STAR.test(lv4)) {
               NbtCompound lv5 = lv4.getSubNbt("Explosion");
               if (lv5 != null) {
                  lv3.add(lv5);
               }
            }
         }
      }

      lv2.putByte("Flight", (byte)i);
      if (!lv3.isEmpty()) {
         lv2.put("Explosions", lv3);
      }

      return lv;
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public ItemStack getOutput(DynamicRegistryManager registryManager) {
      return new ItemStack(Items.FIREWORK_ROCKET);
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.FIREWORK_ROCKET;
   }

   static {
      PAPER = Ingredient.ofItems(Items.PAPER);
      DURATION_MODIFIER = Ingredient.ofItems(Items.GUNPOWDER);
      FIREWORK_STAR = Ingredient.ofItems(Items.FIREWORK_STAR);
   }
}
