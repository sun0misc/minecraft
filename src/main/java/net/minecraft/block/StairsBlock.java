package net.minecraft.block;

import java.util.stream.IntStream;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;

public class StairsBlock extends Block implements Waterloggable {
   public static final DirectionProperty FACING;
   public static final EnumProperty HALF;
   public static final EnumProperty SHAPE;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape TOP_SHAPE;
   protected static final VoxelShape BOTTOM_SHAPE;
   protected static final VoxelShape BOTTOM_NORTH_WEST_CORNER_SHAPE;
   protected static final VoxelShape BOTTOM_SOUTH_WEST_CORNER_SHAPE;
   protected static final VoxelShape TOP_NORTH_WEST_CORNER_SHAPE;
   protected static final VoxelShape TOP_SOUTH_WEST_CORNER_SHAPE;
   protected static final VoxelShape BOTTOM_NORTH_EAST_CORNER_SHAPE;
   protected static final VoxelShape BOTTOM_SOUTH_EAST_CORNER_SHAPE;
   protected static final VoxelShape TOP_NORTH_EAST_CORNER_SHAPE;
   protected static final VoxelShape TOP_SOUTH_EAST_CORNER_SHAPE;
   protected static final VoxelShape[] TOP_SHAPES;
   protected static final VoxelShape[] BOTTOM_SHAPES;
   private static final int[] SHAPE_INDICES;
   private final Block baseBlock;
   private final BlockState baseBlockState;

   private static VoxelShape[] composeShapes(VoxelShape base, VoxelShape northWest, VoxelShape northEast, VoxelShape southWest, VoxelShape southEast) {
      return (VoxelShape[])IntStream.range(0, 16).mapToObj((i) -> {
         return composeShape(i, base, northWest, northEast, southWest, southEast);
      }).toArray((i) -> {
         return new VoxelShape[i];
      });
   }

   private static VoxelShape composeShape(int i, VoxelShape base, VoxelShape northWest, VoxelShape northEast, VoxelShape southWest, VoxelShape southEast) {
      VoxelShape lv = base;
      if ((i & 1) != 0) {
         lv = VoxelShapes.union(base, northWest);
      }

      if ((i & 2) != 0) {
         lv = VoxelShapes.union(lv, northEast);
      }

      if ((i & 4) != 0) {
         lv = VoxelShapes.union(lv, southWest);
      }

      if ((i & 8) != 0) {
         lv = VoxelShapes.union(lv, southEast);
      }

      return lv;
   }

