package net.minecraft.screen;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public abstract class ForgingScreenHandler extends ScreenHandler {
   private static final int field_41901 = 9;
   private static final int field_41902 = 3;
   protected final ScreenHandlerContext context;
   protected final PlayerEntity player;
   protected final Inventory input;
   private final List inputSlotIndices;
   protected final CraftingResultInventory output = new CraftingResultInventory();
   private final int resultSlotIndex;

   protected abstract boolean canTakeOutput(PlayerEntity player, boolean present);

   protected abstract void onTakeOutput(PlayerEntity player, ItemStack stack);

   protected abstract boolean canUse(BlockState state);

   public ForgingScreenHandler(@Nullable ScreenHandlerType type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
      super(type, syncId);
      this.context = context;
      this.player = playerInventory.player;
      ForgingSlotsManager lv = this.getForgingSlotsManager();
      this.input = this.createInputInventory(lv.getInputSlotCount());
      this.inputSlotIndices = lv.getInputSlotIndices();
      this.resultSlotIndex = lv.getResultSlotIndex();
      this.addInputSlots(lv);
      this.addResultSlot(lv);
      this.addPlayerInventorySlots(playerInventory);
   }

   private void addInputSlots(ForgingSlotsManager forgingSlotsManager) {
      Iterator var2 = forgingSlotsManager.getInputSlots().iterator();

      while(var2.hasNext()) {
         final ForgingSlotsManager.ForgingSlot lv = (ForgingSlotsManager.ForgingSlot)var2.next();
         this.addSlot(new Slot(this.input, lv.slotId(), lv.x(), lv.y()) {
            public boolean canInsert(ItemStack stack) {
               return lv.mayPlace().test(stack);
            }
         });
      }

   }

   private void addResultSlot(ForgingSlotsManager forgingSlotsManager) {
      this.addSlot(new Slot(this.output, forgingSlotsManager.getResultSlot().slotId(), forgingSlotsManager.getResultSlot().x(), forgingSlotsManager.getResultSlot().y()) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public boolean canTakeItems(PlayerEntity playerEntity) {
            return ForgingScreenHandler.this.canTakeOutput(playerEntity, this.hasStack());
         }

         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            ForgingScreenHandler.this.onTakeOutput(player, stack);
         }
      });
   }

   private void addPlayerInventorySlots(PlayerInventory playerInventory) {
      int i;
      for(i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(i = 0; i < 9; ++i) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
      }

   }

   public abstract void updateResult();

   protected abstract ForgingSlotsManager getForgingSlotsManager();

   private SimpleInventory createInputInventory(int size) {
      return new SimpleInventory(size) {
         public void markDirty() {
            super.markDirty();
            ForgingScreenHandler.this.onContentChanged(this);
         }
      };
   }

   public void onContentChanged(Inventory inventory) {
      super.onContentChanged(inventory);
      if (inventory == this.input) {
         this.updateResult();
      }

   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.input);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return (Boolean)this.context.get((world, pos) -> {
         return !this.canUse(world.getBlockState(pos)) ? false : player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 64.0;
      }, true);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         int j = this.getPlayerInventoryStartIndex();
         int k = this.getPlayerHotbarEndIndex();
         if (slot == this.getResultSlotIndex()) {
            if (!this.insertItem(lv3, j, k, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (this.inputSlotIndices.contains(slot)) {
            if (!this.insertItem(lv3, j, k, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.isValidIngredient(lv3) && slot >= this.getPlayerInventoryStartIndex() && slot < this.getPlayerHotbarEndIndex()) {
            int l = this.getSlotFor(lv);
            if (!this.insertItem(lv3, l, this.getResultSlotIndex(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= this.getPlayerInventoryStartIndex() && slot < this.getPlayerInventoryEndIndex()) {
            if (!this.insertItem(lv3, this.getPlayerHotbarStartIndex(), this.getPlayerHotbarEndIndex(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= this.getPlayerHotbarStartIndex() && slot < this.getPlayerHotbarEndIndex() && !this.insertItem(lv3, this.getPlayerInventoryStartIndex(), this.getPlayerInventoryEndIndex(), false)) {
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

   protected boolean isValidIngredient(ItemStack stack) {
      return true;
   }

   public int getSlotFor(ItemStack stack) {
      return this.input.isEmpty() ? 0 : (Integer)this.inputSlotIndices.get(0);
   }

   public int getResultSlotIndex() {
      return this.resultSlotIndex;
   }

   private int getPlayerInventoryStartIndex() {
      return this.getResultSlotIndex() + 1;
   }

   private int getPlayerInventoryEndIndex() {
      return this.getPlayerInventoryStartIndex() + 27;
   }

   private int getPlayerHotbarStartIndex() {
      return this.getPlayerInventoryEndIndex();
   }

   private int getPlayerHotbarEndIndex() {
      return this.getPlayerHotbarStartIndex() + 9;
   }
}
