package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DiskFeature extends Feature {
   public DiskFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      DiskFeatureConfig lv = (DiskFeatureConfig)context.getConfig();
      BlockPos lv2 = context.getOrigin();
      StructureWorldAccess lv3 = context.getWorld();
      Random lv4 = context.getRandom();
      boolean bl = false;
      int i = lv2.getY();
      int j = i + lv.halfHeight();
      int k = i - lv.halfHeight() - 1;
      int l = lv.radius().get(lv4);
      BlockPos.Mutable lv5 = new BlockPos.Mutable();
      Iterator var12 = BlockPos.iterate(lv2.add(-l, 0, -l), lv2.add(l, 0, l)).iterator();

      while(var12.hasNext()) {
         BlockPos lv6 = (BlockPos)var12.next();
         int m = lv6.getX() - lv2.getX();
         int n = lv6.getZ() - lv2.getZ();
         if (m * m + n * n <= l * l) {
            bl |= this.placeBlock(lv, lv3, lv4, j, k, lv5.set(lv6));
         }
      }

      return bl;
   }

   protected boolean placeBlock(DiskFeatureConfig config, StructureWorldAccess world, Random random, int topY, int bottomY, BlockPos.Mutable pos) {
      boolean bl = false;
      BlockState lv = null;

      for(int k = topY; k > bottomY; --k) {
         pos.setY(k);
         if (config.target().test(world, pos)) {
            BlockState lv2 = config.stateProvider().getBlockState(world, random, pos);
            world.setBlockState(pos, lv2, Block.NOTIFY_LISTENERS);
            this.markBlocksAboveForPostProcessing(world, pos);
            bl = true;
         }
      }

      return bl;
   }
}
