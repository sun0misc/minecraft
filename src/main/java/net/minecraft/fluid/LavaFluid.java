package net.minecraft.fluid;

import java.util.Optional;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class LavaFluid extends FlowableFluid {
   public static final float MIN_HEIGHT_TO_REPLACE = 0.44444445F;

   public Fluid getFlowing() {
      return Fluids.FLOWING_LAVA;
   }

   public Fluid getStill() {
      return Fluids.LAVA;
   }

   public Item getBucketItem() {
      return Items.LAVA_BUCKET;
   }

   public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
      BlockPos lv = pos.up();
      if (world.getBlockState(lv).isAir() && !world.getBlockState(lv).isOpaqueFullCube(world, lv)) {
         if (random.nextInt(100) == 0) {
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + 1.0;
            double f = (double)pos.getZ() + random.nextDouble();
            world.addParticle(ParticleTypes.LAVA, d, e, f, 0.0, 0.0, 0.0);
            world.playSound(d, e, f, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
         }

         if (random.nextInt(200) == 0) {
            world.playSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
         }
      }

   }

   public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
      if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
         int i = random.nextInt(3);
         if (i > 0) {
            BlockPos lv = pos;

            for(int j = 0; j < i; ++j) {
               lv = lv.add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
               if (!world.canSetBlock(lv)) {
                  return;
               }

               BlockState lv2 = world.getBlockState(lv);
               if (lv2.isAir()) {
                  if (this.canLightFire(world, lv)) {
                     world.setBlockState(lv, AbstractFireBlock.getState(world, lv));
                     return;
                  }
               } else if (lv2.getMaterial().blocksMovement()) {
                  return;
               }
            }
         } else {
            for(int k = 0; k < 3; ++k) {
               BlockPos lv3 = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
               if (!world.canSetBlock(lv3)) {
                  return;
               }

               if (world.isAir(lv3.up()) && this.hasBurnableBlock(world, lv3)) {
                  world.setBlockState(lv3.up(), AbstractFireBlock.getState(world, lv3));
               }
            }
         }

      }
   }

   private boolean canLightFire(WorldView world, BlockPos pos) {
      Direction[] var3 = Direction.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction lv = var3[var5];
         if (this.hasBurnableBlock(world, pos.offset(lv))) {
            return true;
         }
      }

      return false;
   }

   private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
      return pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && !world.isChunkLoaded(pos) ? false : world.getBlockState(pos).isBurnable();
   }

   @Nullable
   public ParticleEffect getParticle() {
      return ParticleTypes.DRIPPING_LAVA;
   }

   protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
      this.playExtinguishEvent(world, pos);
   }

   public int getFlowSpeed(WorldView world) {
      return world.getDimension().ultrawarm() ? 4 : 2;
   }

   public BlockState toBlockState(FluidState state) {
      return (BlockState)Blocks.LAVA.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
   }

   public boolean matchesType(Fluid fluid) {
      return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
   }

   public int getLevelDecreasePerBlock(WorldView world) {
      return world.getDimension().ultrawarm() ? 1 : 2;
   }

   public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
      return state.getHeight(world, pos) >= 0.44444445F && fluid.isIn(FluidTags.WATER);
   }

   public int getTickRate(WorldView world) {
      return world.getDimension().ultrawarm() ? 10 : 30;
   }

   public int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState) {
      int i = this.getTickRate(world);
      if (!oldState.isEmpty() && !newState.isEmpty() && !(Boolean)oldState.get(FALLING) && !(Boolean)newState.get(FALLING) && newState.getHeight(world, pos) > oldState.getHeight(world, pos) && world.getRandom().nextInt(4) != 0) {
         i *= 4;
      }

      return i;
   }

   private void playExtinguishEvent(WorldAccess world, BlockPos pos) {
      world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
   }

   protected boolean isInfinite(World world) {
      return world.getGameRules().getBoolean(GameRules.LAVA_SOURCE_CONVERSION);
   }

   protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
      if (direction == Direction.DOWN) {
         FluidState lv = world.getFluidState(pos);
         if (this.isIn(FluidTags.LAVA) && lv.isIn(FluidTags.WATER)) {
            if (state.getBlock() instanceof FluidBlock) {
               world.setBlockState(pos, Blocks.STONE.getDefaultState(), Block.NOTIFY_ALL);
            }

            this.playExtinguishEvent(world, pos);
            return;
         }
      }

      super.flow(world, pos, state, direction, fluidState);
   }

   protected boolean hasRandomTicks() {
      return true;
   }

   protected float getBlastResistance() {
      return 100.0F;
   }

   public Optional getBucketFillSound() {
      return Optional.of(SoundEvents.ITEM_BUCKET_FILL_LAVA);
   }

   public static class Flowing extends LavaFluid {
      protected void appendProperties(StateManager.Builder builder) {
         super.appendProperties(builder);
         builder.add(LEVEL);
      }

      public int getLevel(FluidState state) {
         return (Integer)state.get(LEVEL);
      }

      public boolean isStill(FluidState state) {
         return false;
      }
   }

   public static class Still extends LavaFluid {
      public int getLevel(FluidState state) {
         return 8;
      }

      public boolean isStill(FluidState state) {
         return true;
      }
   }
}
