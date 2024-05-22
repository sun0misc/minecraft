/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.ArmorSlot;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class PlayerScreenHandler
extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {
    public static final int field_30802 = 0;
    public static final int CRAFTING_RESULT_ID = 0;
    public static final int CRAFTING_INPUT_START = 1;
    public static final int CRAFTING_INPUT_COUNT = 4;
    public static final int CRAFTING_INPUT_END = 5;
    public static final int EQUIPMENT_START = 5;
    public static final int EQUIPMENT_COUNT = 4;
    public static final int EQUIPMENT_END = 9;
    public static final int INVENTORY_START = 9;
    public static final int INVENTORY_END = 36;
    public static final int HOTBAR_START = 36;
    public static final int HOTBAR_END = 45;
    public static final int OFFHAND_ID = 45;
    public static final Identifier BLOCK_ATLAS_TEXTURE = Identifier.method_60656("textures/atlas/blocks.png");
    public static final Identifier EMPTY_HELMET_SLOT_TEXTURE = Identifier.method_60656("item/empty_armor_slot_helmet");
    public static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = Identifier.method_60656("item/empty_armor_slot_chestplate");
    public static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = Identifier.method_60656("item/empty_armor_slot_leggings");
    public static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = Identifier.method_60656("item/empty_armor_slot_boots");
    public static final Identifier EMPTY_OFFHAND_ARMOR_SLOT = Identifier.method_60656("item/empty_armor_slot_shield");
    private static final Map<EquipmentSlot, Identifier> EMPTY_ARMOR_SLOT_TEXTURES = Map.of(EquipmentSlot.FEET, EMPTY_BOOTS_SLOT_TEXTURE, EquipmentSlot.LEGS, EMPTY_LEGGINGS_SLOT_TEXTURE, EquipmentSlot.CHEST, EMPTY_CHESTPLATE_SLOT_TEXTURE, EquipmentSlot.HEAD, EMPTY_HELMET_SLOT_TEXTURE);
    private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final RecipeInputInventory craftingInput = new CraftingInventory(this, 2, 2);
    private final CraftingResultInventory craftingResult = new CraftingResultInventory();
    public final boolean onServer;
    private final PlayerEntity owner;

    public PlayerScreenHandler(PlayerInventory inventory, boolean onServer, final PlayerEntity owner) {
        super(null, 0);
        int i;
        this.onServer = onServer;
        this.owner = owner;
        this.addSlot(new CraftingResultSlot(inventory.player, this.craftingInput, this.craftingResult, 0, 154, 28));
        for (i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.addSlot(new Slot(this.craftingInput, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }
        for (i = 0; i < 4; ++i) {
            EquipmentSlot lv = EQUIPMENT_SLOT_ORDER[i];
            Identifier lv2 = EMPTY_ARMOR_SLOT_TEXTURES.get(lv);
            this.addSlot(new ArmorSlot(inventory, owner, lv, 39 - i, 8, 8 + i * 18, lv2));
        }
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
        this.addSlot(new Slot(this, inventory, 40, 77, 62){

            @Override
            public void setStack(ItemStack stack, ItemStack previousStack) {
                owner.onEquipStack(EquipmentSlot.OFFHAND, previousStack, stack);
                super.setStack(stack, previousStack);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BLOCK_ATLAS_TEXTURE, EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });
    }

    public static boolean isInHotbar(int slot) {
        return slot >= 36 && slot < 45 || slot == 45;
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.craftingInput.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.craftingResult.clear();
        this.craftingInput.clear();
    }

    @Override
    public boolean matches(RecipeEntry<CraftingRecipe> recipe) {
        return recipe.value().matches(this.craftingInput.createRecipeInput(), this.owner.getWorld());
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        CraftingScreenHandler.updateResult(this, this.owner.getWorld(), this.owner, this.craftingInput, this.craftingResult, null);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.craftingResult.clear();
        if (player.getWorld().isClient) {
            return;
        }
        this.dropInventory(player, this.craftingInput);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack lv = ItemStack.EMPTY;
        Slot lv2 = (Slot)this.slots.get(slot);
        if (lv2.hasStack()) {
            int j;
            ItemStack lv3 = lv2.getStack();
            lv = lv3.copy();
            EquipmentSlot lv4 = player.getPreferredEquipmentSlot(lv);
            if (slot == 0) {
                if (!this.insertItem(lv3, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
                lv2.onQuickTransfer(lv3, lv);
            } else if (slot >= 1 && slot < 5 ? !this.insertItem(lv3, 9, 45, false) : (slot >= 5 && slot < 9 ? !this.insertItem(lv3, 9, 45, false) : (lv4.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !((Slot)this.slots.get(8 - lv4.getEntitySlotId())).hasStack() ? !this.insertItem(lv3, j = 8 - lv4.getEntitySlotId(), j + 1, false) : (lv4 == EquipmentSlot.OFFHAND && !((Slot)this.slots.get(45)).hasStack() ? !this.insertItem(lv3, 45, 46, false) : (slot >= 9 && slot < 36 ? !this.insertItem(lv3, 36, 45, false) : (slot >= 36 && slot < 45 ? !this.insertItem(lv3, 9, 36, false) : !this.insertItem(lv3, 9, 45, false))))))) {
                return ItemStack.EMPTY;
            }
            if (lv3.isEmpty()) {
                lv2.setStack(ItemStack.EMPTY, lv);
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

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.craftingResult && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.craftingInput.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.craftingInput.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 5;
    }

    public RecipeInputInventory getCraftingInput() {
        return this.craftingInput;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }
}