   protected StairsBlock(BlockState baseBlockState, AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(HALF, BlockHalf.BOTTOM)).with(SHAPE, StairShape.STRAIGHT)).with(WATERLOGGED, false));
      this.baseBlock = baseBlockState.getBlock();
      this.baseBlockState = baseBlockState;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (state.get(HALF) == BlockHalf.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_INDICES[this.getShapeIndexIndex(state)]];
   }

   private int getShapeIndexIndex(BlockState state) {
      return ((StairShape)state.get(SHAPE)).ordinal() * 4 + ((Direction)state.get(FACING)).getHorizontal();
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      this.baseBlock.randomDisplayTick(state, world, pos, random);
   }

   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
      this.baseBlockState.onBlockBreakStart(world, pos, player);
   }

   public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
      this.baseBlock.onBroken(world, pos, state);
   }

   public float getBlastResistance() {
      return this.baseBlock.getBlastResistance();
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!state.isOf(state.getBlock())) {
         world.updateNeighbor(this.baseBlockState, pos, Blocks.AIR, pos, false);
         this.baseBlock.onBlockAdded(this.baseBlockState, world, pos, oldState, false);
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         this.baseBlockState.onStateReplaced(world, pos, newState, moved);
      }
   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
      this.baseBlock.onSteppedOn(world, pos, state, entity);
   }

   public boolean hasRandomTicks(BlockState state) {
      return this.baseBlock.hasRandomTicks(state);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      this.baseBlock.randomTick(state, world, pos, random);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      this.baseBlock.scheduledTick(state, world, pos, random);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      return this.baseBlockState.onUse(world, player, hand, hit);
   }

   public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
      this.baseBlock.onDestroyedByExplosion(world, pos, explosion);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction lv = ctx.getSide();
      BlockPos lv2 = ctx.getBlockPos();
      FluidState lv3 = ctx.getWorld().getFluidState(lv2);
      BlockState lv4 = (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(HALF, lv != Direction.DOWN && (lv == Direction.UP || !(ctx.getHitPos().y - (double)lv2.getY() > 0.5)) ? BlockHalf.BOTTOM : BlockHalf.TOP)).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
      return (BlockState)lv4.with(SHAPE, getStairShape(lv4, ctx.getWorld(), lv2));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return direction.getAxis().isHorizontal() ? (BlockState)state.with(SHAPE, getStairShape(state, world, pos)) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   private static StairShape getStairShape(BlockState state, BlockView world, BlockPos pos) {
      Direction lv = (Direction)state.get(FACING);
      BlockState lv2 = world.getBlockState(pos.offset(lv));
      if (isStairs(lv2) && state.get(HALF) == lv2.get(HALF)) {
         Direction lv3 = (Direction)lv2.get(FACING);
         if (lv3.getAxis() != ((Direction)state.get(FACING)).getAxis() && isDifferentOrientation(state, world, pos, lv3.getOpposite())) {
            if (lv3 == lv.rotateYCounterclockwise()) {
               return StairShape.OUTER_LEFT;
            }

            return StairShape.OUTER_RIGHT;
         }
      }

      BlockState lv4 = world.getBlockState(pos.offset(lv.getOpposite()));
      if (isStairs(lv4) && state.get(HALF) == lv4.get(HALF)) {
         Direction lv5 = (Direction)lv4.get(FACING);
         if (lv5.getAxis() != ((Direction)state.get(FACING)).getAxis() && isDifferentOrientation(state, world, pos, lv5)) {
            if (lv5 == lv.rotateYCounterclockwise()) {
               return StairShape.INNER_LEFT;
            }

            return StairShape.INNER_RIGHT;
         }
      }

      return StairShape.STRAIGHT;
   }

   private static boolean isDifferentOrientation(BlockState state, BlockView world, BlockPos pos, Direction dir) {
      BlockState lv = world.getBlockState(pos.offset(dir));
      return !isStairs(lv) || lv.get(FACING) != state.get(FACING) || lv.get(HALF) != state.get(HALF);
   }

   public static boolean isStairs(BlockState state) {
      return state.getBlock() instanceof StairsBlock;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      Direction lv = (Direction)state.get(FACING);
      StairShape lv2 = (StairShape)state.get(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            if (lv.getAxis() == Direction.Axis.Z) {
               switch (lv2) {
                  case INNER_LEFT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                  case INNER_RIGHT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                  case OUTER_LEFT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                  case OUTER_RIGHT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                  default:
                     return state.rotate(BlockRotation.CLOCKWISE_180);
               }
            }
            break;
         case FRONT_BACK:
            if (lv.getAxis() == Direction.Axis.X) {
               switch (lv2) {
                  case INNER_LEFT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                  case INNER_RIGHT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                  case OUTER_LEFT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                  case OUTER_RIGHT:
                     return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                  case STRAIGHT:
                     return state.rotate(BlockRotation.CLOCKWISE_180);
               }
            }
      }

      return super.mirror(state, mirror);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, HALF, SHAPE, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      HALF = Properties.BLOCK_HALF;
      SHAPE = Properties.STAIR_SHAPE;
      WATERLOGGED = Properties.WATERLOGGED;
      TOP_SHAPE = SlabBlock.TOP_SHAPE;
      BOTTOM_SHAPE = SlabBlock.BOTTOM_SHAPE;
      BOTTOM_NORTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 8.0, 8.0, 8.0);
      BOTTOM_SOUTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0, 0.0, 8.0, 8.0, 8.0, 16.0);
      TOP_NORTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
      TOP_SOUTH_WEST_CORNER_SHAPE = Block.createCuboidShape(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
      BOTTOM_NORTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0, 0.0, 0.0, 16.0, 8.0, 8.0);
      BOTTOM_SOUTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0, 0.0, 8.0, 16.0, 8.0, 16.0);
      TOP_NORTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
      TOP_SOUTH_EAST_CORNER_SHAPE = Block.createCuboidShape(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
      TOP_SHAPES = composeShapes(TOP_SHAPE, BOTTOM_NORTH_WEST_CORNER_SHAPE, BOTTOM_NORTH_EAST_CORNER_SHAPE, BOTTOM_SOUTH_WEST_CORNER_SHAPE, BOTTOM_SOUTH_EAST_CORNER_SHAPE);
      BOTTOM_SHAPES = composeShapes(BOTTOM_SHAPE, TOP_NORTH_WEST_CORNER_SHAPE, TOP_NORTH_EAST_CORNER_SHAPE, TOP_SOUTH_WEST_CORNER_SHAPE, TOP_SOUTH_EAST_CORNER_SHAPE);
      SHAPE_INDICES = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
   }
}
