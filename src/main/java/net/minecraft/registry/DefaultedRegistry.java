/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.registry;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DefaultedRegistry<T>
extends Registry<T> {
    @Override
    @NotNull
    public Identifier getId(T var1);

    @Override
    @NotNull
    public T get(@Nullable Identifier var1);

    @Override
    @NotNull
    public T get(int var1);

    public Identifier getDefaultId();
}

