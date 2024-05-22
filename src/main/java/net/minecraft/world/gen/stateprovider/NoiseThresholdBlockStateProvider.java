/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.AbstractNoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;

public class NoiseThresholdBlockStateProvider
extends AbstractNoiseBlockStateProvider {
    public static final MapCodec<NoiseThresholdBlockStateProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> NoiseThresholdBlockStateProvider.fillCodecFields(instance).and(instance.group(((MapCodec)Codec.floatRange(-1.0f, 1.0f).fieldOf("threshold")).forGetter(arg -> Float.valueOf(arg.threshold)), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("high_chance")).forGetter(arg -> Float.valueOf(arg.highChance)), ((MapCodec)BlockState.CODEC.fieldOf("default_state")).forGetter(arg -> arg.defaultState), ((MapCodec)Codec.list(BlockState.CODEC).fieldOf("low_states")).forGetter(arg -> arg.lowStates), ((MapCodec)Codec.list(BlockState.CODEC).fieldOf("high_states")).forGetter(arg -> arg.highStates))).apply((Applicative<NoiseThresholdBlockStateProvider, ?>)instance, NoiseThresholdBlockStateProvider::new));
    private final float threshold;
    private final float highChance;
    private final BlockState defaultState;
    private final List<BlockState> lowStates;
    private final List<BlockState> highStates;

    public NoiseThresholdBlockStateProvider(long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, float threshold, float highChance, BlockState defaultState, List<BlockState> lowStates, List<BlockState> highStates) {
        super(seed, noiseParameters, scale);
        this.threshold = threshold;
        this.highChance = highChance;
        this.defaultState = defaultState;
        this.lowStates = lowStates;
        this.highStates = highStates;
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        double d = this.getNoiseValue(pos, this.scale);
        if (d < (double)this.threshold) {
            return Util.getRandom(this.lowStates, random);
        }
        if (random.nextFloat() < this.highChance) {
            return Util.getRandom(this.highStates, random);
        }
        return this.defaultState;
    }
}

