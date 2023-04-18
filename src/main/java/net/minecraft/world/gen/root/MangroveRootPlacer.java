package net.minecraft.world.gen.root;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class MangroveRootPlacer extends RootPlacer {
   public static final int field_38769 = 8;
   public static final int field_38770 = 15;
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return method_43182(instance).and(MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter((rootPlacer) -> {
         return rootPlacer.mangroveRootPlacement;
      })).apply(instance, MangroveRootPlacer::new);
   });
   private final MangroveRootPlacement mangroveRootPlacement;

   public MangroveRootPlacer(IntProvider trunkOffsetY, BlockStateProvider rootProvider, Optional aboveRootPlacement, MangroveRootPlacement mangroveRootPlacement) {
      super(trunkOffsetY, rootProvider, aboveRootPlacement);
      this.mangroveRootPlacement = mangroveRootPlacement;
   }

   public boolean generate(TestableWorld world, BiConsumer replacer, Random random, BlockPos pos, BlockPos trunkPos, TreeFeatureConfig config) {
      List list = Lists.newArrayList();
      BlockPos.Mutable lv = pos.mutableCopy();

      while(lv.getY() < trunkPos.getY()) {
         if (!this.canGrowThrough(world, lv)) {
            return false;
         }

         lv.move(Direction.UP);
      }

      list.add(trunkPos.down());
      Iterator var9 = Direction.Type.HORIZONTAL.iterator();

      while(var9.hasNext()) {
         Direction lv2 = (Direction)var9.next();
         BlockPos lv3 = trunkPos.offset(lv2);
         List list2 = Lists.newArrayList();
         if (!this.canGrow(world, random, lv3, lv2, trunkPos, list2, 0)) {
            return false;
         }

         list.addAll(list2);
         list.add(trunkPos.offset(lv2));
      }

      var9 = list.iterator();

      while(var9.hasNext()) {
         BlockPos lv4 = (BlockPos)var9.next();
         this.placeRoots(world, replacer, random, lv4, config);
      }

      return true;
   }

   private boolean canGrow(TestableWorld world, Random random, BlockPos pos, Direction direction, BlockPos origin, List offshootPositions, int rootLength) {
      int j = this.mangroveRootPlacement.maxRootLength();
      if (rootLength != j && offshootPositions.size() <= j) {
         List list2 = this.getOffshootPositions(pos, direction, random, origin);
         Iterator var10 = list2.iterator();

         while(var10.hasNext()) {
            BlockPos lv = (BlockPos)var10.next();
            if (this.canGrowThrough(world, lv)) {
               offshootPositions.add(lv);
               if (!this.canGrow(world, random, lv, direction, origin, offshootPositions, rootLength + 1)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected List getOffshootPositions(BlockPos pos, Direction direction, Random random, BlockPos origin) {
      BlockPos lv = pos.down();
      BlockPos lv2 = pos.offset(direction);
      int i = pos.getManhattanDistance(origin);
      int j = this.mangroveRootPlacement.maxRootWidth();
      float f = this.mangroveRootPlacement.randomSkewChance();
      if (i > j - 3 && i <= j) {
         return random.nextFloat() < f ? List.of(lv, lv2.down()) : List.of(lv);
      } else if (i > j) {
         return List.of(lv);
      } else if (random.nextFloat() < f) {
         return List.of(lv);
      } else {
         return random.nextBoolean() ? List.of(lv2) : List.of(lv);
      }
   }

   protected boolean canGrowThrough(TestableWorld world, BlockPos pos) {
      return super.canGrowThrough(world, pos) || world.testBlockState(pos, (state) -> {
         return state.isIn(this.mangroveRootPlacement.canGrowThrough());
      });
   }

   protected void placeRoots(TestableWorld world, BiConsumer replacer, Random random, BlockPos pos, TreeFeatureConfig config) {
      if (world.testBlockState(pos, (state) -> {
         return state.isIn(this.mangroveRootPlacement.muddyRootsIn());
      })) {
         BlockState lv = this.mangroveRootPlacement.muddyRootsProvider().get(random, pos);
         replacer.accept(pos, this.applyWaterlogging(world, pos, lv));
      } else {
         super.placeRoots(world, replacer, random, pos, config);
      }

   }

   protected RootPlacerType getType() {
      return RootPlacerType.MANGROVE_ROOT_PLACER;
   }
}
