package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

public class BasaltColumnsFeature extends Feature {
   private static final ImmutableList CANNOT_REPLACE_BLOCKS;
   private static final int field_31495 = 5;
   private static final int field_31496 = 50;
   private static final int field_31497 = 8;
   private static final int field_31498 = 15;

   public BasaltColumnsFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      int i = context.getGenerator().getSeaLevel();
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Random lv3 = context.getRandom();
      BasaltColumnsFeatureConfig lv4 = (BasaltColumnsFeatureConfig)context.getConfig();
      if (!canPlaceAt(lv2, i, lv.mutableCopy())) {
         return false;
      } else {
         int j = lv4.getHeight().get(lv3);
         boolean bl = lv3.nextFloat() < 0.9F;
         int k = Math.min(j, bl ? 5 : 8);
         int l = bl ? 50 : 15;
         boolean bl2 = false;
         Iterator var12 = BlockPos.iterateRandomly(lv3, l, lv.getX() - k, lv.getY(), lv.getZ() - k, lv.getX() + k, lv.getY(), lv.getZ() + k).iterator();

         while(var12.hasNext()) {
            BlockPos lv5 = (BlockPos)var12.next();
            int m = j - lv5.getManhattanDistance(lv);
            if (m >= 0) {
               bl2 |= this.placeBasaltColumn(lv2, i, lv5, m, lv4.getReach().get(lv3));
            }
         }

         return bl2;
      }
   }

   private boolean placeBasaltColumn(WorldAccess world, int seaLevel, BlockPos pos, int height, int reach) {
      boolean bl = false;
      Iterator var7 = BlockPos.iterate(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach).iterator();

      while(true) {
         int l;
         BlockPos lv2;
         do {
            if (!var7.hasNext()) {
               return bl;
            }

            BlockPos lv = (BlockPos)var7.next();
            l = lv.getManhattanDistance(pos);
            lv2 = isAirOrLavaOcean(world, seaLevel, lv) ? moveDownToGround(world, seaLevel, lv.mutableCopy(), l) : moveUpToAir(world, lv.mutableCopy(), l);
         } while(lv2 == null);

         int m = height - l / 2;

         for(BlockPos.Mutable lv3 = lv2.mutableCopy(); m >= 0; --m) {
            if (isAirOrLavaOcean(world, seaLevel, lv3)) {
               this.setBlockState(world, lv3, Blocks.BASALT.getDefaultState());
               lv3.move(Direction.UP);
               bl = true;
            } else {
               if (!world.getBlockState(lv3).isOf(Blocks.BASALT)) {
                  break;
               }

               lv3.move(Direction.UP);
            }
         }
      }
   }

   @Nullable
   private static BlockPos moveDownToGround(WorldAccess world, int seaLevel, BlockPos.Mutable mutablePos, int distance) {
      while(mutablePos.getY() > world.getBottomY() + 1 && distance > 0) {
         --distance;
         if (canPlaceAt(world, seaLevel, mutablePos)) {
            return mutablePos;
         }

         mutablePos.move(Direction.DOWN);
      }

      return null;
   }

   private static boolean canPlaceAt(WorldAccess world, int seaLevel, BlockPos.Mutable mutablePos) {
      if (!isAirOrLavaOcean(world, seaLevel, mutablePos)) {
         return false;
      } else {
         BlockState lv = world.getBlockState(mutablePos.move(Direction.DOWN));
         mutablePos.move(Direction.UP);
         return !lv.isAir() && !CANNOT_REPLACE_BLOCKS.contains(lv.getBlock());
      }
   }

   @Nullable
   private static BlockPos moveUpToAir(WorldAccess world, BlockPos.Mutable mutablePos, int distance) {
      while(mutablePos.getY() < world.getTopY() && distance > 0) {
         --distance;
         BlockState lv = world.getBlockState(mutablePos);
         if (CANNOT_REPLACE_BLOCKS.contains(lv.getBlock())) {
            return null;
         }

         if (lv.isAir()) {
            return mutablePos;
         }

         mutablePos.move(Direction.UP);
      }

      return null;
   }

   private static boolean isAirOrLavaOcean(WorldAccess world, int seaLevel, BlockPos pos) {
      BlockState lv = world.getBlockState(pos);
      return lv.isAir() || lv.isOf(Blocks.LAVA) && pos.getY() <= seaLevel;
   }

   static {
      CANNOT_REPLACE_BLOCKS = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
   }
}
