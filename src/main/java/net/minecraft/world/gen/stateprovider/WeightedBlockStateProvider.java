/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;

public class WeightedBlockStateProvider
extends BlockStateProvider {
    public static final MapCodec<WeightedBlockStateProvider> CODEC = DataPool.createCodec(BlockState.CODEC).comapFlatMap(WeightedBlockStateProvider::wrap, arg -> arg.states).fieldOf("entries");
    private final DataPool<BlockState> states;

    private static DataResult<WeightedBlockStateProvider> wrap(DataPool<BlockState> states) {
        if (states.isEmpty()) {
            return DataResult.error(() -> "WeightedStateProvider with no states");
        }
        return DataResult.success(new WeightedBlockStateProvider(states));
    }

    public WeightedBlockStateProvider(DataPool<BlockState> states) {
        this.states = states;
    }

    public WeightedBlockStateProvider(DataPool.Builder<BlockState> states) {
        this(states.build());
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        return this.states.getDataOrEmpty(random).orElseThrow(IllegalStateException::new);
    }
}

