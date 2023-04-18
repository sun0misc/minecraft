package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class SpongeBlock extends Block {
   public static final int field_31250 = 6;
   public static final int field_31251 = 64;
   private static final Direction[] field_43257 = Direction.values();

   protected SpongeBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!oldState.isOf(state.getBlock())) {
         this.update(world, pos);
      }
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      this.update(world, pos);
      super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
   }

   protected void update(World world, BlockPos pos) {
      if (this.absorbWater(world, pos)) {
         world.setBlockState(pos, Blocks.WET_SPONGE.getDefaultState(), Block.NOTIFY_LISTENERS);
         world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(Blocks.WATER.getDefaultState()));
      }

   }

   private boolean absorbWater(World world, BlockPos pos) {
      return BlockPos.iterateRecursively(pos, 6, 65, (currentPos, queuer) -> {
         Direction[] var2 = field_43257;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Direction lv = var2[var4];
            queuer.accept(currentPos.offset(lv));
         }

      }, (currentPos) -> {
         if (currentPos.equals(pos)) {
            return true;
         } else {
            BlockState lv = world.getBlockState(currentPos);
            FluidState lv2 = world.getFluidState(currentPos);
            if (!lv2.isIn(FluidTags.WATER)) {
               return false;
            } else {
               Block lv3 = lv.getBlock();
               if (lv3 instanceof FluidDrainable) {
                  FluidDrainable lv4 = (FluidDrainable)lv3;
                  if (!lv4.tryDrainFluid(world, currentPos, lv).isEmpty()) {
                     return true;
                  }
               }

               if (lv.getBlock() instanceof FluidBlock) {
                  world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
               } else {
                  if (!lv.isOf(Blocks.KELP) && !lv.isOf(Blocks.KELP_PLANT) && !lv.isOf(Blocks.SEAGRASS) && !lv.isOf(Blocks.TALL_SEAGRASS)) {
                     return false;
                  }

                  BlockEntity lv5 = lv.hasBlockEntity() ? world.getBlockEntity(currentPos) : null;
                  dropStacks(lv, world, currentPos, lv5);
                  world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
               }

               return true;
            }
         }
      }) > 1;
   }
}
