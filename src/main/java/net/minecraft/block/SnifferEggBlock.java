package net.minecraft.block;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

public class SnifferEggBlock extends Block {
   public static final int FINAL_HATCH_STAGE = 2;
   public static final IntProperty HATCH;
   private static final int HATCHING_TIME = 24000;
   private static final int BOOSTED_HATCHING_TIME = 12000;
   private static final int MAX_RANDOM_CRACK_TIME_OFFSET = 300;
   private static final VoxelShape SHAPE;

   public SnifferEggBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HATCH, 0));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HATCH);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public int getHatchStage(BlockState state) {
      return (Integer)state.get(HATCH);
   }

   private boolean isReadyToHatch(BlockState state) {
      return this.getHatchStage(state) == 2;
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      return world.getBlockState(pos).getFluidState().isOf(Fluids.EMPTY);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!this.isReadyToHatch(state)) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_SNIFFER_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
         world.setBlockState(pos, (BlockState)state.with(HATCH, this.getHatchStage(state) + 1), Block.NOTIFY_LISTENERS);
      } else {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_SNIFFER_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
         world.breakBlock(pos, false);
         SnifferEntity lv = (SnifferEntity)EntityType.SNIFFER.create(world);
         if (lv != null) {
            Vec3d lv2 = pos.toCenterPos();
            lv.setBaby(true);
            lv.refreshPositionAndAngles(lv2.getX(), lv2.getY(), lv2.getZ(), MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
            world.spawnEntity(lv);
         }

      }
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      boolean bl2 = isAboveHatchBooster(world, pos);
      if (!world.isClient()) {
         world.syncWorldEvent(WorldEvents.SNIFFER_EGG_CRACKS, pos, bl2 ? 1 : 0);
      }

      int i = bl2 ? 12000 : 24000;
      int j = i / 3;
      world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(state));
      world.scheduleBlockTick(pos, this, j + world.random.nextInt(300));
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public static boolean isAboveHatchBooster(BlockView world, BlockPos pos) {
      return world.getBlockState(pos.down()).isIn(BlockTags.SNIFFER_EGG_HATCH_BOOST);
   }

   static {
      HATCH = Properties.HATCH;
      SHAPE = Block.createCuboidShape(1.0, 0.0, 2.0, 15.0, 16.0, 14.0);
   }
}
