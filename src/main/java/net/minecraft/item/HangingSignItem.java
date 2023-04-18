package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class HangingSignItem extends SignItem {
   public HangingSignItem(Block hangingSign, Block wallHangingSign, Item.Settings settings) {
      super(settings, hangingSign, wallHangingSign, Direction.UP);
   }

   protected boolean canPlaceAt(WorldView world, BlockState state, BlockPos pos) {
      Block var5 = state.getBlock();
      if (var5 instanceof WallHangingSignBlock lv) {
         if (!lv.canAttachAt(state, world, pos)) {
            return false;
         }
      }

      return super.canPlaceAt(world, state, pos);
   }
}
