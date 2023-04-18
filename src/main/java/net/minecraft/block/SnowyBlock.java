package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class SnowyBlock extends Block {
   public static final BooleanProperty SNOWY;

   protected SnowyBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SNOWY, false));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.UP ? (BlockState)state.with(SNOWY, isSnow(neighborState)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
      return (BlockState)this.getDefaultState().with(SNOWY, isSnow(lv));
   }

   private static boolean isSnow(BlockState state) {
      return state.isIn(BlockTags.SNOW);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(SNOWY);
   }

   static {
      SNOWY = Properties.SNOWY;
   }
}
