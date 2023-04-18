package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class DispenserBlockEntity extends LootableContainerBlockEntity {
   public static final int INVENTORY_SIZE = 9;
   private DefaultedList inventory;

   protected DispenserBlockEntity(BlockEntityType arg, BlockPos arg2, BlockState arg3) {
      super(arg, arg2, arg3);
      this.inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
   }

   public DispenserBlockEntity(BlockPos pos, BlockState state) {
      this(BlockEntityType.DISPENSER, pos, state);
   }

   public int size() {
      return 9;
   }

   public int chooseNonEmptySlot(Random random) {
      this.checkLootInteraction((PlayerEntity)null);
      int i = -1;
      int j = 1;

      for(int k = 0; k < this.inventory.size(); ++k) {
         if (!((ItemStack)this.inventory.get(k)).isEmpty() && random.nextInt(j++) == 0) {
            i = k;
         }
      }

      return i;
   }

   public int addToFirstFreeSlot(ItemStack stack) {
      for(int i = 0; i < this.inventory.size(); ++i) {
         if (((ItemStack)this.inventory.get(i)).isEmpty()) {
            this.setStack(i, stack);
            return i;
         }
      }

      return -1;
   }

   protected Text getContainerName() {
      return Text.translatable("container.dispenser");
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.deserializeLootTable(nbt)) {
         Inventories.readNbt(nbt, this.inventory);
      }

   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.serializeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory);
      }

   }

   protected DefaultedList getInvStackList() {
      return this.inventory;
   }

   protected void setInvStackList(DefaultedList list) {
      this.inventory = list;
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new Generic3x3ContainerScreenHandler(syncId, playerInventory, this);
   }
}
