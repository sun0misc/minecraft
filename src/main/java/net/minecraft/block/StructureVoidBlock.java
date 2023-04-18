package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class StructureVoidBlock extends Block {
   private static final double field_31257 = 5.0;
   private static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

   protected StructureVoidBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 1.0F;
   }
}
