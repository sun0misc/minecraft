package net.minecraft.enchantment;

import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.Vanishable;

public enum EnchantmentTarget {
   ARMOR {
      public boolean isAcceptableItem(Item item) {
         return item instanceof ArmorItem;
      }
   },
   ARMOR_FEET {
      public boolean isAcceptableItem(Item item) {
         boolean var10000;
         if (item instanceof ArmorItem lv) {
            if (lv.getSlotType() == EquipmentSlot.FEET) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   },
   ARMOR_LEGS {
      public boolean isAcceptableItem(Item item) {
         boolean var10000;
         if (item instanceof ArmorItem lv) {
            if (lv.getSlotType() == EquipmentSlot.LEGS) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   },
   ARMOR_CHEST {
      public boolean isAcceptableItem(Item item) {
         boolean var10000;
         if (item instanceof ArmorItem lv) {
            if (lv.getSlotType() == EquipmentSlot.CHEST) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   },
   ARMOR_HEAD {
      public boolean isAcceptableItem(Item item) {
         boolean var10000;
         if (item instanceof ArmorItem lv) {
            if (lv.getSlotType() == EquipmentSlot.HEAD) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   },
   WEAPON {
      public boolean isAcceptableItem(Item item) {
         return item instanceof SwordItem;
      }
   },
   DIGGER {
      public boolean isAcceptableItem(Item item) {
         return item instanceof MiningToolItem;
      }
   },
   FISHING_ROD {
      public boolean isAcceptableItem(Item item) {
         return item instanceof FishingRodItem;
      }
   },
   TRIDENT {
      public boolean isAcceptableItem(Item item) {
         return item instanceof TridentItem;
      }
   },
   BREAKABLE {
      public boolean isAcceptableItem(Item item) {
         return item.isDamageable();
      }
   },
   BOW {
      public boolean isAcceptableItem(Item item) {
         return item instanceof BowItem;
      }
   },
   WEARABLE {
      public boolean isAcceptableItem(Item item) {
         return item instanceof Equipment || Block.getBlockFromItem(item) instanceof Equipment;
      }
   },
   CROSSBOW {
      public boolean isAcceptableItem(Item item) {
         return item instanceof CrossbowItem;
      }
   },
   VANISHABLE {
      public boolean isAcceptableItem(Item item) {
         return item instanceof Vanishable || Block.getBlockFromItem(item) instanceof Vanishable || BREAKABLE.isAcceptableItem(item);
      }
   };

   public abstract boolean isAcceptableItem(Item item);

   // $FF: synthetic method
   private static EnchantmentTarget[] method_36688() {
      return new EnchantmentTarget[]{ARMOR, ARMOR_FEET, ARMOR_LEGS, ARMOR_CHEST, ARMOR_HEAD, WEAPON, DIGGER, FISHING_ROD, TRIDENT, BREAKABLE, BOW, WEARABLE, CROSSBOW, VANISHABLE};
   }
}
