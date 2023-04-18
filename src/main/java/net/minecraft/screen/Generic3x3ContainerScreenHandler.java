package net.minecraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class Generic3x3ContainerScreenHandler extends ScreenHandler {
   private static final int field_30788 = 9;
   private static final int field_30789 = 9;
   private static final int field_30790 = 36;
   private static final int field_30791 = 36;
   private static final int field_30792 = 45;
   private final Inventory inventory;

   public Generic3x3ContainerScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleInventory(9));
   }

   public Generic3x3ContainerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
      super(ScreenHandlerType.GENERIC_3X3, syncId);
      checkSize(inventory, 9);
      this.inventory = inventory;
      inventory.onOpen(playerInventory.player);

      int j;
      int k;
      for(j = 0; j < 3; ++j) {
         for(k = 0; k < 3; ++k) {
            this.addSlot(new Slot(inventory, k + j * 3, 62 + k * 18, 17 + j * 18));
         }
      }

      for(j = 0; j < 3; ++j) {
         for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot < 9) {
            if (!this.insertItem(lv3, 9, 45, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 0, 9, false)) {
            return ItemStack.EMPTY;
         }

         if (lv3.isEmpty()) {
            lv2.setStack(ItemStack.EMPTY);
         } else {
            lv2.markDirty();
         }

         if (lv3.getCount() == lv.getCount()) {
            return ItemStack.EMPTY;
         }

         lv2.onTakeItem(player, lv3);
      }

      return lv;
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.inventory.onClose(player);
   }
}
