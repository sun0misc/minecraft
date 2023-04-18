package net.minecraft.block;

import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;

public abstract class SpreadableBlock extends SnowyBlock {
   protected SpreadableBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   private static boolean canSurvive(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.up();
      BlockState lv2 = world.getBlockState(lv);
      if (lv2.isOf(Blocks.SNOW) && (Integer)lv2.get(SnowBlock.LAYERS) == 1) {
         return true;
      } else if (lv2.getFluidState().getLevel() == 8) {
         return false;
      } else {
         int i = ChunkLightProvider.getRealisticOpacity(world, state, pos, lv2, lv, Direction.UP, lv2.getOpacity(world, lv));
         return i < world.getMaxLightLevel();
      }
   }

   private static boolean canSpread(BlockState state, WorldView world, BlockPos pos) {
      BlockPos lv = pos.up();
      return canSurvive(state, world, pos) && !world.getFluidState(lv).isIn(FluidTags.WATER);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!canSurvive(state, world, pos)) {
         world.setBlockState(pos, Blocks.DIRT.getDefaultState());
      } else {
         if (world.getLightLevel(pos.up()) >= 9) {
            BlockState lv = this.getDefaultState();

            for(int i = 0; i < 4; ++i) {
               BlockPos lv2 = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
               if (world.getBlockState(lv2).isOf(Blocks.DIRT) && canSpread(lv, world, lv2)) {
                  world.setBlockState(lv2, (BlockState)lv.with(SNOWY, world.getBlockState(lv2.up()).isOf(Blocks.SNOW)));
               }
            }
         }

      }
   }
}
