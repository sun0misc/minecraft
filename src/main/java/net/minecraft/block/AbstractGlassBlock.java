package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public abstract class AbstractGlassBlock extends TransparentBlock {
   protected AbstractGlassBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 1.0F;
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }
}
