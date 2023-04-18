package net.minecraft.block;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class MangroveRootsBlock extends Block implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;

   protected MangroveRootsBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, false));
   }

   public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
      return stateFrom.isOf(Blocks.MANGROVE_ROOTS) && direction.getAxis() == Direction.Axis.Y;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      boolean bl = lv.getFluid() == Fluids.WATER;
      return (BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(WATERLOGGED);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
   }
}
