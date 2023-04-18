package net.minecraft.block;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class FlowerbedBlock extends PlantBlock implements Fertilizable {
   public static final int field_42762 = 1;
   public static final int field_42763 = 4;
   public static final DirectionProperty FACING;
   public static final IntProperty FLOWER_AMOUNT;

   protected FlowerbedBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(FLOWER_AMOUNT, 1));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   public boolean canReplace(BlockState state, ItemPlacementContext context) {
      return !context.shouldCancelInteraction() && context.getStack().isOf(this.asItem()) && (Integer)state.get(FLOWER_AMOUNT) < 4 ? true : super.canReplace(state, context);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos());
      return lv.isOf(this) ? (BlockState)lv.with(FLOWER_AMOUNT, Math.min(4, (Integer)lv.get(FLOWER_AMOUNT) + 1)) : (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, FLOWER_AMOUNT);
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      int i = (Integer)state.get(FLOWER_AMOUNT);
      if (i < 4) {
         world.setBlockState(pos, (BlockState)state.with(FLOWER_AMOUNT, i + 1), Block.NOTIFY_LISTENERS);
      } else {
         dropStack(world, pos, new ItemStack(this));
      }

   }

   static {
      FACING = Properties.HORIZONTAL_FACING;
      FLOWER_AMOUNT = Properties.FLOWER_AMOUNT;
   }
}
