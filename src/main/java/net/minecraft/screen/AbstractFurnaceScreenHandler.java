package net.minecraft.screen;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.slot.FurnaceFuelSlot;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public abstract class AbstractFurnaceScreenHandler extends AbstractRecipeScreenHandler {
   public static final int field_30738 = 0;
   public static final int field_30739 = 1;
   public static final int field_30740 = 2;
   public static final int field_30741 = 3;
   public static final int field_30742 = 4;
   private static final int field_30743 = 3;
   private static final int field_30744 = 30;
   private static final int field_30745 = 30;
   private static final int field_30746 = 39;
   private final Inventory inventory;
   private final PropertyDelegate propertyDelegate;
   protected final World world;
   private final RecipeType recipeType;
   private final RecipeBookCategory category;

   protected AbstractFurnaceScreenHandler(ScreenHandlerType type, RecipeType recipeType, RecipeBookCategory category, int syncId, PlayerInventory playerInventory) {
      this(type, recipeType, category, syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(4));
   }

   protected AbstractFurnaceScreenHandler(ScreenHandlerType type, RecipeType recipeType, RecipeBookCategory category, int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
      super(type, syncId);
      this.recipeType = recipeType;
      this.category = category;
      checkSize(inventory, 3);
      checkDataCount(propertyDelegate, 4);
      this.inventory = inventory;
      this.propertyDelegate = propertyDelegate;
      this.world = playerInventory.player.world;
      this.addSlot(new Slot(inventory, 0, 56, 17));
      this.addSlot(new FurnaceFuelSlot(this, inventory, 1, 56, 53));
      this.addSlot(new FurnaceOutputSlot(playerInventory.player, inventory, 2, 116, 35));

      int j;
      for(j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
      }

      this.addProperties(propertyDelegate);
   }

   public void populateRecipeFinder(RecipeMatcher finder) {
      if (this.inventory instanceof RecipeInputProvider) {
         ((RecipeInputProvider)this.inventory).provideRecipeInputs(finder);
      }

   }

   public void clearCraftingSlots() {
      this.getSlot(0).setStackNoCallbacks(ItemStack.EMPTY);
      this.getSlot(2).setStackNoCallbacks(ItemStack.EMPTY);
   }

   public boolean matches(Recipe recipe) {
      return recipe.matches(this.inventory, this.world);
   }

   public int getCraftingResultSlotIndex() {
      return 2;
   }

   public int getCraftingWidth() {
      return 1;
   }

   public int getCraftingHeight() {
      return 1;
   }

   public int getCraftingSlotCount() {
      return 3;
   }

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot == 2) {
            if (!this.insertItem(lv3, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot != 1 && slot != 0) {
            if (this.isSmeltable(lv3)) {
               if (!this.insertItem(lv3, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.isFuel(lv3)) {
               if (!this.insertItem(lv3, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (slot >= 3 && slot < 30) {
               if (!this.insertItem(lv3, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (slot >= 30 && slot < 39 && !this.insertItem(lv3, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (lv3.isEmpty()) {
            lv2.setStack(ItemStack.EMPTY);
         } else {
            lv2.markDirty();
         }

         if (lv3.getCount() == lv.getCount()) {
            return ItemStack.EMPTY;
         }

         lv2.onTakeItem(player, lv3);
      }

      return lv;
   }

   protected boolean isSmeltable(ItemStack itemStack) {
      return this.world.getRecipeManager().getFirstMatch(this.recipeType, new SimpleInventory(new ItemStack[]{itemStack}), this.world).isPresent();
   }

   protected boolean isFuel(ItemStack itemStack) {
      return AbstractFurnaceBlockEntity.canUseAsFuel(itemStack);
   }

   public int getCookProgress() {
      int i = this.propertyDelegate.get(2);
      int j = this.propertyDelegate.get(3);
      return j != 0 && i != 0 ? i * 24 / j : 0;
   }

   public int getFuelProgress() {
      int i = this.propertyDelegate.get(1);
      if (i == 0) {
         i = 200;
      }

      return this.propertyDelegate.get(0) * 13 / i;
   }

   public boolean isBurning() {
      return this.propertyDelegate.get(0) > 0;
   }

   public RecipeBookCategory getCategory() {
      return this.category;
   }

   public boolean canInsertIntoSlot(int index) {
      return index != 1;
   }
}
