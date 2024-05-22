/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.structure.rule.BlockStateMatchRuleTest;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class EmeraldOreFeatureConfig
implements FeatureConfig {
    public static final Codec<EmeraldOreFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.list(OreFeatureConfig.Target.CODEC).fieldOf("targets")).forGetter(config -> config.targets)).apply((Applicative<EmeraldOreFeatureConfig, ?>)instance, EmeraldOreFeatureConfig::new));
    public final List<OreFeatureConfig.Target> targets;

    public EmeraldOreFeatureConfig(BlockState target, BlockState state) {
        this(ImmutableList.of(OreFeatureConfig.createTarget(new BlockStateMatchRuleTest(target), state)));
    }

    public EmeraldOreFeatureConfig(List<OreFeatureConfig.Target> targets) {
        this.targets = targets;
    }
}

