/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class RarityFilterPlacementModifier
extends AbstractConditionalPlacementModifier {
    public static final MapCodec<RarityFilterPlacementModifier> MODIFIER_CODEC = ((MapCodec)Codecs.POSITIVE_INT.fieldOf("chance")).xmap(RarityFilterPlacementModifier::new, arg -> arg.chance);
    private final int chance;

    private RarityFilterPlacementModifier(int chance) {
        this.chance = chance;
    }

    public static RarityFilterPlacementModifier of(int chance) {
        return new RarityFilterPlacementModifier(chance);
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        return random.nextFloat() < 1.0f / (float)this.chance;
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.RARITY_FILTER;
    }
}

