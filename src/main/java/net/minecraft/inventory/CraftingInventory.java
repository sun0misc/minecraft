package net.minecraft.inventory;

import java.util.Iterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class CraftingInventory implements Inventory, RecipeInputProvider {
   private final DefaultedList stacks;
   private final int width;
   private final int height;
   private final ScreenHandler handler;

   public CraftingInventory(ScreenHandler handler, int width, int height) {
      this.stacks = DefaultedList.ofSize(width * height, ItemStack.EMPTY);
      this.handler = handler;
      this.width = width;
      this.height = height;
   }

   public int size() {
      return this.stacks.size();
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

   public ItemStack getStack(int slot) {
      return slot >= this.size() ? ItemStack.EMPTY : (ItemStack)this.stacks.get(slot);
   }

   public ItemStack removeStack(int slot) {
      return Inventories.removeStack(this.stacks, slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      ItemStack lv = Inventories.splitStack(this.stacks, slot, amount);
      if (!lv.isEmpty()) {
         this.handler.onContentChanged(this);
      }

      return lv;
   }

   public void setStack(int slot, ItemStack stack) {
      this.stacks.set(slot, stack);
      this.handler.onContentChanged(this);
   }

   public void markDirty() {
   }

   public boolean canPlayerUse(PlayerEntity player) {
      return true;
   }

   public void clear() {
      this.stacks.clear();
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   public void provideRecipeInputs(RecipeMatcher finder) {
      Iterator var2 = this.stacks.iterator();

      while(var2.hasNext()) {
         ItemStack lv = (ItemStack)var2.next();
         finder.addUnenchantedInput(lv);
      }

   }
}
