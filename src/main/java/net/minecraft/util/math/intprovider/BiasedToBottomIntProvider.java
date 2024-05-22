/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;

public class BiasedToBottomIntProvider
extends IntProvider {
    public static final MapCodec<BiasedToBottomIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(provider -> provider.min), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(provider -> provider.max)).apply((Applicative<BiasedToBottomIntProvider, ?>)instance, BiasedToBottomIntProvider::new)).validate(provider -> {
        if (provider.max < provider.min) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + arg.min + ", max_inclusive: " + arg.max);
        }
        return DataResult.success(provider);
    });
    private final int min;
    private final int max;

    private BiasedToBottomIntProvider(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static BiasedToBottomIntProvider create(int min, int max) {
        return new BiasedToBottomIntProvider(min, max);
    }

    @Override
    public int get(Random random) {
        return this.min + random.nextInt(random.nextInt(this.max - this.min + 1) + 1);
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.BIASED_TO_BOTTOM;
    }

    public String toString() {
        return "[" + this.min + "-" + this.max + "]";
    }
}

