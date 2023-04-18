package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BrushableBlock extends BlockWithEntity implements LandingBlock {
   private static final IntProperty DUSTED;
   public static final int field_42773 = 2;
   private final Block baseBlock;
   private final SoundEvent brushingSound;
   private final SoundEvent brushingCompleteSound;

   public BrushableBlock(Block baseBlock, AbstractBlock.Settings settings, SoundEvent brushingSound, SoundEvent brushingCompleteSound) {
      super(settings);
      this.baseBlock = baseBlock;
      this.brushingSound = brushingSound;
      this.brushingCompleteSound = brushingCompleteSound;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DUSTED, 0));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(DUSTED);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      world.scheduleBlockTick(pos, this, 2);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      world.scheduleBlockTick(pos, this, 2);
      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockEntity var6 = world.getBlockEntity(pos);
      if (var6 instanceof BrushableBlockEntity lv) {
         lv.scheduledTick();
      }

      if (FallingBlock.canFallThrough(world.getBlockState(pos.down())) && pos.getY() >= world.getBottomY()) {
         FallingBlockEntity lv2 = FallingBlockEntity.spawnFromBlock(world, pos, state);
         lv2.setDestroyedOnLanding();
      }
   }

   public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
      Vec3d lv = fallingBlockEntity.getBoundingBox().getCenter();
      world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, BlockPos.ofFloored(lv), Block.getRawIdFromState(fallingBlockEntity.getBlockState()));
      world.emitGameEvent(fallingBlockEntity, GameEvent.BLOCK_DESTROY, lv);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (random.nextInt(16) == 0) {
         BlockPos lv = pos.down();
         if (FallingBlock.canFallThrough(world.getBlockState(lv))) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() - 0.05;
            double f = (double)pos.getZ() + random.nextDouble();
            world.addParticle(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
         }
      }

   }

   public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new BrushableBlockEntity(pos, state);
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public SoundEvent getBrushingSound() {
      return this.brushingSound;
   }

   public SoundEvent getBrushingCompleteSound() {
      return this.brushingCompleteSound;
   }

   static {
      DUSTED = Properties.DUSTED;
   }
}
