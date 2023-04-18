package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndIslandFeature extends Feature {
   public EndIslandFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      Random lv2 = context.getRandom();
      BlockPos lv3 = context.getOrigin();
      float f = (float)lv2.nextInt(3) + 4.0F;

      for(int i = 0; f > 0.5F; --i) {
         for(int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
            for(int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
               if ((float)(j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
                  this.setBlockState(lv, lv3.add(j, i, k), Blocks.END_STONE.getDefaultState());
               }
            }
         }

         f -= (float)lv2.nextInt(2) + 0.5F;
      }

      return true;
   }
}
