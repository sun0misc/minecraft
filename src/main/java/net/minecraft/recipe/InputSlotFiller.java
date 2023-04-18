package net.minecraft.recipe;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class InputSlotFiller implements RecipeGridAligner {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final RecipeMatcher matcher = new RecipeMatcher();
   protected PlayerInventory inventory;
   protected AbstractRecipeScreenHandler handler;

   public InputSlotFiller(AbstractRecipeScreenHandler handler) {
      this.handler = handler;
   }

   public void fillInputSlots(ServerPlayerEntity entity, @Nullable Recipe recipe, boolean craftAll) {
      if (recipe != null && entity.getRecipeBook().contains(recipe)) {
         this.inventory = entity.getInventory();
         if (this.canReturnInputs() || entity.isCreative()) {
            this.matcher.clear();
            entity.getInventory().populateRecipeFinder(this.matcher);
            this.handler.populateRecipeFinder(this.matcher);
            if (this.matcher.match(recipe, (IntList)null)) {
               this.fillInputSlots(recipe, craftAll);
            } else {
               this.returnInputs();
               entity.networkHandler.sendPacket(new CraftFailedResponseS2CPacket(entity.currentScreenHandler.syncId, recipe));
            }

            entity.getInventory().markDirty();
         }
      }
   }

   protected void returnInputs() {
      for(int i = 0; i < this.handler.getCraftingSlotCount(); ++i) {
         if (this.handler.canInsertIntoSlot(i)) {
            ItemStack lv = this.handler.getSlot(i).getStack().copy();
            this.inventory.offer(lv, false);
            this.handler.getSlot(i).setStackNoCallbacks(lv);
         }
      }

      this.handler.clearCraftingSlots();
   }

   protected void fillInputSlots(Recipe recipe, boolean craftAll) {
      boolean bl2 = this.handler.matches(recipe);
      int i = this.matcher.countCrafts(recipe, (IntList)null);
      int j;
      if (bl2) {
         for(j = 0; j < this.handler.getCraftingHeight() * this.handler.getCraftingWidth() + 1; ++j) {
            if (j != this.handler.getCraftingResultSlotIndex()) {
               ItemStack lv = this.handler.getSlot(j).getStack();
               if (!lv.isEmpty() && Math.min(i, lv.getMaxCount()) < lv.getCount() + 1) {
                  return;
               }
            }
         }
      }

      j = this.getAmountToFill(craftAll, i, bl2);
      IntList intList = new IntArrayList();
      if (this.matcher.match(recipe, intList, j)) {
         int k = j;
         IntListIterator var8 = intList.iterator();

         while(var8.hasNext()) {
            int l = (Integer)var8.next();
            int m = RecipeMatcher.getStackFromId(l).getMaxCount();
            if (m < k) {
               k = m;
            }
         }

         if (this.matcher.match(recipe, intList, k)) {
            this.returnInputs();
            this.alignRecipeToGrid(this.handler.getCraftingWidth(), this.handler.getCraftingHeight(), this.handler.getCraftingResultSlotIndex(), recipe, intList.iterator(), k);
         }
      }

   }

   public void acceptAlignedInput(Iterator inputs, int slot, int amount, int gridX, int gridY) {
      Slot lv = this.handler.getSlot(slot);
      ItemStack lv2 = RecipeMatcher.getStackFromId((Integer)inputs.next());
      if (!lv2.isEmpty()) {
         for(int m = 0; m < amount; ++m) {
            this.fillInputSlot(lv, lv2);
         }
      }

   }

   protected int getAmountToFill(boolean craftAll, int limit, boolean recipeInCraftingSlots) {
      int j = 1;
      if (craftAll) {
         j = limit;
      } else if (recipeInCraftingSlots) {
         j = 64;

         for(int k = 0; k < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++k) {
            if (k != this.handler.getCraftingResultSlotIndex()) {
               ItemStack lv = this.handler.getSlot(k).getStack();
               if (!lv.isEmpty() && j > lv.getCount()) {
                  j = lv.getCount();
               }
            }
         }

         if (j < 64) {
            ++j;
         }
      }

      return j;
   }

   protected void fillInputSlot(Slot slot, ItemStack stack) {
      int i = this.inventory.indexOf(stack);
      if (i != -1) {
         ItemStack lv = this.inventory.getStack(i);
         if (!lv.isEmpty()) {
            if (lv.getCount() > 1) {
               this.inventory.removeStack(i, 1);
            } else {
               this.inventory.removeStack(i);
            }

            if (slot.getStack().isEmpty()) {
               slot.setStackNoCallbacks(lv.copyWithCount(1));
            } else {
               slot.getStack().increment(1);
            }

         }
      }
   }

   private boolean canReturnInputs() {
      List list = Lists.newArrayList();
      int i = this.getFreeInventorySlots();

      for(int j = 0; j < this.handler.getCraftingWidth() * this.handler.getCraftingHeight() + 1; ++j) {
         if (j != this.handler.getCraftingResultSlotIndex()) {
            ItemStack lv = this.handler.getSlot(j).getStack().copy();
            if (!lv.isEmpty()) {
               int k = this.inventory.getOccupiedSlotWithRoomForStack(lv);
               if (k == -1 && list.size() <= i) {
                  Iterator var6 = list.iterator();

                  while(var6.hasNext()) {
                     ItemStack lv2 = (ItemStack)var6.next();
                     if (lv2.isItemEqual(lv) && lv2.getCount() != lv2.getMaxCount() && lv2.getCount() + lv.getCount() <= lv2.getMaxCount()) {
                        lv2.increment(lv.getCount());
                        lv.setCount(0);
                        break;
                     }
                  }

                  if (!lv.isEmpty()) {
                     if (list.size() >= i) {
                        return false;
                     }

                     list.add(lv);
                  }
               } else if (k == -1) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private int getFreeInventorySlots() {
      int i = 0;
      Iterator var2 = this.inventory.main.iterator();

      while(var2.hasNext()) {
         ItemStack lv = (ItemStack)var2.next();
         if (lv.isEmpty()) {
            ++i;
         }
      }

      return i;
   }
}
