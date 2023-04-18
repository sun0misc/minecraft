package net.minecraft.block;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;

public class TwistingVinesBlock extends AbstractPlantStemBlock {
   public static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 15.0, 12.0);

   public TwistingVinesBlock(AbstractBlock.Settings arg) {
      super(arg, Direction.UP, SHAPE, false, 0.1);
   }

   protected int getGrowthLength(Random random) {
      return VineLogic.getGrowthLength(random);
   }

   protected Block getPlant() {
      return Blocks.TWISTING_VINES_PLANT;
   }

   protected boolean chooseStemState(BlockState state) {
      return VineLogic.isValidForWeepingStem(state);
   }
}
