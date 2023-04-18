package net.minecraft.screen;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BeaconScreenHandler extends ScreenHandler {
   private static final int field_30756 = 0;
   private static final int field_30757 = 1;
   private static final int field_30758 = 3;
   private static final int field_30759 = 1;
   private static final int field_30760 = 28;
   private static final int field_30761 = 28;
   private static final int field_30762 = 37;
   private final Inventory payment;
   private final PaymentSlot paymentSlot;
   private final ScreenHandlerContext context;
   private final PropertyDelegate propertyDelegate;

   public BeaconScreenHandler(int syncId, Inventory inventory) {
      this(syncId, inventory, new ArrayPropertyDelegate(3), ScreenHandlerContext.EMPTY);
   }

   public BeaconScreenHandler(int syncId, Inventory inventory, PropertyDelegate propertyDelegate, ScreenHandlerContext context) {
      super(ScreenHandlerType.BEACON, syncId);
      this.payment = new SimpleInventory(1) {
         public boolean isValid(int slot, ItemStack stack) {
            return stack.isIn(ItemTags.BEACON_PAYMENT_ITEMS);
         }

         public int getMaxCountPerStack() {
            return 1;
         }
      };
      checkDataCount(propertyDelegate, 3);
      this.propertyDelegate = propertyDelegate;
      this.context = context;
      this.paymentSlot = new PaymentSlot(this.payment, 0, 136, 110);
      this.addSlot(this.paymentSlot);
      this.addProperties(propertyDelegate);
      int j = true;
      int k = true;

      int l;
      for(l = 0; l < 3; ++l) {
         for(int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m + l * 9 + 9, 36 + m * 18, 137 + l * 18));
         }
      }

      for(l = 0; l < 9; ++l) {
         this.addSlot(new Slot(inventory, l, 36 + l * 18, 195));
      }

   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      if (!player.world.isClient) {
         ItemStack lv = this.paymentSlot.takeStack(this.paymentSlot.getMaxItemCount());
         if (!lv.isEmpty()) {
            player.dropItem(lv, false);
         }

      }
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.BEACON);
   }

   public void setProperty(int id, int value) {
      super.setProperty(id, value);
      this.sendContentUpdates();
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot == 0) {
            if (!this.insertItem(lv3, 1, 37, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (!this.paymentSlot.hasStack() && this.paymentSlot.canInsert(lv3) && lv3.getCount() == 1) {
            if (!this.insertItem(lv3, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 1 && slot < 28) {
            if (!this.insertItem(lv3, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 28 && slot < 37) {
            if (!this.insertItem(lv3, 1, 28, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 1, 37, false)) {
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

   public int getProperties() {
      return this.propertyDelegate.get(0);
   }

   @Nullable
   public StatusEffect getPrimaryEffect() {
      return StatusEffect.byRawId(this.propertyDelegate.get(1));
   }

   @Nullable
   public StatusEffect getSecondaryEffect() {
      return StatusEffect.byRawId(this.propertyDelegate.get(2));
   }

   public void setEffects(Optional primary, Optional secondary) {
      if (this.paymentSlot.hasStack()) {
         this.propertyDelegate.set(1, (Integer)primary.map(StatusEffect::getRawId).orElse(-1));
         this.propertyDelegate.set(2, (Integer)secondary.map(StatusEffect::getRawId).orElse(-1));
         this.paymentSlot.takeStack(1);
         this.context.run(World::markDirty);
      }

   }

   public boolean hasPayment() {
      return !this.payment.getStack(0).isEmpty();
   }

   class PaymentSlot extends Slot {
      public PaymentSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean canInsert(ItemStack stack) {
         return stack.isIn(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      public int getMaxItemCount() {
         return 1;
      }
   }
}
