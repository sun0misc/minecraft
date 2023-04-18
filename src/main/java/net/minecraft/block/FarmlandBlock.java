package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class FarmlandBlock extends Block {
   public static final IntProperty MOISTURE;
   protected static final VoxelShape SHAPE;
   public static final int MAX_MOISTURE = 7;

   protected FarmlandBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(MOISTURE, 0));
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
         world.scheduleBlockTick(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.up());
      return !lv.getMaterial().isSolid() || lv.getBlock() instanceof FenceGateBlock || lv.getBlock() instanceof PistonExtensionBlock;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return !this.getDefaultState().canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? Blocks.DIRT.getDefaultState() : super.getPlacementState(ctx);
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         setToDirt((Entity)null, state, world, pos);
      }

   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      int i = (Integer)state.get(MOISTURE);
      if (!isWaterNearby(world, pos) && !world.hasRain(pos.up())) {
         if (i > 0) {
            world.setBlockState(pos, (BlockState)state.with(MOISTURE, i - 1), Block.NOTIFY_LISTENERS);
         } else if (!hasCrop(world, pos)) {
            setToDirt((Entity)null, state, world, pos);
         }
      } else if (i < 7) {
         world.setBlockState(pos, (BlockState)state.with(MOISTURE, 7), Block.NOTIFY_LISTENERS);
      }

   }

   public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
      if (!world.isClient && world.random.nextFloat() < fallDistance - 0.5F && entity instanceof LivingEntity && (entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) && entity.getWidth() * entity.getWidth() * entity.getHeight() > 0.512F) {
         setToDirt(entity, state, world, pos);
      }

      super.onLandedUpon(world, state, pos, entity, fallDistance);
   }

   public static void setToDirt(@Nullable Entity entity, BlockState state, World world, BlockPos pos) {
      BlockState lv = pushEntitiesUpBeforeBlockChange(state, Blocks.DIRT.getDefaultState(), world, pos);
      world.setBlockState(pos, lv);
      world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(entity, lv));
   }

   private static boolean hasCrop(BlockView world, BlockPos pos) {
      Block lv = world.getBlockState(pos.up()).getBlock();
      return lv instanceof Crop;
   }

   private static boolean isWaterNearby(WorldView world, BlockPos pos) {
      Iterator var2 = BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4)).iterator();

      BlockPos lv;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         lv = (BlockPos)var2.next();
      } while(!world.getFluidState(lv).isIn(FluidTags.WATER));

      return true;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(MOISTURE);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   static {
      MOISTURE = Properties.MOISTURE;
      SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
   }
}
