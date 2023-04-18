package net.minecraft.block;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class KelpBlock extends AbstractPlantStemBlock implements FluidFillable {
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);
   private static final double GROWTH_CHANCE = 0.14;

   protected KelpBlock(AbstractBlock.Settings arg) {
      super(arg, Direction.UP, SHAPE, true, 0.14);
   }

   protected boolean chooseStemState(BlockState state) {
      return state.isOf(Blocks.WATER);
   }

   protected Block getPlant() {
      return Blocks.KELP_PLANT;
   }

   protected boolean canAttachTo(BlockState state) {
      return !state.isOf(Blocks.MAGMA_BLOCK);
   }

   public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      return false;
   }

   public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      return false;
   }

   protected int getGrowthLength(Random random) {
      return 1;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return lv.isIn(FluidTags.WATER) && lv.getLevel() == 8 ? super.getPlacementState(ctx) : null;
   }

   public FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getStill(false);
   }
}
