package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TallBlockItem extends BlockItem {
   public TallBlockItem(Block arg, Item.Settings arg2) {
      super(arg, arg2);
   }

   protected boolean place(ItemPlacementContext context, BlockState state) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos().up();
      BlockState lv3 = lv.isWater(lv2) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
      lv.setBlockState(lv2, lv3, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD | Block.FORCE_STATE);
      return super.place(context, state);
   }
}
