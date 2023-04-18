package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class GeodeFeature extends Feature {
   private static final Direction[] DIRECTIONS = Direction.values();

   public GeodeFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      GeodeFeatureConfig lv = (GeodeFeatureConfig)context.getConfig();
      Random lv2 = context.getRandom();
      BlockPos lv3 = context.getOrigin();
      StructureWorldAccess lv4 = context.getWorld();
      int i = lv.minGenOffset;
      int j = lv.maxGenOffset;
      List list = Lists.newLinkedList();
      int k = lv.distributionPoints.get(lv2);
      ChunkRandom lv5 = new ChunkRandom(new CheckedRandom(lv4.getSeed()));
      DoublePerlinNoiseSampler lv6 = DoublePerlinNoiseSampler.create(lv5, -4, 1.0);
      List list2 = Lists.newLinkedList();
      double d = (double)k / (double)lv.outerWallDistance.getMax();
      GeodeLayerThicknessConfig lv7 = lv.layerThicknessConfig;
      GeodeLayerConfig lv8 = lv.layerConfig;
      GeodeCrackConfig lv9 = lv.crackConfig;
      double e = 1.0 / Math.sqrt(lv7.filling);
      double f = 1.0 / Math.sqrt(lv7.innerLayer + d);
      double g = 1.0 / Math.sqrt(lv7.middleLayer + d);
      double h = 1.0 / Math.sqrt(lv7.outerLayer + d);
      double l = 1.0 / Math.sqrt(lv9.baseCrackSize + lv2.nextDouble() / 2.0 + (k > 3 ? d : 0.0));
      boolean bl = (double)lv2.nextFloat() < lv9.generateCrackChance;
      int m = 0;

      int n;
      int o;
      BlockPos lv10;
      BlockState lv11;
      for(n = 0; n < k; ++n) {
         o = lv.outerWallDistance.get(lv2);
         int p = lv.outerWallDistance.get(lv2);
         int q = lv.outerWallDistance.get(lv2);
         lv10 = lv3.add(o, p, q);
         lv11 = lv4.getBlockState(lv10);
         if (lv11.isAir() || lv11.isIn(BlockTags.GEODE_INVALID_BLOCKS)) {
            ++m;
            if (m > lv.invalidBlocksThreshold) {
               return false;
            }
         }

         list.add(Pair.of(lv10, lv.pointOffset.get(lv2)));
      }

      if (bl) {
         n = lv2.nextInt(4);
         o = k * 2 + 1;
         if (n == 0) {
            list2.add(lv3.add(o, 7, 0));
            list2.add(lv3.add(o, 5, 0));
            list2.add(lv3.add(o, 1, 0));
         } else if (n == 1) {
            list2.add(lv3.add(0, 7, o));
            list2.add(lv3.add(0, 5, o));
            list2.add(lv3.add(0, 1, o));
         } else if (n == 2) {
            list2.add(lv3.add(o, 7, o));
            list2.add(lv3.add(o, 5, o));
            list2.add(lv3.add(o, 1, o));
         } else {
            list2.add(lv3.add(0, 7, 0));
            list2.add(lv3.add(0, 5, 0));
            list2.add(lv3.add(0, 1, 0));
         }
      }

      List list3 = Lists.newArrayList();
      Predicate predicate = notInBlockTagPredicate(lv.layerConfig.cannotReplace);
      Iterator var48 = BlockPos.iterate(lv3.add(i, i, i), lv3.add(j, j, j)).iterator();

      while(true) {
         while(true) {
            double s;
            double t;
            BlockPos lv12;
            do {
               if (!var48.hasNext()) {
                  List list4 = lv8.innerBlocks;
                  Iterator var51 = list3.iterator();

                  while(true) {
                     while(var51.hasNext()) {
                        lv10 = (BlockPos)var51.next();
                        lv11 = (BlockState)Util.getRandom(list4, lv2);
                        Direction[] var53 = DIRECTIONS;
                        int var37 = var53.length;

                        for(int var54 = 0; var54 < var37; ++var54) {
                           Direction lv17 = var53[var54];
                           if (lv11.contains(Properties.FACING)) {
                              lv11 = (BlockState)lv11.with(Properties.FACING, lv17);
                           }

                           BlockPos lv18 = lv10.offset(lv17);
                           BlockState lv19 = lv4.getBlockState(lv18);
                           if (lv11.contains(Properties.WATERLOGGED)) {
                              lv11 = (BlockState)lv11.with(Properties.WATERLOGGED, lv19.getFluidState().isStill());
                           }

                           if (BuddingAmethystBlock.canGrowIn(lv19)) {
                              this.setBlockStateIf(lv4, lv18, lv11, predicate);
                              break;
                           }
                        }
                     }

                     return true;
                  }
               }

               lv12 = (BlockPos)var48.next();
               double r = lv6.sample((double)lv12.getX(), (double)lv12.getY(), (double)lv12.getZ()) * lv.noiseMultiplier;
               s = 0.0;
               t = 0.0;

               Iterator var40;
               Pair pair;
               for(var40 = list.iterator(); var40.hasNext(); s += MathHelper.inverseSqrt(lv12.getSquaredDistance((Vec3i)pair.getFirst()) + (double)(Integer)pair.getSecond()) + r) {
                  pair = (Pair)var40.next();
               }

               BlockPos lv13;
               for(var40 = list2.iterator(); var40.hasNext(); t += MathHelper.inverseSqrt(lv12.getSquaredDistance(lv13) + (double)lv9.crackPointOffset) + r) {
                  lv13 = (BlockPos)var40.next();
               }
            } while(s < h);

            if (bl && t >= l && s < e) {
               this.setBlockStateIf(lv4, lv12, Blocks.AIR.getDefaultState(), predicate);
               Direction[] var56 = DIRECTIONS;
               int var59 = var56.length;

               for(int var42 = 0; var42 < var59; ++var42) {
                  Direction lv14 = var56[var42];
                  BlockPos lv15 = lv12.offset(lv14);
                  FluidState lv16 = lv4.getFluidState(lv15);
                  if (!lv16.isEmpty()) {
                     lv4.scheduleFluidTick(lv15, lv16.getFluid(), 0);
                  }
               }
            } else if (s >= e) {
               this.setBlockStateIf(lv4, lv12, lv8.fillingProvider.get(lv2, lv12), predicate);
            } else if (s >= f) {
               boolean bl2 = (double)lv2.nextFloat() < lv.useAlternateLayer0Chance;
               if (bl2) {
                  this.setBlockStateIf(lv4, lv12, lv8.alternateInnerLayerProvider.get(lv2, lv12), predicate);
               } else {
                  this.setBlockStateIf(lv4, lv12, lv8.innerLayerProvider.get(lv2, lv12), predicate);
               }

               if ((!lv.placementsRequireLayer0Alternate || bl2) && (double)lv2.nextFloat() < lv.usePotentialPlacementsChance) {
                  list3.add(lv12.toImmutable());
               }
            } else if (s >= g) {
               this.setBlockStateIf(lv4, lv12, lv8.middleLayerProvider.get(lv2, lv12), predicate);
            } else if (s >= h) {
               this.setBlockStateIf(lv4, lv12, lv8.outerLayerProvider.get(lv2, lv12), predicate);
            }
         }
      }
   }
}
