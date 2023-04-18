package net.minecraft.village;

import java.util.OptionalInt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface Merchant {
   void setCustomer(@Nullable PlayerEntity customer);

   @Nullable
   PlayerEntity getCustomer();

   TradeOfferList getOffers();

   void setOffersFromServer(TradeOfferList offers);

   void trade(TradeOffer offer);

   void onSellingItem(ItemStack stack);

   int getExperience();

   void setExperienceFromServer(int experience);

   boolean isLeveledMerchant();

   SoundEvent getYesSound();

   default boolean canRefreshTrades() {
      return false;
   }

   default void sendOffers(PlayerEntity player, Text test, int levelProgress) {
      OptionalInt optionalInt = player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> {
         return new MerchantScreenHandler(syncId, playerInventory, this);
      }, test));
      if (optionalInt.isPresent()) {
         TradeOfferList lv = this.getOffers();
         if (!lv.isEmpty()) {
            player.sendTradeOffers(optionalInt.getAsInt(), lv, levelProgress, this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades());
         }
      }

   }

   boolean isClient();
}
