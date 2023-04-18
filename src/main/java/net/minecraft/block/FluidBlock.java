package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
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
import net.minecraft.world.WorldEvents;

public class FluidBlock extends Block implements FluidDrainable {
   public static final IntProperty LEVEL;
   protected final FlowableFluid fluid;
   private final List statesByLevel;
   public static final VoxelShape COLLISION_SHAPE;
   public static final ImmutableList FLOW_DIRECTIONS;

   protected FluidBlock(FlowableFluid fluid, AbstractBlock.Settings settings) {
      super(settings);
      this.fluid = fluid;
      this.statesByLevel = Lists.newArrayList();
      this.statesByLevel.add(fluid.getStill(false));

      for(int i = 1; i < 8; ++i) {
         this.statesByLevel.add(fluid.getFlowing(8 - i, false));
      }

      this.statesByLevel.add(fluid.getFlowing(8, true));
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 0));
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return context.isAbove(COLLISION_SHAPE, pos, true) && (Integer)state.get(LEVEL) == 0 && context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState()) ? COLLISION_SHAPE : VoxelShapes.empty();
   }

   public boolean hasRandomTicks(BlockState state) {
      return state.getFluidState().hasRandomTicks();
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      state.getFluidState().onRandomTick(world, pos, random);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return false;
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return !this.fluid.isIn(FluidTags.LAVA);
   }

   public FluidState getFluidState(BlockState state) {
      int i = (Integer)state.get(LEVEL);
      return (FluidState)this.statesByLevel.get(Math.min(i, 8));
   }

   public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
      return stateFrom.getFluidState().getFluid().matchesType(this.fluid);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public List getDroppedStacks(BlockState state, LootContext.Builder builder) {
      return Collections.emptyList();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (this.receiveNeighborFluids(world, pos, state)) {
         world.scheduleFluidTick(pos, state.getFluidState().getFluid(), this.fluid.getTickRate(world));
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (state.getFluidState().isStill() || neighborState.getFluidState().isStill()) {
         world.scheduleFluidTick(pos, state.getFluidState().getFluid(), this.fluid.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (this.receiveNeighborFluids(world, pos, state)) {
         world.scheduleFluidTick(pos, state.getFluidState().getFluid(), this.fluid.getTickRate(world));
      }

   }

   private boolean receiveNeighborFluids(World world, BlockPos pos, BlockState state) {
      if (this.fluid.isIn(FluidTags.LAVA)) {
         boolean bl = world.getBlockState(pos.down()).isOf(Blocks.SOUL_SOIL);
         UnmodifiableIterator var5 = FLOW_DIRECTIONS.iterator();

         while(var5.hasNext()) {
            Direction lv = (Direction)var5.next();
            BlockPos lv2 = pos.offset(lv.getOpposite());
            if (world.getFluidState(lv2).isIn(FluidTags.WATER)) {
               Block lv3 = world.getFluidState(pos).isStill() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
               world.setBlockState(pos, lv3.getDefaultState());
               this.playExtinguishSound(world, pos);
               return false;
            }

            if (bl && world.getBlockState(lv2).isOf(Blocks.BLUE_ICE)) {
               world.setBlockState(pos, Blocks.BASALT.getDefaultState());
               this.playExtinguishSound(world, pos);
               return false;
            }
         }
      }

      return true;
   }

   private void playExtinguishSound(WorldAccess world, BlockPos pos) {
      world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LEVEL);
   }

   public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
      if ((Integer)state.get(LEVEL) == 0) {
         world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
         return new ItemStack(this.fluid.getBucketItem());
      } else {
         return ItemStack.EMPTY;
      }
   }

   public Optional getBucketFillSound() {
      return this.fluid.getBucketFillSound();
   }

   static {
      LEVEL = Properties.LEVEL_15;
      COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
      FLOW_DIRECTIONS = ImmutableList.of(Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST);
   }
}
