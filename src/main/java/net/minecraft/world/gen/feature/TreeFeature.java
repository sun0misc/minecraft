package net.minecraft.world.gen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.root.RootPlacer;
import net.minecraft.world.gen.treedecorator.TreeDecorator;

public class TreeFeature extends Feature {
   private static final int FORCE_STATE_AND_NOTIFY_ALL = 19;

   public TreeFeature(Codec codec) {
      super(codec);
   }

   private static boolean isVine(TestableWorld world, BlockPos pos) {
      return world.testBlockState(pos, (state) -> {
         return state.isOf(Blocks.VINE);
      });
   }

   public static boolean isAirOrLeaves(TestableWorld world, BlockPos pos) {
      return world.testBlockState(pos, (state) -> {
         return state.isAir() || state.isIn(BlockTags.LEAVES);
      });
   }

   private static void setBlockStateWithoutUpdatingNeighbors(ModifiableWorld world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
   }

   public static boolean canReplace(TestableWorld world, BlockPos pos) {
      return world.testBlockState(pos, (state) -> {
         return state.isAir() || state.isIn(BlockTags.REPLACEABLE_BY_TREES);
      });
   }

   private boolean generate(StructureWorldAccess world, Random random, BlockPos pos, BiConsumer rootPlacerReplacer, BiConsumer trunkPlacerReplacer, FoliagePlacer.BlockPlacer blockPlacer, TreeFeatureConfig config) {
      int i = config.trunkPlacer.getHeight(random);
      int j = config.foliagePlacer.getRandomHeight(random, i, config);
      int k = i - j;
      int l = config.foliagePlacer.getRandomRadius(random, k);
      BlockPos lv = (BlockPos)config.rootPlacer.map((rootPlacer) -> {
         return rootPlacer.trunkOffset(pos, random);
      }).orElse(pos);
      int m = Math.min(pos.getY(), lv.getY());
      int n = Math.max(pos.getY(), lv.getY()) + i + 1;
      if (m >= world.getBottomY() + 1 && n <= world.getTopY()) {
         OptionalInt optionalInt = config.minimumSize.getMinClippedHeight();
         int o = this.getTopPosition(world, i, lv, config);
         if (o < i && (optionalInt.isEmpty() || o < optionalInt.getAsInt())) {
            return false;
         } else if (config.rootPlacer.isPresent() && !((RootPlacer)config.rootPlacer.get()).generate(world, rootPlacerReplacer, random, pos, lv, config)) {
            return false;
         } else {
            List list = config.trunkPlacer.generate(world, trunkPlacerReplacer, random, o, lv, config);
            list.forEach((node) -> {
               config.foliagePlacer.generate(world, blockPlacer, random, config, o, node, j, l);
            });
            return true;
         }
      } else {
         return false;
      }
   }

   private int getTopPosition(TestableWorld world, int height, BlockPos pos, TreeFeatureConfig config) {
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int j = 0; j <= height + 1; ++j) {
         int k = config.minimumSize.getRadius(height, j);

         for(int l = -k; l <= k; ++l) {
            for(int m = -k; m <= k; ++m) {
               lv.set((Vec3i)pos, l, j, m);
               if (!config.trunkPlacer.canReplaceOrIsLog(world, lv) || !config.ignoreVines && isVine(world, lv)) {
                  return j - 2;
               }
            }
         }
      }

