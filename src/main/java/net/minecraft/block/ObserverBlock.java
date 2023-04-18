package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ObserverBlock extends FacingBlock {
   public static final BooleanProperty POWERED;

   public ObserverBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.SOUTH)).with(POWERED, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, POWERED);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(POWERED)) {
         world.setBlockState(pos, (BlockState)state.with(POWERED, false), Block.NOTIFY_LISTENERS);
      } else {
         world.setBlockState(pos, (BlockState)state.with(POWERED, true), Block.NOTIFY_LISTENERS);
         world.scheduleBlockTick(pos, this, 2);
      }

      this.updateNeighbors(world, pos, state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (state.get(FACING) == direction && !(Boolean)state.get(POWERED)) {
         this.scheduleTick(world, pos);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   private void scheduleTick(WorldAccess world, BlockPos pos) {
      if (!world.isClient() && !world.getBlockTickScheduler().isQueued(pos, this)) {
         world.scheduleBlockTick(pos, this, 2);
      }

   }

   protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
      Direction lv = (Direction)state.get(FACING);
      BlockPos lv2 = pos.offset(lv.getOpposite());
      world.updateNeighbor(lv2, this, pos);
      world.updateNeighborsExcept(lv2, this, lv);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return state.getWeakRedstonePower(world, pos, direction);
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) && state.get(FACING) == direction ? 15 : 0;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!state.isOf(oldState.getBlock())) {
         if (!world.isClient() && (Boolean)state.get(POWERED) && !world.getBlockTickScheduler().isQueued(pos, this)) {
            BlockState lv = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            this.updateNeighbors(world, pos, lv);
         }

      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if (!world.isClient && (Boolean)state.get(POWERED) && world.getBlockTickScheduler().isQueued(pos, this)) {
            this.updateNeighbors(world, pos, (BlockState)state.with(POWERED, false));
         }

      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite().getOpposite());
   }

   static {
      POWERED = Properties.POWERED;
   }
}
