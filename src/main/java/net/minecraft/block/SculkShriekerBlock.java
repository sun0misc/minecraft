package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class SculkShriekerBlock extends BlockWithEntity implements Waterloggable {
   public static final BooleanProperty SHRIEKING;
   public static final BooleanProperty WATERLOGGED;
   public static final BooleanProperty CAN_SUMMON;
   protected static final VoxelShape SHAPE;
   public static final double TOP;

   public SculkShriekerBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SHRIEKING, false)).with(WATERLOGGED, false)).with(CAN_SUMMON, false));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(SHRIEKING);
      builder.add(WATERLOGGED);
      builder.add(CAN_SUMMON);
   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
      if (world instanceof ServerWorld lv) {
         ServerPlayerEntity lv2 = SculkShriekerBlockEntity.findResponsiblePlayerFromEntity(entity);
         if (lv2 != null) {
            lv.getBlockEntity(pos, BlockEntityType.SCULK_SHRIEKER).ifPresent((blockEntity) -> {
               blockEntity.shriek(lv, lv2);
            });
         }
      }

      super.onSteppedOn(world, pos, state, entity);
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (world instanceof ServerWorld lv) {
         if ((Boolean)state.get(SHRIEKING) && !state.isOf(newState.getBlock())) {
            lv.getBlockEntity(pos, BlockEntityType.SCULK_SHRIEKER).ifPresent((blockEntity) -> {
               blockEntity.warn(lv);
            });
         }
      }

      super.onStateReplaced(state, world, pos, newState, moved);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(SHRIEKING)) {
         world.setBlockState(pos, (BlockState)state.with(SHRIEKING, false), Block.NOTIFY_ALL);
         world.getBlockEntity(pos, BlockEntityType.SCULK_SHRIEKER).ifPresent((blockEntity) -> {
            blockEntity.warn(world);
         });
      }

   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return SHAPE;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SculkShriekerBlockEntity(pos, state);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (dropExperience) {
         this.dropExperienceWhenMined(world, pos, tool, ConstantIntProvider.create(5));
      }

   }

   @Nullable
   public GameEventListener getGameEventListener(ServerWorld world, BlockEntity blockEntity) {
      if (blockEntity instanceof SculkShriekerBlockEntity lv) {
         return lv.getVibrationListener();
      } else {
         return null;
      }
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return !world.isClient ? BlockWithEntity.checkType(type, BlockEntityType.SCULK_SHRIEKER, (worldx, pos, statex, blockEntity) -> {
         blockEntity.getVibrationListener().tick(worldx);
      }) : null;
   }

   static {
      SHRIEKING = Properties.SHRIEKING;
      WATERLOGGED = Properties.WATERLOGGED;
      CAN_SUMMON = Properties.CAN_SUMMON;
      SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
      TOP = SHAPE.getMax(Direction.Axis.Y);
   }
}
