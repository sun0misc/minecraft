package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.VibrationListener;
import org.jetbrains.annotations.Nullable;

public class SculkSensorBlock extends BlockWithEntity implements Waterloggable {
   public static final int field_31239 = 40;
   public static final EnumProperty SCULK_SENSOR_PHASE;
   public static final IntProperty POWER;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape OUTLINE_SHAPE;
   private static final float[] RESONATION_NOTE_PITCHES;

   public SculkSensorBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE)).with(POWER, 0)).with(WATERLOGGED, false));
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockPos lv = ctx.getBlockPos();
      FluidState lv2 = ctx.getWorld().getFluidState(lv);
      return (BlockState)this.getDefaultState().with(WATERLOGGED, lv2.getFluid() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (getPhase(state) == SculkSensorPhase.ACTIVE) {
         setCooldown(world, pos, state);
      }

   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
      if (!world.isClient() && isInactive(state) && entity.getType() != EntityType.WARDEN) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity lv2 = (SculkSensorBlockEntity)lv;
            if (world instanceof ServerWorld) {
               ServerWorld lv3 = (ServerWorld)world;
               VibrationListener lv4 = lv2.getEventListener();
               if (lv4.getCallback().accepts(lv3, lv4, pos, GameEvent.STEP, GameEvent.Emitter.of(state))) {
                  lv4.forceListen(lv3, GameEvent.STEP, GameEvent.Emitter.of(entity), entity.getPos());
               }
            }
         }
      }

      super.onSteppedOn(world, pos, state, entity);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!world.isClient() && !state.isOf(oldState.getBlock())) {
         if ((Integer)state.get(POWER) > 0 && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.setBlockState(pos, (BlockState)state.with(POWER, 0), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
         }

      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if (getPhase(state) == SculkSensorPhase.ACTIVE) {
            updateNeighbors(world, pos, state);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   private static void updateNeighbors(World world, BlockPos pos, BlockState state) {
      Block lv = state.getBlock();
      world.updateNeighborsAlways(pos, lv);
      world.updateNeighborsAlways(pos.down(), lv);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SculkSensorBlockEntity(pos, state);
   }

   @Nullable
   public GameEventListener getGameEventListener(ServerWorld world, BlockEntity blockEntity) {
      if (blockEntity instanceof SculkSensorBlockEntity lv) {
         return lv.getEventListener();
      } else {
         return null;
      }
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return !world.isClient ? checkType(type, BlockEntityType.SCULK_SENSOR, (worldx, pos, statex, blockEntity) -> {
         blockEntity.getEventListener().tick(worldx);
      }) : null;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return OUTLINE_SHAPE;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Integer)state.get(POWER);
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction == Direction.UP ? state.getWeakRedstonePower(world, pos, direction) : 0;
   }

   public static SculkSensorPhase getPhase(BlockState state) {
      return (SculkSensorPhase)state.get(SCULK_SENSOR_PHASE);
   }

   public static boolean isInactive(BlockState state) {
      return getPhase(state) == SculkSensorPhase.INACTIVE;
   }

   public static void setCooldown(World world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, (BlockState)((BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE)).with(POWER, 0), Block.NOTIFY_ALL);
      if (!(Boolean)state.get(WATERLOGGED)) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING_STOP, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
      }

      updateNeighbors(world, pos, state);
   }

   @VisibleForTesting
   public int getCooldownTime() {
      return 40;
   }

   public void setActive(@Nullable Entity sourceEntity, World world, BlockPos pos, BlockState state, int power, int frequency) {
      world.setBlockState(pos, (BlockState)((BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.ACTIVE)).with(POWER, power), Block.NOTIFY_ALL);
      world.scheduleBlockTick(pos, state.getBlock(), this.getCooldownTime());
      updateNeighbors(world, pos, state);
      tryResonate(sourceEntity, world, pos, frequency);
      world.emitGameEvent(sourceEntity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, pos);
      if (!(Boolean)state.get(WATERLOGGED)) {
         world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
      }

   }

   public static void tryResonate(@Nullable Entity sourceEntity, World world, BlockPos pos, int frequency) {
      Direction[] var4 = Direction.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction lv = var4[var6];
         BlockPos lv2 = pos.offset(lv);
         BlockState lv3 = world.getBlockState(lv2);
         if (lv3.isIn(BlockTags.VIBRATION_RESONATORS)) {
            world.emitGameEvent(VibrationListener.getResonation(frequency), lv2, GameEvent.Emitter.of(sourceEntity, lv3));
            float f = RESONATION_NOTE_PITCHES[frequency];
            world.playSound((PlayerEntity)null, lv2, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 1.0F, f);
         }
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (getPhase(state) == SculkSensorPhase.ACTIVE) {
         Direction lv = Direction.random(random);
         if (lv != Direction.UP && lv != Direction.DOWN) {
            double d = (double)pos.getX() + 0.5 + (lv.getOffsetX() == 0 ? 0.5 - random.nextDouble() : (double)lv.getOffsetX() * 0.6);
            double e = (double)pos.getY() + 0.25;
            double f = (double)pos.getZ() + 0.5 + (lv.getOffsetZ() == 0 ? 0.5 - random.nextDouble() : (double)lv.getOffsetZ() * 0.6);
            double g = (double)random.nextFloat() * 0.04;
            world.addParticle(DustColorTransitionParticleEffect.DEFAULT, d, e, f, 0.0, g, 0.0);
         }
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(SCULK_SENSOR_PHASE, POWER, WATERLOGGED);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof SculkSensorBlockEntity lv2) {
         return getPhase(state) == SculkSensorPhase.ACTIVE ? lv2.getLastVibrationFrequency() : 0;
      } else {
         return 0;
      }
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (dropExperience) {
         this.dropExperienceWhenMined(world, pos, tool, ConstantIntProvider.create(5));
      }

   }

   static {
      SCULK_SENSOR_PHASE = Properties.SCULK_SENSOR_PHASE;
      POWER = Properties.POWER;
      WATERLOGGED = Properties.WATERLOGGED;
      OUTLINE_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
      RESONATION_NOTE_PITCHES = (float[])Util.make(new float[16], (frequency) -> {
         int[] is = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

         for(int i = 0; i < 16; ++i) {
            frequency[i] = NoteBlock.getNotePitch(is[i]);
         }

      });
   }
}
