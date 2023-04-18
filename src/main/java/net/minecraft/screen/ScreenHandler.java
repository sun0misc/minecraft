package net.minecraft.screen;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ScreenHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int EMPTY_SPACE_SLOT_INDEX = -999;
   public static final int field_30731 = 0;
   public static final int field_30732 = 1;
   public static final int field_30733 = 2;
   public static final int field_30734 = 0;
   public static final int field_30735 = 1;
   public static final int field_30736 = 2;
   public static final int field_30737 = Integer.MAX_VALUE;
   private final DefaultedList trackedStacks = DefaultedList.of();
   public final DefaultedList slots = DefaultedList.of();
   private final List properties = Lists.newArrayList();
   private ItemStack cursorStack;
   private final DefaultedList previousTrackedStacks;
   private final IntList trackedPropertyValues;
   private ItemStack previousCursorStack;
   private int revision;
   @Nullable
   private final ScreenHandlerType type;
   public final int syncId;
   private int quickCraftButton;
   private int quickCraftStage;
   private final Set quickCraftSlots;
   private final List listeners;
   @Nullable
   private ScreenHandlerSyncHandler syncHandler;
   private boolean disableSync;

   protected ScreenHandler(@Nullable ScreenHandlerType type, int syncId) {
      this.cursorStack = ItemStack.EMPTY;
      this.previousTrackedStacks = DefaultedList.of();
      this.trackedPropertyValues = new IntArrayList();
      this.previousCursorStack = ItemStack.EMPTY;
      this.quickCraftButton = -1;
      this.quickCraftSlots = Sets.newHashSet();
      this.listeners = Lists.newArrayList();
      this.type = type;
      this.syncId = syncId;
   }

   protected static boolean canUse(ScreenHandlerContext context, PlayerEntity player, Block block) {
      return (Boolean)context.get((world, pos) -> {
         return !world.getBlockState(pos).isOf(block) ? false : player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 64.0;
      }, true);
   }

   public ScreenHandlerType getType() {
      if (this.type == null) {
         throw new UnsupportedOperationException("Unable to construct this menu by type");
      } else {
         return this.type;
      }
   }

   protected static void checkSize(Inventory inventory, int expectedSize) {
      int j = inventory.size();
      if (j < expectedSize) {
         throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + expectedSize);
      }
   }

   protected static void checkDataCount(PropertyDelegate data, int expectedCount) {
      int j = data.size();
      if (j < expectedCount) {
         throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + expectedCount);
      }
   }

   public boolean isValid(int slot) {
      return slot == -1 || slot == -999 || slot < this.slots.size();
   }

   protected Slot addSlot(Slot slot) {
      slot.id = this.slots.size();
      this.slots.add(slot);
      this.trackedStacks.add(ItemStack.EMPTY);
      this.previousTrackedStacks.add(ItemStack.EMPTY);
      return slot;
   }

   protected Property addProperty(Property property) {
      this.properties.add(property);
      this.trackedPropertyValues.add(0);
      return property;
   }

   protected void addProperties(PropertyDelegate propertyDelegate) {
      for(int i = 0; i < propertyDelegate.size(); ++i) {
         this.addProperty(Property.create(propertyDelegate, i));
      }

   }

   public void addListener(ScreenHandlerListener listener) {
      if (!this.listeners.contains(listener)) {
         this.listeners.add(listener);
         this.sendContentUpdates();
      }
   }

   public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
      this.syncHandler = handler;
      this.syncState();
   }

   public void syncState() {
      int i = 0;

      int j;
      for(j = this.slots.size(); i < j; ++i) {
         this.previousTrackedStacks.set(i, ((Slot)this.slots.get(i)).getStack().copy());
      }

      this.previousCursorStack = this.getCursorStack().copy();
      i = 0;

      for(j = this.properties.size(); i < j; ++i) {
         this.trackedPropertyValues.set(i, ((Property)this.properties.get(i)).get());
      }

      if (this.syncHandler != null) {
         this.syncHandler.updateState(this, this.previousTrackedStacks, this.previousCursorStack, this.trackedPropertyValues.toIntArray());
      }

   }

   public void removeListener(ScreenHandlerListener listener) {
      this.listeners.remove(listener);
   }

   public DefaultedList getStacks() {
      DefaultedList lv = DefaultedList.of();
      Iterator var2 = this.slots.iterator();

      while(var2.hasNext()) {
         Slot lv2 = (Slot)var2.next();
         lv.add(lv2.getStack());
      }

      return lv;
   }

   public void sendContentUpdates() {
      int i;
      for(i = 0; i < this.slots.size(); ++i) {
         ItemStack lv = ((Slot)this.slots.get(i)).getStack();
         Objects.requireNonNull(lv);
         Supplier supplier = Suppliers.memoize(lv::copy);
         this.updateTrackedSlot(i, lv, supplier);
         this.checkSlotUpdates(i, lv, supplier);
      }

      this.checkCursorStackUpdates();

      for(i = 0; i < this.properties.size(); ++i) {
         Property lv2 = (Property)this.properties.get(i);
         int j = lv2.get();
         if (lv2.hasChanged()) {
            this.notifyPropertyUpdate(i, j);
         }

         this.checkPropertyUpdates(i, j);
      }

   }

   public void updateToClient() {
      int i;
      for(i = 0; i < this.slots.size(); ++i) {
         ItemStack lv = ((Slot)this.slots.get(i)).getStack();
         Objects.requireNonNull(lv);
         this.updateTrackedSlot(i, lv, lv::copy);
      }

      for(i = 0; i < this.properties.size(); ++i) {
         Property lv2 = (Property)this.properties.get(i);
         if (lv2.hasChanged()) {
            this.notifyPropertyUpdate(i, lv2.get());
         }
      }

      this.syncState();
   }

   private void notifyPropertyUpdate(int index, int value) {
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         ScreenHandlerListener lv = (ScreenHandlerListener)var3.next();
         lv.onPropertyUpdate(this, index, value);
      }

   }

   private void updateTrackedSlot(int slot, ItemStack stack, Supplier copySupplier) {
      ItemStack lv = (ItemStack)this.trackedStacks.get(slot);
      if (!ItemStack.areEqual(lv, stack)) {
         ItemStack lv2 = (ItemStack)copySupplier.get();
         this.trackedStacks.set(slot, lv2);
         Iterator var6 = this.listeners.iterator();

         while(var6.hasNext()) {
            ScreenHandlerListener lv3 = (ScreenHandlerListener)var6.next();
            lv3.onSlotUpdate(this, slot, lv2);
         }
      }

   }

   private void checkSlotUpdates(int slot, ItemStack stack, Supplier copySupplier) {
      if (!this.disableSync) {
         ItemStack lv = (ItemStack)this.previousTrackedStacks.get(slot);
         if (!ItemStack.areEqual(lv, stack)) {
            ItemStack lv2 = (ItemStack)copySupplier.get();
            this.previousTrackedStacks.set(slot, lv2);
            if (this.syncHandler != null) {
               this.syncHandler.updateSlot(this, slot, lv2);
            }
         }

      }
   }

   private void checkPropertyUpdates(int id, int value) {
      if (!this.disableSync) {
         int k = this.trackedPropertyValues.getInt(id);
         if (k != value) {
            this.trackedPropertyValues.set(id, value);
            if (this.syncHandler != null) {
               this.syncHandler.updateProperty(this, id, value);
            }
         }

      }
   }

   private void checkCursorStackUpdates() {
      if (!this.disableSync) {
         if (!ItemStack.areEqual(this.getCursorStack(), this.previousCursorStack)) {
            this.previousCursorStack = this.getCursorStack().copy();
            if (this.syncHandler != null) {
               this.syncHandler.updateCursorStack(this, this.previousCursorStack);
            }
         }

      }
   }

   public void setPreviousTrackedSlot(int slot, ItemStack stack) {
      this.previousTrackedStacks.set(slot, stack.copy());
   }

   public void setPreviousTrackedSlotMutable(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.previousTrackedStacks.size()) {
         this.previousTrackedStacks.set(slot, stack);
      } else {
         LOGGER.debug("Incorrect slot index: {} available slots: {}", slot, this.previousTrackedStacks.size());
      }
   }

   public void setPreviousCursorStack(ItemStack stack) {
      this.previousCursorStack = stack.copy();
   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      return false;
   }

   public Slot getSlot(int index) {
      return (Slot)this.slots.get(index);
   }

   public abstract ItemStack quickMove(PlayerEntity player, int slot);

   public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
      try {
         this.internalOnSlotClick(slotIndex, button, actionType, player);
      } catch (Exception var8) {
         CrashReport lv = CrashReport.create(var8, "Container click");
         CrashReportSection lv2 = lv.addElement("Click info");
         lv2.add("Menu Type", () -> {
            return this.type != null ? Registries.SCREEN_HANDLER.getId(this.type).toString() : "<no type>";
         });
         lv2.add("Menu Class", () -> {
            return this.getClass().getCanonicalName();
         });
         lv2.add("Slot Count", (Object)this.slots.size());
         lv2.add("Slot", (Object)slotIndex);
         lv2.add("Button", (Object)button);
         lv2.add("Type", (Object)actionType);
         throw new CrashException(lv);
      }
   }

   private void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
      PlayerInventory lv = player.getInventory();
      Slot lv2;
      ItemStack lv3;
      ItemStack lv4;
      int l;
      int m;
      if (actionType == SlotActionType.QUICK_CRAFT) {
         int k = this.quickCraftStage;
         this.quickCraftStage = unpackQuickCraftStage(button);
         if ((k != 1 || this.quickCraftStage != 2) && k != this.quickCraftStage) {
            this.endQuickCraft();
         } else if (this.getCursorStack().isEmpty()) {
            this.endQuickCraft();
         } else if (this.quickCraftStage == 0) {
            this.quickCraftButton = unpackQuickCraftButton(button);
            if (shouldQuickCraftContinue(this.quickCraftButton, player)) {
               this.quickCraftStage = 1;
               this.quickCraftSlots.clear();
            } else {
               this.endQuickCraft();
            }
         } else if (this.quickCraftStage == 1) {
            lv2 = (Slot)this.slots.get(slotIndex);
            lv3 = this.getCursorStack();
            if (canInsertItemIntoSlot(lv2, lv3, true) && lv2.canInsert(lv3) && (this.quickCraftButton == 2 || lv3.getCount() > this.quickCraftSlots.size()) && this.canInsertIntoSlot(lv2)) {
               this.quickCraftSlots.add(lv2);
            }
         } else if (this.quickCraftStage == 2) {
            if (!this.quickCraftSlots.isEmpty()) {
               if (this.quickCraftSlots.size() == 1) {
                  l = ((Slot)this.quickCraftSlots.iterator().next()).id;
                  this.endQuickCraft();
                  this.internalOnSlotClick(l, this.quickCraftButton, SlotActionType.PICKUP, player);
                  return;
               }

               lv4 = this.getCursorStack().copy();
               if (lv4.isEmpty()) {
                  this.endQuickCraft();
                  return;
               }

               m = this.getCursorStack().getCount();
               Iterator var9 = this.quickCraftSlots.iterator();

               label303:
               while(true) {
                  Slot lv5;
                  ItemStack lv6;
                  do {
                     do {
                        do {
                           do {
                              if (!var9.hasNext()) {
                                 lv4.setCount(m);
                                 this.setCursorStack(lv4);
                                 break label303;
                              }

                              lv5 = (Slot)var9.next();
                              lv6 = this.getCursorStack();
                           } while(lv5 == null);
                        } while(!canInsertItemIntoSlot(lv5, lv6, true));
                     } while(!lv5.canInsert(lv6));
                  } while(this.quickCraftButton != 2 && lv6.getCount() < this.quickCraftSlots.size());

                  if (this.canInsertIntoSlot(lv5)) {
                     int n = lv5.hasStack() ? lv5.getStack().getCount() : 0;
                     int o = Math.min(lv4.getMaxCount(), lv5.getMaxItemCount(lv4));
                     int p = Math.min(calculateStackSize(this.quickCraftSlots, this.quickCraftButton, lv4) + n, o);
                     m -= p - n;
                     lv5.setStack(lv4.copyWithCount(p));
                  }
               }
            }

            this.endQuickCraft();
         } else {
            this.endQuickCraft();
         }
      } else if (this.quickCraftStage != 0) {
         this.endQuickCraft();
      } else {
         int q;
         if ((actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE) && (button == 0 || button == 1)) {
            ClickType lv7 = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
            if (slotIndex == EMPTY_SPACE_SLOT_INDEX) {
               if (!this.getCursorStack().isEmpty()) {
                  if (lv7 == ClickType.LEFT) {
                     player.dropItem(this.getCursorStack(), true);
                     this.setCursorStack(ItemStack.EMPTY);
                  } else {
                     player.dropItem(this.getCursorStack().split(1), true);
                  }
               }
            } else if (actionType == SlotActionType.QUICK_MOVE) {
               if (slotIndex < 0) {
                  return;
               }

               lv2 = (Slot)this.slots.get(slotIndex);
               if (!lv2.canTakeItems(player)) {
                  return;
               }

               for(lv3 = this.quickMove(player, slotIndex); !lv3.isEmpty() && ItemStack.areItemsEqual(lv2.getStack(), lv3); lv3 = this.quickMove(player, slotIndex)) {
               }
            } else {
               if (slotIndex < 0) {
                  return;
               }

               lv2 = (Slot)this.slots.get(slotIndex);
               lv3 = lv2.getStack();
               ItemStack lv8 = this.getCursorStack();
               player.onPickupSlotClick(lv8, lv2.getStack(), lv7);
               if (!this.handleSlotClick(player, lv7, lv2, lv3, lv8)) {
                  if (lv3.isEmpty()) {
                     if (!lv8.isEmpty()) {
                        q = lv7 == ClickType.LEFT ? lv8.getCount() : 1;
                        this.setCursorStack(lv2.insertStack(lv8, q));
                     }
                  } else if (lv2.canTakeItems(player)) {
                     if (lv8.isEmpty()) {
                        q = lv7 == ClickType.LEFT ? lv3.getCount() : (lv3.getCount() + 1) / 2;
                        Optional optional = lv2.tryTakeStackRange(q, Integer.MAX_VALUE, player);
                        optional.ifPresent((stack) -> {
                           this.setCursorStack(stack);
                           lv2.onTakeItem(player, stack);
                        });
                     } else if (lv2.canInsert(lv8)) {
                        if (ItemStack.canCombine(lv3, lv8)) {
                           q = lv7 == ClickType.LEFT ? lv8.getCount() : 1;
                           this.setCursorStack(lv2.insertStack(lv8, q));
                        } else if (lv8.getCount() <= lv2.getMaxItemCount(lv8)) {
                           this.setCursorStack(lv3);
                           lv2.setStack(lv8);
                        }
                     } else if (ItemStack.canCombine(lv3, lv8)) {
                        Optional optional2 = lv2.tryTakeStackRange(lv3.getCount(), lv8.getMaxCount() - lv8.getCount(), player);
                        optional2.ifPresent((stack) -> {
                           lv8.increment(stack.getCount());
                           lv2.onTakeItem(player, stack);
                        });
                     }
                  }
               }

               lv2.markDirty();
            }
         } else {
            Slot lv9;
            int r;
            if (actionType == SlotActionType.SWAP) {
               lv9 = (Slot)this.slots.get(slotIndex);
               lv4 = lv.getStack(button);
               lv3 = lv9.getStack();
               if (!lv4.isEmpty() || !lv3.isEmpty()) {
                  if (lv4.isEmpty()) {
                     if (lv9.canTakeItems(player)) {
                        lv.setStack(button, lv3);
                        lv9.onTake(lv3.getCount());
                        lv9.setStack(ItemStack.EMPTY);
                        lv9.onTakeItem(player, lv3);
                     }
                  } else if (lv3.isEmpty()) {
                     if (lv9.canInsert(lv4)) {
                        r = lv9.getMaxItemCount(lv4);
                        if (lv4.getCount() > r) {
                           lv9.setStack(lv4.split(r));
                        } else {
                           lv.setStack(button, ItemStack.EMPTY);
                           lv9.setStack(lv4);
                        }
                     }
                  } else if (lv9.canTakeItems(player) && lv9.canInsert(lv4)) {
                     r = lv9.getMaxItemCount(lv4);
                     if (lv4.getCount() > r) {
                        lv9.setStack(lv4.split(r));
                        lv9.onTakeItem(player, lv3);
                        if (!lv.insertStack(lv3)) {
                           player.dropItem(lv3, true);
                        }
                     } else {
                        lv.setStack(button, lv3);
                        lv9.setStack(lv4);
                        lv9.onTakeItem(player, lv3);
                     }
                  }
               }
            } else if (actionType == SlotActionType.CLONE && player.getAbilities().creativeMode && this.getCursorStack().isEmpty() && slotIndex >= 0) {
               lv9 = (Slot)this.slots.get(slotIndex);
               if (lv9.hasStack()) {
                  lv4 = lv9.getStack();
                  this.setCursorStack(lv4.copyWithCount(lv4.getMaxCount()));
               }
            } else if (actionType == SlotActionType.THROW && this.getCursorStack().isEmpty() && slotIndex >= 0) {
               lv9 = (Slot)this.slots.get(slotIndex);
               l = button == 0 ? 1 : lv9.getStack().getCount();
               lv3 = lv9.takeStackRange(l, Integer.MAX_VALUE, player);
               player.dropItem(lv3, true);
            } else if (actionType == SlotActionType.PICKUP_ALL && slotIndex >= 0) {
               lv9 = (Slot)this.slots.get(slotIndex);
               lv4 = this.getCursorStack();
               if (!lv4.isEmpty() && (!lv9.hasStack() || !lv9.canTakeItems(player))) {
                  m = button == 0 ? 0 : this.slots.size() - 1;
                  r = button == 0 ? 1 : -1;

                  for(q = 0; q < 2; ++q) {
                     for(int s = m; s >= 0 && s < this.slots.size() && lv4.getCount() < lv4.getMaxCount(); s += r) {
                        Slot lv10 = (Slot)this.slots.get(s);
                        if (lv10.hasStack() && canInsertItemIntoSlot(lv10, lv4, true) && lv10.canTakeItems(player) && this.canInsertIntoSlot(lv4, lv10)) {
                           ItemStack lv11 = lv10.getStack();
                           if (q != 0 || lv11.getCount() != lv11.getMaxCount()) {
                              ItemStack lv12 = lv10.takeStackRange(lv11.getCount(), lv4.getMaxCount() - lv4.getCount(), player);
                              lv4.increment(lv12.getCount());
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private boolean handleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack, ItemStack cursorStack) {
      FeatureSet lv = player.getWorld().getEnabledFeatures();
      if (cursorStack.isItemEnabled(lv) && cursorStack.onStackClicked(slot, clickType, player)) {
         return true;
      } else {
         return stack.isItemEnabled(lv) && stack.onClicked(cursorStack, slot, clickType, player, this.getCursorStackReference());
      }
   }

   private StackReference getCursorStackReference() {
      return new StackReference() {
         public ItemStack get() {
            return ScreenHandler.this.getCursorStack();
         }

         public boolean set(ItemStack stack) {
            ScreenHandler.this.setCursorStack(stack);
            return true;
         }
      };
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return true;
   }

   public void onClosed(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity) {
         ItemStack lv = this.getCursorStack();
         if (!lv.isEmpty()) {
            if (player.isAlive() && !((ServerPlayerEntity)player).isDisconnected()) {
               player.getInventory().offerOrDrop(lv);
            } else {
               player.dropItem(lv, false);
            }

            this.setCursorStack(ItemStack.EMPTY);
         }
      }

   }

   protected void dropInventory(PlayerEntity player, Inventory inventory) {
      int i;
      if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity)player).isDisconnected()) {
         for(i = 0; i < inventory.size(); ++i) {
            player.dropItem(inventory.removeStack(i), false);
         }

      } else {
         for(i = 0; i < inventory.size(); ++i) {
            PlayerInventory lv = player.getInventory();
            if (lv.player instanceof ServerPlayerEntity) {
               lv.offerOrDrop(inventory.removeStack(i));
            }
         }

      }
   }

   public void onContentChanged(Inventory inventory) {
      this.sendContentUpdates();
   }

   public void setStackInSlot(int slot, int revision, ItemStack stack) {
      this.getSlot(slot).setStackNoCallbacks(stack);
      this.revision = revision;
   }

   public void updateSlotStacks(int revision, List stacks, ItemStack cursorStack) {
      for(int j = 0; j < stacks.size(); ++j) {
         this.getSlot(j).setStackNoCallbacks((ItemStack)stacks.get(j));
      }

      this.cursorStack = cursorStack;
      this.revision = revision;
   }

   public void setProperty(int id, int value) {
      ((Property)this.properties.get(id)).set(value);
   }

   public abstract boolean canUse(PlayerEntity player);

   protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
      boolean bl2 = false;
      int k = startIndex;
      if (fromLast) {
         k = endIndex - 1;
      }

      Slot lv;
      ItemStack lv2;
      if (stack.isStackable()) {
         while(!stack.isEmpty()) {
            if (fromLast) {
               if (k < startIndex) {
                  break;
               }
            } else if (k >= endIndex) {
               break;
            }

            lv = (Slot)this.slots.get(k);
            lv2 = lv.getStack();
            if (!lv2.isEmpty() && ItemStack.canCombine(stack, lv2)) {
               int l = lv2.getCount() + stack.getCount();
               if (l <= stack.getMaxCount()) {
                  stack.setCount(0);
                  lv2.setCount(l);
                  lv.markDirty();
                  bl2 = true;
               } else if (lv2.getCount() < stack.getMaxCount()) {
                  stack.decrement(stack.getMaxCount() - lv2.getCount());
                  lv2.setCount(stack.getMaxCount());
                  lv.markDirty();
                  bl2 = true;
               }
            }

            if (fromLast) {
               --k;
            } else {
               ++k;
            }
         }
      }

      if (!stack.isEmpty()) {
         if (fromLast) {
            k = endIndex - 1;
         } else {
            k = startIndex;
         }

         while(true) {
            if (fromLast) {
               if (k < startIndex) {
                  break;
               }
            } else if (k >= endIndex) {
               break;
            }

            lv = (Slot)this.slots.get(k);
            lv2 = lv.getStack();
            if (lv2.isEmpty() && lv.canInsert(stack)) {
               if (stack.getCount() > lv.getMaxItemCount()) {
                  lv.setStack(stack.split(lv.getMaxItemCount()));
               } else {
                  lv.setStack(stack.split(stack.getCount()));
               }

               lv.markDirty();
               bl2 = true;
               break;
            }

            if (fromLast) {
               --k;
            } else {
               ++k;
            }
         }
      }

      return bl2;
   }

   public static int unpackQuickCraftButton(int quickCraftData) {
      return quickCraftData >> 2 & 3;
   }

   public static int unpackQuickCraftStage(int quickCraftData) {
      return quickCraftData & 3;
   }

   public static int packQuickCraftData(int quickCraftStage, int buttonId) {
      return quickCraftStage & 3 | (buttonId & 3) << 2;
   }

   public static boolean shouldQuickCraftContinue(int stage, PlayerEntity player) {
      if (stage == 0) {
         return true;
      } else if (stage == 1) {
         return true;
      } else {
         return stage == 2 && player.getAbilities().creativeMode;
      }
   }

   protected void endQuickCraft() {
      this.quickCraftStage = 0;
      this.quickCraftSlots.clear();
   }

   public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
      boolean bl2 = slot == null || !slot.hasStack();
      if (!bl2 && ItemStack.canCombine(stack, slot.getStack())) {
         return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxCount();
      } else {
         return bl2;
      }
   }

   public static int calculateStackSize(Set slots, int mode, ItemStack stack) {
      int var10000;
      switch (mode) {
         case 0:
            var10000 = MathHelper.floor((float)stack.getCount() / (float)slots.size());
            break;
         case 1:
            var10000 = 1;
            break;
         case 2:
            var10000 = stack.getItem().getMaxCount();
            break;
         default:
            var10000 = stack.getCount();
      }

      return var10000;
   }

   public boolean canInsertIntoSlot(Slot slot) {
      return true;
   }

   public static int calculateComparatorOutput(@Nullable BlockEntity entity) {
      return entity instanceof Inventory ? calculateComparatorOutput((Inventory)entity) : 0;
   }

   public static int calculateComparatorOutput(@Nullable Inventory inventory) {
      if (inventory == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < inventory.size(); ++j) {
            ItemStack lv = inventory.getStack(j);
            if (!lv.isEmpty()) {
               f += (float)lv.getCount() / (float)Math.min(inventory.getMaxCountPerStack(), lv.getMaxCount());
               ++i;
            }
         }

         f /= (float)inventory.size();
         return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
      }
   }

   public void setCursorStack(ItemStack stack) {
      this.cursorStack = stack;
   }

   public ItemStack getCursorStack() {
      return this.cursorStack;
   }

   public void disableSyncing() {
      this.disableSync = true;
   }

   public void enableSyncing() {
      this.disableSync = false;
   }

   public void copySharedSlots(ScreenHandler handler) {
      Table table = HashBasedTable.create();

      int i;
      Slot lv;
      for(i = 0; i < handler.slots.size(); ++i) {
         lv = (Slot)handler.slots.get(i);
         table.put(lv.inventory, lv.getIndex(), i);
      }

      for(i = 0; i < this.slots.size(); ++i) {
         lv = (Slot)this.slots.get(i);
         Integer integer = (Integer)table.get(lv.inventory, lv.getIndex());
         if (integer != null) {
            this.trackedStacks.set(i, (ItemStack)handler.trackedStacks.get(integer));
            this.previousTrackedStacks.set(i, (ItemStack)handler.previousTrackedStacks.get(integer));
         }
      }

   }

   public OptionalInt getSlotIndex(Inventory inventory, int index) {
      for(int j = 0; j < this.slots.size(); ++j) {
         Slot lv = (Slot)this.slots.get(j);
         if (lv.inventory == inventory && index == lv.getIndex()) {
            return OptionalInt.of(j);
         }
      }

      return OptionalInt.empty();
   }

   public int getRevision() {
      return this.revision;
   }

   public int nextRevision() {
      this.revision = this.revision + 1 & 32767;
      return this.revision;
   }
}
