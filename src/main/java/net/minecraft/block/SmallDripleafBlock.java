package net.minecraft.block;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SmallDripleafBlock extends TallPlantBlock implements Fertilizable, Waterloggable {
   private static final BooleanProperty WATERLOGGED;
   public static final DirectionProperty FACING;
   protected static final float field_31246 = 6.0F;
   protected static final VoxelShape SHAPE;

   public SmallDripleafBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HALF, DoubleBlockHalf.LOWER)).with(WATERLOGGED, false)).with(FACING, Direction.NORTH));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isIn(BlockTags.SMALL_DRIPLEAF_PLACEABLE) || world.getFluidState(pos.up()).isEqualAndStill(Fluids.WATER) && super.canPlantOnTop(floor, world, pos);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = super.getPlacementState(ctx);
      return lv != null ? withWaterloggedState(ctx.getWorld(), ctx.getBlockPos(), (BlockState)lv.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())) : null;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (!world.isClient()) {
         BlockPos lv = pos.up();
         BlockState lv2 = TallPlantBlock.withWaterloggedState(world, lv, (BlockState)((BlockState)this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER)).with(FACING, (Direction)state.get(FACING)));
         world.setBlockState(lv, lv2, Block.NOTIFY_ALL);
      }

   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      if (state.get(HALF) == DoubleBlockHalf.UPPER) {
         return super.canPlaceAt(state, world, pos);
      } else {
         BlockPos lv = pos.down();
         BlockState lv2 = world.getBlockState(lv);
         return this.canPlantOnTop(lv2, world, lv);
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HALF, WATERLOGGED, FACING);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockPos lv;
      if (state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
         lv = pos.up();
         world.setBlockState(lv, world.getFluidState(lv).getBlockState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
         BigDripleafBlock.grow((WorldAccess)world, random, pos, (Direction)((Direction)state.get(FACING)));
      } else {
         lv = pos.down();
         this.grow(world, random, lv, world.getBlockState(lv));
      }

   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public float getVerticalModelOffsetMultiplier() {
      return 0.1F;
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      FACING = Properties.HORIZONTAL_FACING;
      SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
   }
}
