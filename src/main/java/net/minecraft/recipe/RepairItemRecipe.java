package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RepairItemRecipe extends SpecialCraftingRecipe {
   public RepairItemRecipe(Identifier arg, CraftingRecipeCategory arg2) {
      super(arg, arg2);
   }

   public boolean matches(CraftingInventory arg, World arg2) {
      List list = Lists.newArrayList();

      for(int i = 0; i < arg.size(); ++i) {
         ItemStack lv = arg.getStack(i);
         if (!lv.isEmpty()) {
            list.add(lv);
            if (list.size() > 1) {
               ItemStack lv2 = (ItemStack)list.get(0);
               if (!lv.isOf(lv2.getItem()) || lv2.getCount() != 1 || lv.getCount() != 1 || !lv2.getItem().isDamageable()) {
                  return false;
               }
            }
         }
      }

      return list.size() == 2;
   }

   public ItemStack craft(CraftingInventory arg, DynamicRegistryManager arg2) {
      List list = Lists.newArrayList();

      ItemStack lv;
      for(int i = 0; i < arg.size(); ++i) {
         lv = arg.getStack(i);
         if (!lv.isEmpty()) {
            list.add(lv);
            if (list.size() > 1) {
               ItemStack lv2 = (ItemStack)list.get(0);
               if (!lv.isOf(lv2.getItem()) || lv2.getCount() != 1 || lv.getCount() != 1 || !lv2.getItem().isDamageable()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      if (list.size() == 2) {
         ItemStack lv3 = (ItemStack)list.get(0);
         lv = (ItemStack)list.get(1);
         if (lv3.isOf(lv.getItem()) && lv3.getCount() == 1 && lv.getCount() == 1 && lv3.getItem().isDamageable()) {
            Item lv4 = lv3.getItem();
            int j = lv4.getMaxDamage() - lv3.getDamage();
            int k = lv4.getMaxDamage() - lv.getDamage();
            int l = j + k + lv4.getMaxDamage() * 5 / 100;
            int m = lv4.getMaxDamage() - l;
            if (m < 0) {
               m = 0;
            }

            ItemStack lv5 = new ItemStack(lv3.getItem());
            lv5.setDamage(m);
            Map map = Maps.newHashMap();
            Map map2 = EnchantmentHelper.get(lv3);
            Map map3 = EnchantmentHelper.get(lv);
            Registries.ENCHANTMENT.stream().filter(Enchantment::isCursed).forEach((enchantment) -> {
               int i = Math.max((Integer)map2.getOrDefault(enchantment, 0), (Integer)map3.getOrDefault(enchantment, 0));
               if (i > 0) {
                  map.put(enchantment, i);
               }

            });
            if (!map.isEmpty()) {
               EnchantmentHelper.set(map, lv5);
            }

            return lv5;
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean fits(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer getSerializer() {
      return RecipeSerializer.REPAIR_ITEM;
   }
}
