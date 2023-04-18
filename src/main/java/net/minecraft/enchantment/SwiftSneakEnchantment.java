package net.minecraft.enchantment;

import net.minecraft.entity.EquipmentSlot;

public class SwiftSneakEnchantment extends Enchantment {
   public SwiftSneakEnchantment(Enchantment.Rarity rarity, EquipmentSlot... slots) {
      super(rarity, EnchantmentTarget.ARMOR_LEGS, slots);
   }

   public int getMinPower(int level) {
      return level * 25;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 50;
   }

   public boolean isTreasure() {
      return true;
   }

   public boolean isAvailableForEnchantedBookOffer() {
      return false;
   }

   public boolean isAvailableForRandomSelection() {
      return false;
   }

   public int getMaxLevel() {
      return 3;
   }
}
