package net.minecraft.block;

import java.util.function.ToIntFunction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class LightBlock extends Block implements Waterloggable {
   public static final int field_33722 = 15;
   public static final IntProperty LEVEL_15;
   public static final BooleanProperty WATERLOGGED;
   public static final ToIntFunction STATE_TO_LUMINANCE;

   public LightBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL_15, 15)).with(WATERLOGGED, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(LEVEL_15, WATERLOGGED);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!world.isClient && player.isCreativeLevelTwoOp()) {
         world.setBlockState(pos, (BlockState)state.cycle(LEVEL_15), Block.NOTIFY_LISTENERS);
         return ActionResult.SUCCESS;
      } else {
         return ActionResult.CONSUME;
      }
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return context.isHolding(Items.LIGHT) ? VoxelShapes.fullCube() : VoxelShapes.empty();
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 1.0F;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return addNbtForLevel(super.getPickStack(world, pos, state), (Integer)state.get(LEVEL_15));
   }

   public static ItemStack addNbtForLevel(ItemStack stack, int level) {
      if (level != 15) {
         NbtCompound lv = new NbtCompound();
         lv.putString(LEVEL_15.getName(), String.valueOf(level));
         stack.setSubNbt("BlockStateTag", lv);
      }

      return stack;
   }

   static {
      LEVEL_15 = Properties.LEVEL_15;
      WATERLOGGED = Properties.WATERLOGGED;
      STATE_TO_LUMINANCE = (state) -> {
         return (Integer)state.get(LEVEL_15);
      };
   }
}
