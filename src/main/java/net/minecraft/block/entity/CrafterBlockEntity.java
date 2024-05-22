/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrafterBlockEntity
extends LootableContainerBlockEntity
implements RecipeInputInventory {
    public static final int GRID_WIDTH = 3;
    public static final int GRID_HEIGHT = 3;
    public static final int GRID_SIZE = 9;
    public static final int SLOT_DISABLED = 1;
    public static final int SLOT_ENABLED = 0;
    public static final int TRIGGERED_PROPERTY = 9;
    public static final int PROPERTIES_COUNT = 10;
    private DefaultedList<ItemStack> inputStacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int craftingTicksRemaining = 0;
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate(this){
        private final int[] disabledSlots = new int[9];
        private int triggered = 0;

        @Override
        public int get(int index) {
            return index == 9 ? this.triggered : this.disabledSlots[index];
        }

        @Override
        public void set(int index, int value) {
            if (index == 9) {
                this.triggered = value;
            } else {
                this.disabledSlots[index] = value;
            }
        }

        @Override
        public int size() {
            return 10;
        }
    };

    public CrafterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CRAFTER, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.crafter");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CrafterScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    public void setSlotEnabled(int slot, boolean enabled) {
        if (!this.canToggleSlot(slot)) {
            return;
        }
        this.propertyDelegate.set(slot, enabled ? 0 : 1);
        this.markDirty();
    }

    public boolean isSlotDisabled(int slot) {
        if (slot >= 0 && slot < 9) {
            return this.propertyDelegate.get(slot) == 1;
        }
        return false;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (this.propertyDelegate.get(slot) == 1) {
            return false;
        }
        ItemStack lv = this.inputStacks.get(slot);
        int j = lv.getCount();
        if (j >= lv.getMaxCount()) {
            return false;
        }
        if (lv.isEmpty()) {
            return true;
        }
        return !this.betterSlotExists(j, lv, slot);
    }

    private boolean betterSlotExists(int count, ItemStack stack, int slot) {
        for (int k = slot + 1; k < 9; ++k) {
            ItemStack lv;
            if (this.isSlotDisabled(k) || !(lv = this.getStack(k)).isEmpty() && (lv.getCount() >= count || !ItemStack.areItemsAndComponentsEqual(lv, stack))) continue;
            return true;
        }
        return false;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.craftingTicksRemaining = nbt.getInt("crafting_ticks_remaining");
        this.inputStacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inputStacks, registryLookup);
        }
        int[] is = nbt.getIntArray("disabled_slots");
        for (int i = 0; i < 9; ++i) {
            this.propertyDelegate.set(i, 0);
        }
        for (int j : is) {
            if (!this.canToggleSlot(j)) continue;
            this.propertyDelegate.set(j, 1);
        }
        this.propertyDelegate.set(9, nbt.getInt("triggered"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
        if (!this.writeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inputStacks, registryLookup);
        }
        this.putDisabledSlots(nbt);
        this.putTriggered(nbt);
    }

    @Override
    public int size() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack lv : this.inputStacks) {
            if (lv.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inputStacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (this.isSlotDisabled(slot)) {
            this.setSlotEnabled(slot, true);
        }
        super.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return this.inputStacks;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inputStacks = inventory;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack lv : this.inputStacks) {
            finder.addUnenchantedInput(lv);
        }
    }

    private void putDisabledSlots(NbtCompound nbt) {
        IntArrayList intList = new IntArrayList();
        for (int i = 0; i < 9; ++i) {
            if (!this.isSlotDisabled(i)) continue;
            intList.add(i);
        }
        nbt.putIntArray("disabled_slots", intList);
    }

    private void putTriggered(NbtCompound nbt) {
        nbt.putInt("triggered", this.propertyDelegate.get(9));
    }

    public void setTriggered(boolean triggered) {
        this.propertyDelegate.set(9, triggered ? 1 : 0);
    }

    @VisibleForTesting
    public boolean isTriggered() {
        return this.propertyDelegate.get(9) == 1;
    }

    public static void tickCrafting(World world, BlockPos pos, BlockState state, CrafterBlockEntity blockEntity) {
        int i = blockEntity.craftingTicksRemaining - 1;
        if (i < 0) {
            return;
        }
        blockEntity.craftingTicksRemaining = i;
        if (i == 0) {
            world.setBlockState(pos, (BlockState)state.with(CrafterBlock.CRAFTING, false), Block.NOTIFY_ALL);
        }
    }

    public void setCraftingTicksRemaining(int craftingTicksRemaining) {
        this.craftingTicksRemaining = craftingTicksRemaining;
    }

    public int getComparatorOutput() {
        int i = 0;
        for (int j = 0; j < this.size(); ++j) {
            ItemStack lv = this.getStack(j);
            if (lv.isEmpty() && !this.isSlotDisabled(j)) continue;
            ++i;
        }
        return i;
    }

    private boolean canToggleSlot(int slot) {
        return slot > -1 && slot < 9 && this.inputStacks.get(slot).isEmpty();
    }

    public /* synthetic */ List getHeldStacks() {
        return this.getHeldStacks();
    }
}

