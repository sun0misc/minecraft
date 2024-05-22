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

public class UniformFloatProvider
extends FloatProvider {
    public static final MapCodec<UniformFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("min_inclusive")).forGetter(provider -> Float.valueOf(provider.min)), ((MapCodec)Codec.FLOAT.fieldOf("max_exclusive")).forGetter(provider -> Float.valueOf(provider.max))).apply((Applicative<UniformFloatProvider, ?>)instance, UniformFloatProvider::new)).validate(provider -> {
        if (provider.max <= provider.min) {
            return DataResult.error(() -> "Max must be larger than min, min_inclusive: " + arg.min + ", max_exclusive: " + arg.max);
        }
        return DataResult.success(provider);
    });
    private final float min;
    private final float max;

    private UniformFloatProvider(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public static UniformFloatProvider create(float min, float max) {
        if (max <= min) {
            throw new IllegalArgumentException("Max must exceed min");
        }
        return new UniformFloatProvider(min, max);
    }

    @Override
    public float get(Random random) {
        return MathHelper.nextBetween(random, this.min, this.max);
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
        return FloatProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.min + "-" + this.max + "]";
    }
}

