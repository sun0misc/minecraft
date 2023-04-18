package net.minecraft.block;

import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AbstractPlantStemBlock extends AbstractPlantPartBlock implements Fertilizable {
   public static final IntProperty AGE;
   public static final int MAX_AGE = 25;
   private final double growthChance;

   protected AbstractPlantStemBlock(AbstractBlock.Settings settings, Direction growthDirection, VoxelShape outlineShape, boolean tickWater, double growthChance) {
      super(settings, growthDirection, outlineShape, tickWater);
      this.growthChance = growthChance;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public BlockState getRandomGrowthState(WorldAccess world) {
      return (BlockState)this.getDefaultState().with(AGE, world.getRandom().nextInt(25));
   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(AGE) < 25;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Integer)state.get(AGE) < 25 && random.nextDouble() < this.growthChance) {
         BlockPos lv = pos.offset(this.growthDirection);
         if (this.chooseStemState(world.getBlockState(lv))) {
            world.setBlockState(lv, this.age(state, world.random));
         }
      }

   }

   protected BlockState age(BlockState state, Random random) {
      return (BlockState)state.cycle(AGE);
   }

   public BlockState withMaxAge(BlockState state) {
      return (BlockState)state.with(AGE, 25);
   }

   public boolean hasMaxAge(BlockState state) {
      return (Integer)state.get(AGE) == 25;
   }

   protected BlockState copyState(BlockState from, BlockState to) {
      return to;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == this.growthDirection.getOpposite() && !state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      if (direction != this.growthDirection || !neighborState.isOf(this) && !neighborState.isOf(this.getPlant())) {
         if (this.tickWater) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      } else {
         return this.copyState(state, this.getPlant().getDefaultState());
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return this.chooseStemState(world.getBlockState(pos.offset(this.growthDirection)));
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      BlockPos lv = pos.offset(this.growthDirection);
      int i = Math.min((Integer)state.get(AGE) + 1, 25);
      int j = this.getGrowthLength(random);

      for(int k = 0; k < j && this.chooseStemState(world.getBlockState(lv)); ++k) {
         world.setBlockState(lv, (BlockState)state.with(AGE, i));
         lv = lv.offset(this.growthDirection);
         i = Math.min(i + 1, 25);
      }

   }

   protected abstract int getGrowthLength(Random random);

   protected abstract boolean chooseStemState(BlockState state);

   protected AbstractPlantStemBlock getStem() {
      return this;
   }

   static {
      AGE = Properties.AGE_25;
   }
}
