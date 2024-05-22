/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.random;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface RandomSplitter {
    default public Random split(BlockPos pos) {
        return this.split(pos.getX(), pos.getY(), pos.getZ());
    }

    default public Random split(Identifier seed) {
        return this.split(seed.toString());
    }

    public Random split(String var1);

    public Random split(long var1);

    public Random split(int var1, int var2, int var3);

    @VisibleForTesting
    public void addDebugInfo(StringBuilder var1);
}

