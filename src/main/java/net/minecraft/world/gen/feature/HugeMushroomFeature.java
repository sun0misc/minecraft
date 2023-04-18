package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public abstract class HugeMushroomFeature extends Feature {
   public HugeMushroomFeature(Codec codec) {
      super(codec);
   }

   protected void generateStem(WorldAccess world, Random random, BlockPos pos, HugeMushroomFeatureConfig config, int height, BlockPos.Mutable mutablePos) {
      for(int j = 0; j < height; ++j) {
         mutablePos.set(pos).move(Direction.UP, j);
         if (!world.getBlockState(mutablePos).isOpaqueFullCube(world, mutablePos)) {
            this.setBlockState(world, mutablePos, config.stemProvider.get(random, pos));
         }
      }

   }

   protected int getHeight(Random random) {
      int i = random.nextInt(3) + 4;
      if (random.nextInt(12) == 0) {
         i *= 2;
      }

      return i;
   }

   protected boolean canGenerate(WorldAccess world, BlockPos pos, int height, BlockPos.Mutable mutablePos, HugeMushroomFeatureConfig config) {
      int j = pos.getY();
      if (j >= world.getBottomY() + 1 && j + height + 1 < world.getTopY()) {
         BlockState lv = world.getBlockState(pos.down());
         if (!isSoil(lv) && !lv.isIn(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return false;
         } else {
            for(int k = 0; k <= height; ++k) {
               int l = this.getCapSize(-1, -1, config.foliageRadius, k);

               for(int m = -l; m <= l; ++m) {
                  for(int n = -l; n <= l; ++n) {
                     BlockState lv2 = world.getBlockState(mutablePos.set((Vec3i)pos, m, k, n));
                     if (!lv2.isAir() && !lv2.isIn(BlockTags.LEAVES)) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      Random lv3 = context.getRandom();
      HugeMushroomFeatureConfig lv4 = (HugeMushroomFeatureConfig)context.getConfig();
      int i = this.getHeight(lv3);
      BlockPos.Mutable lv5 = new BlockPos.Mutable();
      if (!this.canGenerate(lv, lv2, i, lv5, lv4)) {
         return false;
      } else {
         this.generateCap(lv, lv3, lv2, i, lv5, lv4);
         this.generateStem(lv, lv3, lv2, lv4, i, lv5);
         return true;
      }
   }

   protected abstract int getCapSize(int i, int j, int capSize, int y);

   protected abstract void generateCap(WorldAccess world, Random random, BlockPos start, int y, BlockPos.Mutable mutable, HugeMushroomFeatureConfig config);
}
