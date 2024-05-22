/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;

public abstract class IntProvider {
    private static final Codec<Either<Integer, IntProvider>> INT_CODEC = Codec.either(Codec.INT, Registries.INT_PROVIDER_TYPE.getCodec().dispatch(IntProvider::getType, IntProviderType::codec));
    public static final Codec<IntProvider> VALUE_CODEC = INT_CODEC.xmap(either -> either.map(ConstantIntProvider::create, provider -> provider), provider -> provider.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantIntProvider)provider).getValue()) : Either.right(provider));
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = IntProvider.createValidatingCodec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = IntProvider.createValidatingCodec(1, Integer.MAX_VALUE);

    public static Codec<IntProvider> createValidatingCodec(int min, int max) {
        return IntProvider.createValidatingCodec(min, max, VALUE_CODEC);
    }

    public static <T extends IntProvider> Codec<T> createValidatingCodec(int min, int max, Codec<T> providerCodec) {
        return providerCodec.validate(provider -> IntProvider.method_58612(min, max, provider));
    }

    private static <T extends IntProvider> DataResult<T> method_58612(int i, int j, T arg) {
        if (arg.getMin() < i) {
            return DataResult.error(() -> "Value provider too low: " + i + " [" + arg.getMin() + "-" + arg.getMax() + "]");
        }
        if (arg.getMax() > j) {
            return DataResult.error(() -> "Value provider too high: " + j + " [" + arg.getMin() + "-" + arg.getMax() + "]");
        }
        return DataResult.success(arg);
    }

    public abstract int get(Random var1);

    public abstract int getMin();

    public abstract int getMax();

    public abstract IntProviderType<?> getType();
}

