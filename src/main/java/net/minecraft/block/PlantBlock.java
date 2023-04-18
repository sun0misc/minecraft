package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PlantBlock extends Block {
   protected PlantBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.FARMLAND);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.down();
      return this.canPlantOnTop(world.getBlockState(lv), world, lv);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return state.getFluidState().isEmpty();
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return type == NavigationType.AIR && !this.collidable ? true : super.canPathfindThrough(state, world, pos, type);
   }
}
