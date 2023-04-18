package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class RandomFeature extends Feature {
   public RandomFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      RandomFeatureConfig lv = (RandomFeatureConfig)context.getConfig();
      Random lv2 = context.getRandom();
      StructureWorldAccess lv3 = context.getWorld();
      ChunkGenerator lv4 = context.getGenerator();
      BlockPos lv5 = context.getOrigin();
      Iterator var7 = lv.features.iterator();

      RandomFeatureEntry lv6;
      do {
         if (!var7.hasNext()) {
            return ((PlacedFeature)lv.defaultFeature.value()).generateUnregistered(lv3, lv4, lv2, lv5);
         }

         lv6 = (RandomFeatureEntry)var7.next();
      } while(!(lv2.nextFloat() < lv6.chance));

      return lv6.generate(lv3, lv4, lv2, lv5);
   }
}
