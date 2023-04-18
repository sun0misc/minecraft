package net.minecraft.screen;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BannerPatternTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;

public class LoomScreenHandler extends ScreenHandler {
   private static final int field_39120 = -1;
   private static final int field_30826 = 4;
   private static final int field_30827 = 31;
   private static final int field_30828 = 31;
   private static final int field_30829 = 40;
   private final ScreenHandlerContext context;
   final Property selectedPattern;
   private List bannerPatterns;
   Runnable inventoryChangeListener;
   final Slot bannerSlot;
   final Slot dyeSlot;
   private final Slot patternSlot;
   private final Slot outputSlot;
   long lastTakeResultTime;
   private final Inventory input;
   private final Inventory output;

   public LoomScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
   }

   public LoomScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
      super(ScreenHandlerType.LOOM, syncId);
      this.selectedPattern = Property.create();
      this.bannerPatterns = List.of();
      this.inventoryChangeListener = () -> {
      };
      this.input = new SimpleInventory(3) {
         public void markDirty() {
            super.markDirty();
            LoomScreenHandler.this.onContentChanged(this);
            LoomScreenHandler.this.inventoryChangeListener.run();
         }
      };
      this.output = new SimpleInventory(1) {
         public void markDirty() {
            super.markDirty();
            LoomScreenHandler.this.inventoryChangeListener.run();
         }
      };
      this.context = context;
      this.bannerSlot = this.addSlot(new Slot(this.input, 0, 13, 26) {
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof BannerItem;
         }
      });
      this.dyeSlot = this.addSlot(new Slot(this.input, 1, 33, 26) {
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof DyeItem;
         }
      });
      this.patternSlot = this.addSlot(new Slot(this.input, 2, 23, 45) {
         public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof BannerPatternItem;
         }
      });
      this.outputSlot = this.addSlot(new Slot(this.output, 0, 143, 58) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            LoomScreenHandler.this.bannerSlot.takeStack(1);
            LoomScreenHandler.this.dyeSlot.takeStack(1);
            if (!LoomScreenHandler.this.bannerSlot.hasStack() || !LoomScreenHandler.this.dyeSlot.hasStack()) {
               LoomScreenHandler.this.selectedPattern.set(-1);
            }

            context.run((world, pos) -> {
               long l = world.getTime();
               if (LoomScreenHandler.this.lastTakeResultTime != l) {
                  world.playSound((PlayerEntity)null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  LoomScreenHandler.this.lastTakeResultTime = l;
               }

            });
            super.onTakeItem(player, stack);
         }
      });

      int j;
      for(j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
      }

      this.addProperty(this.selectedPattern);
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.LOOM);
   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      if (id >= 0 && id < this.bannerPatterns.size()) {
         this.selectedPattern.set(id);
         this.updateOutputSlot((RegistryEntry)this.bannerPatterns.get(id));
         return true;
      } else {
         return false;
      }
   }

   private List getPatternsFor(ItemStack stack) {
      if (stack.isEmpty()) {
         return (List)Registries.BANNER_PATTERN.getEntryList(BannerPatternTags.NO_ITEM_REQUIRED).map(ImmutableList::copyOf).orElse(ImmutableList.of());
      } else {
         Item var3 = stack.getItem();
         if (var3 instanceof BannerPatternItem) {
            BannerPatternItem lv = (BannerPatternItem)var3;
            return (List)Registries.BANNER_PATTERN.getEntryList(lv.getPattern()).map(ImmutableList::copyOf).orElse(ImmutableList.of());
         } else {
            return List.of();
         }
      }
   }

   private boolean isPatternIndexValid(int index) {
      return index >= 0 && index < this.bannerPatterns.size();
   }

   public void onContentChanged(Inventory inventory) {
      ItemStack lv = this.bannerSlot.getStack();
      ItemStack lv2 = this.dyeSlot.getStack();
      ItemStack lv3 = this.patternSlot.getStack();
      if (!lv.isEmpty() && !lv2.isEmpty()) {
         int i = this.selectedPattern.get();
         boolean bl = this.isPatternIndexValid(i);
         List list = this.bannerPatterns;
         this.bannerPatterns = this.getPatternsFor(lv3);
         RegistryEntry lv4;
         if (this.bannerPatterns.size() == 1) {
            this.selectedPattern.set(0);
            lv4 = (RegistryEntry)this.bannerPatterns.get(0);
         } else if (!bl) {
            this.selectedPattern.set(-1);
            lv4 = null;
         } else {
            RegistryEntry lv5 = (RegistryEntry)list.get(i);
            int j = this.bannerPatterns.indexOf(lv5);
            if (j != -1) {
               lv4 = lv5;
               this.selectedPattern.set(j);
            } else {
               lv4 = null;
               this.selectedPattern.set(-1);
            }
         }

         if (lv4 != null) {
            NbtCompound lv6 = BlockItem.getBlockEntityNbt(lv);
            boolean bl2 = lv6 != null && lv6.contains("Patterns", NbtElement.LIST_TYPE) && !lv.isEmpty() && lv6.getList("Patterns", NbtElement.COMPOUND_TYPE).size() >= 6;
            if (bl2) {
               this.selectedPattern.set(-1);
               this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
               this.updateOutputSlot(lv4);
            }
         } else {
            this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
         }

         this.sendContentUpdates();
      } else {
         this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
         this.bannerPatterns = List.of();
         this.selectedPattern.set(-1);
      }
   }

   public List getBannerPatterns() {
      return this.bannerPatterns;
   }

   public int getSelectedPattern() {
      return this.selectedPattern.get();
   }

   public void setInventoryChangeListener(Runnable inventoryChangeListener) {
      this.inventoryChangeListener = inventoryChangeListener;
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot == this.outputSlot.id) {
            if (!this.insertItem(lv3, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot != this.dyeSlot.id && slot != this.bannerSlot.id && slot != this.patternSlot.id) {
            if (lv3.getItem() instanceof BannerItem) {
               if (!this.insertItem(lv3, this.bannerSlot.id, this.bannerSlot.id + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (lv3.getItem() instanceof DyeItem) {
               if (!this.insertItem(lv3, this.dyeSlot.id, this.dyeSlot.id + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (lv3.getItem() instanceof BannerPatternItem) {
               if (!this.insertItem(lv3, this.patternSlot.id, this.patternSlot.id + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (slot >= 4 && slot < 31) {
               if (!this.insertItem(lv3, 31, 40, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (slot >= 31 && slot < 40 && !this.insertItem(lv3, 4, 31, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 4, 40, false)) {
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
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.input);
      });
   }

   private void updateOutputSlot(RegistryEntry pattern) {
      ItemStack lv = this.bannerSlot.getStack();
      ItemStack lv2 = this.dyeSlot.getStack();
      ItemStack lv3 = ItemStack.EMPTY;
      if (!lv.isEmpty() && !lv2.isEmpty()) {
         lv3 = lv.copyWithCount(1);
         DyeColor lv4 = ((DyeItem)lv2.getItem()).getColor();
         NbtCompound lv5 = BlockItem.getBlockEntityNbt(lv3);
         NbtList lv6;
         if (lv5 != null && lv5.contains("Patterns", NbtElement.LIST_TYPE)) {
            lv6 = lv5.getList("Patterns", NbtElement.COMPOUND_TYPE);
         } else {
            lv6 = new NbtList();
            if (lv5 == null) {
               lv5 = new NbtCompound();
            }

            lv5.put("Patterns", lv6);
         }

         NbtCompound lv7 = new NbtCompound();
         lv7.putString("Pattern", ((BannerPattern)pattern.value()).getId());
         lv7.putInt("Color", lv4.getId());
         lv6.add(lv7);
         BlockItem.setBlockEntityNbt(lv3, BlockEntityType.BANNER, lv5);
      }

      if (!ItemStack.areEqual(lv3, this.outputSlot.getStack())) {
         this.outputSlot.setStackNoCallbacks(lv3);
      }

   }

   public Slot getBannerSlot() {
      return this.bannerSlot;
   }

   public Slot getDyeSlot() {
      return this.dyeSlot;
   }

   public Slot getPatternSlot() {
      return this.patternSlot;
   }

   public Slot getOutputSlot() {
      return this.outputSlot;
   }
}
