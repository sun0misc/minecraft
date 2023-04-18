package net.minecraft.block;

import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class DoorBlock extends Block {
   public static final DirectionProperty FACING;
   public static final BooleanProperty OPEN;
   public static final EnumProperty HINGE;
   public static final BooleanProperty POWERED;
   public static final EnumProperty HALF;
   protected static final float field_31083 = 3.0F;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape EAST_SHAPE;
   protected static final VoxelShape WEST_SHAPE;
   private final BlockSetType blockSetType;

   protected DoorBlock(AbstractBlock.Settings settings, BlockSetType blockSetType) {
      super(settings.sounds(blockSetType.soundType()));
      this.blockSetType = blockSetType;
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false)).with(HINGE, DoorHinge.LEFT)).with(POWERED, false)).with(HALF, DoubleBlockHalf.LOWER));
   }

   public BlockSetType getBlockSetType() {
      return this.blockSetType;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction lv = (Direction)state.get(FACING);
      boolean bl = !(Boolean)state.get(OPEN);
      boolean bl2 = state.get(HINGE) == DoorHinge.RIGHT;
      switch (lv) {
         case EAST:
         default:
            return bl ? WEST_SHAPE : (bl2 ? SOUTH_SHAPE : NORTH_SHAPE);
         case SOUTH:
            return bl ? NORTH_SHAPE : (bl2 ? WEST_SHAPE : EAST_SHAPE);
         case WEST:
            return bl ? EAST_SHAPE : (bl2 ? NORTH_SHAPE : SOUTH_SHAPE);
         case NORTH:
            return bl ? SOUTH_SHAPE : (bl2 ? EAST_SHAPE : WEST_SHAPE);
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      DoubleBlockHalf lv = (DoubleBlockHalf)state.get(HALF);
      if (direction.getAxis() == Direction.Axis.Y && lv == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
         return neighborState.isOf(this) && neighborState.get(HALF) != lv ? (BlockState)((BlockState)((BlockState)((BlockState)state.with(FACING, (Direction)neighborState.get(FACING))).with(OPEN, (Boolean)neighborState.get(OPEN))).with(HINGE, (DoorHinge)neighborState.get(HINGE))).with(POWERED, (Boolean)neighborState.get(POWERED)) : Blocks.AIR.getDefaultState();
      } else {
         return lv == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.isCreative()) {
         TallPlantBlock.onBreakInCreative(world, pos, state, player);
      }

      super.onBreak(world, pos, state, player);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      switch (type) {
         case LAND:
            return (Boolean)state.get(OPEN);
         case WATER:
            return false;
         case AIR:
            return (Boolean)state.get(OPEN);
         default:
            return false;
      }
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockPos lv = ctx.getBlockPos();
      World lv2 = ctx.getWorld();
      if (lv.getY() < lv2.getTopY() - 1 && lv2.getBlockState(lv.up()).canReplace(ctx)) {
         boolean bl = lv2.isReceivingRedstonePower(lv) || lv2.isReceivingRedstonePower(lv.up());
         return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(HINGE, this.getHinge(ctx))).with(POWERED, bl)).with(OPEN, bl)).with(HALF, DoubleBlockHalf.LOWER);
      } else {
         return null;
      }
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      world.setBlockState(pos.up(), (BlockState)state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
   }

   private DoorHinge getHinge(ItemPlacementContext ctx) {
      BlockView lv = ctx.getWorld();
      BlockPos lv2 = ctx.getBlockPos();
      Direction lv3 = ctx.getHorizontalPlayerFacing();
      BlockPos lv4 = lv2.up();
      Direction lv5 = lv3.rotateYCounterclockwise();
      BlockPos lv6 = lv2.offset(lv5);
      BlockState lv7 = lv.getBlockState(lv6);
      BlockPos lv8 = lv4.offset(lv5);
      BlockState lv9 = lv.getBlockState(lv8);
      Direction lv10 = lv3.rotateYClockwise();
      BlockPos lv11 = lv2.offset(lv10);
      BlockState lv12 = lv.getBlockState(lv11);
      BlockPos lv13 = lv4.offset(lv10);
      BlockState lv14 = lv.getBlockState(lv13);
      int i = (lv7.isFullCube(lv, lv6) ? -1 : 0) + (lv9.isFullCube(lv, lv8) ? -1 : 0) + (lv12.isFullCube(lv, lv11) ? 1 : 0) + (lv14.isFullCube(lv, lv13) ? 1 : 0);
      boolean bl = lv7.isOf(this) && lv7.get(HALF) == DoubleBlockHalf.LOWER;
      boolean bl2 = lv12.isOf(this) && lv12.get(HALF) == DoubleBlockHalf.LOWER;
      if ((!bl || bl2) && i <= 0) {
         if ((!bl2 || bl) && i >= 0) {
            int j = lv3.getOffsetX();
            int k = lv3.getOffsetZ();
            Vec3d lv15 = ctx.getHitPos();
            double d = lv15.x - (double)lv2.getX();
            double e = lv15.z - (double)lv2.getZ();
            return (j >= 0 || !(e < 0.5)) && (j <= 0 || !(e > 0.5)) && (k >= 0 || !(d > 0.5)) && (k <= 0 || !(d < 0.5)) ? DoorHinge.LEFT : DoorHinge.RIGHT;
         } else {
            return DoorHinge.LEFT;
         }
      } else {
         return DoorHinge.RIGHT;
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!this.blockSetType.canOpenByHand()) {
         return ActionResult.PASS;
      } else {
         state = (BlockState)state.cycle(OPEN);
         world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
         this.playOpenCloseSound(player, world, pos, (Boolean)state.get(OPEN));
         world.emitGameEvent(player, this.isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
         return ActionResult.success(world.isClient);
      }
   }

   public boolean isOpen(BlockState state) {
      return (Boolean)state.get(OPEN);
   }

   public void setOpen(@Nullable Entity entity, World world, BlockState state, BlockPos pos, boolean open) {
      if (state.isOf(this) && (Boolean)state.get(OPEN) != open) {
         world.setBlockState(pos, (BlockState)state.with(OPEN, open), Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
         this.playOpenCloseSound(entity, world, pos, open);
         world.emitGameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      boolean bl2 = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
      if (!this.getDefaultState().isOf(sourceBlock) && bl2 != (Boolean)state.get(POWERED)) {
         if (bl2 != (Boolean)state.get(OPEN)) {
            this.playOpenCloseSound((Entity)null, world, pos, bl2);
            world.emitGameEvent((Entity)null, bl2 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
         }

         world.setBlockState(pos, (BlockState)((BlockState)state.with(POWERED, bl2)).with(OPEN, bl2), Block.NOTIFY_LISTENERS);
      }

   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.down();
      BlockState lv2 = world.getBlockState(lv);
      return state.get(HALF) == DoubleBlockHalf.LOWER ? lv2.isSideSolidFullSquare(world, lv, Direction.UP) : lv2.isOf(this);
   }

   private void playOpenCloseSound(@Nullable Entity entity, World world, BlockPos pos, boolean open) {
      world.playSound(entity, pos, open ? this.blockSetType.doorOpen() : this.blockSetType.doorClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return mirror == BlockMirror.NONE ? state : (BlockState)state.rotate(mirror.getRotation((Direction)state.get(FACING))).cycle(HINGE);
   }

   public long getRenderingSeed(BlockState state, BlockPos pos) {
      return MathHelper.hashCode(pos.getX(), pos.down(state.get(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pos.getZ());
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HALF, FACING, OPEN, HINGE, POWERED);
   }

   public static boolean canOpenByHand(World world, BlockPos pos) {
      return canOpenByHand(world.getBlockState(pos));
   }

   public static boolean canOpenByHand(BlockState state) {
      Block var2 = state.getBlock();
      boolean var10000;
      if (var2 instanceof DoorBlock lv) {
         if (lv.getBlockSetType().canOpenByHand()) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      OPEN = Properties.OPEN;
      HINGE = Properties.DOOR_HINGE;
      POWERED = Properties.POWERED;
      HALF = Properties.DOUBLE_BLOCK_HALF;
      NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
      SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
      EAST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
      WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
   }
}
