/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import java.util.Iterator;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface Degradable<T extends Enum<T>> {
    public static final int DEGRADING_RANGE = 4;

    public Optional<BlockState> getDegradationResult(BlockState var1);

    public float getDegradationChanceMultiplier();

    default public void tickDegradation(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        float f = 0.05688889f;
        if (random.nextFloat() < 0.05688889f) {
            this.tryDegrade(state, world, pos, random).ifPresent(degraded -> world.setBlockState(pos, (BlockState)degraded));
        }
    }

    public T getDegradationLevel();

    default public Optional<BlockState> tryDegrade(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos lv;
        int l;
        int i = ((Enum)this.getDegradationLevel()).ordinal();
        int j = 0;
        int k = 0;
        Iterator<BlockPos> iterator = BlockPos.iterateOutwards(pos, 4, 4, 4).iterator();
        while (iterator.hasNext() && (l = (lv = iterator.next()).getManhattanDistance(pos)) <= 4) {
            Block block;
            if (lv.equals(pos) || !((block = world.getBlockState(lv).getBlock()) instanceof Degradable)) continue;
            Degradable lv2 = (Degradable)((Object)block);
            T enum_ = lv2.getDegradationLevel();
            if (this.getDegradationLevel().getClass() != enum_.getClass()) continue;
            int m = ((Enum)enum_).ordinal();
            if (m < i) {
                return Optional.empty();
            }
            if (m > i) {
                ++k;
                continue;
            }
            ++j;
        }
        float f = (float)(k + 1) / (float)(k + j + 1);
        float g = f * f * this.getDegradationChanceMultiplier();
        if (random.nextFloat() < g) {
            return this.getDegradationResult(state);
        }
        return Optional.empty();
    }
}

