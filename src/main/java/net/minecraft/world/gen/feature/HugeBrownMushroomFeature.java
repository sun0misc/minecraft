package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.MushroomBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class HugeBrownMushroomFeature extends HugeMushroomFeature {
   public HugeBrownMushroomFeature(Codec codec) {
      super(codec);
   }

   protected void generateCap(WorldAccess world, Random random, BlockPos start, int y, BlockPos.Mutable mutable, HugeMushroomFeatureConfig config) {
      int j = config.foliageRadius;

      for(int k = -j; k <= j; ++k) {
         for(int l = -j; l <= j; ++l) {
            boolean bl = k == -j;
            boolean bl2 = k == j;
            boolean bl3 = l == -j;
            boolean bl4 = l == j;
            boolean bl5 = bl || bl2;
            boolean bl6 = bl3 || bl4;
            if (!bl5 || !bl6) {
               mutable.set((Vec3i)start, k, y, l);
               if (!world.getBlockState(mutable).isOpaqueFullCube(world, mutable)) {
                  boolean bl7 = bl || bl6 && k == 1 - j;
                  boolean bl8 = bl2 || bl6 && k == j - 1;
                  boolean bl9 = bl3 || bl5 && l == 1 - j;
                  boolean bl10 = bl4 || bl5 && l == j - 1;
                  BlockState lv = config.capProvider.get(random, start);
                  if (lv.contains(MushroomBlock.WEST) && lv.contains(MushroomBlock.EAST) && lv.contains(MushroomBlock.NORTH) && lv.contains(MushroomBlock.SOUTH)) {
                     lv = (BlockState)((BlockState)((BlockState)((BlockState)lv.with(MushroomBlock.WEST, bl7)).with(MushroomBlock.EAST, bl8)).with(MushroomBlock.NORTH, bl9)).with(MushroomBlock.SOUTH, bl10);
                  }

                  this.setBlockState(world, mutable, lv);
               }
            }
         }
      }

   }

   protected int getCapSize(int i, int j, int capSize, int y) {
      return y <= 3 ? 0 : capSize;
   }
}
