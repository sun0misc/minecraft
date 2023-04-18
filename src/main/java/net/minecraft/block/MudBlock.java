package net.minecraft.block;

import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class MudBlock extends Block {
   protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

   public MudBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
      return VoxelShapes.fullCube();
   }

   public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.fullCube();
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 0.2F;
   }
}
