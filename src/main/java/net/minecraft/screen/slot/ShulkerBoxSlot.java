package net.minecraft.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class ShulkerBoxSlot extends Slot {
   public ShulkerBoxSlot(Inventory arg, int i, int j, int k) {
      super(arg, i, j, k);
   }

   public boolean canInsert(ItemStack stack) {
      return stack.getItem().canBeNested();
   }
}
