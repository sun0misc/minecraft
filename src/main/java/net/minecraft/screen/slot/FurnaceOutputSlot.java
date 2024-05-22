/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen.slot;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class FurnaceOutputSlot
extends Slot {
    private final PlayerEntity player;
    private int amount;

    public FurnaceOutputSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (this.hasStack()) {
            this.amount += Math.min(amount, this.getStack().getCount());
        }
        return super.takeStack(amount);
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);
        super.onTakeItem(player, stack);
    }

    @Override
    protected void onCrafted(ItemStack stack, int amount) {
        this.amount += amount;
        this.onCrafted(stack);
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
        Object object = this.player;
        if (object instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv = (ServerPlayerEntity)object;
            object = this.inventory;
            if (object instanceof AbstractFurnaceBlockEntity) {
                AbstractFurnaceBlockEntity lv2 = (AbstractFurnaceBlockEntity)object;
                lv2.dropExperienceForRecipesUsed(lv);
            }
        }
        this.amount = 0;
    }
}

