package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class RedstoneBlock extends Block {
   public RedstoneBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return 15;
   }
}
