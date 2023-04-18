package net.minecraft.inventory;

import net.minecraft.item.ItemStack;

public interface SingleStackInventory extends Inventory {
   default int size() {
      return 1;
   }

   default boolean isEmpty() {
      return this.getStack().isEmpty();
   }

   default void clear() {
      this.removeStack();
   }

   default ItemStack getStack() {
      return this.getStack(0);
   }

   default ItemStack removeStack() {
      return this.removeStack(0);
   }

   default void setStack(ItemStack stack) {
      this.setStack(0, stack);
   }

   default ItemStack removeStack(int slot) {
      return this.removeStack(slot, this.getMaxCountPerStack());
   }
}
