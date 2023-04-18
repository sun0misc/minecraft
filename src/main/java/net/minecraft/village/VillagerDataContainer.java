package net.minecraft.village;

import net.minecraft.entity.VariantHolder;

public interface VillagerDataContainer extends VariantHolder {
   VillagerData getVillagerData();

   void setVillagerData(VillagerData villagerData);

   default VillagerType getVariant() {
      return this.getVillagerData().getType();
   }

   default void setVariant(VillagerType arg) {
      this.setVillagerData(this.getVillagerData().withType(arg));
   }

   // $FF: synthetic method
   default Object getVariant() {
      return this.getVariant();
   }
}
