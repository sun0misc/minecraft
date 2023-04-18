package net.minecraft.block;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BubbleColumnBlock extends Block implements FluidDrainable {
   public static final BooleanProperty DRAG;
   private static final int SCHEDULED_TICK_DELAY = 5;

   public BubbleColumnBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DRAG, true));
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      BlockState lv = world.getBlockState(pos.up());
      if (lv.isAir()) {
         entity.onBubbleColumnSurfaceCollision((Boolean)state.get(DRAG));
         if (!world.isClient) {
            ServerWorld lv2 = (ServerWorld)world;

            for(int i = 0; i < 2; ++i) {
               lv2.spawnParticles(ParticleTypes.SPLASH, (double)pos.getX() + world.random.nextDouble(), (double)(pos.getY() + 1), (double)pos.getZ() + world.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
               lv2.spawnParticles(ParticleTypes.BUBBLE, (double)pos.getX() + world.random.nextDouble(), (double)(pos.getY() + 1), (double)pos.getZ() + world.random.nextDouble(), 1, 0.0, 0.01, 0.0, 0.2);
            }
         }
      } else {
         entity.onBubbleColumnCollision((Boolean)state.get(DRAG));
      }

   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      update(world, pos, state, world.getBlockState(pos.down()));
   }

   public FluidState getFluidState(BlockState state) {
      return Fluids.WATER.getStill(false);
   }

   public static void update(WorldAccess world, BlockPos pos, BlockState state) {
      update(world, pos, world.getBlockState(pos), state);
   }

   public static void update(WorldAccess world, BlockPos pos, BlockState water, BlockState bubbleSource) {
      if (isStillWater(water)) {
         BlockState lv = getBubbleState(bubbleSource);
         world.setBlockState(pos, lv, Block.NOTIFY_LISTENERS);
         BlockPos.Mutable lv2 = pos.mutableCopy().move(Direction.UP);

         while(isStillWater(world.getBlockState(lv2))) {
            if (!world.setBlockState(lv2, lv, Block.NOTIFY_LISTENERS)) {
               return;
            }

            lv2.move(Direction.UP);
         }

      }
   }

   private static boolean isStillWater(BlockState state) {
      return state.isOf(Blocks.BUBBLE_COLUMN) || state.isOf(Blocks.WATER) && state.getFluidState().getLevel() >= 8 && state.getFluidState().isStill();
   }

   private static BlockState getBubbleState(BlockState state) {
      if (state.isOf(Blocks.BUBBLE_COLUMN)) {
         return state;
      } else if (state.isOf(Blocks.SOUL_SAND)) {
         return (BlockState)Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, false);
      } else {
         return state.isOf(Blocks.MAGMA_BLOCK) ? (BlockState)Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, true) : Blocks.WATER.getDefaultState();
      }
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      double d = (double)pos.getX();
      double e = (double)pos.getY();
      double f = (double)pos.getZ();
      if ((Boolean)state.get(DRAG)) {
         world.addImportantParticle(ParticleTypes.CURRENT_DOWN, d + 0.5, e + 0.8, f, 0.0, 0.0, 0.0);
         if (random.nextInt(200) == 0) {
            world.playSound(d, e, f, SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
         }
      } else {
         world.addImportantParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + 0.5, e, f + 0.5, 0.0, 0.04, 0.0);
         world.addImportantParticle(ParticleTypes.BUBBLE_COLUMN_UP, d + (double)random.nextFloat(), e + (double)random.nextFloat(), f + (double)random.nextFloat(), 0.0, 0.04, 0.0);
         if (random.nextInt(200) == 0) {
            world.playSound(d, e, f, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
         }
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      if (!state.canPlaceAt(world, pos) || direction == Direction.DOWN || direction == Direction.UP && !neighborState.isOf(Blocks.BUBBLE_COLUMN) && isStillWater(neighborState)) {
         world.scheduleBlockTick(pos, this, 5);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.down());
      return lv.isOf(Blocks.BUBBLE_COLUMN) || lv.isOf(Blocks.MAGMA_BLOCK) || lv.isOf(Blocks.SOUL_SAND);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(DRAG);
   }

   public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
      return new ItemStack(Items.WATER_BUCKET);
   }

   public Optional getBucketFillSound() {
      return Fluids.WATER.getBucketFillSound();
   }

   static {
      DRAG = Properties.DRAG;
   }
}
