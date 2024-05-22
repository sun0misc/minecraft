/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.AbstractNoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;

public class NoiseBlockStateProvider
extends AbstractNoiseBlockStateProvider {
    public static final MapCodec<NoiseBlockStateProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> NoiseBlockStateProvider.fillNoiseCodecFields(instance).apply((Applicative)instance, NoiseBlockStateProvider::new));
    protected final List<BlockState> states;

    protected static <P extends NoiseBlockStateProvider> Products.P4<RecordCodecBuilder.Mu<P>, Long, DoublePerlinNoiseSampler.NoiseParameters, Float, List<BlockState>> fillNoiseCodecFields(RecordCodecBuilder.Instance<P> instance) {
        return NoiseBlockStateProvider.fillCodecFields(instance).and(((MapCodec)Codec.list(BlockState.CODEC).fieldOf("states")).forGetter(arg -> arg.states));
    }

    public NoiseBlockStateProvider(long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, List<BlockState> states) {
        super(seed, noiseParameters, scale);
        this.states = states;
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.NOISE_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        return this.getStateFromList(this.states, pos, this.scale);
    }

    protected BlockState getStateFromList(List<BlockState> states, BlockPos pos, double scale) {
        double e = this.getNoiseValue(pos, scale);
        return this.getStateAtValue(states, e);
    }

    protected BlockState getStateAtValue(List<BlockState> states, double value) {
        double e = MathHelper.clamp((1.0 + value) / 2.0, 0.0, 0.9999);
        return states.get((int)(e * (double)states.size()));
    }
}

