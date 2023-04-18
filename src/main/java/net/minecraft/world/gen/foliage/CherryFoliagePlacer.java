package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class CherryFoliagePlacer extends FoliagePlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and(instance.group(IntProvider.createValidatingCodec(4, 16).fieldOf("height").forGetter((foliagePlacer) -> {
         return foliagePlacer.height;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("wide_bottom_layer_hole_chance").forGetter((foliagePlacer) -> {
         return foliagePlacer.wideBottomLayerHoleChance;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("corner_hole_chance").forGetter((foliagePlacer) -> {
         return foliagePlacer.wideBottomLayerHoleChance;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter((foliagePlacer) -> {
         return foliagePlacer.hangingLeavesChance;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_extension_chance").forGetter((foliagePlacer) -> {
         return foliagePlacer.hangingLeavesExtensionChance;
      }))).apply(instance, CherryFoliagePlacer::new);
   });
   private final IntProvider height;
   private final float wideBottomLayerHoleChance;
   private final float cornerHoleChance;
   private final float hangingLeavesChance;
   private final float hangingLeavesExtensionChance;

   public CherryFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float wideBottomLayerHoleChance, float cornerHoleChance, float hangingLeavesChance, float hangingLeavesExtensionChance) {
      super(radius, offset);
      this.height = height;
      this.wideBottomLayerHoleChance = wideBottomLayerHoleChance;
      this.cornerHoleChance = cornerHoleChance;
      this.hangingLeavesChance = hangingLeavesChance;
      this.hangingLeavesExtensionChance = hangingLeavesExtensionChance;
   }

   protected FoliagePlacerType getType() {
      return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
   }

   protected void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
      boolean bl = treeNode.isGiantTrunk();
      BlockPos lv = treeNode.getCenter().up(offset);
      int m = radius + treeNode.getFoliageRadius() - 1;
      this.generateSquare(world, placer, random, config, lv, m - 2, foliageHeight - 3, bl);
      this.generateSquare(world, placer, random, config, lv, m - 1, foliageHeight - 4, bl);

      for(int n = foliageHeight - 5; n >= 0; --n) {
         this.generateSquare(world, placer, random, config, lv, m, n, bl);
      }

      this.generateSquareWithHangingLeaves(world, placer, random, config, lv, m, -1, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
      this.generateSquareWithHangingLeaves(world, placer, random, config, lv, m - 1, -2, bl, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height.get(random);
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      if (y == -1 && (dx == radius || dz == radius) && random.nextFloat() < this.wideBottomLayerHoleChance) {
         return true;
      } else {
         boolean bl2 = dx == radius && dz == radius;
         boolean bl3 = radius > 2;
         if (bl3) {
            return bl2 || dx + dz > radius * 2 - 2 && random.nextFloat() < this.cornerHoleChance;
         } else {
            return bl2 && random.nextFloat() < this.cornerHoleChance;
         }
      }
   }
}
