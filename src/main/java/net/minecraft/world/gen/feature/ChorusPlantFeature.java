package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class ChorusPlantFeature extends Feature {
   public ChorusPlantFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      Random lv3 = context.getRandom();
      if (lv.isAir(lv2) && lv.getBlockState(lv2.down()).isOf(Blocks.END_STONE)) {
         ChorusFlowerBlock.generate(lv, lv2, lv3, 8);
         return true;
      } else {
         return false;
      }
   }
}
