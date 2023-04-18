package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BarrierBlock extends Block {
   protected BarrierBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 1.0F;
   }
}
