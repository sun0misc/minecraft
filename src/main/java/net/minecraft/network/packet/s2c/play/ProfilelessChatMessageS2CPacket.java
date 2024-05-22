/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record ProfilelessChatMessageS2CPacket(Text message, MessageType.Parameters chatType) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, ProfilelessChatMessageS2CPacket> CODEC = PacketCodec.tuple(TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, ProfilelessChatMessageS2CPacket::message, MessageType.Parameters.CODEC, ProfilelessChatMessageS2CPacket::chatType, ProfilelessChatMessageS2CPacket::new);

    @Override
    public PacketType<ProfilelessChatMessageS2CPacket> getPacketId() {
        return PlayPackets.DISGUISED_CHAT;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onProfilelessChatMessage(this);
    }

    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}

