package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public abstract class FoliagePlacer {
   public static final Codec TYPE_CODEC;
   protected final IntProvider radius;
   protected final IntProvider offset;

   protected static Products.P2 fillFoliagePlacerFields(RecordCodecBuilder.Instance instance) {
      return instance.group(IntProvider.createValidatingCodec(0, 16).fieldOf("radius").forGetter((placer) -> {
         return placer.radius;
      }), IntProvider.createValidatingCodec(0, 16).fieldOf("offset").forGetter((placer) -> {
         return placer.offset;
      }));
   }

   public FoliagePlacer(IntProvider radius, IntProvider offset) {
      this.radius = radius;
      this.offset = offset;
   }

   protected abstract FoliagePlacerType getType();

   public void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius) {
      this.generate(world, placer, random, config, trunkHeight, treeNode, foliageHeight, radius, this.getRandomOffset(random));
   }

   protected abstract void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius, int offset);

   public abstract int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config);

   public int getRandomRadius(Random random, int baseHeight) {
      return this.radius.get(random);
   }

   private int getRandomOffset(Random random) {
      return this.offset.get(random);
   }

   protected abstract boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk);

   protected boolean isPositionInvalid(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      int m;
      int n;
      if (giantTrunk) {
         m = Math.min(Math.abs(dx), Math.abs(dx - 1));
         n = Math.min(Math.abs(dz), Math.abs(dz - 1));
      } else {
         m = Math.abs(dx);
         n = Math.abs(dz);
      }

      return this.isInvalidForLeaves(random, m, y, n, radius, giantTrunk);
   }

   protected void generateSquare(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos centerPos, int radius, int y, boolean giantTrunk) {
      int k = giantTrunk ? 1 : 0;
      BlockPos.Mutable lv = new BlockPos.Mutable();

      for(int l = -radius; l <= radius + k; ++l) {
         for(int m = -radius; m <= radius + k; ++m) {
            if (!this.isPositionInvalid(random, l, y, m, radius, giantTrunk)) {
               lv.set((Vec3i)centerPos, l, y, m);
               placeFoliageBlock(world, placer, random, config, lv);
            }
         }
      }

   }

   protected final void generateSquareWithHangingLeaves(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos centerPos, int radius, int y, boolean giantTrunk, float hangingLeavesChance, float hangingLeavesExtensionChance) {
      this.generateSquare(world, placer, random, config, centerPos, radius, y, giantTrunk);
      int k = giantTrunk ? 1 : 0;
      BlockPos lv = centerPos.down();
      BlockPos.Mutable lv2 = new BlockPos.Mutable();
      Iterator var14 = Direction.Type.HORIZONTAL.iterator();

      while(var14.hasNext()) {
         Direction lv3 = (Direction)var14.next();
         Direction lv4 = lv3.rotateYClockwise();
         int l = lv4.getDirection() == Direction.AxisDirection.POSITIVE ? radius + k : radius;
         lv2.set((Vec3i)centerPos, 0, y - 1, 0).move(lv4, l).move(lv3, -radius);
         int m = -radius;

         while(m < radius + k) {
            boolean bl2 = placer.hasPlacedBlock(lv2.move(Direction.UP));
            lv2.move(Direction.DOWN);
            if (bl2 && placeFoliageBlock(world, placer, random, config, hangingLeavesChance, lv, lv2)) {
               lv2.move(Direction.DOWN);
               placeFoliageBlock(world, placer, random, config, hangingLeavesExtensionChance, lv, lv2);
               lv2.move(Direction.UP);
            }

            ++m;
            lv2.move(lv3);
         }
      }

   }

   private static boolean placeFoliageBlock(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, float chance, BlockPos origin, BlockPos.Mutable pos) {
      if (pos.getManhattanDistance(origin) >= 7) {
         return false;
      } else {
         return random.nextFloat() > chance ? false : placeFoliageBlock(world, placer, random, config, pos);
      }
   }

   protected static boolean placeFoliageBlock(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos pos) {
      if (!TreeFeature.canReplace(world, pos)) {
         return false;
      } else {
         BlockState lv = config.foliageProvider.get(random, pos);
         if (lv.contains(Properties.WATERLOGGED)) {
            lv = (BlockState)lv.with(Properties.WATERLOGGED, world.testFluidState(pos, (fluidState) -> {
               return fluidState.isEqualAndStill(Fluids.WATER);
            }));
         }

         placer.placeBlock(pos, lv);
         return true;
      }
   }

   static {
      TYPE_CODEC = Registries.FOLIAGE_PLACER_TYPE.getCodec().dispatch(FoliagePlacer::getType, FoliagePlacerType::getCodec);
   }

   public interface BlockPlacer {
      void placeBlock(BlockPos pos, BlockState state);

      boolean hasPlacedBlock(BlockPos pos);
   }

   public static final class TreeNode {
      private final BlockPos center;
      private final int foliageRadius;
      private final boolean giantTrunk;

      public TreeNode(BlockPos center, int foliageRadius, boolean giantTrunk) {
         this.center = center;
         this.foliageRadius = foliageRadius;
         this.giantTrunk = giantTrunk;
      }

      public BlockPos getCenter() {
         return this.center;
      }

      public int getFoliageRadius() {
         return this.foliageRadius;
      }

      public boolean isGiantTrunk() {
         return this.giantTrunk;
      }
   }
}
