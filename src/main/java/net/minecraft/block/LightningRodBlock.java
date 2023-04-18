package net.minecraft.block;

import net.minecraft.client.util.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;

public class LightningRodBlock extends RodBlock implements Waterloggable {
   public static final BooleanProperty WATERLOGGED;
   public static final BooleanProperty POWERED;
   private static final int SCHEDULED_TICK_DELAY = 8;
   public static final int MAX_REDIRECT_DISTANCE = 128;
   private static final int field_31191 = 200;

   public LightningRodBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.UP)).with(WATERLOGGED, false)).with(POWERED, false));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      FluidState lv = ctx.getWorld().getFluidState(ctx.getBlockPos());
      boolean bl = lv.getFluid() == Fluids.WATER;
      return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getSide())).with(WATERLOGGED, bl);
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

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Boolean)state.get(POWERED) && state.get(FACING) == direction ? 15 : 0;
   }

   public void setPowered(BlockState state, World world, BlockPos pos) {
      world.setBlockState(pos, (BlockState)state.with(POWERED, true), Block.NOTIFY_ALL);
      this.updateNeighbors(state, world, pos);
      world.scheduleBlockTick(pos, this, 8);
      world.syncWorldEvent(WorldEvents.ELECTRICITY_SPARKS, pos, ((Direction)state.get(FACING)).getAxis().ordinal());
   }

   private void updateNeighbors(BlockState state, World world, BlockPos pos) {
      world.updateNeighborsAlways(pos.offset(((Direction)state.get(FACING)).getOpposite()), this);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      world.setBlockState(pos, (BlockState)state.with(POWERED, false), Block.NOTIFY_ALL);
      this.updateNeighbors(state, world, pos);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (world.isThundering() && (long)world.random.nextInt(200) <= world.getTime() % 200L && pos.getY() == world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1) {
         ParticleUtil.spawnParticle(((Direction)state.get(FACING)).getAxis(), world, pos, 0.125, ParticleTypes.ELECTRIC_SPARK, UniformIntProvider.create(1, 2));
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if ((Boolean)state.get(POWERED)) {
            this.updateNeighbors(state, world, pos);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!state.isOf(oldState.getBlock())) {
         if ((Boolean)state.get(POWERED) && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, false), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
         }

      }
   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      if (world.isThundering() && projectile instanceof TridentEntity && ((TridentEntity)projectile).hasChanneling()) {
         BlockPos lv = hit.getBlockPos();
         if (world.isSkyVisible(lv)) {
            LightningEntity lv2 = (LightningEntity)EntityType.LIGHTNING_BOLT.create(world);
            if (lv2 != null) {
               lv2.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(lv.up()));
               Entity lv3 = projectile.getOwner();
               lv2.setChanneler(lv3 instanceof ServerPlayerEntity ? (ServerPlayerEntity)lv3 : null);
               world.spawnEntity(lv2);
            }

            world.playSound((PlayerEntity)null, lv, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, 5.0F, 1.0F);
         }
      }

   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, POWERED, WATERLOGGED);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   static {
      WATERLOGGED = Properties.WATERLOGGED;
      POWERED = Properties.POWERED;
   }
}
