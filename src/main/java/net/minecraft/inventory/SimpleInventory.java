package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class SimpleInventory implements Inventory, RecipeInputProvider {
   private final int size;
   private final DefaultedList stacks;
   @Nullable
   private List listeners;

   public SimpleInventory(int size) {
      this.size = size;
      this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
   }

   public SimpleInventory(ItemStack... items) {
      this.size = items.length;
      this.stacks = DefaultedList.copyOf(ItemStack.EMPTY, items);
   }

   public void addListener(InventoryChangedListener listener) {
      if (this.listeners == null) {
         this.listeners = Lists.newArrayList();
      }

      this.listeners.add(listener);
   }

   public void removeListener(InventoryChangedListener listener) {
      if (this.listeners != null) {
         this.listeners.remove(listener);
      }

   }

   public ItemStack getStack(int slot) {
      return slot >= 0 && slot < this.stacks.size() ? (ItemStack)this.stacks.get(slot) : ItemStack.EMPTY;
   }

   public List clearToList() {
      List list = (List)this.stacks.stream().filter((stack) -> {
         return !stack.isEmpty();
      }).collect(Collectors.toList());
      this.clear();
      return list;
   }

   public ItemStack removeStack(int slot, int amount) {
      ItemStack lv = Inventories.splitStack(this.stacks, slot, amount);
      if (!lv.isEmpty()) {
         this.markDirty();
      }

      return lv;
   }

   public ItemStack removeItem(Item item, int count) {
      ItemStack lv = new ItemStack(item, 0);

      for(int j = this.size - 1; j >= 0; --j) {
         ItemStack lv2 = this.getStack(j);
         if (lv2.getItem().equals(item)) {
            int k = count - lv.getCount();
            ItemStack lv3 = lv2.split(k);
            lv.increment(lv3.getCount());
            if (lv.getCount() == count) {
               break;
            }
         }
      }

      if (!lv.isEmpty()) {
         this.markDirty();
      }

      return lv;
   }

   public ItemStack addStack(ItemStack stack) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack lv = stack.copy();
         this.addToExistingSlot(lv);
         if (lv.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            this.addToNewSlot(lv);
            return lv.isEmpty() ? ItemStack.EMPTY : lv;
         }
      }
   }

   public boolean canInsert(ItemStack stack) {
      boolean bl = false;
      Iterator var3 = this.stacks.iterator();

      while(var3.hasNext()) {
         ItemStack lv = (ItemStack)var3.next();
         if (lv.isEmpty() || ItemStack.canCombine(lv, stack) && lv.getCount() < lv.getMaxCount()) {
            bl = true;
            break;
         }
      }

      return bl;
   }

   public ItemStack removeStack(int slot) {
      ItemStack lv = (ItemStack)this.stacks.get(slot);
      if (lv.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.stacks.set(slot, ItemStack.EMPTY);
         return lv;
      }
   }

   public void setStack(int slot, ItemStack stack) {
      this.stacks.set(slot, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
         stack.setCount(this.getMaxCountPerStack());
      }

      this.markDirty();
   }

   public int size() {
      return this.size;
   }

   public boolean isEmpty() {
      Iterator var1 = this.stacks.iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         lv = (ItemStack)var1.next();
      } while(lv.isEmpty());

      return false;
   }

   public void markDirty() {
      if (this.listeners != null) {
         Iterator var1 = this.listeners.iterator();

         while(var1.hasNext()) {
            InventoryChangedListener lv = (InventoryChangedListener)var1.next();
            lv.onInventoryChanged(this);
         }
      }

   }

   public boolean canPlayerUse(PlayerEntity player) {
      return true;
   }

   public void clear() {
      this.stacks.clear();
      this.markDirty();
   }

   public void provideRecipeInputs(RecipeMatcher finder) {
      Iterator var2 = this.stacks.iterator();

      while(var2.hasNext()) {
         ItemStack lv = (ItemStack)var2.next();
         finder.addInput(lv);
      }

   }

   public String toString() {
      return ((List)this.stacks.stream().filter((stack) -> {
         return !stack.isEmpty();
      }).collect(Collectors.toList())).toString();
   }

   private void addToNewSlot(ItemStack stack) {
      for(int i = 0; i < this.size; ++i) {
         ItemStack lv = this.getStack(i);
         if (lv.isEmpty()) {
            this.setStack(i, stack.copyAndEmpty());
            return;
         }
      }

   }

   private void addToExistingSlot(ItemStack stack) {
      for(int i = 0; i < this.size; ++i) {
         ItemStack lv = this.getStack(i);
         if (ItemStack.canCombine(lv, stack)) {
            this.transfer(stack, lv);
            if (stack.isEmpty()) {
               return;
            }
         }
      }

   }

   private void transfer(ItemStack source, ItemStack target) {
      int i = Math.min(this.getMaxCountPerStack(), target.getMaxCount());
      int j = Math.min(source.getCount(), i - target.getCount());
      if (j > 0) {
         target.increment(j);
         source.decrement(j);
         this.markDirty();
      }

   }

   public void readNbtList(NbtList nbtList) {
      this.clear();

      for(int i = 0; i < nbtList.size(); ++i) {
         ItemStack lv = ItemStack.fromNbt(nbtList.getCompound(i));
         if (!lv.isEmpty()) {
            this.addStack(lv);
         }
      }

   }

   public NbtList toNbtList() {
      NbtList lv = new NbtList();

      for(int i = 0; i < this.size(); ++i) {
         ItemStack lv2 = this.getStack(i);
         if (!lv2.isEmpty()) {
            lv.add(lv2.writeNbt(new NbtCompound()));
         }
      }

      return lv;
   }
}