      return height;
   }

   protected void setBlockState(ModifiableWorld world, BlockPos pos, BlockState state) {
      setBlockStateWithoutUpdatingNeighbors(world, pos, state);
   }

   public final boolean generate(FeatureContext context) {
      final StructureWorldAccess lv = context.getWorld();
      Random lv2 = context.getRandom();
      BlockPos lv3 = context.getOrigin();
      TreeFeatureConfig lv4 = (TreeFeatureConfig)context.getConfig();
      Set set = Sets.newHashSet();
      Set set2 = Sets.newHashSet();
      final Set set3 = Sets.newHashSet();
      Set set4 = Sets.newHashSet();
      BiConsumer biConsumer = (pos, state) -> {
         set.add(pos.toImmutable());
         lv.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
      };
      BiConsumer biConsumer2 = (pos, state) -> {
         set2.add(pos.toImmutable());
         lv.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
      };
      FoliagePlacer.BlockPlacer lv5 = new FoliagePlacer.BlockPlacer() {
         public void placeBlock(BlockPos pos, BlockState state) {
            set3.add(pos.toImmutable());
            lv.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
         }

         public boolean hasPlacedBlock(BlockPos pos) {
            return set3.contains(pos);
         }
      };
      BiConsumer biConsumer3 = (pos, state) -> {
         set4.add(pos.toImmutable());
         lv.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
      };
      boolean bl = this.generate(lv, lv2, lv3, biConsumer, biConsumer2, lv5, lv4);
      if (bl && (!set2.isEmpty() || !set3.isEmpty())) {
         if (!lv4.decorators.isEmpty()) {
            TreeDecorator.Generator lv6 = new TreeDecorator.Generator(lv, biConsumer3, lv2, set2, set3, set);
            lv4.decorators.forEach((decorator) -> {
               decorator.generate(lv6);
            });
         }

         return (Boolean)BlockBox.encompassPositions(Iterables.concat(set, set2, set3, set4)).map((box) -> {
            VoxelSet lvx = placeLogsAndLeaves(lv, box, set2, set4, set);
            StructureTemplate.updateCorner(lv, 3, lvx, box.getMinX(), box.getMinY(), box.getMinZ());
            return true;
         }).orElse(false);
      } else {
         return false;
      }
   }

   private static VoxelSet placeLogsAndLeaves(WorldAccess world, BlockBox box, Set trunkPositions, Set decorationPositions, Set rootPositions) {
      VoxelSet lv = new BitSetVoxelSet(box.getBlockCountX(), box.getBlockCountY(), box.getBlockCountZ());
      int i = true;
      List list = Lists.newArrayList();

      for(int j = 0; j < 7; ++j) {
         list.add(Sets.newHashSet());
      }

      Iterator var22 = Lists.newArrayList(Sets.union(decorationPositions, rootPositions)).iterator();

      while(var22.hasNext()) {
         BlockPos lv2 = (BlockPos)var22.next();
         if (box.contains(lv2)) {
            lv.set(lv2.getX() - box.getMinX(), lv2.getY() - box.getMinY(), lv2.getZ() - box.getMinZ());
         }
      }

      BlockPos.Mutable lv3 = new BlockPos.Mutable();
      int k = 0;
      ((Set)list.get(0)).addAll(trunkPositions);

      while(true) {
         while(k >= 7 || !((Set)list.get(k)).isEmpty()) {
            if (k >= 7) {
               return lv;
            }

            Iterator iterator = ((Set)list.get(k)).iterator();
            BlockPos lv4 = (BlockPos)iterator.next();
            iterator.remove();
            if (box.contains(lv4)) {
               if (k != 0) {
                  BlockState lv5 = world.getBlockState(lv4);
                  setBlockStateWithoutUpdatingNeighbors(world, lv4, (BlockState)lv5.with(Properties.DISTANCE_1_7, k));
               }

               lv.set(lv4.getX() - box.getMinX(), lv4.getY() - box.getMinY(), lv4.getZ() - box.getMinZ());
               Direction[] var25 = Direction.values();
               int var13 = var25.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  Direction lv6 = var25[var14];
                  lv3.set(lv4, (Direction)lv6);
                  if (box.contains(lv3)) {
                     int l = lv3.getX() - box.getMinX();
                     int m = lv3.getY() - box.getMinY();
                     int n = lv3.getZ() - box.getMinZ();
                     if (!lv.contains(l, m, n)) {
                        BlockState lv7 = world.getBlockState(lv3);
                        OptionalInt optionalInt = LeavesBlock.getOptionalDistanceFromLog(lv7);
                        if (!optionalInt.isEmpty()) {
                           int o = Math.min(optionalInt.getAsInt(), k + 1);
                           if (o < 7) {
                              ((Set)list.get(o)).add(lv3.toImmutable());
                              k = Math.min(k, o);
                           }
                        }
                     }
                  }
               }
            }
         }

         ++k;
      }
   }
}
