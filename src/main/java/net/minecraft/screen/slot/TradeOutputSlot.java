/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.Stats;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;

public class TradeOutputSlot
extends Slot {
    private final MerchantInventory merchantInventory;
    private final PlayerEntity player;
    private int amount;
    private final Merchant merchant;

    public TradeOutputSlot(PlayerEntity player, Merchant merchant, MerchantInventory merchantInventory, int index, int x, int y) {
        super(merchantInventory, index, x, y);
        this.player = player;
        this.merchant = merchant;
        this.merchantInventory = merchantInventory;
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
    protected void onCrafted(ItemStack stack, int amount) {
        this.amount += amount;
        this.onCrafted(stack);
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
        this.amount = 0;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);
        TradeOffer lv = this.merchantInventory.getTradeOffer();
        if (lv != null) {
            ItemStack lv3;
            ItemStack lv2 = this.merchantInventory.getStack(0);
            if (lv.depleteBuyItems(lv2, lv3 = this.merchantInventory.getStack(1)) || lv.depleteBuyItems(lv3, lv2)) {
                this.merchant.trade(lv);
                player.incrementStat(Stats.TRADED_WITH_VILLAGER);
                this.merchantInventory.setStack(0, lv2);
                this.merchantInventory.setStack(1, lv3);
            }
            this.merchant.setExperienceFromServer(this.merchant.getExperience() + lv.getMerchantExperience());
        }
    }
}

