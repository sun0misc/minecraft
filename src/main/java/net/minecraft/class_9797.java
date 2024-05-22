/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

public interface class_9797 {
    default public int method_60772(ServerWorld arg, Entity arg2) {
        return 0;
    }

    @Nullable
    public TeleportTarget method_60770(ServerWorld var1, Entity var2, BlockPos var3);

    default public class_9798 method_60778() {
        return class_9798.NONE;
    }

    public static enum class_9798 {
        CONFUSION,
        NONE;

    }
}

