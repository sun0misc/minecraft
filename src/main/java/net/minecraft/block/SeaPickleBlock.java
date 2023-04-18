package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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

public class SeaPickleBlock extends PlantBlock implements Fertilizable, Waterloggable {
   public static final int MAX_PICKLES = 4;
   public static final IntProperty PICKLES;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape ONE_PICKLE_SHAPE;
   protected static final VoxelShape TWO_PICKLES_SHAPE;
   protected static final VoxelShape THREE_PICKLES_SHAPE;
   protected static final VoxelShape FOUR_PICKLES_SHAPE;

   protected SeaPickleBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(PICKLES, 1)).with(WATERLOGGED, true));
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos());
      if (lv.isOf(this)) {
         return (BlockState)lv.with(PICKLES, Math.min(4, (Integer)lv.get(PICKLES) + 1));
      } else {
         FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
         boolean bl = lv2.getFluid() == Fluids.WATER;
         return (BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl);
      }
   }

   public static boolean isDry(BlockState state) {
      return !(Boolean)state.get(WATERLOGGED);
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return !floor.getCollisionShape(world, pos).getFace(Direction.UP).isEmpty() || floor.isSideSolidFullSquare(world, pos, Direction.UP);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.down();
      return this.canPlantOnTop(world.getBlockState(lv), world, lv);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (!state.canPlaceAt(world, pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         if ((Boolean)state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return !context.shouldCancelInteraction() && context.getStack().isOf(this.asItem()) && (Integer)state.get(PICKLES) < 4 ? true : super.canReplace(state, context);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Integer)state.get(PICKLES)) {
         case 1:
         default:
            return ONE_PICKLE_SHAPE;
         case 2:
            return TWO_PICKLES_SHAPE;
         case 3:
            return THREE_PICKLES_SHAPE;
         case 4:
            return FOUR_PICKLES_SHAPE;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(PICKLES, WATERLOGGED);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      if (!isDry(state) && world.getBlockState(pos.down()).isIn(BlockTags.CORAL_BLOCKS)) {
         int i = true;
         int j = 1;
         int k = true;
         int l = 0;
         int m = pos.getX() - 2;
         int n = 0;

         for(int o = 0; o < 5; ++o) {
            for(int p = 0; p < j; ++p) {
               int q = 2 + pos.getY() - 1;

               for(int r = q - 2; r < q; ++r) {
                  BlockPos lv = new BlockPos(m + o, r, pos.getZ() - n + p);
                  if (lv != pos && random.nextInt(6) == 0 && world.getBlockState(lv).isOf(Blocks.WATER)) {
                     BlockState lv2 = world.getBlockState(lv.down());
                     if (lv2.isIn(BlockTags.CORAL_BLOCKS)) {
                        world.setBlockState(lv, (BlockState)Blocks.SEA_PICKLE.getDefaultState().with(PICKLES, random.nextInt(4) + 1), Block.NOTIFY_ALL);
                     }
                  }
               }
            }

            if (l < 2) {
               j += 2;
               ++n;
            } else {
               j -= 2;
               --n;
            }

            ++l;
         }

         world.setBlockState(pos, (BlockState)state.with(PICKLES, 4), Block.NOTIFY_LISTENERS);
      }

   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      PICKLES = Properties.PICKLES;
      WATERLOGGED = Properties.WATERLOGGED;
      ONE_PICKLE_SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
      TWO_PICKLES_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);
      THREE_PICKLES_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 6.0, 14.0);
      FOUR_PICKLES_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);
   }
}
