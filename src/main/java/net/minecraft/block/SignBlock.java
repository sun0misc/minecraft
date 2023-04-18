package net.minecraft.block;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SignBlock extends AbstractSignBlock {
   public static final IntProperty ROTATION;

   public SignBlock(AbstractBlock.Settings arg, WoodType arg2) {
      super(arg.sounds(arg2.soundType()), arg2);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ROTATION, 0)).with(WATERLOGGED, false));
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getMaterial().isSolid();
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)((BlockState)this.getDefaultState().with(ROTATION, RotationPropertyHelper.fromYaw(ctx.getPlayerYaw() + 180.0F))).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.DOWN && !this.canPlaceAt(state, world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public float getRotationDegrees(BlockState state) {
      return RotationPropertyHelper.toDegrees((Integer)state.get(ROTATION));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(ROTATION, rotation.rotate((Integer)state.get(ROTATION), 16));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(ROTATION, mirror.mirror((Integer)state.get(ROTATION), 16));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(ROTATION, WATERLOGGED);
   }

   static {
      ROTATION = Properties.ROTATION;
   }
}
