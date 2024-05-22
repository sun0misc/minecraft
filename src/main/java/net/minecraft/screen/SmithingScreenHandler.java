/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.screen;

import java.util.List;
import java.util.OptionalInt;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class SmithingScreenHandler
extends ForgingScreenHandler {
    public static final int TEMPLATE_ID = 0;
    public static final int EQUIPMENT_ID = 1;
    public static final int MATERIAL_ID = 2;
    public static final int OUTPUT_ID = 3;
    public static final int TEMPLATE_X = 8;
    public static final int EQUIPMENT_X = 26;
    public static final int MATERIAL_X = 44;
    private static final int OUTPUT_X = 98;
    public static final int SLOT_Y = 48;
    private final World world;
    @Nullable
    private RecipeEntry<SmithingRecipe> currentRecipe;
    private final List<RecipeEntry<SmithingRecipe>> recipes;

    public SmithingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public SmithingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ScreenHandlerType.SMITHING, syncId, playerInventory, context);
        this.world = playerInventory.player.getWorld();
        this.recipes = this.world.getRecipeManager().listAllOfType(RecipeType.SMITHING);
    }

    @Override
    protected ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.create().input(0, 8, 48, stack -> this.recipes.stream().anyMatch(recipe -> ((SmithingRecipe)recipe.value()).testTemplate((ItemStack)stack))).input(1, 26, 48, stack -> this.recipes.stream().anyMatch(recipe -> ((SmithingRecipe)recipe.value()).testBase((ItemStack)stack))).input(2, 44, 48, stack -> this.recipes.stream().anyMatch(recipe -> ((SmithingRecipe)recipe.value()).testAddition((ItemStack)stack))).output(3, 98, 48).build();
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isOf(Blocks.SMITHING_TABLE);
    }

    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return this.currentRecipe != null && this.currentRecipe.value().matches(this.createRecipeInput(), this.world);
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        stack.onCraftByPlayer(player.getWorld(), player, stack.getCount());
        this.output.unlockLastRecipe(player, this.getInputStacks());
        this.decrementStack(0);
        this.decrementStack(1);
        this.decrementStack(2);
        this.context.run((world, pos) -> world.syncWorldEvent(WorldEvents.SMITHING_TABLE_USED, (BlockPos)pos, 0));
    }

    private List<ItemStack> getInputStacks() {
        return List.of(this.input.getStack(0), this.input.getStack(1), this.input.getStack(2));
    }

    private SmithingRecipeInput createRecipeInput() {
        return new SmithingRecipeInput(this.input.getStack(0), this.input.getStack(1), this.input.getStack(2));
    }

    private void decrementStack(int slot) {
        ItemStack lv = this.input.getStack(slot);
        if (!lv.isEmpty()) {
            lv.decrement(1);
            this.input.setStack(slot, lv);
        }
    }

    @Override
    public void updateResult() {
        SmithingRecipeInput lv = this.createRecipeInput();
        List<RecipeEntry<SmithingRecipe>> list = this.world.getRecipeManager().getAllMatches(RecipeType.SMITHING, lv, this.world);
        if (list.isEmpty()) {
            this.output.setStack(0, ItemStack.EMPTY);
        } else {
            RecipeEntry<SmithingRecipe> lv2 = list.get(0);
            ItemStack lv3 = lv2.value().craft(lv, this.world.getRegistryManager());
            if (lv3.isItemEnabled(this.world.getEnabledFeatures())) {
                this.currentRecipe = lv2;
                this.output.setLastRecipe(lv2);
                this.output.setStack(0, lv3);
            }
        }
    }

    @Override
    public int getSlotFor(ItemStack stack) {
        return this.getQuickMoveSlot(stack).orElse(0);
    }

    private static OptionalInt getQuickMoveSlot(SmithingRecipe recipe, ItemStack stack) {
        if (recipe.testTemplate(stack)) {
            return OptionalInt.of(0);
        }
        if (recipe.testBase(stack)) {
            return OptionalInt.of(1);
        }
        if (recipe.testAddition(stack)) {
            return OptionalInt.of(2);
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public boolean isValidIngredient(ItemStack stack) {
        return this.getQuickMoveSlot(stack).isPresent();
    }

    private OptionalInt getQuickMoveSlot(ItemStack stack) {
        return this.recipes.stream().flatMapToInt(recipe -> SmithingScreenHandler.getQuickMoveSlot((SmithingRecipe)recipe.value(), stack).stream()).filter(slot -> !this.getSlot(slot).hasStack()).findFirst();
    }
}

