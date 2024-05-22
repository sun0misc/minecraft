/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record PlayerListHeaderS2CPacket(Text header, Text footer) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, PlayerListHeaderS2CPacket> CODEC = PacketCodec.tuple(TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, PlayerListHeaderS2CPacket::header, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, PlayerListHeaderS2CPacket::footer, PlayerListHeaderS2CPacket::new);

    @Override
    public PacketType<PlayerListHeaderS2CPacket> getPacketId() {
        return PlayPackets.TAB_LIST;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onPlayerListHeader(this);
    }
}

