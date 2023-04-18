package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class MegaPineFoliagePlacer extends FoliagePlacer {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and(IntProvider.createValidatingCodec(0, 24).fieldOf("crown_height").forGetter((placer) -> {
         return placer.crownHeight;
      })).apply(instance, MegaPineFoliagePlacer::new);
   });
   private final IntProvider crownHeight;

   public MegaPineFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider crownHeight) {
      super(radius, offset);
      this.crownHeight = crownHeight;
   }

   protected FoliagePlacerType getType() {
      return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
   }

   protected void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
      BlockPos lv = treeNode.getCenter();
      int m = 0;

      for(int n = lv.getY() - foliageHeight + offset; n <= lv.getY() + offset; ++n) {
         int o = lv.getY() - n;
         int p = radius + treeNode.getFoliageRadius() + MathHelper.floor((float)o / (float)foliageHeight * 3.5F);
         int q;
         if (o > 0 && p == m && (n & 1) == 0) {
            q = p + 1;
         } else {
            q = p;
         }

         this.generateSquare(world, placer, random, config, new BlockPos(lv.getX(), n, lv.getZ()), q, 0, treeNode.isGiantTrunk());
         m = p;
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.crownHeight.get(random);
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      if (dx + dz >= 7) {
         return true;
      } else {
         return dx * dx + dz * dz > radius * radius;
      }
   }
}
