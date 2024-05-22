/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class CraftingInventory
implements RecipeInputInventory {
    private final DefaultedList<ItemStack> stacks;
    private final int width;
    private final int height;
    private final ScreenHandler handler;

    public CraftingInventory(ScreenHandler handler, int width, int height) {
        this(handler, width, height, DefaultedList.ofSize(width * height, ItemStack.EMPTY));
    }

    public CraftingInventory(ScreenHandler handler, int width, int height, DefaultedList<ItemStack> stacks) {
        this.stacks = stacks;
        this.handler = handler;
        this.width = width;
        this.height = height;
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack lv : this.stacks) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= this.size()) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack lv = Inventories.splitStack(this.stacks, slot, amount);
        if (!lv.isEmpty()) {
            this.handler.onContentChanged(this);
        }
        return lv;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        this.handler.onContentChanged(this);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public List<ItemStack> getHeldStacks() {
        return List.copyOf(this.stacks);
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack lv : this.stacks) {
            finder.addUnenchantedInput(lv);
        }
    }
}

