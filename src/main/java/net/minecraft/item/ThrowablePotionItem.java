package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ThrowablePotionItem extends PotionItem {
   public ThrowablePotionItem(Item.Settings arg) {
      super(arg);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      if (!world.isClient) {
         PotionEntity lv2 = new PotionEntity(world, user);
         lv2.setItem(lv);
         lv2.setVelocity(user, user.getPitch(), user.getYaw(), -20.0F, 0.5F, 1.0F);
         world.spawnEntity(lv2);
      }

      user.incrementStat(Stats.USED.getOrCreateStat(this));
      if (!user.getAbilities().creativeMode) {
         lv.decrement(1);
      }

      return TypedActionResult.success(lv, world.isClient());
   }
}
