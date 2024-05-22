/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;

public class ProbabilityConfig
implements FeatureConfig {
    public static final Codec<ProbabilityConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(arg -> Float.valueOf(arg.probability))).apply((Applicative<ProbabilityConfig, ?>)instance, ProbabilityConfig::new));
    public final float probability;

    public ProbabilityConfig(float probability) {
        this.probability = probability;
    }
}

