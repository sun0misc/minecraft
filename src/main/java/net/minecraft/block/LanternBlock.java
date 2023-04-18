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
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class LanternBlock extends Block implements Waterloggable {
   public static final BooleanProperty HANGING;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape STANDING_SHAPE;
   protected static final VoxelShape HANGING_SHAPE;

   public LanternBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HANGING, false)).with(WATERLOGGED, false));
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      Direction[] var3 = ctx.getPlacementDirections();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction lv2 = var3[var5];
         if (lv2.getAxis() == Direction.Axis.Y) {
            BlockState lv3 = (BlockState)this.getDefaultState().with(HANGING, lv2 == Direction.UP);
            if (lv3.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
               return (BlockState)lv3.with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
            }
         }
      }

      return null;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (Boolean)state.get(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HANGING, WATERLOGGED);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      Direction lv = attachedDirection(state).getOpposite();
      return Block.sideCoversSmallSquare(world, pos.offset(lv), lv.getOpposite());
   }

   protected static Direction attachedDirection(BlockState state) {
      return (Boolean)state.get(HANGING) ? Direction.DOWN : Direction.UP;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return attachedDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      HANGING = Properties.HANGING;
      WATERLOGGED = Properties.WATERLOGGED;
      STANDING_SHAPE = VoxelShapes.union(Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 7.0, 11.0), Block.createCuboidShape(6.0, 7.0, 6.0, 10.0, 9.0, 10.0));
      HANGING_SHAPE = VoxelShapes.union(Block.createCuboidShape(5.0, 1.0, 5.0, 11.0, 8.0, 11.0), Block.createCuboidShape(6.0, 8.0, 6.0, 10.0, 10.0, 10.0));
   }
}
