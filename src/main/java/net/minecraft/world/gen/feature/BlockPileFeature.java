package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BlockPileFeature extends Feature {
   public BlockPileFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      BlockPileFeatureConfig lv4 = (BlockPileFeatureConfig)context.getConfig();
      if (lv.getY() < lv2.getBottomY() + 5) {
         return false;
      } else {
         int i = 2 + lv3.nextInt(2);
         int j = 2 + lv3.nextInt(2);
         Iterator var8 = BlockPos.iterate(lv.add(-i, 0, -j), lv.add(i, 1, j)).iterator();

         while(var8.hasNext()) {
            BlockPos lv5 = (BlockPos)var8.next();
            int k = lv.getX() - lv5.getX();
            int l = lv.getZ() - lv5.getZ();
            if ((float)(k * k + l * l) <= lv3.nextFloat() * 10.0F - lv3.nextFloat() * 6.0F) {
               this.addPileBlock(lv2, lv5, lv3, lv4);
            } else if ((double)lv3.nextFloat() < 0.031) {
               this.addPileBlock(lv2, lv5, lv3, lv4);
            }
         }

         return true;
      }
   }

   private boolean canPlace(WorldAccess world, BlockPos pos, Random random) {
      BlockPos lv = pos.down();
      BlockState lv2 = world.getBlockState(lv);
      return lv2.isOf(Blocks.DIRT_PATH) ? random.nextBoolean() : lv2.isSideSolidFullSquare(world, lv, Direction.UP);
   }

   private void addPileBlock(WorldAccess world, BlockPos pos, Random random, BlockPileFeatureConfig config) {
      if (world.isAir(pos) && this.canPlace(world, pos, random)) {
         world.setBlockState(pos, config.stateProvider.get(random, pos), Block.NO_REDRAW);
      }

   }
}
