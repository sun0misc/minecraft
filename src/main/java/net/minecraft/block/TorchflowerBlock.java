package net.minecraft.block;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TorchflowerBlock extends CropBlock {
   public static final int field_42775 = 2;
   public static final IntProperty AGE;
   private static final float field_42777 = 3.0F;
   private static final VoxelShape[] SHAPES;
   private static final int field_44479 = 1;

   public TorchflowerBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(AGE);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPES[(Integer)state.get(this.getAgeProperty())];
   }

   public IntProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 2;
   }

   protected ItemConvertible getSeedsItem() {
      return Items.TORCHFLOWER_SEEDS;
   }

   public BlockState withAge(int age) {
      return age == 2 ? Blocks.TORCHFLOWER.getDefaultState() : super.withAge(age);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (random.nextInt(3) != 0) {
         super.randomTick(state, world, pos, random);
      }

   }

   protected int getGrowthAmount(World world) {
      return 1;
   }

   static {
      AGE = Properties.AGE_1;
      SHAPES = new VoxelShape[]{Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0), Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 10.0, 11.0)};
   }
}
