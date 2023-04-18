package net.minecraft.client.item;

import net.minecraft.util.collection.DefaultedList;

public class BundleTooltipData implements TooltipData {
   private final DefaultedList inventory;
   private final int bundleOccupancy;

   public BundleTooltipData(DefaultedList inventory, int bundleOccupancy) {
      this.inventory = inventory;
      this.bundleOccupancy = bundleOccupancy;
   }

   public DefaultedList getInventory() {
      return this.inventory;
   }

   public int getBundleOccupancy() {
      return this.bundleOccupancy;
   }
}
