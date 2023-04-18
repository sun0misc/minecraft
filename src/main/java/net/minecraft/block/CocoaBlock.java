package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CocoaBlock extends HorizontalFacingBlock implements Fertilizable {
   public static final int MAX_AGE = 2;
   public static final IntProperty AGE;
   protected static final int field_31062 = 4;
   protected static final int field_31063 = 5;
   protected static final int field_31064 = 2;
   protected static final int field_31065 = 6;
   protected static final int field_31066 = 7;
   protected static final int field_31067 = 3;
   protected static final int field_31068 = 8;
   protected static final int field_31069 = 9;
   protected static final int field_31070 = 4;
   protected static final VoxelShape[] AGE_TO_EAST_SHAPE;
   protected static final VoxelShape[] AGE_TO_WEST_SHAPE;
   protected static final VoxelShape[] AGE_TO_NORTH_SHAPE;
   protected static final VoxelShape[] AGE_TO_SOUTH_SHAPE;

   public CocoaBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(AGE, 0));
   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(AGE) < 2;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (world.random.nextInt(5) == 0) {
         int i = (Integer)state.get(AGE);
         if (i < 2) {
            world.setBlockState(pos, (BlockState)state.with(AGE, i + 1), Block.NOTIFY_LISTENERS);
         }
      }

   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.offset((Direction)state.get(FACING)));
      return lv.isIn(BlockTags.JUNGLE_LOGS);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      int i = (Integer)state.get(AGE);
      switch ((Direction)state.get(FACING)) {
         case SOUTH:
            return AGE_TO_SOUTH_SHAPE[i];
         case NORTH:
         default:
            return AGE_TO_NORTH_SHAPE[i];
         case WEST:
            return AGE_TO_WEST_SHAPE[i];
         case EAST:
            return AGE_TO_EAST_SHAPE[i];
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = this.getDefaultState();
      WorldView lv2 = ctx.getWorld();
      BlockPos lv3 = ctx.getBlockPos();
      Direction[] var5 = ctx.getPlacementDirections();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv4 = var5[var7];
         if (lv4.getAxis().isHorizontal()) {
            lv = (BlockState)lv.with(FACING, lv4);
            if (lv.canPlaceAt(lv2, lv3)) {
               return lv;
            }
         }
      }

      return null;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return (Integer)state.get(AGE) < 2;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      world.setBlockState(pos, (BlockState)state.with(AGE, (Integer)state.get(AGE) + 1), Block.NOTIFY_LISTENERS);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, AGE);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      AGE = Properties.AGE_2;
      AGE_TO_EAST_SHAPE = new VoxelShape[]{Block.createCuboidShape(11.0, 7.0, 6.0, 15.0, 12.0, 10.0), Block.createCuboidShape(9.0, 5.0, 5.0, 15.0, 12.0, 11.0), Block.createCuboidShape(7.0, 3.0, 4.0, 15.0, 12.0, 12.0)};
      AGE_TO_WEST_SHAPE = new VoxelShape[]{Block.createCuboidShape(1.0, 7.0, 6.0, 5.0, 12.0, 10.0), Block.createCuboidShape(1.0, 5.0, 5.0, 7.0, 12.0, 11.0), Block.createCuboidShape(1.0, 3.0, 4.0, 9.0, 12.0, 12.0)};
      AGE_TO_NORTH_SHAPE = new VoxelShape[]{Block.createCuboidShape(6.0, 7.0, 1.0, 10.0, 12.0, 5.0), Block.createCuboidShape(5.0, 5.0, 1.0, 11.0, 12.0, 7.0), Block.createCuboidShape(4.0, 3.0, 1.0, 12.0, 12.0, 9.0)};
      AGE_TO_SOUTH_SHAPE = new VoxelShape[]{Block.createCuboidShape(6.0, 7.0, 11.0, 10.0, 12.0, 15.0), Block.createCuboidShape(5.0, 5.0, 9.0, 11.0, 12.0, 15.0), Block.createCuboidShape(4.0, 3.0, 7.0, 12.0, 12.0, 15.0)};
   }
}
