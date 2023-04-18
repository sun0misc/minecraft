package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HopperBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty ENABLED;
   private static final VoxelShape TOP_SHAPE;
   private static final VoxelShape MIDDLE_SHAPE;
   private static final VoxelShape OUTSIDE_SHAPE;
   private static final VoxelShape DEFAULT_SHAPE;
   private static final VoxelShape DOWN_SHAPE;
   private static final VoxelShape EAST_SHAPE;
   private static final VoxelShape NORTH_SHAPE;
   private static final VoxelShape SOUTH_SHAPE;
   private static final VoxelShape WEST_SHAPE;
   private static final VoxelShape DOWN_RAYCAST_SHAPE;
   private static final VoxelShape EAST_RAYCAST_SHAPE;
   private static final VoxelShape NORTH_RAYCAST_SHAPE;
   private static final VoxelShape SOUTH_RAYCAST_SHAPE;
   private static final VoxelShape WEST_RAYCAST_SHAPE;

   public HopperBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.DOWN)).with(ENABLED, true));
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction)state.get(FACING)) {
         case DOWN:
            return DOWN_SHAPE;
         case NORTH:
            return NORTH_SHAPE;
         case SOUTH:
            return SOUTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         case EAST:
            return EAST_SHAPE;
         default:
            return DEFAULT_SHAPE;
      }
   }

   public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
      switch ((Direction)state.get(FACING)) {
         case DOWN:
            return DOWN_RAYCAST_SHAPE;
         case NORTH:
            return NORTH_RAYCAST_SHAPE;
         case SOUTH:
            return SOUTH_RAYCAST_SHAPE;
         case WEST:
            return WEST_RAYCAST_SHAPE;
         case EAST:
            return EAST_RAYCAST_SHAPE;
         default:
            return Hopper.INSIDE_SHAPE;
      }
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction lv = ctx.getSide().getOpposite();
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, lv.getAxis() == Direction.Axis.Y ? Direction.DOWN : lv)).with(ENABLED, true);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new HopperBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? null : checkType(type, BlockEntityType.HOPPER, HopperBlockEntity::serverTick);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof HopperBlockEntity) {
            ((HopperBlockEntity)lv).setCustomName(itemStack.getName());
         }
      }

   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.updateEnabled(world, pos, state, 2);
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof HopperBlockEntity) {
            player.openHandledScreen((HopperBlockEntity)lv);
            player.incrementStat(Stats.INSPECT_HOPPER);
         }

         return ActionResult.CONSUME;
      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      this.updateEnabled(world, pos, state, 4);
   }

   private void updateEnabled(World world, BlockPos pos, BlockState state, int flags) {
      boolean bl = !world.isReceivingRedstonePower(pos);
      if (bl != (Boolean)state.get(ENABLED)) {
         world.setBlockState(pos, (BlockState)state.with(ENABLED, bl), flags);
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof HopperBlockEntity) {
            ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((HopperBlockEntity)lv));
            world.updateComparators(pos, this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, ENABLED);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof HopperBlockEntity) {
         HopperBlockEntity.onEntityCollided(world, pos, state, entity, (HopperBlockEntity)lv);
      }

   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = Properties.HOPPER_FACING;
      ENABLED = Properties.ENABLED;
      TOP_SHAPE = Block.createCuboidShape(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
      MIDDLE_SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
      OUTSIDE_SHAPE = VoxelShapes.union(MIDDLE_SHAPE, TOP_SHAPE);
      DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(OUTSIDE_SHAPE, Hopper.INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST);
      DOWN_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
      EAST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
      NORTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
      SOUTH_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
      WEST_SHAPE = VoxelShapes.union(DEFAULT_SHAPE, Block.createCuboidShape(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
      DOWN_RAYCAST_SHAPE = Hopper.INSIDE_SHAPE;
      EAST_RAYCAST_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(12.0, 8.0, 6.0, 16.0, 10.0, 10.0));
      NORTH_RAYCAST_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(6.0, 8.0, 0.0, 10.0, 10.0, 4.0));
      SOUTH_RAYCAST_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(6.0, 8.0, 12.0, 10.0, 10.0, 16.0));
      WEST_RAYCAST_SHAPE = VoxelShapes.union(Hopper.INSIDE_SHAPE, Block.createCuboidShape(0.0, 8.0, 6.0, 4.0, 10.0, 10.0));
   }
}
