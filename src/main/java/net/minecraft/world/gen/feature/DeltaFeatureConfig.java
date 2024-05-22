/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DeltaFeatureConfig
implements FeatureConfig {
    public static final Codec<DeltaFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("contents")).forGetter(config -> config.contents), ((MapCodec)BlockState.CODEC.fieldOf("rim")).forGetter(config -> config.rim), ((MapCodec)IntProvider.createValidatingCodec(0, 16).fieldOf("size")).forGetter(config -> config.size), ((MapCodec)IntProvider.createValidatingCodec(0, 16).fieldOf("rim_size")).forGetter(config -> config.rimSize)).apply((Applicative<DeltaFeatureConfig, ?>)instance, DeltaFeatureConfig::new));
    private final BlockState contents;
    private final BlockState rim;
    private final IntProvider size;
    private final IntProvider rimSize;

    public DeltaFeatureConfig(BlockState contents, BlockState rim, IntProvider size, IntProvider rimSize) {
        this.contents = contents;
        this.rim = rim;
        this.size = size;
        this.rimSize = rimSize;
    }

    public BlockState getContents() {
        return this.contents;
    }

    public BlockState getRim() {
        return this.rim;
    }

    public IntProvider getSize() {
        return this.size;
    }

    public IntProvider getRimSize() {
        return this.rimSize;
    }
}

