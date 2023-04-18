package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class PineFoliagePlacer extends FoliagePlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and(IntProvider.createValidatingCodec(0, 24).fieldOf("height").forGetter((placer) -> {
         return placer.height;
      })).apply(instance, PineFoliagePlacer::new);
   });
   private final IntProvider height;

   public PineFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height) {
      super(radius, offset);
      this.height = height;
   }

   protected FoliagePlacerType getType() {
      return FoliagePlacerType.PINE_FOLIAGE_PLACER;
   }

   protected void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
      int m = 0;

      for(int n = offset; n >= offset - foliageHeight; --n) {
         this.generateSquare(world, placer, random, config, treeNode.getCenter(), m, n, treeNode.isGiantTrunk());
         if (m >= 1 && n == offset - foliageHeight + 1) {
            --m;
         } else if (m < radius + treeNode.getFoliageRadius()) {
            ++m;
         }
      }

   }

   public int getRandomRadius(Random random, int baseHeight) {
      return super.getRandomRadius(random, baseHeight) + random.nextInt(Math.max(baseHeight + 1, 1));
   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height.get(random);
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      return dx == radius && dz == radius && radius > 0;
   }
}
