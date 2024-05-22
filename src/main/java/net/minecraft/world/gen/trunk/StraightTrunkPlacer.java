/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.trunk;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class StraightTrunkPlacer
extends TrunkPlacer {
    public static final MapCodec<StraightTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> StraightTrunkPlacer.fillTrunkPlacerFields(instance).apply(instance, StraightTrunkPlacer::new));

    public StraightTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        StraightTrunkPlacer.setToDirt(world, replacer, random, startPos.down(), config);
        for (int j = 0; j < height; ++j) {
            this.getAndSetState(world, replacer, random, startPos.up(j), config);
        }
        return ImmutableList.of(new FoliagePlacer.TreeNode(startPos.up(height), 0, false));
    }
}

