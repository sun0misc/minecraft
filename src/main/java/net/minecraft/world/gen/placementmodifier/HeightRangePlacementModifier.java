/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.TrapezoidHeightProvider;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class HeightRangePlacementModifier
extends PlacementModifier {
    public static final MapCodec<HeightRangePlacementModifier> MODIFIER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)HeightProvider.CODEC.fieldOf("height")).forGetter(arg -> arg.height)).apply((Applicative<HeightRangePlacementModifier, ?>)instance, HeightRangePlacementModifier::new));
    private final HeightProvider height;

    private HeightRangePlacementModifier(HeightProvider height) {
        this.height = height;
    }

    public static HeightRangePlacementModifier of(HeightProvider height) {
        return new HeightRangePlacementModifier(height);
    }

    public static HeightRangePlacementModifier uniform(YOffset minOffset, YOffset maxOffset) {
        return HeightRangePlacementModifier.of(UniformHeightProvider.create(minOffset, maxOffset));
    }

    public static HeightRangePlacementModifier trapezoid(YOffset minOffset, YOffset maxOffset) {
        return HeightRangePlacementModifier.of(TrapezoidHeightProvider.create(minOffset, maxOffset));
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        return Stream.of(pos.withY(this.height.get(random, context)));
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.HEIGHT_RANGE;
    }
}

