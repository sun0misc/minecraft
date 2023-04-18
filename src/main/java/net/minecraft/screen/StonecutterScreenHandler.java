package net.minecraft.screen;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class StonecutterScreenHandler extends ScreenHandler {
   public static final int field_30842 = 0;
   public static final int field_30843 = 1;
   private static final int field_30844 = 2;
   private static final int field_30845 = 29;
   private static final int field_30846 = 29;
   private static final int field_30847 = 38;
   private final ScreenHandlerContext context;
   private final Property selectedRecipe;
   private final World world;
   private List availableRecipes;
   private ItemStack inputStack;
   long lastTakeTime;
   final Slot inputSlot;
   final Slot outputSlot;
   Runnable contentsChangedListener;
   public final Inventory input;
   final CraftingResultInventory output;

   public StonecutterScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
   }

   public StonecutterScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
      super(ScreenHandlerType.STONECUTTER, syncId);
      this.selectedRecipe = Property.create();
      this.availableRecipes = Lists.newArrayList();
      this.inputStack = ItemStack.EMPTY;
      this.contentsChangedListener = () -> {
      };
      this.input = new SimpleInventory(1) {
         public void markDirty() {
            super.markDirty();
            StonecutterScreenHandler.this.onContentChanged(this);
            StonecutterScreenHandler.this.contentsChangedListener.run();
         }
      };
      this.output = new CraftingResultInventory();
      this.context = context;
      this.world = playerInventory.player.world;
      this.inputSlot = this.addSlot(new Slot(this.input, 0, 20, 33));
      this.outputSlot = this.addSlot(new Slot(this.output, 1, 143, 33) {
         public boolean canInsert(ItemStack stack) {
            return false;
         }

         public void onTakeItem(PlayerEntity player, ItemStack stack) {
            stack.onCraft(player.world, player, stack.getCount());
            StonecutterScreenHandler.this.output.unlockLastRecipe(player);
            ItemStack lv = StonecutterScreenHandler.this.inputSlot.takeStack(1);
            if (!lv.isEmpty()) {
               StonecutterScreenHandler.this.populateResult();
            }

            context.run((world, pos) -> {
               long l = world.getTime();
               if (StonecutterScreenHandler.this.lastTakeTime != l) {
                  world.playSound((PlayerEntity)null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  StonecutterScreenHandler.this.lastTakeTime = l;
               }

            });
            super.onTakeItem(player, stack);
         }
      });

      int j;
      for(j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
      }

      this.addProperty(this.selectedRecipe);
   }

   public int getSelectedRecipe() {
      return this.selectedRecipe.get();
   }

   public List getAvailableRecipes() {
      return this.availableRecipes;
   }

   public int getAvailableRecipeCount() {
      return this.availableRecipes.size();
   }

   public boolean canCraft() {
      return this.inputSlot.hasStack() && !this.availableRecipes.isEmpty();
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.STONECUTTER);
   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      if (this.isInBounds(id)) {
         this.selectedRecipe.set(id);
         this.populateResult();
      }

      return true;
   }

   private boolean isInBounds(int id) {
      return id >= 0 && id < this.availableRecipes.size();
   }

   public void onContentChanged(Inventory inventory) {
      ItemStack lv = this.inputSlot.getStack();
      if (!lv.isOf(this.inputStack.getItem())) {
         this.inputStack = lv.copy();
         this.updateInput(inventory, lv);
      }

   }

   private void updateInput(Inventory input, ItemStack stack) {
      this.availableRecipes.clear();
      this.selectedRecipe.set(-1);
      this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
      if (!stack.isEmpty()) {
         this.availableRecipes = this.world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, input, this.world);
      }

   }

   void populateResult() {
      if (!this.availableRecipes.isEmpty() && this.isInBounds(this.selectedRecipe.get())) {
         StonecuttingRecipe lv = (StonecuttingRecipe)this.availableRecipes.get(this.selectedRecipe.get());
         ItemStack lv2 = lv.craft(this.input, this.world.getRegistryManager());
         if (lv2.isItemEnabled(this.world.getEnabledFeatures())) {
            this.output.setLastRecipe(lv);
            this.outputSlot.setStackNoCallbacks(lv2);
         } else {
            this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
         }
      } else {
         this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
      }

      this.sendContentUpdates();
   }

   public ScreenHandlerType getType() {
      return ScreenHandlerType.STONECUTTER;
   }

   public void setContentsChangedListener(Runnable contentsChangedListener) {
      this.contentsChangedListener = contentsChangedListener;
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         Item lv4 = lv3.getItem();
         lv = lv3.copy();
         if (slot == 1) {
            lv4.onCraft(lv3, player.world, player);
            if (!this.insertItem(lv3, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot == 0) {
            if (!this.insertItem(lv3, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.world.getRecipeManager().getFirstMatch(RecipeType.STONECUTTING, new SimpleInventory(new ItemStack[]{lv3}), this.world).isPresent()) {
            if (!this.insertItem(lv3, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 2 && slot < 29) {
            if (!this.insertItem(lv3, 29, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 29 && slot < 38 && !this.insertItem(lv3, 2, 29, false)) {
            return ItemStack.EMPTY;
         }

         if (lv3.isEmpty()) {
            lv2.setStack(ItemStack.EMPTY);
         }

         lv2.markDirty();
         if (lv3.getCount() == lv.getCount()) {
            return ItemStack.EMPTY;
         }

         lv2.onTakeItem(player, lv3);
         this.sendContentUpdates();
      }

      return lv;
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.output.removeStack(1);
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.input);
      });
   }
}
