package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomPatchFeature extends Feature {
   public RandomPatchFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      RandomPatchFeatureConfig lv = (RandomPatchFeatureConfig)context.getConfig();
      Random lv2 = context.getRandom();
      BlockPos lv3 = context.getOrigin();
      StructureWorldAccess lv4 = context.getWorld();
      int i = 0;
      BlockPos.Mutable lv5 = new BlockPos.Mutable();
      int j = lv.xzSpread() + 1;
      int k = lv.ySpread() + 1;

      for(int l = 0; l < lv.tries(); ++l) {
         lv5.set((Vec3i)lv3, lv2.nextInt(j) - lv2.nextInt(j), lv2.nextInt(k) - lv2.nextInt(k), lv2.nextInt(j) - lv2.nextInt(j));
         if (((PlacedFeature)lv.feature().value()).generateUnregistered(lv4, context.getGenerator(), lv2, lv5)) {
            ++i;
         }
      }

      return i > 0;
   }
}
