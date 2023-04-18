package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BambooFeature extends Feature {
   private static final BlockState BAMBOO;
   private static final BlockState BAMBOO_TOP_1;
   private static final BlockState BAMBOO_TOP_2;
   private static final BlockState BAMBOO_TOP_3;

   public BambooFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      int i = 0;
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      ProbabilityConfig lv4 = (ProbabilityConfig)context.getConfig();
      BlockPos.Mutable lv5 = lv.mutableCopy();
      BlockPos.Mutable lv6 = lv.mutableCopy();
      if (lv2.isAir(lv5)) {
         if (Blocks.BAMBOO.getDefaultState().canPlaceAt(lv2, lv5)) {
            int j = lv3.nextInt(12) + 5;
            int k;
            if (lv3.nextFloat() < lv4.probability) {
               k = lv3.nextInt(4) + 1;

               for(int l = lv.getX() - k; l <= lv.getX() + k; ++l) {
                  for(int m = lv.getZ() - k; m <= lv.getZ() + k; ++m) {
                     int n = l - lv.getX();
                     int o = m - lv.getZ();
                     if (n * n + o * o <= k * k) {
                        lv6.set(l, lv2.getTopY(Heightmap.Type.WORLD_SURFACE, l, m) - 1, m);
                        if (isSoil(lv2.getBlockState(lv6))) {
                           lv2.setBlockState(lv6, Blocks.PODZOL.getDefaultState(), Block.NOTIFY_LISTENERS);
                        }
                     }
                  }
               }
            }

            for(k = 0; k < j && lv2.isAir(lv5); ++k) {
               lv2.setBlockState(lv5, BAMBOO, Block.NOTIFY_LISTENERS);
               lv5.move(Direction.UP, 1);
            }

            if (lv5.getY() - lv.getY() >= 3) {
               lv2.setBlockState(lv5, BAMBOO_TOP_1, Block.NOTIFY_LISTENERS);
               lv2.setBlockState(lv5.move(Direction.DOWN, 1), BAMBOO_TOP_2, Block.NOTIFY_LISTENERS);
               lv2.setBlockState(lv5.move(Direction.DOWN, 1), BAMBOO_TOP_3, Block.NOTIFY_LISTENERS);
            }
         }

         ++i;
      }

      return i > 0;
   }

   static {
      BAMBOO = (BlockState)((BlockState)((BlockState)Blocks.BAMBOO.getDefaultState().with(BambooBlock.AGE, 1)).with(BambooBlock.LEAVES, BambooLeaves.NONE)).with(BambooBlock.STAGE, 0);
      BAMBOO_TOP_1 = (BlockState)((BlockState)BAMBOO.with(BambooBlock.LEAVES, BambooLeaves.LARGE)).with(BambooBlock.STAGE, 1);
      BAMBOO_TOP_2 = (BlockState)BAMBOO.with(BambooBlock.LEAVES, BambooLeaves.LARGE);
      BAMBOO_TOP_3 = (BlockState)BAMBOO.with(BambooBlock.LEAVES, BambooLeaves.SMALL);
   }
}
