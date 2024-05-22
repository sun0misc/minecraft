/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.floatprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.random.Random;

public class ClampedNormalFloatProvider
extends FloatProvider {
    public static final MapCodec<ClampedNormalFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("mean")).forGetter(provider -> Float.valueOf(provider.mean)), ((MapCodec)Codec.FLOAT.fieldOf("deviation")).forGetter(provider -> Float.valueOf(provider.deviation)), ((MapCodec)Codec.FLOAT.fieldOf("min")).forGetter(provider -> Float.valueOf(provider.min)), ((MapCodec)Codec.FLOAT.fieldOf("max")).forGetter(provider -> Float.valueOf(provider.max))).apply((Applicative<ClampedNormalFloatProvider, ?>)instance, ClampedNormalFloatProvider::new)).validate(provider -> {
        if (provider.max < provider.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + arg.min + ", " + arg.max + "]");
        }
        return DataResult.success(provider);
    });
    private final float mean;
    private final float deviation;
    private final float min;
    private final float max;

    public static ClampedNormalFloatProvider create(float mean, float deviation, float min, float max) {
        return new ClampedNormalFloatProvider(mean, deviation, min, max);
    }

    private ClampedNormalFloatProvider(float mean, float deviation, float min, float max) {
        this.mean = mean;
        this.deviation = deviation;
        this.min = min;
        this.max = max;
    }

    @Override
    public float get(Random random) {
        return ClampedNormalFloatProvider.get(random, this.mean, this.deviation, this.min, this.max);
    }

    public static float get(Random random, float mean, float deviation, float min, float max) {
        return MathHelper.clamp(MathHelper.nextGaussian(random, mean, deviation), min, max);
    }

    @Override
    public float getMin() {
        return this.min;
    }

    @Override
    public float getMax() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
    }
}

