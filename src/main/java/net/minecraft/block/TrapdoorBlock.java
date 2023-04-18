package net.minecraft.block;

import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class TrapdoorBlock extends HorizontalFacingBlock implements Waterloggable {
   public static final BooleanProperty OPEN;
   public static final EnumProperty HALF;
   public static final BooleanProperty POWERED;
   public static final BooleanProperty WATERLOGGED;
   protected static final int field_31266 = 3;
   protected static final VoxelShape EAST_SHAPE;
   protected static final VoxelShape WEST_SHAPE;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape OPEN_BOTTOM_SHAPE;
   protected static final VoxelShape OPEN_TOP_SHAPE;
   private final BlockSetType blockSetType;

   protected TrapdoorBlock(AbstractBlock.Settings settings, BlockSetType blockSetType) {
      super(settings.sounds(blockSetType.soundType()));
      this.blockSetType = blockSetType;
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false)).with(HALF, BlockHalf.BOTTOM)).with(POWERED, false)).with(WATERLOGGED, false));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (!(Boolean)state.get(OPEN)) {
         return state.get(HALF) == BlockHalf.TOP ? OPEN_TOP_SHAPE : OPEN_BOTTOM_SHAPE;
      } else {
         switch ((Direction)state.get(FACING)) {
            case NORTH:
            default:
               return NORTH_SHAPE;
            case SOUTH:
               return SOUTH_SHAPE;
            case WEST:
               return WEST_SHAPE;
            case EAST:
               return EAST_SHAPE;
         }
      }
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      switch (type) {
         case LAND:
            return (Boolean)state.get(OPEN);
         case WATER:
            return (Boolean)state.get(WATERLOGGED);
         case AIR:
            return (Boolean)state.get(OPEN);
         default:
            return false;
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!this.blockSetType.canOpenByHand()) {
         return ActionResult.PASS;
      } else {
         state = (BlockState)state.cycle(OPEN);
         world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
         if ((Boolean)state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
         }

         this.playToggleSound(player, world, pos, (Boolean)state.get(OPEN));
         return ActionResult.success(world.isClient);
      }
   }

   protected void playToggleSound(@Nullable PlayerEntity player, World world, BlockPos pos, boolean open) {
      world.playSound(player, pos, open ? this.blockSetType.trapdoorOpen() : this.blockSetType.trapdoorClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
      world.emitGameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (!world.isClient) {
         boolean bl2 = world.isReceivingRedstonePower(pos);
         if (bl2 != (Boolean)state.get(POWERED)) {
            if ((Boolean)state.get(OPEN) != bl2) {
               state = (BlockState)state.with(OPEN, bl2);
               this.playToggleSound((PlayerEntity)null, world, pos, bl2);
            }

            world.setBlockState(pos, (BlockState)state.with(POWERED, bl2), Block.NOTIFY_LISTENERS);
            if ((Boolean)state.get(WATERLOGGED)) {
               world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            }
         }

      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockState lv = this.getDefaultState();
      FluidState lv2 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      Direction lv3 = ctx.getSide();
      if (!ctx.canReplaceExisting() && lv3.getAxis().isHorizontal()) {
         lv = (BlockState)((BlockState)lv.with(FACING, lv3)).with(HALF, ctx.getHitPos().y - (double)ctx.getBlockPos().getY() > 0.5 ? BlockHalf.TOP : BlockHalf.BOTTOM);
      } else {
         lv = (BlockState)((BlockState)lv.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(HALF, lv3 == Direction.UP ? BlockHalf.BOTTOM : BlockHalf.TOP);
      }

      if (ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos())) {
         lv = (BlockState)((BlockState)lv.with(OPEN, true)).with(POWERED, true);
      }

      return (BlockState)lv.with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   static {
      OPEN = Properties.OPEN;
      HALF = Properties.BLOCK_HALF;
      POWERED = Properties.POWERED;
      WATERLOGGED = Properties.WATERLOGGED;
      EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
      WEST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
      SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
      NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
      OPEN_BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
      OPEN_TOP_SHAPE = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
   }
}
