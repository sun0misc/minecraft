package net.minecraft.block;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TallSeagrassBlock extends TallPlantBlock implements FluidFillable {
   public static final EnumProperty HALF;
   protected static final float field_31262 = 6.0F;
   protected static final VoxelShape SHAPE;

   public TallSeagrassBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
      return floor.isSideSolidFullSquare(world, pos, Direction.UP) && !floor.isOf(Blocks.MAGMA_BLOCK);
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(Blocks.SEAGRASS);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = super.getPlacementState(ctx);
      if (lv != null) {
         FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos().up());
         if (lv2.isIn(FluidTags.WATER) && lv2.getLevel() == 8) {
            return lv;
         }
      }

      return null;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      if (state.get(HALF) == DoubleBlockHalf.UPPER) {
         BlockState lv = world.getBlockState(pos.down());
         return lv.isOf(this) && lv.get(HALF) == DoubleBlockHalf.LOWER;
      } else {
         FluidState lv2 = world.getFluidState(pos);
         return super.canPlaceAt(state, world, pos) && lv2.isIn(FluidTags.WATER) && lv2.getLevel() == 8;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getStill(false);
   }

   public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
      return false;
   }

   public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
      return false;
   }

   static {
      HALF = TallPlantBlock.HALF;
      SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   }
}
