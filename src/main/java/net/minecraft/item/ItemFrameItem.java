package net.minecraft.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ItemFrameItem extends DecorationItem {
   public ItemFrameItem(EntityType arg, Item.Settings arg2) {
      super(arg, arg2);
   }

   protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
      return !player.world.isOutOfHeightLimit(pos) && player.canPlaceOn(pos, side, stack);
   }
}
