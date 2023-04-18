package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface Equipment extends Vanishable {
   EquipmentSlot getSlotType();

   default SoundEvent getEquipSound() {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
   }

   default TypedActionResult equipAndSwap(Item item, World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      EquipmentSlot lv2 = MobEntity.getPreferredEquipmentSlot(lv);
      ItemStack lv3 = user.getEquippedStack(lv2);
      if (!EnchantmentHelper.hasBindingCurse(lv3) && !ItemStack.areEqual(lv, lv3)) {
         if (!world.isClient()) {
            user.incrementStat(Stats.USED.getOrCreateStat(item));
         }

         ItemStack lv4 = lv3.isEmpty() ? lv : lv3.copyAndEmpty();
         ItemStack lv5 = lv.copyAndEmpty();
         user.equipStack(lv2, lv5);
         return TypedActionResult.success(lv4, world.isClient());
      } else {
         return TypedActionResult.fail(lv);
      }
   }

   @Nullable
   static Equipment fromStack(ItemStack stack) {
      Item var2 = stack.getItem();
      if (var2 instanceof Equipment lv) {
         return lv;
      } else {
         Item var3 = stack.getItem();
         if (var3 instanceof BlockItem lv2) {
            Block var6 = lv2.getBlock();
            if (var6 instanceof Equipment lv3) {
               return lv3;
            }
         }

         return null;
      }
   }
}
