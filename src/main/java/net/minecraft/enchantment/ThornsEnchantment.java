package net.minecraft.enchantment;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public class ThornsEnchantment extends Enchantment {
   private static final float ATTACK_CHANCE_PER_LEVEL = 0.15F;

   public ThornsEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_CHEST, slotTypes);
   }

   public int getMinPower(int level) {
      return 10 + 20 * (level - 1);
   }

   public int getMaxPower(int level) {
      return super.getMinPower(level) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean isAcceptableItem(ItemStack stack) {
      return stack.getItem() instanceof ArmorItem ? true : super.isAcceptableItem(stack);
   }

   public void onUserDamaged(LivingEntity user, Entity attacker, int level) {
      Random lv = user.getRandom();
      Map.Entry entry = EnchantmentHelper.chooseEquipmentWith(Enchantments.THORNS, user);
      if (shouldDamageAttacker(level, lv)) {
         if (attacker != null) {
            attacker.damage(user.getDamageSources().thorns(user), (float)getDamageAmount(level, lv));
         }

         if (entry != null) {
            ((ItemStack)entry.getValue()).damage(2, (LivingEntity)user, (Consumer)((entity) -> {
               entity.sendEquipmentBreakStatus((EquipmentSlot)entry.getKey());
            }));
         }
      }

   }

   public static boolean shouldDamageAttacker(int level, Random random) {
      if (level <= 0) {
         return false;
      } else {
         return random.nextFloat() < 0.15F * (float)level;
      }
   }

   public static int getDamageAmount(int level, Random random) {
      return level > 10 ? level - 10 : 1 + random.nextInt(4);
   }
}
