package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EmeraldOreFeature extends Feature {
   public EmeraldOreFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      EmeraldOreFeatureConfig lv3 = (EmeraldOreFeatureConfig)context.getConfig();
      Iterator var5 = lv3.targets.iterator();

      while(var5.hasNext()) {
         OreFeatureConfig.Target lv4 = (OreFeatureConfig.Target)var5.next();
         if (lv4.target.test(lv.getBlockState(lv2), context.getRandom())) {
            lv.setBlockState(lv2, lv4.state, Block.NOTIFY_LISTENERS);
            break;
         }
      }

      return true;
   }
}
