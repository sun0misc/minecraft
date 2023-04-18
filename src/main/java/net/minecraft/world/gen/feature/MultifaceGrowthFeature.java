package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class MultifaceGrowthFeature extends Feature {
   public MultifaceGrowthFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      StructureWorldAccess lv = context.getWorld();
      BlockPos lv2 = context.getOrigin();
      Random lv3 = context.getRandom();
      MultifaceGrowthFeatureConfig lv4 = (MultifaceGrowthFeatureConfig)context.getConfig();
      if (!isAirOrWater(lv.getBlockState(lv2))) {
         return false;
      } else {
         List list = lv4.shuffleDirections(lv3);
         if (generate(lv, lv2, lv.getBlockState(lv2), lv4, lv3, list)) {
            return true;
         } else {
            BlockPos.Mutable lv5 = lv2.mutableCopy();
            Iterator var8 = list.iterator();

            while(var8.hasNext()) {
               Direction lv6 = (Direction)var8.next();
               lv5.set(lv2);
               List list2 = lv4.shuffleDirections(lv3, lv6.getOpposite());

               for(int i = 0; i < lv4.searchRange; ++i) {
                  lv5.set(lv2, (Direction)lv6);
                  BlockState lv7 = lv.getBlockState(lv5);
                  if (!isAirOrWater(lv7) && !lv7.isOf(lv4.lichen)) {
                     break;
                  }

                  if (generate(lv, lv5, lv7, lv4, lv3, list2)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   public static boolean generate(StructureWorldAccess world, BlockPos pos, BlockState state, MultifaceGrowthFeatureConfig config, Random random, List directions) {
      BlockPos.Mutable lv = pos.mutableCopy();
      Iterator var7 = directions.iterator();

      Direction lv2;
      BlockState lv3;
      do {
         if (!var7.hasNext()) {
            return false;
         }

         lv2 = (Direction)var7.next();
         lv3 = world.getBlockState(lv.set(pos, (Direction)lv2));
      } while(!lv3.isIn(config.canPlaceOn));

      BlockState lv4 = config.lichen.withDirection(state, world, pos, lv2);
      if (lv4 == null) {
         return false;
      } else {
         world.setBlockState(pos, lv4, Block.NOTIFY_ALL);
         world.getChunk(pos).markBlockForPostProcessing(pos);
         if (random.nextFloat() < config.spreadChance) {
            config.lichen.getGrower().grow(lv4, world, pos, lv2, (Random)random, true);
         }

         return true;
      }
   }

   private static boolean isAirOrWater(BlockState state) {
      return state.isAir() || state.isOf(Blocks.WATER);
   }
}
