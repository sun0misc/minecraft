package net.minecraft.village;

import java.util.Iterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class MerchantInventory implements Inventory {
   private final Merchant merchant;
   private final DefaultedList inventory;
   @Nullable
   private TradeOffer tradeOffer;
   private int offerIndex;
   private int merchantRewardedExperience;

   public MerchantInventory(Merchant merchant) {
      this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
      this.merchant = merchant;
   }

   public int size() {
      return this.inventory.size();
   }

   public boolean isEmpty() {
      Iterator var1 = this.inventory.iterator();

      ItemStack lv;
      do {
         if (!var1.hasNext()) {
            return true;
         }

         lv = (ItemStack)var1.next();
      } while(lv.isEmpty());

      return false;
   }

   public ItemStack getStack(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   public ItemStack removeStack(int slot, int amount) {
      ItemStack lv = (ItemStack)this.inventory.get(slot);
      if (slot == 2 && !lv.isEmpty()) {
         return Inventories.splitStack(this.inventory, slot, lv.getCount());
      } else {
         ItemStack lv2 = Inventories.splitStack(this.inventory, slot, amount);
         if (!lv2.isEmpty() && this.needsOfferUpdate(slot)) {
            this.updateOffers();
         }

         return lv2;
      }
   }

   private boolean needsOfferUpdate(int slot) {
      return slot == 0 || slot == 1;
   }

   public ItemStack removeStack(int slot) {
      return Inventories.removeStack(this.inventory, slot);
   }

   public void setStack(int slot, ItemStack stack) {
      this.inventory.set(slot, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getMaxCountPerStack()) {
         stack.setCount(this.getMaxCountPerStack());
      }

      if (this.needsOfferUpdate(slot)) {
         this.updateOffers();
      }

   }

   public boolean canPlayerUse(PlayerEntity player) {
      return this.merchant.getCustomer() == player;
   }

   public void markDirty() {
      this.updateOffers();
   }

   public void updateOffers() {
      this.tradeOffer = null;
      ItemStack lv;
      ItemStack lv2;
      if (((ItemStack)this.inventory.get(0)).isEmpty()) {
         lv = (ItemStack)this.inventory.get(1);
         lv2 = ItemStack.EMPTY;
      } else {
         lv = (ItemStack)this.inventory.get(0);
         lv2 = (ItemStack)this.inventory.get(1);
      }

      if (lv.isEmpty()) {
         this.setStack(2, ItemStack.EMPTY);
         this.merchantRewardedExperience = 0;
      } else {
         TradeOfferList lv3 = this.merchant.getOffers();
         if (!lv3.isEmpty()) {
            TradeOffer lv4 = lv3.getValidOffer(lv, lv2, this.offerIndex);
            if (lv4 == null || lv4.isDisabled()) {
               this.tradeOffer = lv4;
               lv4 = lv3.getValidOffer(lv2, lv, this.offerIndex);
            }

            if (lv4 != null && !lv4.isDisabled()) {
               this.tradeOffer = lv4;
               this.setStack(2, lv4.copySellItem());
               this.merchantRewardedExperience = lv4.getMerchantExperience();
            } else {
               this.setStack(2, ItemStack.EMPTY);
               this.merchantRewardedExperience = 0;
            }
         }

         this.merchant.onSellingItem(this.getStack(2));
      }
   }

   @Nullable
   public TradeOffer getTradeOffer() {
      return this.tradeOffer;
   }

   public void setOfferIndex(int index) {
      this.offerIndex = index;
      this.updateOffers();
   }

   public void clear() {
      this.inventory.clear();
   }

   public int getMerchantRewardedExperience() {
      return this.merchantRewardedExperience;
   }
}
