package net.minecraft.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowderSnowCauldronBlock extends LeveledCauldronBlock {
   public PowderSnowCauldronBlock(AbstractBlock.Settings arg, Predicate predicate, Map map) {
      super(arg, predicate, map);
   }

   protected void onFireCollision(BlockState state, World world, BlockPos pos) {
      decrementFluidLevel((BlockState)Blocks.WATER_CAULDRON.getDefaultState().with(LEVEL, (Integer)state.get(LEVEL)), world, pos);
   }
}
