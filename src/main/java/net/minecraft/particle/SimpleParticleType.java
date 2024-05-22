/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class SimpleParticleType
extends ParticleType<SimpleParticleType>
implements ParticleEffect {
    private final MapCodec<SimpleParticleType> codec = MapCodec.unit(this::getType);
    private final PacketCodec<RegistryByteBuf, SimpleParticleType> packetCodec = PacketCodec.unit(this);

    protected SimpleParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    public SimpleParticleType getType() {
        return this;
    }

    @Override
    public MapCodec<SimpleParticleType> getCodec() {
        return this.codec;
    }

    @Override
    public PacketCodec<RegistryByteBuf, SimpleParticleType> getPacketCodec() {
        return this.packetCodec;
    }

    public /* synthetic */ ParticleType getType() {
        return this.getType();
    }
}

