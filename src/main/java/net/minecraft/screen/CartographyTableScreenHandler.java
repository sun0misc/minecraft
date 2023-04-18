package net.minecraft.screen;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class CartographyTableScreenHandler extends ScreenHandler {
   public static final int MAP_SLOT_INDEX = 0;
   public static final int MATERIAL_SLOT_INDEX = 1;
   public static final int RESULT_SLOT_INDEX = 2;
   private static final int field_30776 = 3;
   private static final int field_30777 = 30;
   private static final int field_30778 = 30;
   private static final int field_30779 = 39;
   private final ScreenHandlerContext context;
   long lastTakeResultTime;
   public final Inventory inventory;
   private final CraftingResultInventory resultInventory;

   public CartographyTableScreenHandler(int syncId, PlayerInventory inventory) {
      this(syncId, inventory, ScreenHandlerContext.EMPTY);
   }

   public CartographyTableScreenHandler(int syncId, PlayerInventory inventory, final ScreenHandlerContext context) {
      super(ScreenHandlerType.CARTOGRAPHY_TABLE, syncId);
      this.inventory = new SimpleInventory(2) {
         public void markDirty() {
            CartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
         }
      };
      this.resultInventory = new CraftingResultInventory() {
         public void markDirty() {
            CartographyTableScreenHandler.this.onContentChanged(this);
            super.markDirty();
         }
      };
      this.context = context;
      this.addSlot(new Slot(this.inventory, 0, 15, 15) {
         public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.FILLED_MAP);
         }
      });
      this.addSlot(new Slot(this.inventory, 1, 15, 52) {
         public boolean canInsert(ItemStack stack) {
            return stack.isOf(Items.PAPER) || stack.isOf(Items.MAP) || stack.isOf(Items.GLASS_PANE);
         }
      });
      this.addSlot(new Slot(this.resultInventory, 2, 145, 39) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            ((Slot)CartographyTableScreenHandler.this.slots.get(0)).takeStack(1);
            ((Slot)CartographyTableScreenHandler.this.slots.get(1)).takeStack(1);
            stack.getItem().onCraft(stack, player.world, player);
            context.run((world, pos) -> {
               long l = world.getTime();
               if (CartographyTableScreenHandler.this.lastTakeResultTime != l) {
                  world.playSound((PlayerEntity)null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  CartographyTableScreenHandler.this.lastTakeResultTime = l;
               }

            });
            super.onTakeItem(player, stack);
         }
      });

      int j;
      for(j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(inventory, j, 8 + j * 18, 142));
      }

   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.CARTOGRAPHY_TABLE);
   }

   public void onContentChanged(Inventory inventory) {
      ItemStack lv = this.inventory.getStack(0);
      ItemStack lv2 = this.inventory.getStack(1);
      ItemStack lv3 = this.resultInventory.getStack(2);
      if (lv3.isEmpty() || !lv.isEmpty() && !lv2.isEmpty()) {
         if (!lv.isEmpty() && !lv2.isEmpty()) {
            this.updateResult(lv, lv2, lv3);
         }
      } else {
         this.resultInventory.removeStack(2);
      }

   }

   private void updateResult(ItemStack map, ItemStack item, ItemStack oldResult) {
      this.context.run((world, pos) -> {
         MapState lv = FilledMapItem.getMapState(map, world);
         if (lv != null) {
            ItemStack lv2;
            if (item.isOf(Items.PAPER) && !lv.locked && lv.scale < 4) {
               lv2 = map.copyWithCount(1);
               lv2.getOrCreateNbt().putInt("map_scale_direction", 1);
               this.sendContentUpdates();
            } else if (item.isOf(Items.GLASS_PANE) && !lv.locked) {
               lv2 = map.copyWithCount(1);
               lv2.getOrCreateNbt().putBoolean("map_to_lock", true);
               this.sendContentUpdates();
            } else {
               if (!item.isOf(Items.MAP)) {
                  this.resultInventory.removeStack(2);
                  this.sendContentUpdates();
                  return;
               }

               lv2 = map.copyWithCount(2);
               this.sendContentUpdates();
            }

            if (!ItemStack.areEqual(lv2, oldResult)) {
               this.resultInventory.setStack(2, lv2);
               this.sendContentUpdates();
            }

         }
      });
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.resultInventory && super.canInsertIntoSlot(stack, slot);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot == 2) {
            lv3.getItem().onCraft(lv3, player.world, player);
            if (!this.insertItem(lv3, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot != 1 && slot != 0) {
            if (lv3.isOf(Items.FILLED_MAP)) {
               if (!this.insertItem(lv3, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!lv3.isOf(Items.PAPER) && !lv3.isOf(Items.MAP) && !lv3.isOf(Items.GLASS_PANE)) {
               if (slot >= 3 && slot < 30) {
                  if (!this.insertItem(lv3, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.insertItem(lv3, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (lv3.isEmpty()) {
            lv2.setStack(ItemStack.EMPTY);
         }

         lv2.markDirty();
         if (lv3.getCount() == lv.getCount()) {
            return ItemStack.EMPTY;
         }

         lv2.onTakeItem(player, lv3);
         this.sendContentUpdates();
      }

      return lv;
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.resultInventory.removeStack(2);
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.inventory);
      });
   }
}
