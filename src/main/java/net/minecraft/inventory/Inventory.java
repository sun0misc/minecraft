package net.minecraft.inventory;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Inventory extends Clearable {
   int MAX_COUNT_PER_STACK = 64;
   int field_42619 = 8;

   int size();

   boolean isEmpty();

   ItemStack getStack(int slot);

   ItemStack removeStack(int slot, int amount);

   ItemStack removeStack(int slot);

   void setStack(int slot, ItemStack stack);

   default int getMaxCountPerStack() {
      return 64;
   }

   void markDirty();

   boolean canPlayerUse(PlayerEntity player);

   default void onOpen(PlayerEntity player) {
   }

   default void onClose(PlayerEntity player) {
   }

   default boolean isValid(int slot, ItemStack stack) {
      return true;
   }

   default boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
      return true;
   }

   default int count(Item item) {
      int i = 0;

      for(int j = 0; j < this.size(); ++j) {
         ItemStack lv = this.getStack(j);
         if (lv.getItem().equals(item)) {
            i += lv.getCount();
         }
      }

      return i;
   }

   default boolean containsAny(Set items) {
      return this.containsAny((stack) -> {
         return !stack.isEmpty() && items.contains(stack.getItem());
      });
   }

   default boolean containsAny(Predicate predicate) {
      for(int i = 0; i < this.size(); ++i) {
         ItemStack lv = this.getStack(i);
         if (predicate.test(lv)) {
            return true;
         }
      }

      return false;
   }

   static boolean canPlayerUse(BlockEntity blockEntity, PlayerEntity player) {
      return canPlayerUse(blockEntity, player, 8);
   }

   static boolean canPlayerUse(BlockEntity blockEntity, PlayerEntity player, int range) {
      World lv = blockEntity.getWorld();
      BlockPos lv2 = blockEntity.getPos();
      if (lv == null) {
         return false;
      } else if (lv.getBlockEntity(lv2) != blockEntity) {
         return false;
      } else {
         return player.squaredDistanceTo((double)lv2.getX() + 0.5, (double)lv2.getY() + 0.5, (double)lv2.getZ() + 0.5) <= (double)(range * range);
      }
   }
}
