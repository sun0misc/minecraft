package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EmptyMapItem extends NetworkSyncedItem {
   public EmptyMapItem(Item.Settings arg) {
      super(arg);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      if (world.isClient) {
         return TypedActionResult.success(lv);
      } else {
         if (!user.getAbilities().creativeMode) {
            lv.decrement(1);
         }

         user.incrementStat(Stats.USED.getOrCreateStat(this));
         user.world.playSoundFromEntity((PlayerEntity)null, user, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, user.getSoundCategory(), 1.0F, 1.0F);
         ItemStack lv2 = FilledMapItem.createMap(world, user.getBlockX(), user.getBlockZ(), (byte)0, true, false);
         if (lv.isEmpty()) {
            return TypedActionResult.consume(lv2);
         } else {
            if (!user.getInventory().insertStack(lv2.copy())) {
               user.dropItem(lv2, false);
            }

            return TypedActionResult.consume(lv);
         }
      }
   }
}
