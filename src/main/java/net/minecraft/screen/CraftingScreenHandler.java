package net.minecraft.screen;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class CraftingScreenHandler extends AbstractRecipeScreenHandler {
   public static final int field_30781 = 0;
   private static final int field_30782 = 1;
   private static final int field_30783 = 10;
   private static final int field_30784 = 10;
   private static final int field_30785 = 37;
   private static final int field_30786 = 37;
   private static final int field_30787 = 46;
   private final CraftingInventory input;
   private final CraftingResultInventory result;
   private final ScreenHandlerContext context;
   private final PlayerEntity player;

   public CraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
   }

   public CraftingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
      super(ScreenHandlerType.CRAFTING, syncId);
      this.input = new CraftingInventory(this, 3, 3);
      this.result = new CraftingResultInventory();
      this.context = context;
      this.player = playerInventory.player;
      this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));

      int j;
      int k;
      for(j = 0; j < 3; ++j) {
         for(k = 0; k < 3; ++k) {
            this.addSlot(new Slot(this.input, k + j * 3, 30 + k * 18, 17 + j * 18));
         }
      }

      for(j = 0; j < 3; ++j) {
         for(k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(j = 0; j < 9; ++j) {
         this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
      }

   }

   protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory) {
      if (!world.isClient) {
         ServerPlayerEntity lv = (ServerPlayerEntity)player;
         ItemStack lv2 = ItemStack.EMPTY;
         Optional optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
         if (optional.isPresent()) {
            CraftingRecipe lv3 = (CraftingRecipe)optional.get();
            if (resultInventory.shouldCraftRecipe(world, lv, lv3)) {
               ItemStack lv4 = lv3.craft(craftingInventory, world.getRegistryManager());
               if (lv4.isItemEnabled(world.getEnabledFeatures())) {
                  lv2 = lv4;
               }
            }
         }

         resultInventory.setStack(0, lv2);
         handler.setPreviousTrackedSlot(0, lv2);
         lv.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, lv2));
      }
   }

   public void onContentChanged(Inventory inventory) {
      this.context.run((world, pos) -> {
         updateResult(this, world, this.player, this.input, this.result);
      });
   }

   public void populateRecipeFinder(RecipeMatcher finder) {
      this.input.provideRecipeInputs(finder);
   }

   public void clearCraftingSlots() {
      this.input.clear();
      this.result.clear();
   }

   public boolean matches(Recipe recipe) {
      return recipe.matches(this.input, this.player.world);
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.context.run((world, pos) -> {
         this.dropInventory(player, this.input);
      });
   }

   public boolean canUse(PlayerEntity player) {
      return canUse(this.context, player, Blocks.CRAFTING_TABLE);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2 != null && lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         if (slot == 0) {
            this.context.run((world, pos) -> {
               lv3.getItem().onCraft(lv3, world, player);
            });
            if (!this.insertItem(lv3, 10, 46, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot >= 10 && slot < 46) {
            if (!this.insertItem(lv3, 1, 10, false)) {
               if (slot < 37) {
                  if (!this.insertItem(lv3, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.insertItem(lv3, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.insertItem(lv3, 10, 46, false)) {
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
         if (slot == 0) {
            player.dropItem(lv3, false);
         }
      }

      return lv;
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
   }

   public int getCraftingResultSlotIndex() {
      return 0;
   }

   public int getCraftingWidth() {
      return this.input.getWidth();
   }

   public int getCraftingHeight() {
      return this.input.getHeight();
   }

   public int getCraftingSlotCount() {
      return 10;
   }

   public RecipeBookCategory getCategory() {
      return RecipeBookCategory.CRAFTING;
   }

   public boolean canInsertIntoSlot(int index) {
      return index != this.getCraftingResultSlotIndex();
   }
}
