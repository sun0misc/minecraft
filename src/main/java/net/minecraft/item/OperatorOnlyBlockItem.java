package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class OperatorOnlyBlockItem extends BlockItem {
   public OperatorOnlyBlockItem(Block arg, Item.Settings arg2) {
      super(arg, arg2);
   }

   @Nullable
   protected BlockState getPlacementState(ItemPlacementContext context) {
      PlayerEntity lv = context.getPlayer();
      return lv != null && !lv.isCreativeLevelTwoOp() ? null : super.getPlacementState(context);
   }
}
