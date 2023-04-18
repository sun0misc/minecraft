package net.minecraft.block;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LidOpenable;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class ChestBlock extends AbstractChestBlock implements Waterloggable {
   public static final DirectionProperty FACING;
   public static final EnumProperty CHEST_TYPE;
   public static final BooleanProperty WATERLOGGED;
   public static final int field_31057 = 1;
   protected static final int field_31058 = 1;
   protected static final int field_31059 = 14;
   protected static final VoxelShape DOUBLE_NORTH_SHAPE;
   protected static final VoxelShape DOUBLE_SOUTH_SHAPE;
   protected static final VoxelShape DOUBLE_WEST_SHAPE;
   protected static final VoxelShape DOUBLE_EAST_SHAPE;
   protected static final VoxelShape SINGLE_SHAPE;
   private static final DoubleBlockProperties.PropertyRetriever INVENTORY_RETRIEVER;
   private static final DoubleBlockProperties.PropertyRetriever NAME_RETRIEVER;

   protected ChestBlock(AbstractBlock.Settings arg, Supplier supplier) {
      super(arg, supplier);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(CHEST_TYPE, ChestType.SINGLE)).with(WATERLOGGED, false));
   }

   public static DoubleBlockProperties.Type getDoubleBlockType(BlockState state) {
      ChestType lv = (ChestType)state.get(CHEST_TYPE);
      if (lv == ChestType.SINGLE) {
         return DoubleBlockProperties.Type.SINGLE;
      } else {
         return lv == ChestType.RIGHT ? DoubleBlockProperties.Type.FIRST : DoubleBlockProperties.Type.SECOND;
      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      if (neighborState.isOf(this) && direction.getAxis().isHorizontal()) {
         ChestType lv = (ChestType)neighborState.get(CHEST_TYPE);
         if (state.get(CHEST_TYPE) == ChestType.SINGLE && lv != ChestType.SINGLE && state.get(FACING) == neighborState.get(FACING) && getFacing(neighborState) == direction.getOpposite()) {
            return (BlockState)state.with(CHEST_TYPE, lv.getOpposite());
         }
      } else if (getFacing(state) == direction) {
         return (BlockState)state.with(CHEST_TYPE, ChestType.SINGLE);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      if (state.get(CHEST_TYPE) == ChestType.SINGLE) {
         return SINGLE_SHAPE;
      } else {
         switch (getFacing(state)) {
            case NORTH:
            default:
               return DOUBLE_NORTH_SHAPE;
            case SOUTH:
               return DOUBLE_SOUTH_SHAPE;
            case WEST:
               return DOUBLE_WEST_SHAPE;
            case EAST:
               return DOUBLE_EAST_SHAPE;
         }
      }
   }

   public static Direction getFacing(BlockState state) {
      Direction lv = (Direction)state.get(FACING);
      return state.get(CHEST_TYPE) == ChestType.LEFT ? lv.rotateYClockwise() : lv.rotateYCounterclockwise();
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      ChestType lv = ChestType.SINGLE;
      Direction lv2 = ctx.getHorizontalPlayerFacing().getOpposite();
      FluidState lv3 = ctx.getWorld().getFluidState(ctx.getBlockPos());
      boolean bl = ctx.shouldCancelInteraction();
      Direction lv4 = ctx.getSide();
      if (lv4.getAxis().isHorizontal() && bl) {
         Direction lv5 = this.getNeighborChestDirection(ctx, lv4.getOpposite());
         if (lv5 != null && lv5.getAxis() != lv4.getAxis()) {
            lv2 = lv5;
            lv = lv5.rotateYCounterclockwise() == lv4.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
         }
      }

      if (lv == ChestType.SINGLE && !bl) {
         if (lv2 == this.getNeighborChestDirection(ctx, lv2.rotateYClockwise())) {
            lv = ChestType.LEFT;
         } else if (lv2 == this.getNeighborChestDirection(ctx, lv2.rotateYCounterclockwise())) {
            lv = ChestType.RIGHT;
         }
      }

      return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, lv2)).with(CHEST_TYPE, lv)).with(WATERLOGGED, lv3.getFluid() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   @Nullable
   private Direction getNeighborChestDirection(ItemPlacementContext ctx, Direction dir) {
      BlockState lv = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(dir));
      return lv.isOf(this) && lv.get(CHEST_TYPE) == ChestType.SINGLE ? (Direction)lv.get(FACING) : null;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      if (itemStack.hasCustomName()) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof ChestBlockEntity) {
            ((ChestBlockEntity)lv).setCustomName(itemStack.getName());
         }
      }

   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof Inventory) {
            ItemScatterer.spawn(world, pos, (Inventory)lv);
            world.updateComparators(pos, this);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         NamedScreenHandlerFactory lv = this.createScreenHandlerFactory(state, world, pos);
         if (lv != null) {
            player.openHandledScreen(lv);
            player.incrementStat(this.getOpenStat());
            PiglinBrain.onGuardedBlockInteracted(player, true);
         }

         return ActionResult.CONSUME;
      }
   }

   protected Stat getOpenStat() {
      return Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST);
   }

   public BlockEntityType getExpectedEntityType() {
      return (BlockEntityType)this.entityTypeRetriever.get();
   }

   @Nullable
   public static Inventory getInventory(ChestBlock block, BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
      return (Inventory)((Optional)block.getBlockEntitySource(state, world, pos, ignoreBlocked).apply(INVENTORY_RETRIEVER)).orElse((Object)null);
   }

   public DoubleBlockProperties.PropertySource getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
      BiPredicate biPredicate;
      if (ignoreBlocked) {
         biPredicate = (worldx, posx) -> {
            return false;
         };
      } else {
         biPredicate = ChestBlock::isChestBlocked;
      }

      return DoubleBlockProperties.toPropertySource((BlockEntityType)this.entityTypeRetriever.get(), ChestBlock::getDoubleBlockType, ChestBlock::getFacing, FACING, state, world, pos, biPredicate);
   }

   @Nullable
   public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
      return (NamedScreenHandlerFactory)((Optional)this.getBlockEntitySource(state, world, pos, false).apply(NAME_RETRIEVER)).orElse((Object)null);
   }

   public static DoubleBlockProperties.PropertyRetriever getAnimationProgressRetriever(final LidOpenable progress) {
      return new DoubleBlockProperties.PropertyRetriever() {
         public Float2FloatFunction getFromBoth(ChestBlockEntity arg, ChestBlockEntity arg2) {
            return (tickDelta) -> {
               return Math.max(arg.getAnimationProgress(tickDelta), arg2.getAnimationProgress(tickDelta));
            };
         }

         public Float2FloatFunction getFrom(ChestBlockEntity arg) {
            Objects.requireNonNull(arg);
            return arg::getAnimationProgress;
         }

         public Float2FloatFunction getFallback() {
            LidOpenable var10000 = progress;
            Objects.requireNonNull(var10000);
            return var10000::getAnimationProgress;
         }

         // $FF: synthetic method
         public Object getFallback() {
            return this.getFallback();
         }
      };
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new ChestBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? checkType(type, this.getExpectedEntityType(), ChestBlockEntity::clientTick) : null;
   }

   public static boolean isChestBlocked(WorldAccess world, BlockPos pos) {
      return hasBlockOnTop(world, pos) || hasCatOnTop(world, pos);
   }

   private static boolean hasBlockOnTop(BlockView world, BlockPos pos) {
      BlockPos lv = pos.up();
      return world.getBlockState(lv).isSolidBlock(world, lv);
   }

   private static boolean hasCatOnTop(WorldAccess world, BlockPos pos) {
      List list = world.getNonSpectatingEntities(CatEntity.class, new Box((double)pos.getX(), (double)(pos.getY() + 1), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1)));
      if (!list.isEmpty()) {
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            CatEntity lv = (CatEntity)var3.next();
            if (lv.isInSittingPose()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return ScreenHandler.calculateComparatorOutput(getInventory(this, state, world, pos, false));
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, CHEST_TYPE, WATERLOGGED);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof ChestBlockEntity) {
         ((ChestBlockEntity)lv).onScheduledTick();
      }

   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      CHEST_TYPE = Properties.CHEST_TYPE;
      WATERLOGGED = Properties.WATERLOGGED;
      DOUBLE_NORTH_SHAPE = Block.createCuboidShape(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
      DOUBLE_SOUTH_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
      DOUBLE_WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
      DOUBLE_EAST_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);
      SINGLE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
      INVENTORY_RETRIEVER = new DoubleBlockProperties.PropertyRetriever() {
         public Optional getFromBoth(ChestBlockEntity arg, ChestBlockEntity arg2) {
            return Optional.of(new DoubleInventory(arg, arg2));
         }

         public Optional getFrom(ChestBlockEntity arg) {
            return Optional.of(arg);
         }

         public Optional getFallback() {
            return Optional.empty();
         }

         // $FF: synthetic method
         public Object getFallback() {
            return this.getFallback();
         }
      };
      NAME_RETRIEVER = new DoubleBlockProperties.PropertyRetriever() {
         public Optional getFromBoth(final ChestBlockEntity arg, final ChestBlockEntity arg2) {
            final Inventory lv = new DoubleInventory(arg, arg2);
            return Optional.of(new NamedScreenHandlerFactory() {
               @Nullable
               public ScreenHandler createMenu(int i, PlayerInventory argx, PlayerEntity arg2x) {
                  if (arg.checkUnlocked(arg2x) && arg2.checkUnlocked(arg2x)) {
                     arg.checkLootInteraction(argx.player);
                     arg2.checkLootInteraction(argx.player);
                     return GenericContainerScreenHandler.createGeneric9x6(i, argx, lv);
                  } else {
                     return null;
                  }
               }

               public Text getDisplayName() {
                  if (arg.hasCustomName()) {
                     return arg.getDisplayName();
                  } else {
                     return (Text)(arg2.hasCustomName() ? arg2.getDisplayName() : Text.translatable("container.chestDouble"));
                  }
               }
            });
         }

         public Optional getFrom(ChestBlockEntity arg) {
            return Optional.of(arg);
         }

         public Optional getFallback() {
            return Optional.empty();
         }

         // $FF: synthetic method
         public Object getFallback() {
            return this.getFallback();
         }
      };
   }
}
