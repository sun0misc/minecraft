package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ChainBlock extends PillarBlock implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   protected static final float field_31054 = 6.5F;
   protected static final float field_31055 = 9.5F;
   protected static final VoxelShape Y_SHAPE;
   protected static final VoxelShape Z_SHAPE;
   protected static final VoxelShape X_SHAPE;

   public ChainBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, false)).with(AXIS, Direction.Axis.Y));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction.Axis)state.get(AXIS)) {
         case X:
         default:
            return X_SHAPE;
         case Z:
            return Z_SHAPE;
         case Y:
            return Y_SHAPE;
      }
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

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(WATERLOGGED).add(AXIS);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      Y_SHAPE = Block.createCuboidShape(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);
      Z_SHAPE = Block.createCuboidShape(6.5, 6.5, 0.0, 9.5, 9.5, 16.0);
      X_SHAPE = Block.createCuboidShape(0.0, 6.5, 6.5, 16.0, 9.5, 9.5);
   }
}
