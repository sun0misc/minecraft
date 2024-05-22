/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.root;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record AboveRootPlacement(BlockStateProvider aboveRootProvider, float aboveRootPlacementChance) {
    public static final Codec<AboveRootPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("above_root_provider")).forGetter(aboveRootPlacement -> aboveRootPlacement.aboveRootProvider), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("above_root_placement_chance")).forGetter(aboveRootPlacement -> Float.valueOf(aboveRootPlacement.aboveRootPlacementChance))).apply((Applicative<AboveRootPlacement, ?>)instance, AboveRootPlacement::new));
}

