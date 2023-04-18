package net.minecraft.block;

import java.util.Optional;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BigDripleafStemBlock extends HorizontalFacingBlock implements Fertilizable, Waterloggable {
   private static final BooleanProperty WATERLOGGED;
   private static final int field_31021 = 6;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape EAST_SHAPE;
   protected static final VoxelShape WEST_SHAPE;

   protected BigDripleafStemBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction)state.get(FACING)) {
         case SOUTH:
            return SOUTH_SHAPE;
         case NORTH:
         default:
            return NORTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         case EAST:
            return EAST_SHAPE;
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(WATERLOGGED, FACING);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.down();
      BlockState lv2 = world.getBlockState(lv);
      BlockState lv3 = world.getBlockState(pos.up());
      return (lv2.isOf(this) || lv2.isIn(BlockTags.BIG_DRIPLEAF_PLACEABLE)) && (lv3.isOf(this) || lv3.isOf(Blocks.BIG_DRIPLEAF));
   }

   protected static boolean placeStemAt(WorldAccess world, BlockPos pos, FluidState fluidState, Direction direction) {
      BlockState lv = (BlockState)((BlockState)Blocks.BIG_DRIPLEAF_STEM.getDefaultState().with(WATERLOGGED, fluidState.isEqualAndStill(Fluids.WATER))).with(FACING, direction);
      return world.setBlockState(pos, lv, Block.NOTIFY_ALL);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((direction == Direction.DOWN || direction == Direction.UP) && !state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      Optional optional = BlockLocating.findColumnEnd(world, pos, state.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (!optional.isPresent()) {
         return false;
      } else {
         BlockPos lv = ((BlockPos)optional.get()).up();
         BlockState lv2 = world.getBlockState(lv);
         return BigDripleafBlock.canGrowInto(world, lv, lv2);
      }
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      Optional optional = BlockLocating.findColumnEnd(world, pos, state.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (optional.isPresent()) {
         BlockPos lv = (BlockPos)optional.get();
         BlockPos lv2 = lv.up();
         Direction lv3 = (Direction)state.get(FACING);
         placeStemAt(world, lv, world.getFluidState(lv), lv3);
         BigDripleafBlock.placeDripleafAt(world, lv2, world.getFluidState(lv2), lv3);
      }
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(Blocks.BIG_DRIPLEAF);
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      NORTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 9.0, 11.0, 16.0, 15.0);
      SOUTH_SHAPE = Block.createCuboidShape(5.0, 0.0, 1.0, 11.0, 16.0, 7.0);
      EAST_SHAPE = Block.createCuboidShape(1.0, 0.0, 5.0, 7.0, 16.0, 11.0);
      WEST_SHAPE = Block.createCuboidShape(9.0, 0.0, 5.0, 15.0, 16.0, 11.0);
   }
}
