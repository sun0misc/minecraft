package net.minecraft.enchantment;

import net.minecraft.util.collection.Weighted;

public class EnchantmentLevelEntry extends Weighted.Absent {
   public final Enchantment enchantment;
   public final int level;

   public EnchantmentLevelEntry(Enchantment enchantment, int level) {
      super(enchantment.getRarity().getWeight());
      this.enchantment = enchantment;
      this.level = level;
   }
}
