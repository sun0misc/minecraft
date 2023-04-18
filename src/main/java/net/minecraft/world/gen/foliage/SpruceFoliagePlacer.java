package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class SpruceFoliagePlacer extends FoliagePlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and(IntProvider.createValidatingCodec(0, 24).fieldOf("trunk_height").forGetter((placer) -> {
         return placer.trunkHeight;
      })).apply(instance, SpruceFoliagePlacer::new);
   });
   private final IntProvider trunkHeight;

   public SpruceFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider trunkHeight) {
      super(radius, offset);
      this.trunkHeight = trunkHeight;
   }

   protected FoliagePlacerType getType() {
      return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
   }

   protected void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
      BlockPos lv = treeNode.getCenter();
      int m = random.nextInt(2);
      int n = 1;
      int o = 0;

      for(int p = offset; p >= -foliageHeight; --p) {
         this.generateSquare(world, placer, random, config, lv, m, p, treeNode.isGiantTrunk());
         if (m >= n) {
            m = o;
            o = 1;
            n = Math.min(n + 1, radius + treeNode.getFoliageRadius());
         } else {
            ++m;
         }
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return Math.max(4, trunkHeight - this.trunkHeight.get(random));
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      return dx == radius && dz == radius && radius > 0;
   }
}
