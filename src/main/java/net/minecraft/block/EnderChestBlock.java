package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class EnderChestBlock extends AbstractChestBlock implements Waterloggable {
   public static final DirectionProperty FACING;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape SHAPE;
   private static final Text CONTAINER_NAME;

   protected EnderChestBlock(AbstractBlock.Settings arg) {
      super(arg, () -> {
         return BlockEntityType.ENDER_CHEST;
      });
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false));
   }

   public DoubleBlockProperties.PropertySource getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked) {
      return DoubleBlockProperties.PropertyRetriever::getFallback;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(WATERLOGGED, lv.getFluid() == Fluids.WATER);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      EnderChestInventory lv = player.getEnderChestInventory();
      BlockEntity lv2 = world.getBlockEntity(pos);
      if (lv != null && lv2 instanceof EnderChestBlockEntity) {
         BlockPos lv3 = pos.up();
         if (world.getBlockState(lv3).isSolidBlock(world, lv3)) {
            return ActionResult.success(world.isClient);
         } else if (world.isClient) {
            return ActionResult.SUCCESS;
         } else {
            EnderChestBlockEntity lv4 = (EnderChestBlockEntity)lv2;
            lv.setActiveBlockEntity(lv4);
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, playerx) -> {
               return GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, lv);
            }, CONTAINER_NAME));
            player.incrementStat(Stats.OPEN_ENDERCHEST);
            PiglinBrain.onGuardedBlockInteracted(player, true);
            return ActionResult.CONSUME;
         }
      } else {
         return ActionResult.success(world.isClient);
      }
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new EnderChestBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? checkType(type, BlockEntityType.ENDER_CHEST, EnderChestBlockEntity::clientTick) : null;
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      for(int i = 0; i < 3; ++i) {
         int j = random.nextInt(2) * 2 - 1;
         int k = random.nextInt(2) * 2 - 1;
         double d = (double)pos.getX() + 0.5 + 0.25 * (double)j;
         double e = (double)((float)pos.getY() + random.nextFloat());
         double f = (double)pos.getZ() + 0.5 + 0.25 * (double)k;
         double g = (double)(random.nextFloat() * (float)j);
         double h = ((double)random.nextFloat() - 0.5) * 0.125;
         double l = (double)(random.nextFloat() * (float)k);
         world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l);
      }

   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, WATERLOGGED);
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

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof EnderChestBlockEntity) {
         ((EnderChestBlockEntity)lv).onScheduledTick();
      }

   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      WATERLOGGED = Properties.WATERLOGGED;
      SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
      CONTAINER_NAME = Text.translatable("container.enderchest");
   }
}
