package net.minecraft.block;

import java.util.OptionalInt;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
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
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class LeavesBlock extends Block implements Waterloggable {
   public static final int MAX_DISTANCE = 7;
   public static final IntProperty DISTANCE;
   public static final BooleanProperty PERSISTENT;
   public static final BooleanProperty WATERLOGGED;
   private static final int field_31112 = 1;

   public LeavesBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(PERSISTENT, false)).with(WATERLOGGED, false));
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.empty();
   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(DISTANCE) == 7 && !(Boolean)state.get(PERSISTENT);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (this.shouldDecay(state)) {
         dropStacks(state, world, pos);
         world.removeBlock(pos, false);
      }

   }

   protected boolean shouldDecay(BlockState state) {
      return !(Boolean)state.get(PERSISTENT) && (Integer)state.get(DISTANCE) == 7;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      world.setBlockState(pos, updateDistanceFromLogs(state, world, pos), Block.NOTIFY_ALL);
   }

   public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
      return 1;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      int i = getDistanceFromLog(neighborState) + 1;
      if (i != 1 || (Integer)state.get(DISTANCE) != i) {
         world.scheduleBlockTick(pos, this, 1);
      }

      return state;
   }

   private static BlockState updateDistanceFromLogs(BlockState state, WorldAccess world, BlockPos pos) {
      int i = 7;
      BlockPos.Mutable lv = new BlockPos.Mutable();
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction lv2 = var5[var7];
         lv.set(pos, (Direction)lv2);
         i = Math.min(i, getDistanceFromLog(world.getBlockState(lv)) + 1);
         if (i == 1) {
            break;
         }
      }

      return (BlockState)state.with(DISTANCE, i);
   }

   private static int getDistanceFromLog(BlockState state) {
      return getOptionalDistanceFromLog(state).orElse(7);
   }

   public static OptionalInt getOptionalDistanceFromLog(BlockState state) {
      if (state.isIn(BlockTags.LOGS)) {
         return OptionalInt.of(0);
      } else {
         return state.contains(DISTANCE) ? OptionalInt.of((Integer)state.get(DISTANCE)) : OptionalInt.empty();
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (world.hasRain(pos.up())) {
         if (random.nextInt(15) == 1) {
            BlockPos lv = pos.down();
            BlockState lv2 = world.getBlockState(lv);
            if (!lv2.isOpaque() || !lv2.isSideSolidFullSquare(world, lv, Direction.UP)) {
               ParticleUtil.spawnParticle(world, pos, (Random)random, (ParticleEffect)ParticleTypes.DRIPPING_WATER);
            }
         }
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      BlockState lv2 = (BlockState)((BlockState)this.getDefaultState().with(PERSISTENT, true)).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
      return updateDistanceFromLogs(lv2, ctx.getWorld(), ctx.getBlockPos());
   }

   static {
      DISTANCE = Properties.DISTANCE_1_7;
      PERSISTENT = Properties.PERSISTENT;
      WATERLOGGED = Properties.WATERLOGGED;
   }
}
