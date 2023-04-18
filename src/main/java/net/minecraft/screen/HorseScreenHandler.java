package net.minecraft.screen;

import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class HorseScreenHandler extends ScreenHandler {
   private final Inventory inventory;
   private final AbstractHorseEntity entity;

   public HorseScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, final AbstractHorseEntity entity) {
      super((ScreenHandlerType)null, syncId);
      this.inventory = inventory;
      this.entity = entity;
      int j = true;
      inventory.onOpen(playerInventory.player);
      int k = true;
      this.addSlot(new Slot(inventory, 0, 8, 18) {
         public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.SADDLE) && !this.hasStack() && entity.canBeSaddled();
         }

         public boolean isEnabled() {
            return entity.canBeSaddled();
         }
      });
      this.addSlot(new Slot(inventory, 1, 8, 36) {
         public boolean canInsert(ItemStack stack) {
            return entity.isHorseArmor(stack);
         }

         public boolean isEnabled() {
            return entity.hasArmorSlot();
         }

         public int getMaxItemCount() {
            return 1;
         }
      });
      int l;
      int m;
      if (this.hasChest(entity)) {
         for(l = 0; l < 3; ++l) {
            for(m = 0; m < ((AbstractDonkeyEntity)entity).getInventoryColumns(); ++m) {
               this.addSlot(new Slot(inventory, 2 + m + l * ((AbstractDonkeyEntity)entity).getInventoryColumns(), 80 + m * 18, 18 + l * 18));
            }
         }
      }

      for(l = 0; l < 3; ++l) {
         for(m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m + l * 9 + 9, 8 + m * 18, 102 + l * 18 + -18));
         }
      }

      for(l = 0; l < 9; ++l) {
         this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return !this.entity.areInventoriesDifferent(this.inventory) && this.inventory.canPlayerUse(player) && this.entity.isAlive() && this.entity.distanceTo(player) < 8.0F;
   }

   private boolean hasChest(AbstractHorseEntity horse) {
      return horse instanceof AbstractDonkeyEntity && ((AbstractDonkeyEntity)horse).hasChest();
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         int j = this.inventory.size();
         if (slot < j) {
            if (!this.insertItem(lv3, j, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(1).canInsert(lv3) && !this.getSlot(1).hasStack()) {
            if (!this.insertItem(lv3, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.getSlot(0).canInsert(lv3)) {
            if (!this.insertItem(lv3, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (j <= 2 || !this.insertItem(lv3, 2, j, false)) {
            int l = j + 27;
            int n = l + 9;
            if (slot >= l && slot < n) {
               if (!this.insertItem(lv3, j, l, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (slot >= j && slot < l) {
               if (!this.insertItem(lv3, l, n, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(lv3, l, l, false)) {
               return ItemStack.EMPTY;
            }

            return ItemStack.EMPTY;
         }

         if (lv3.isEmpty()) {
            lv2.setStack(ItemStack.EMPTY);
         } else {
            lv2.markDirty();
         }
      }

      return lv;
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.inventory.onClose(player);
   }
}
