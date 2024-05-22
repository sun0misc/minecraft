/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.foliage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public class LargeOakFoliagePlacer
extends BlobFoliagePlacer {
    public static final MapCodec<LargeOakFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> LargeOakFoliagePlacer.createCodec(instance).apply(instance, LargeOakFoliagePlacer::new));

    public LargeOakFoliagePlacer(IntProvider arg, IntProvider arg2, int i) {
        super(arg, arg2, i);
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
    }

    @Override
    protected void generate(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
        for (int m = offset; m >= offset - foliageHeight; --m) {
            int n = radius + (m == offset || m == offset - foliageHeight ? 0 : 1);
            this.generateSquare(world, placer, random, config, treeNode.getCenter(), n, m, treeNode.isGiantTrunk());
        }
    }

    @Override
    protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        return MathHelper.square((float)dx + 0.5f) + MathHelper.square((float)dz + 0.5f) > (float)(radius * radius);
    }
}

