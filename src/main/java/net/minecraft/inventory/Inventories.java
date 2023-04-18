package net.minecraft.inventory;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;

public class Inventories {
   public static ItemStack splitStack(List stacks, int slot, int amount) {
      return slot >= 0 && slot < stacks.size() && !((ItemStack)stacks.get(slot)).isEmpty() && amount > 0 ? ((ItemStack)stacks.get(slot)).split(amount) : ItemStack.EMPTY;
   }

   public static ItemStack removeStack(List stacks, int slot) {
      return slot >= 0 && slot < stacks.size() ? (ItemStack)stacks.set(slot, ItemStack.EMPTY) : ItemStack.EMPTY;
   }

   public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList stacks) {
      return writeNbt(nbt, stacks, true);
   }

   public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList stacks, boolean setIfEmpty) {
      NbtList lv = new NbtList();

      for(int i = 0; i < stacks.size(); ++i) {
         ItemStack lv2 = (ItemStack)stacks.get(i);
         if (!lv2.isEmpty()) {
            NbtCompound lv3 = new NbtCompound();
            lv3.putByte("Slot", (byte)i);
            lv2.writeNbt(lv3);
            lv.add(lv3);
         }
      }

      if (!lv.isEmpty() || setIfEmpty) {
         nbt.put("Items", lv);
      }

      return nbt;
   }

   public static void readNbt(NbtCompound nbt, DefaultedList stacks) {
      NbtList lv = nbt.getList("Items", NbtElement.COMPOUND_TYPE);

      for(int i = 0; i < lv.size(); ++i) {
         NbtCompound lv2 = lv.getCompound(i);
         int j = lv2.getByte("Slot") & 255;
         if (j >= 0 && j < stacks.size()) {
            stacks.set(j, ItemStack.fromNbt(lv2));
         }
      }

   }

   public static int remove(Inventory inventory, Predicate shouldRemove, int maxCount, boolean dryRun) {
      int j = 0;

      for(int k = 0; k < inventory.size(); ++k) {
         ItemStack lv = inventory.getStack(k);
         int l = remove(lv, shouldRemove, maxCount - j, dryRun);
         if (l > 0 && !dryRun && lv.isEmpty()) {
            inventory.setStack(k, ItemStack.EMPTY);
         }

         j += l;
      }

      return j;
   }

   public static int remove(ItemStack stack, Predicate shouldRemove, int maxCount, boolean dryRun) {
      if (!stack.isEmpty() && shouldRemove.test(stack)) {
         if (dryRun) {
            return stack.getCount();
         } else {
            int j = maxCount < 0 ? stack.getCount() : Math.min(maxCount, stack.getCount());
            stack.decrement(j);
            return j;
         }
      } else {
         return 0;
      }
   }
}
