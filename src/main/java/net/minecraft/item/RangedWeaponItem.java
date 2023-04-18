package net.minecraft.item;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;

public abstract class RangedWeaponItem extends Item {
   public static final Predicate BOW_PROJECTILES = (stack) -> {
      return stack.isIn(ItemTags.ARROWS);
   };
   public static final Predicate CROSSBOW_HELD_PROJECTILES;

   public RangedWeaponItem(Item.Settings arg) {
      super(arg);
   }

   public Predicate getHeldProjectiles() {
      return this.getProjectiles();
   }

   public abstract Predicate getProjectiles();

   public static ItemStack getHeldProjectile(LivingEntity entity, Predicate predicate) {
      if (predicate.test(entity.getStackInHand(Hand.OFF_HAND))) {
         return entity.getStackInHand(Hand.OFF_HAND);
      } else {
         return predicate.test(entity.getStackInHand(Hand.MAIN_HAND)) ? entity.getStackInHand(Hand.MAIN_HAND) : ItemStack.EMPTY;
      }
   }

   public int getEnchantability() {
      return 1;
   }

   public abstract int getRange();

   static {
      CROSSBOW_HELD_PROJECTILES = BOW_PROJECTILES.or((stack) -> {
         return stack.isOf(Items.FIREWORK_ROCKET);
      });
   }
}
