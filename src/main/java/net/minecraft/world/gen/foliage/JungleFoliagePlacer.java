package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class JungleFoliagePlacer extends FoliagePlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((placer) -> {
         return placer.height;
      })).apply(instance, JungleFoliagePlacer::new);
   });
   protected final int height;

   public JungleFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
      super(radius, offset);
      this.height = height;
   }

   protected FoliagePlacerType getType() {
      return FoliagePlacerType.JUNGLE_FOLIAGE_PLACER;
   }

   protected void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
      int m = treeNode.isGiantTrunk() ? foliageHeight : 1 + random.nextInt(2);

      for(int n = offset; n >= offset - m; --n) {
         int o = radius + treeNode.getFoliageRadius() + 1 - n;
         this.generateSquare(world, placer, random, config, treeNode.getCenter(), o, n, treeNode.isGiantTrunk());
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height;
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      if (dx + dz >= 7) {
         return true;
      } else {
         return dx * dx + dz * dz > radius * radius;
      }
   }
}
