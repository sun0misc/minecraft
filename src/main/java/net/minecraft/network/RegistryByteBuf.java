/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;

public class RegistryByteBuf
extends PacketByteBuf {
    private final DynamicRegistryManager registryManager;

    public RegistryByteBuf(ByteBuf buf, DynamicRegistryManager registryManager) {
        super(buf);
        this.registryManager = registryManager;
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.registryManager;
    }

    public static Function<ByteBuf, RegistryByteBuf> makeFactory(DynamicRegistryManager registryManager) {
        return buf -> new RegistryByteBuf((ByteBuf)buf, registryManager);
    }
}

