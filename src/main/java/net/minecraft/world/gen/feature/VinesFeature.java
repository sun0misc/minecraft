package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class VinesFeature extends Feature {
   public VinesFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      context.getConfig();
      if (!lv.isAir(lv2)) {
         return false;
      } else {
         Direction[] var4 = Direction.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Direction lv3 = var4[var6];
            if (lv3 != Direction.DOWN && VineBlock.shouldConnectTo(lv, lv2.offset(lv3), lv3)) {
               lv.setBlockState(lv2, (BlockState)Blocks.VINE.getDefaultState().with(VineBlock.getFacingProperty(lv3), true), Block.NOTIFY_LISTENERS);
               return true;
            }
         }

         return false;
      }
   }
}
