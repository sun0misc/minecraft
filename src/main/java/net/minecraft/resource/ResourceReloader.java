/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public interface ResourceReloader {
    public CompletableFuture<Void> reload(Synchronizer var1, ResourceManager var2, Profiler var3, Profiler var4, Executor var5, Executor var6);

    default public String getName() {
        return this.getClass().getSimpleName();
    }

    public static interface Synchronizer {
        public <T> CompletableFuture<T> whenPrepared(T var1);
    }
}

