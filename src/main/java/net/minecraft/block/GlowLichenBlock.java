package net.minecraft.block;

import java.util.function.ToIntFunction;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class GlowLichenBlock extends MultifaceGrowthBlock implements Fertilizable, Waterloggable {
   private static final BooleanProperty WATERLOGGED;
   private final LichenGrower grower = new LichenGrower(this);

   public GlowLichenBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)this.getDefaultState().with(WATERLOGGED, false));
   }

   public static ToIntFunction getLuminanceSupplier(int luminance) {
      return (state) -> {
         return MultifaceGrowthBlock.hasAnyDirection(state) ? luminance : 0;
      };
   }

   protected void appendProperties(StateManager.Builder builder) {
      super.appendProperties(builder);
      builder.add(WATERLOGGED);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return !context.getStack().isOf(Items.GLOW_LICHEN) || super.canReplace(state, context);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return Direction.stream().anyMatch((direction) -> {
         return this.grower.canGrow(state, world, pos, direction.getOpposite());
      });
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      this.grower.grow(state, world, pos, random);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return state.getFluidState().isEmpty();
   }

   public LichenGrower getGrower() {
      return this.grower;
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
   }
}
