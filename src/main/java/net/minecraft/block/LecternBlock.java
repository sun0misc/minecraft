package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class LecternBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final BooleanProperty POWERED;
   public static final BooleanProperty HAS_BOOK;
   public static final VoxelShape BOTTOM_SHAPE;
   public static final VoxelShape MIDDLE_SHAPE;
   public static final VoxelShape BASE_SHAPE;
   public static final VoxelShape COLLISION_SHAPE_TOP;
   public static final VoxelShape COLLISION_SHAPE;
   public static final VoxelShape WEST_SHAPE;
   public static final VoxelShape NORTH_SHAPE;
   public static final VoxelShape EAST_SHAPE;
   public static final VoxelShape SOUTH_SHAPE;
   private static final int SCHEDULED_TICK_DELAY = 2;

   protected LecternBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(HAS_BOOK, false));
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return BASE_SHAPE;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      World lv = ctx.getWorld();
      ItemStack lv2 = ctx.getStack();
      PlayerEntity lv3 = ctx.getPlayer();
      boolean bl = false;
      if (!lv.isClient && lv3 != null && lv3.isCreativeLevelTwoOp()) {
         NbtCompound lv4 = BlockItem.getBlockEntityNbt(lv2);
         if (lv4 != null && lv4.contains("Book")) {
            bl = true;
         }
      }

      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(HAS_BOOK, bl);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch ((Direction)state.get(FACING)) {
         case NORTH:
            return NORTH_SHAPE;
         case SOUTH:
            return SOUTH_SHAPE;
         case EAST:
            return EAST_SHAPE;
         case WEST:
            return WEST_SHAPE;
         default:
            return BASE_SHAPE;
      }
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, POWERED, HAS_BOOK);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new LecternBlockEntity(pos, state);
   }

   public static boolean putBookIfAbsent(@Nullable Entity user, World world, BlockPos pos, BlockState state, ItemStack stack) {
      if (!(Boolean)state.get(HAS_BOOK)) {
         if (!world.isClient) {
            putBook(user, world, pos, state, stack);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void putBook(@Nullable Entity user, World world, BlockPos pos, BlockState state, ItemStack stack) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof LecternBlockEntity lv2) {
         lv2.setBook(stack.split(1));
         setHasBook(user, world, pos, state, true);
         world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

   }

   public static void setHasBook(@Nullable Entity user, World world, BlockPos pos, BlockState state, boolean hasBook) {
      BlockState lv = (BlockState)((BlockState)state.with(POWERED, false)).with(HAS_BOOK, hasBook);
      world.setBlockState(pos, lv, Block.NOTIFY_ALL);
      world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, lv));
      updateNeighborAlways(world, pos, state);
   }

   public static void setPowered(World world, BlockPos pos, BlockState state) {
      setPowered(world, pos, state, true);
      world.scheduleBlockTick(pos, state.getBlock(), 2);
      world.syncWorldEvent(WorldEvents.LECTERN_BOOK_PAGE_TURNED, pos, 0);
   }

   private static void setPowered(World world, BlockPos pos, BlockState state, boolean powered) {
      world.setBlockState(pos, (BlockState)state.with(POWERED, powered), Block.NOTIFY_ALL);
      updateNeighborAlways(world, pos, state);
   }

   private static void updateNeighborAlways(World world, BlockPos pos, BlockState state) {
      world.updateNeighborsAlways(pos.down(), state.getBlock());
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      setPowered(world, pos, state, false);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if ((Boolean)state.get(HAS_BOOK)) {
            this.dropBook(state, world, pos);
         }

         if ((Boolean)state.get(POWERED)) {
            world.updateNeighborsAlways(pos.down(), this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   private void dropBook(BlockState state, World world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof LecternBlockEntity lv2) {
         Direction lv3 = (Direction)state.get(FACING);
         ItemStack lv4 = lv2.getBook().copy();
         float f = 0.25F * (float)lv3.getOffsetX();
         float g = 0.25F * (float)lv3.getOffsetZ();
         ItemEntity lv5 = new ItemEntity(world, (double)pos.getX() + 0.5 + (double)f, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5 + (double)g, lv4);
         lv5.setToDefaultPickupDelay();
         world.spawnEntity(lv5);
         lv2.clear();
      }

   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction == Direction.UP && (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      if ((Boolean)state.get(HAS_BOOK)) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof LecternBlockEntity) {
            return ((LecternBlockEntity)lv).getComparatorOutput();
         }
      }

      return 0;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if ((Boolean)state.get(HAS_BOOK)) {
         if (!world.isClient) {
            this.openScreen(world, pos, player);
         }

         return ActionResult.success(world.isClient);
      } else {
         ItemStack lv = player.getStackInHand(hand);
         return !lv.isEmpty() && !lv.isIn(ItemTags.LECTERN_BOOKS) ? ActionResult.CONSUME : ActionResult.PASS;
      }
   }

   @Nullable
   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      return !(Boolean)state.get(HAS_BOOK) ? null : super.createScreenHandlerFactory(state, world, pos);
   }

   private void openScreen(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof LecternBlockEntity) {
         player.openHandledScreen((LecternBlockEntity)lv);
         player.incrementStat(Stats.INTERACT_WITH_LECTERN);
      }

   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      POWERED = Properties.POWERED;
      HAS_BOOK = Properties.HAS_BOOK;
      BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
      MIDDLE_SHAPE = Block.createCuboidShape(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
      BASE_SHAPE = VoxelShapes.union(BOTTOM_SHAPE, MIDDLE_SHAPE);
      COLLISION_SHAPE_TOP = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 15.0, 16.0);
      COLLISION_SHAPE = VoxelShapes.union(BASE_SHAPE, COLLISION_SHAPE_TOP);
      WEST_SHAPE = VoxelShapes.union(Block.createCuboidShape(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0), Block.createCuboidShape(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0), Block.createCuboidShape(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0), BASE_SHAPE);
      NORTH_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333), Block.createCuboidShape(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667), Block.createCuboidShape(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0), BASE_SHAPE);
      EAST_SHAPE = VoxelShapes.union(Block.createCuboidShape(10.666667, 10.0, 0.0, 15.0, 14.0, 16.0), Block.createCuboidShape(6.333333, 12.0, 0.0, 10.666667, 16.0, 16.0), Block.createCuboidShape(2.0, 14.0, 0.0, 6.333333, 18.0, 16.0), BASE_SHAPE);
      SOUTH_SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 10.0, 10.666667, 16.0, 14.0, 15.0), Block.createCuboidShape(0.0, 12.0, 6.333333, 16.0, 16.0, 10.666667), Block.createCuboidShape(0.0, 14.0, 2.0, 16.0, 18.0, 6.333333), BASE_SHAPE);
   }
}
