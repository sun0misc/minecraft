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
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class HeightmapPlacementModifier
extends PlacementModifier {
    public static final MapCodec<HeightmapPlacementModifier> MODIFIER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Heightmap.Type.CODEC.fieldOf("heightmap")).forGetter(arg -> arg.heightmap)).apply((Applicative<HeightmapPlacementModifier, ?>)instance, HeightmapPlacementModifier::new));
    private final Heightmap.Type heightmap;

    private HeightmapPlacementModifier(Heightmap.Type heightmap) {
        this.heightmap = heightmap;
    }

    public static HeightmapPlacementModifier of(Heightmap.Type heightmap) {
        return new HeightmapPlacementModifier(heightmap);
    }

    @Override
    public Stream<BlockPos> getPositions(FeaturePlacementContext context, Random random, BlockPos pos) {
        int j;
        int i = pos.getX();
        int k = context.getTopY(this.heightmap, i, j = pos.getZ());
        if (k > context.getBottomY()) {
            return Stream.of(new BlockPos(i, k, j));
        }
        return Stream.of(new BlockPos[0]);
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.HEIGHTMAP;
    }
}

