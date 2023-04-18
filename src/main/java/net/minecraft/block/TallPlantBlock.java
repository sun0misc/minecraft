package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TallPlantBlock extends PlantBlock {
   public static final EnumProperty HALF;

   public TallPlantBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HALF, DoubleBlockHalf.LOWER));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      DoubleBlockHalf lv = (DoubleBlockHalf)state.get(HALF);
      if (direction.getAxis() == Direction.Axis.Y && lv == DoubleBlockHalf.LOWER == (direction == Direction.UP) && (!neighborState.isOf(this) || neighborState.get(HALF) == lv)) {
         return Blocks.AIR.getDefaultState();
      } else {
         return lv == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockPos lv = ctx.getBlockPos();
      World lv2 = ctx.getWorld();
      return lv.getY() < lv2.getTopY() - 1 && lv2.getBlockState(lv.up()).canReplace(ctx) ? super.getPlacementState(ctx) : null;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      BlockPos lv = pos.up();
      world.setBlockState(lv, withWaterloggedState(world, lv, (BlockState)this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER)), Block.NOTIFY_ALL);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      if (state.get(HALF) != DoubleBlockHalf.UPPER) {
         return super.canPlaceAt(state, world, pos);
      } else {
         BlockState lv = world.getBlockState(pos.down());
         return lv.isOf(this) && lv.get(HALF) == DoubleBlockHalf.LOWER;
      }
   }

   public static void placeAt(WorldAccess world, BlockState state, BlockPos pos, int flags) {
      BlockPos lv = pos.up();
      world.setBlockState(pos, withWaterloggedState(world, pos, (BlockState)state.with(HALF, DoubleBlockHalf.LOWER)), flags);
      world.setBlockState(lv, withWaterloggedState(world, lv, (BlockState)state.with(HALF, DoubleBlockHalf.UPPER)), flags);
   }

   public static BlockState withWaterloggedState(WorldView world, BlockPos pos, BlockState state) {
      return state.contains(Properties.WATERLOGGED) ? (BlockState)state.with(Properties.WATERLOGGED, world.isWater(pos)) : state;
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient) {
         if (player.isCreative()) {
            onBreakInCreative(world, pos, state, player);
         } else {
            dropStacks(state, world, pos, (BlockEntity)null, player, player.getMainHandStack());
         }
      }

      super.onBreak(world, pos, state, player);
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
      super.afterBreak(world, player, pos, Blocks.AIR.getDefaultState(), blockEntity, tool);
   }

   protected static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      DoubleBlockHalf lv = (DoubleBlockHalf)state.get(HALF);
      if (lv == DoubleBlockHalf.UPPER) {
         BlockPos lv2 = pos.down();
         BlockState lv3 = world.getBlockState(lv2);
         if (lv3.isOf(state.getBlock()) && lv3.get(HALF) == DoubleBlockHalf.LOWER) {
            BlockState lv4 = lv3.getFluidState().isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
            world.setBlockState(lv2, lv4, Block.NOTIFY_ALL | Block.SKIP_DROPS);
            world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, lv2, Block.getRawIdFromState(lv3));
         }
      }

   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HALF);
   }

   public long getRenderingSeed(BlockState state, BlockPos pos) {
      return MathHelper.hashCode(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
   }

   static {
      HALF = Properties.DOUBLE_BLOCK_HALF;
   }
}
