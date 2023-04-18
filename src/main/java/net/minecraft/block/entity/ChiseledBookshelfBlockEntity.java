package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class ChiseledBookshelfBlockEntity extends BlockEntity implements Inventory {
   public static final int MAX_BOOKS = 6;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DefaultedList inventory;
   private int lastInteractedSlot;

   public ChiseledBookshelfBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.CHISELED_BOOKSHELF, pos, state);
      this.inventory = DefaultedList.ofSize(6, ItemStack.EMPTY);
      this.lastInteractedSlot = -1;
   }

   private void updateState(int interactedSlot) {
      if (interactedSlot >= 0 && interactedSlot < 6) {
         this.lastInteractedSlot = interactedSlot;
         BlockState lv = this.getCachedState();

         for(int j = 0; j < ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++j) {
            boolean bl = !this.getStack(j).isEmpty();
            BooleanProperty lv2 = (BooleanProperty)ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);
            lv = (BlockState)lv.with(lv2, bl);
         }

         ((World)Objects.requireNonNull(this.world)).setBlockState(this.pos, lv, Block.NOTIFY_ALL);
      } else {
         LOGGER.error("Expected slot 0-5, got {}", interactedSlot);
      }
   }

   public void readNbt(NbtCompound nbt) {
      this.inventory.clear();
      Inventories.readNbt(nbt, this.inventory);
      this.lastInteractedSlot = nbt.getInt("last_interacted_slot");
   }

   protected void writeNbt(NbtCompound nbt) {
      Inventories.writeNbt(nbt, this.inventory, true);
      nbt.putInt("last_interacted_slot", this.lastInteractedSlot);
   }

   public int getOpenSlotCount() {
      return (int)this.inventory.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
   }

   public void clear() {
      this.inventory.clear();
   }

   public int size() {
      return 6;
   }

   public boolean isEmpty() {
      return this.inventory.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getStack(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      ItemStack lv = (ItemStack)Objects.requireNonNullElse((ItemStack)this.inventory.get(slot), ItemStack.EMPTY);
      this.inventory.set(slot, ItemStack.EMPTY);
      if (!lv.isEmpty()) {
         this.updateState(slot);
      }

      return lv;
   }

   public ItemStack removeStack(int slot) {
      return this.removeStack(slot, 1);
   }

   public void setStack(int slot, ItemStack stack) {
      if (stack.isIn(ItemTags.BOOKSHELF_BOOKS)) {
         this.inventory.set(slot, stack);
         this.updateState(slot);
      }

   }

   public int getMaxCountPerStack() {
      return 1;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return Inventory.canPlayerUse(this, player);
   }

   public boolean isValid(int slot, ItemStack stack) {
      return stack.isIn(ItemTags.BOOKSHELF_BOOKS) && this.getStack(slot).isEmpty();
   }

   public int getLastInteractedSlot() {
      return this.lastInteractedSlot;
   }
}
