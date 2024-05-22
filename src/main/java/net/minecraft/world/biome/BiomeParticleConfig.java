/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;

public class BiomeParticleConfig {
    public static final Codec<BiomeParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ParticleTypes.TYPE_CODEC.fieldOf("options")).forGetter(config -> config.particle), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(config -> Float.valueOf(config.probability))).apply((Applicative<BiomeParticleConfig, ?>)instance, BiomeParticleConfig::new));
    private final ParticleEffect particle;
    private final float probability;

    public BiomeParticleConfig(ParticleEffect particle, float probability) {
        this.particle = particle;
        this.probability = probability;
    }

    public ParticleEffect getParticle() {
        return this.particle;
    }

    public boolean shouldAddParticle(Random random) {
        return random.nextFloat() <= this.probability;
    }
}

