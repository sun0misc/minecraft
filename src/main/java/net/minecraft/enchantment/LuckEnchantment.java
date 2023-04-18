package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class LuckEnchantment extends Enchantment {
   protected LuckEnchantment(Enchantment.Rarity arg, EnchantmentTarget arg2, EquipmentSlot... args) {
      super(arg, arg2, args);
   }

   public int getMinPower(int level) {
      return 15 + (level - 1) * 9;
   }

   public int getMaxPower(int level) {
      return super.getMinPower(level) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other) && other != Enchantments.SILK_TOUCH;
   }
}
