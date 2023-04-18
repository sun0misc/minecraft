package net.minecraft.block;

import net.minecraft.block.sapling.MangroveSaplingGenerator;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PropaguleBlock extends SaplingBlock implements Waterloggable {
   public static final IntProperty AGE;
   public static final int field_37589 = 4;
   private static final VoxelShape[] SHAPES;
   private static final BooleanProperty WATERLOGGED;
   public static final BooleanProperty HANGING;
   private static final float field_38749 = 0.85F;

   public PropaguleBlock(AbstractBlock.Settings arg) {
      super(new MangroveSaplingGenerator(0.85F), arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(STAGE, 0)).with(AGE, 0)).with(WATERLOGGED, false)).with(HANGING, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return super.canPlantOnTop(floor, world, pos) || floor.isOf(Blocks.CLAY);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      boolean bl = lv.getFluid() == Fluids.WATER;
      return (BlockState)((BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl)).with(AGE, 4);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Vec3d lv = state.getModelOffset(world, pos);
      VoxelShape lv2;
      if (!(Boolean)state.get(HANGING)) {
         lv2 = SHAPES[4];
      } else {
         lv2 = SHAPES[(Integer)state.get(AGE)];
      }

      return lv2.offset(lv.x, lv.y, lv.z);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return isHanging(state) ? world.getBlockState(pos.up()).isOf(Blocks.MANGROVE_LEAVES) : super.canPlaceAt(state, world, pos);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction == Direction.UP && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!isHanging(state)) {
         if (random.nextInt(7) == 0) {
            this.generate(world, pos, state, random);
         }

      } else {
         if (!isFullyGrown(state)) {
            world.setBlockState(pos, (BlockState)state.cycle(AGE), Block.NOTIFY_LISTENERS);
         }

      }
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return !isHanging(state) || !isFullyGrown(state);
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return isHanging(state) ? !isFullyGrown(state) : super.canGrow(world, random, pos, state);
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      if (isHanging(state) && !isFullyGrown(state)) {
         world.setBlockState(pos, (BlockState)state.cycle(AGE), Block.NOTIFY_LISTENERS);
      } else {
         super.grow(world, random, pos, state);
      }

   }

   private static boolean isHanging(BlockState state) {
      return (Boolean)state.get(HANGING);
   }

   private static boolean isFullyGrown(BlockState state) {
      return (Integer)state.get(AGE) == 4;
   }

   public static BlockState getDefaultHangingState() {
      return getHangingState(0);
   }

   public static BlockState getHangingState(int age) {
      return (BlockState)((BlockState)Blocks.MANGROVE_PROPAGULE.getDefaultState().with(HANGING, true)).with(AGE, age);
   }

   static {
      AGE = Properties.AGE_4;
      SHAPES = new VoxelShape[]{Block.createCuboidShape(7.0, 13.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 10.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 7.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 3.0, 7.0, 9.0, 16.0, 9.0), Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)};
      WATERLOGGED = Properties.WATERLOGGED;
      HANGING = Properties.HANGING;
   }
}
