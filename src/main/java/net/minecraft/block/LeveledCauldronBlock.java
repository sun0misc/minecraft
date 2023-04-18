package net.minecraft.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

public class LeveledCauldronBlock extends AbstractCauldronBlock {
   public static final int MIN_LEVEL = 1;
   public static final int MAX_LEVEL = 3;
   public static final IntProperty LEVEL;
   private static final int BASE_FLUID_HEIGHT = 6;
   private static final double FLUID_HEIGHT_PER_LEVEL = 3.0;
   public static final Predicate RAIN_PREDICATE;
   public static final Predicate SNOW_PREDICATE;
   private final Predicate precipitationPredicate;

   public LeveledCauldronBlock(AbstractBlock.Settings settings, Predicate precipitationPredicate, Map behaviorMap) {
      super(settings, behaviorMap);
      this.precipitationPredicate = precipitationPredicate;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 1));
   }

   public boolean isFull(BlockState state) {
      return (Integer)state.get(LEVEL) == 3;
   }

   protected boolean canBeFilledByDripstone(Fluid fluid) {
      return fluid == Fluids.WATER && this.precipitationPredicate == RAIN_PREDICATE;
   }

   protected double getFluidHeight(BlockState state) {
      return (6.0 + (double)(Integer)state.get(LEVEL) * 3.0) / 16.0;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient && entity.isOnFire() && this.isEntityTouchingFluid(state, pos, entity)) {
         entity.extinguish();
         if (entity.canModifyAt(world, pos)) {
            this.onFireCollision(state, world, pos);
         }
      }

   }

   protected void onFireCollision(BlockState state, World world, BlockPos pos) {
      decrementFluidLevel(state, world, pos);
   }

   public static void decrementFluidLevel(BlockState state, World world, BlockPos pos) {
      int i = (Integer)state.get(LEVEL) - 1;
      BlockState lv = i == 0 ? Blocks.CAULDRON.getDefaultState() : (BlockState)state.with(LEVEL, i);
      world.setBlockState(pos, lv);
      world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv));
   }

   public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
      if (CauldronBlock.canFillWithPrecipitation(world, precipitation) && (Integer)state.get(LEVEL) != 3 && this.precipitationPredicate.test(precipitation)) {
         BlockState lv = (BlockState)state.cycle(LEVEL);
         world.setBlockState(pos, lv);
         world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv));
      }
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return (Integer)state.get(LEVEL);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LEVEL);
   }

   protected void fillFromDripstone(BlockState state, World world, BlockPos pos, Fluid fluid) {
      if (!this.isFull(state)) {
         BlockState lv = (BlockState)state.with(LEVEL, (Integer)state.get(LEVEL) + 1);
         world.setBlockState(pos, lv);
         world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv));
         world.syncWorldEvent(WorldEvents.POINTED_DRIPSTONE_DRIPS_WATER_INTO_CAULDRON, pos, 0);
      }
   }

   static {
      LEVEL = Properties.LEVEL_3;
      RAIN_PREDICATE = (precipitation) -> {
         return precipitation == Biome.Precipitation.RAIN;
      };
      SNOW_PREDICATE = (precipitation) -> {
         return precipitation == Biome.Precipitation.SNOW;
      };
   }
}
