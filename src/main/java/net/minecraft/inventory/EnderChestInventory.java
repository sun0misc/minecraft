package net.minecraft.inventory;

import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

public class EnderChestInventory extends SimpleInventory {
   @Nullable
   private EnderChestBlockEntity activeBlockEntity;

   public EnderChestInventory() {
      super(27);
   }

   public void setActiveBlockEntity(EnderChestBlockEntity blockEntity) {
      this.activeBlockEntity = blockEntity;
   }

   public boolean isActiveBlockEntity(EnderChestBlockEntity blockEntity) {
      return this.activeBlockEntity == blockEntity;
   }

   public void readNbtList(NbtList nbtList) {
      int i;
      for(i = 0; i < this.size(); ++i) {
         this.setStack(i, ItemStack.EMPTY);
      }

      for(i = 0; i < nbtList.size(); ++i) {
         NbtCompound lv = nbtList.getCompound(i);
         int j = lv.getByte("Slot") & 255;
         if (j >= 0 && j < this.size()) {
            this.setStack(j, ItemStack.fromNbt(lv));
         }
      }

   }

   public NbtList toNbtList() {
      NbtList lv = new NbtList();

      for(int i = 0; i < this.size(); ++i) {
         ItemStack lv2 = this.getStack(i);
         if (!lv2.isEmpty()) {
            NbtCompound lv3 = new NbtCompound();
            lv3.putByte("Slot", (byte)i);
            lv2.writeNbt(lv3);
            lv.add(lv3);
         }
      }

      return lv;
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return this.activeBlockEntity != null && !this.activeBlockEntity.canPlayerUse(player) ? false : super.canPlayerUse(player);
   }

   public void onOpen(PlayerEntity player) {
      if (this.activeBlockEntity != null) {
         this.activeBlockEntity.onOpen(player);
      }

      super.onOpen(player);
   }

   public void onClose(PlayerEntity player) {
      if (this.activeBlockEntity != null) {
         this.activeBlockEntity.onClose(player);
      }

      super.onClose(player);
      this.activeBlockEntity = null;
   }
}
