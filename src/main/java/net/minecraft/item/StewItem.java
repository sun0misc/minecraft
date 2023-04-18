package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class StewItem extends Item {
   public StewItem(Item.Settings arg) {
      super(arg);
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      ItemStack lv = super.finishUsing(stack, world, user);
      return user instanceof PlayerEntity && ((PlayerEntity)user).getAbilities().creativeMode ? lv : new ItemStack(Items.BOWL);
   }
}
