package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SnowballItem extends Item {
   public SnowballItem(Item.Settings arg) {
      super(arg);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
      if (!world.isClient) {
         SnowballEntity lv2 = new SnowballEntity(world, user);
         lv2.setItem(lv);
         lv2.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
         world.spawnEntity(lv2);
      }

      user.incrementStat(Stats.USED.getOrCreateStat(this));
      if (!user.getAbilities().creativeMode) {
         lv.decrement(1);
      }

      return TypedActionResult.success(lv, world.isClient());
   }
}
