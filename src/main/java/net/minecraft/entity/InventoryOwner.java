package net.minecraft.entity;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public interface InventoryOwner {
   String INVENTORY_KEY = "Inventory";

   SimpleInventory getInventory();

   static void pickUpItem(MobEntity entity, InventoryOwner inventoryOwner, ItemEntity item) {
      ItemStack lv = item.getStack();
      if (entity.canGather(lv)) {
         SimpleInventory lv2 = inventoryOwner.getInventory();
         boolean bl = lv2.canInsert(lv);
         if (!bl) {
            return;
         }

         entity.triggerItemPickedUpByEntityCriteria(item);
         int i = lv.getCount();
         ItemStack lv3 = lv2.addStack(lv);
         entity.sendPickup(item, i - lv3.getCount());
         if (lv3.isEmpty()) {
            item.discard();
         } else {
            lv.setCount(lv3.getCount());
         }
      }

   }

   default void readInventory(NbtCompound nbt) {
      if (nbt.contains("Inventory", NbtElement.LIST_TYPE)) {
         this.getInventory().readNbtList(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE));
      }

   }

   default void writeInventory(NbtCompound nbt) {
      nbt.put("Inventory", this.getInventory().toNbtList());
   }
}
