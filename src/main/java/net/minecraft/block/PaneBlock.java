package net.minecraft.block;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class PaneBlock extends HorizontalConnectingBlock {
   protected PaneBlock(AbstractBlock.Settings arg) {
      super(1.0F, 1.0F, 16.0F, 16.0F, 16.0F, arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false)).with(WATERLOGGED, false));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockView lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      BlockPos lv4 = lv2.north();
      BlockPos lv5 = lv2.south();
      BlockPos lv6 = lv2.west();
      BlockPos lv7 = lv2.east();
      BlockState lv8 = lv.getBlockState(lv4);
      BlockState lv9 = lv.getBlockState(lv5);
      BlockState lv10 = lv.getBlockState(lv6);
      BlockState lv11 = lv.getBlockState(lv7);
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(NORTH, this.connectsTo(lv8, lv8.isSideSolidFullSquare(lv, lv4, Direction.SOUTH)))).with(SOUTH, this.connectsTo(lv9, lv9.isSideSolidFullSquare(lv, lv5, Direction.NORTH)))).with(WEST, this.connectsTo(lv10, lv10.isSideSolidFullSquare(lv, lv6, Direction.EAST)))).with(EAST, this.connectsTo(lv11, lv11.isSideSolidFullSquare(lv, lv7, Direction.WEST)))).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction.getAxis().isHorizontal() ? (BlockState)state.with((Property)FACING_PROPERTIES.get(direction), this.connectsTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()))) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
      if (stateFrom.isOf(this)) {
         if (!direction.getAxis().isHorizontal()) {
            return true;
         }

         if ((Boolean)state.get((Property)FACING_PROPERTIES.get(direction)) && (Boolean)stateFrom.get((Property)FACING_PROPERTIES.get(direction.getOpposite()))) {
            return true;
         }
      }

      return super.isSideInvisible(state, stateFrom, direction);
   }

   public final boolean connectsTo(BlockState state, boolean sideSolidFullSquare) {
      return !cannotConnect(state) && sideSolidFullSquare || state.getBlock() instanceof PaneBlock || state.isIn(BlockTags.WALLS);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
   }
}
