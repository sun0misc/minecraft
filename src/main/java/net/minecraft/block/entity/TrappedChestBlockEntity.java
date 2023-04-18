package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TrappedChestBlockEntity extends ChestBlockEntity {
   public TrappedChestBlockEntity(BlockPos arg, BlockState arg2) {
      super(BlockEntityType.TRAPPED_CHEST, arg, arg2);
   }

   protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
      super.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount);
      if (oldViewerCount != newViewerCount) {
         Block lv = state.getBlock();
         world.updateNeighborsAlways(pos, lv);
         world.updateNeighborsAlways(pos.down(), lv);
      }

   }
}
