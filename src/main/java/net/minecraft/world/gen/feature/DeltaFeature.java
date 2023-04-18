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

public class DeltaFeature extends Feature {
   private static final ImmutableList CANNOT_REPLACE_BLOCKS;
   private static final Direction[] DIRECTIONS;
   private static final double field_31501 = 0.9;

   public DeltaFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      boolean bl = false;
      Random lv = context.getRandom();
      StructureWorldAccess lv2 = context.getWorld();
      DeltaFeatureConfig lv3 = (DeltaFeatureConfig)context.getConfig();
      BlockPos lv4 = context.getOrigin();
      boolean bl2 = lv.nextDouble() < 0.9;
      int i = bl2 ? lv3.getRimSize().get(lv) : 0;
      int j = bl2 ? lv3.getRimSize().get(lv) : 0;
      boolean bl3 = bl2 && i != 0 && j != 0;
      int k = lv3.getSize().get(lv);
      int l = lv3.getSize().get(lv);
      int m = Math.max(k, l);
      Iterator var14 = BlockPos.iterateOutwards(lv4, k, 0, l).iterator();

      while(var14.hasNext()) {
         BlockPos lv5 = (BlockPos)var14.next();
         if (lv5.getManhattanDistance(lv4) > m) {
            break;
         }

         if (canPlace(lv2, lv5, lv3)) {
            if (bl3) {
               bl = true;
               this.setBlockState(lv2, lv5, lv3.getRim());
            }

            BlockPos lv6 = lv5.add(i, 0, j);
            if (canPlace(lv2, lv6, lv3)) {
               bl = true;
               this.setBlockState(lv2, lv6, lv3.getContents());
            }
         }
      }

      return bl;
   }

   private static boolean canPlace(WorldAccess world, BlockPos pos, DeltaFeatureConfig config) {
      BlockState lv = world.getBlockState(pos);
      if (lv.isOf(config.getContents().getBlock())) {
         return false;
      } else if (CANNOT_REPLACE_BLOCKS.contains(lv.getBlock())) {
         return false;
      } else {
         Direction[] var4 = DIRECTIONS;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Direction lv2 = var4[var6];
            boolean bl = world.getBlockState(pos.offset(lv2)).isAir();
            if (bl && lv2 != Direction.UP || !bl && lv2 == Direction.UP) {
               return false;
            }
         }

         return true;
      }
   }

   static {
      CANNOT_REPLACE_BLOCKS = ImmutableList.of(Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
      DIRECTIONS = Direction.values();
   }
}
