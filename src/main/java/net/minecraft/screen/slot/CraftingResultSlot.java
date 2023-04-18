package net.minecraft.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.util.collection.DefaultedList;

public class CraftingResultSlot extends Slot {
   private final CraftingInventory input;
   private final PlayerEntity player;
   private int amount;

   public CraftingResultSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y) {
      super(inventory, index, x, y);
      this.player = player;
      this.input = input;
   }

   public boolean canInsert(ItemStack stack) {
      return false;
   }

   public ItemStack takeStack(int amount) {
      if (this.hasStack()) {
         this.amount += Math.min(amount, this.getStack().getCount());
      }

      return super.takeStack(amount);
   }

   protected void onCrafted(ItemStack stack, int amount) {
      this.amount += amount;
      this.onCrafted(stack);
   }

   protected void onTake(int amount) {
      this.amount += amount;
   }

   protected void onCrafted(ItemStack stack) {
      if (this.amount > 0) {
         stack.onCraft(this.player.world, this.player, this.amount);
      }

      if (this.inventory instanceof RecipeUnlocker) {
         ((RecipeUnlocker)this.inventory).unlockLastRecipe(this.player);
      }

      this.amount = 0;
   }

   public void onTakeItem(PlayerEntity player, ItemStack stack) {
      this.onCrafted(stack);
      DefaultedList lv = player.world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, this.input, player.world);

      for(int i = 0; i < lv.size(); ++i) {
         ItemStack lv2 = this.input.getStack(i);
         ItemStack lv3 = (ItemStack)lv.get(i);
         if (!lv2.isEmpty()) {
            this.input.removeStack(i, 1);
            lv2 = this.input.getStack(i);
         }

         if (!lv3.isEmpty()) {
            if (lv2.isEmpty()) {
               this.input.setStack(i, lv3);
            } else if (ItemStack.areItemsEqual(lv2, lv3) && ItemStack.areNbtEqual(lv2, lv3)) {
               lv3.increment(lv2.getCount());
               this.input.setStack(i, lv3);
            } else if (!this.player.getInventory().insertStack(lv3)) {
               this.player.dropItem(lv3, false);
            }
         }
      }

   }
}
