package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SpringFeature extends Feature {
   public SpringFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      SpringFeatureConfig lv = (SpringFeatureConfig)context.getConfig();
      StructureWorldAccess lv2 = context.getWorld();
      BlockPos lv3 = context.getOrigin();
      if (!lv2.getBlockState(lv3.up()).isIn(lv.validBlocks)) {
         return false;
      } else if (lv.requiresBlockBelow && !lv2.getBlockState(lv3.down()).isIn(lv.validBlocks)) {
         return false;
      } else {
         BlockState lv4 = lv2.getBlockState(lv3);
         if (!lv4.isAir() && !lv4.isIn(lv.validBlocks)) {
            return false;
         } else {
            int i = 0;
            int j = 0;
            if (lv2.getBlockState(lv3.west()).isIn(lv.validBlocks)) {
               ++j;
            }

            if (lv2.getBlockState(lv3.east()).isIn(lv.validBlocks)) {
               ++j;
            }

            if (lv2.getBlockState(lv3.north()).isIn(lv.validBlocks)) {
               ++j;
            }

            if (lv2.getBlockState(lv3.south()).isIn(lv.validBlocks)) {
               ++j;
            }

            if (lv2.getBlockState(lv3.down()).isIn(lv.validBlocks)) {
               ++j;
            }

            int k = 0;
            if (lv2.isAir(lv3.west())) {
               ++k;
            }

            if (lv2.isAir(lv3.east())) {
               ++k;
            }

            if (lv2.isAir(lv3.north())) {
               ++k;
            }

            if (lv2.isAir(lv3.south())) {
               ++k;
            }

            if (lv2.isAir(lv3.down())) {
               ++k;
            }

            if (j == lv.rockCount && k == lv.holeCount) {
               lv2.setBlockState(lv3, lv.state.getBlockState(), Block.NOTIFY_LISTENERS);
               lv2.scheduleFluidTick(lv3, lv.state.getFluid(), 0);
               ++i;
            }

            return i > 0;
         }
      }
   }
}
