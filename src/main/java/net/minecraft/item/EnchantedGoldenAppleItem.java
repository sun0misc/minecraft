package net.minecraft.item;

public class EnchantedGoldenAppleItem extends Item {
   public EnchantedGoldenAppleItem(Item.Settings arg) {
      super(arg);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }
}
