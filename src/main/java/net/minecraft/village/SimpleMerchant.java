/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.village;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

public class SimpleMerchant
implements Merchant {
    private final PlayerEntity player;
    private TradeOfferList offers = new TradeOfferList();
    private int experience;

    public SimpleMerchant(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public PlayerEntity getCustomer() {
        return this.player;
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity customer) {
    }

    @Override
    public TradeOfferList getOffers() {
        return this.offers;
    }

    @Override
    public void setOffersFromServer(TradeOfferList offers) {
        this.offers = offers;
    }

    @Override
    public void trade(TradeOffer offer) {
        offer.use();
    }

    @Override
    public void onSellingItem(ItemStack stack) {
    }

    @Override
    public boolean isClient() {
        return this.player.getWorld().isClient;
    }

    @Override
    public int getExperience() {
        return this.experience;
    }

    @Override
    public void setExperienceFromServer(int experience) {
        this.experience = experience;
    }

    @Override
    public boolean isLeveledMerchant() {
        return true;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }
}

