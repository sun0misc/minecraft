/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.PositionSourceType;

public interface PositionSource {
    public static final Codec<PositionSource> CODEC = Registries.POSITION_SOURCE_TYPE.getCodec().dispatch(PositionSource::getType, PositionSourceType::getCodec);
    public static final PacketCodec<RegistryByteBuf, PositionSource> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.POSITION_SOURCE_TYPE).dispatch(PositionSource::getType, PositionSourceType::getPacketCodec);

    public Optional<Vec3d> getPos(World var1);

    public PositionSourceType<? extends PositionSource> getType();
}

