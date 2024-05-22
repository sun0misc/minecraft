/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class EnvironmentScanPlacementModifier
extends PlacementModifier {
    private final Direction direction;
    private final BlockPredicate targetPredicate;
    private final BlockPredicate allowedSearchPredicate;
    private final int maxSteps;
    public static final MapCodec<EnvironmentScanPlacementModifier> MODIFIER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Direction.VERTICAL_CODEC.fieldOf("direction_of_search")).forGetter(arg -> arg.direction), ((MapCodec)BlockPredicate.BASE_CODEC.fieldOf("target_condition")).forGetter(arg -> arg.targetPredicate), BlockPredicate.BASE_CODEC.optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue()).forGetter(arg -> arg.allowedSearchPredicate), ((MapCodec)Codec.intRange(1, 32).fieldOf("max_steps")).forGetter(arg -> arg.maxSteps)).apply((Applicative<EnvironmentScanPlacementModifier, ?>)instance, EnvironmentScanPlacementModifier::new));

    private EnvironmentScanPlacementModifier(Direction direction, BlockPredicate targetPredicate, BlockPredicate allowedSearchPredicate, int maxSteps) {
        this.direction = direction;
        this.targetPredicate = targetPredicate;
        this.allowedSearchPredicate = allowedSearchPredicate;
        this.maxSteps = maxSteps;
    }

    public static EnvironmentScanPlacementModifier of(Direction direction, BlockPredicate targetPredicate, BlockPredicate allowedSearchPredicate, int maxSteps) {
        return new EnvironmentScanPlacementModifier(direction, targetPredicate, allowedSearchPredicate, maxSteps);
    }

    public static EnvironmentScanPlacementModifier of(Direction direction, BlockPredicate targetPredicate, int maxSteps) {
        return EnvironmentScanPlacementModifier.of(direction, targetPredicate, BlockPredicate.alwaysTrue(), maxSteps);
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        BlockPos.Mutable lv = pos.mutableCopy();
        StructureWorldAccess lv2 = context.getWorld();
        if (!this.allowedSearchPredicate.test(lv2, lv)) {
            return Stream.of(new BlockPos[0]);
        }
        for (int i = 0; i < this.maxSteps; ++i) {
            if (this.targetPredicate.test(lv2, lv)) {
                return Stream.of(lv);
            }
            lv.move(this.direction);
            if (lv2.isOutOfHeightLimit(lv.getY())) {
                return Stream.of(new BlockPos[0]);
            }
            if (!this.allowedSearchPredicate.test(lv2, lv)) break;
        }
        if (this.targetPredicate.test(lv2, lv)) {
            return Stream.of(lv);
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.ENVIRONMENT_SCAN;
    }
}

