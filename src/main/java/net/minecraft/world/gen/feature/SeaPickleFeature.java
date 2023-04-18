package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.CountConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SeaPickleFeature extends Feature {
   public SeaPickleFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      int i = 0;
      Random lv = context.getRandom();
      StructureWorldAccess lv2 = context.getWorld();
      BlockPos lv3 = context.getOrigin();
      int j = ((CountConfig)context.getConfig()).getCount().get(lv);

      for(int k = 0; k < j; ++k) {
         int l = lv.nextInt(8) - lv.nextInt(8);
         int m = lv.nextInt(8) - lv.nextInt(8);
         int n = lv2.getTopY(Heightmap.Type.OCEAN_FLOOR, lv3.getX() + l, lv3.getZ() + m);
         BlockPos lv4 = new BlockPos(lv3.getX() + l, n, lv3.getZ() + m);
         BlockState lv5 = (BlockState)Blocks.SEA_PICKLE.getDefaultState().with(SeaPickleBlock.PICKLES, lv.nextInt(4) + 1);
         if (lv2.getBlockState(lv4).isOf(Blocks.WATER) && lv5.canPlaceAt(lv2, lv4)) {
            lv2.setBlockState(lv4, lv5, Block.NOTIFY_LISTENERS);
            ++i;
         }
      }

      return i > 0;
   }
}
