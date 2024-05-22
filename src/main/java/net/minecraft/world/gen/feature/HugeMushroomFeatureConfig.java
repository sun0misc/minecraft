/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class HugeMushroomFeatureConfig
implements FeatureConfig {
    public static final Codec<HugeMushroomFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("cap_provider")).forGetter(arg -> arg.capProvider), ((MapCodec)BlockStateProvider.TYPE_CODEC.fieldOf("stem_provider")).forGetter(arg -> arg.stemProvider), ((MapCodec)Codec.INT.fieldOf("foliage_radius")).orElse(2).forGetter(arg -> arg.foliageRadius)).apply((Applicative<HugeMushroomFeatureConfig, ?>)instance, HugeMushroomFeatureConfig::new));
    public final BlockStateProvider capProvider;
    public final BlockStateProvider stemProvider;
    public final int foliageRadius;

    public HugeMushroomFeatureConfig(BlockStateProvider capProvider, BlockStateProvider stemProvider, int foliageRadius) {
        this.capProvider = capProvider;
        this.stemProvider = stemProvider;
        this.foliageRadius = foliageRadius;
    }
}

