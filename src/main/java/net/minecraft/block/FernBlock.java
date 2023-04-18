package net.minecraft.block;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class FernBlock extends PlantBlock implements Fertilizable {
   protected static final float field_31261 = 6.0F;
   protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

   protected FernBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
      return true;
   }

   public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
      TallPlantBlock lv = (TallPlantBlock)(state.isOf(Blocks.FERN) ? Blocks.LARGE_FERN : Blocks.TALL_GRASS);
      if (lv.getDefaultState().canPlaceAt(world, pos) && world.isAir(pos.up())) {
         TallPlantBlock.placeAt(world, lv.getDefaultState(), pos, 2);
      }

   }
}
