package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class ScatteredOreFeature extends Feature {
   private static final int field_31515 = 7;

   ScatteredOreFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      Random lv2 = context.getRandom();
      OreFeatureConfig lv3 = (OreFeatureConfig)context.getConfig();
      BlockPos lv4 = context.getOrigin();
      int i = lv2.nextInt(lv3.size + 1);
      BlockPos.Mutable lv5 = new BlockPos.Mutable();

      for(int j = 0; j < i; ++j) {
         this.setPos(lv5, lv2, lv4, Math.min(j, 7));
         BlockState lv6 = lv.getBlockState(lv5);
         Iterator var10 = lv3.targets.iterator();

         while(var10.hasNext()) {
            OreFeatureConfig.Target lv7 = (OreFeatureConfig.Target)var10.next();
            Objects.requireNonNull(lv);
            if (OreFeature.shouldPlace(lv6, lv::getBlockState, lv2, lv3, lv7, lv5)) {
               lv.setBlockState(lv5, lv7.state, Block.NOTIFY_LISTENERS);
               break;
            }
         }
      }

      return true;
   }

   private void setPos(BlockPos.Mutable mutable, Random arg2, BlockPos origin, int spread) {
      int j = this.getSpread(arg2, spread);
      int k = this.getSpread(arg2, spread);
      int l = this.getSpread(arg2, spread);
      mutable.set((Vec3i)origin, j, k, l);
   }

   private int getSpread(Random arg, int spread) {
      return Math.round((arg.nextFloat() - arg.nextFloat()) * (float)spread);
   }
}
