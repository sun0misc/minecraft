package net.minecraft.screen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class PlayerScreenHandler extends AbstractRecipeScreenHandler {
   public static final int field_30802 = 0;
   public static final int field_30803 = 0;
   public static final int field_30804 = 1;
   public static final int field_30805 = 5;
   public static final int field_30806 = 5;
   public static final int field_30807 = 9;
   public static final int field_30808 = 9;
   public static final int field_30809 = 36;
   public static final int field_30810 = 36;
   public static final int field_30811 = 45;
   public static final int field_30812 = 45;
   public static final Identifier BLOCK_ATLAS_TEXTURE = new Identifier("textures/atlas/blocks.png");
   public static final Identifier EMPTY_HELMET_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
   public static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
   public static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
   public static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_boots");
   public static final Identifier EMPTY_OFFHAND_ARMOR_SLOT = new Identifier("item/empty_armor_slot_shield");
   static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES;
   private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER;
   private final CraftingInventory craftingInput = new CraftingInventory(this, 2, 2);
   private final CraftingResultInventory craftingResult = new CraftingResultInventory();
   public final boolean onServer;
   private final PlayerEntity owner;

   public PlayerScreenHandler(PlayerInventory inventory, boolean onServer, final PlayerEntity owner) {
      super((ScreenHandlerType)null, 0);
      this.onServer = onServer;
      this.owner = owner;
      this.addSlot(new CraftingResultSlot(inventory.player, this.craftingInput, this.craftingResult, 0, 154, 28));

      int i;
      int j;
      for(i = 0; i < 2; ++i) {
         for(j = 0; j < 2; ++j) {
            this.addSlot(new Slot(this.craftingInput, j + i * 2, 98 + j * 18, 18 + i * 18));
         }
      }

      for(i = 0; i < 4; ++i) {
         final EquipmentSlot lv = EQUIPMENT_SLOT_ORDER[i];
         this.addSlot(new Slot(inventory, 39 - i, 8, 8 + i * 18) {
            public void setStack(ItemStack stack) {
               PlayerScreenHandler.onEquipStack(owner, lv, stack, this.getStack());
               super.setStack(stack);
            }

            public int getMaxItemCount() {
               return 1;
            }

            public boolean canInsert(ItemStack stack) {
               return lv == MobEntity.getPreferredEquipmentSlot(stack);
            }

            public boolean canTakeItems(PlayerEntity playerEntity) {
               ItemStack lvx = this.getStack();
               return !lvx.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasBindingCurse(lvx) ? false : super.canTakeItems(playerEntity);
            }

            public Pair getBackgroundSprite() {
               return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_ARMOR_SLOT_TEXTURES[lv.getEntitySlotId()]);
            }
         });
      }

      for(i = 0; i < 3; ++i) {
         for(j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(i = 0; i < 9; ++i) {
         this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
      }

      this.addSlot(new Slot(inventory, 40, 77, 62) {
         public void setStack(ItemStack stack) {
            PlayerScreenHandler.onEquipStack(owner, EquipmentSlot.OFFHAND, stack, this.getStack());
            super.setStack(stack);
         }

         public Pair getBackgroundSprite() {
            return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
         }
      });
   }

   static void onEquipStack(PlayerEntity player, EquipmentSlot slot, ItemStack newStack, ItemStack currentStack) {
      Equipment lv = Equipment.fromStack(newStack);
      if (lv != null) {
         player.onEquipStack(slot, currentStack, newStack);
      }

   }

   public static boolean isInHotbar(int slot) {
      return slot >= 36 && slot < 45 || slot == 45;
   }

   public void populateRecipeFinder(RecipeMatcher finder) {
      this.craftingInput.provideRecipeInputs(finder);
   }

   public void clearCraftingSlots() {
      this.craftingResult.clear();
      this.craftingInput.clear();
   }

   public boolean matches(Recipe recipe) {
      return recipe.matches(this.craftingInput, this.owner.world);
   }

   public void onContentChanged(Inventory inventory) {
      CraftingScreenHandler.updateResult(this, this.owner.world, this.owner, this.craftingInput, this.craftingResult);
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);
      this.craftingResult.clear();
      if (!player.world.isClient) {
         this.dropInventory(player, this.craftingInput);
      }
   }

   public boolean canUse(PlayerEntity player) {
      return true;
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack lv = ItemStack.EMPTY;
      Slot lv2 = (Slot)this.slots.get(slot);
      if (lv2.hasStack()) {
         ItemStack lv3 = lv2.getStack();
         lv = lv3.copy();
         EquipmentSlot lv4 = MobEntity.getPreferredEquipmentSlot(lv);
         if (slot == 0) {
            if (!this.insertItem(lv3, 9, 45, true)) {
               return ItemStack.EMPTY;
            }

            lv2.onQuickTransfer(lv3, lv);
         } else if (slot >= 1 && slot < 5) {
            if (!this.insertItem(lv3, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 5 && slot < 9) {
            if (!this.insertItem(lv3, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (lv4.getType() == EquipmentSlot.Type.ARMOR && !((Slot)this.slots.get(8 - lv4.getEntitySlotId())).hasStack()) {
            int j = 8 - lv4.getEntitySlotId();
            if (!this.insertItem(lv3, j, j + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (lv4 == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasStack()) {
            if (!this.insertItem(lv3, 45, 46, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 9 && slot < 36) {
            if (!this.insertItem(lv3, 36, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 36 && slot < 45) {
            if (!this.insertItem(lv3, 9, 36, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(lv3, 9, 45, false)) {
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
      return slot.inventory != this.craftingResult && super.canInsertIntoSlot(stack, slot);
   }

   public int getCraftingResultSlotIndex() {
      return 0;
   }

   public int getCraftingWidth() {
      return this.craftingInput.getWidth();
   }

   public int getCraftingHeight() {
      return this.craftingInput.getHeight();
   }

   public int getCraftingSlotCount() {
      return 5;
   }

   public CraftingInventory getCraftingInput() {
      return this.craftingInput;
   }

   public RecipeBookCategory getCategory() {
      return RecipeBookCategory.CRAFTING;
   }

   public boolean canInsertIntoSlot(int index) {
      return index != this.getCraftingResultSlotIndex();
   }

   static {
      EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{EMPTY_BOOTS_SLOT_TEXTURE, EMPTY_LEGGINGS_SLOT_TEXTURE, EMPTY_CHESTPLATE_SLOT_TEXTURE, EMPTY_HELMET_SLOT_TEXTURE};
      EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   }
}
