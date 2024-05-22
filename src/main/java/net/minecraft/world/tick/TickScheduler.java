/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;

public interface TickScheduler<T> {
    public void scheduleTick(OrderedTick<T> var1);

    public boolean isQueued(BlockPos var1, T var2);

    public int getTickCount();
}

