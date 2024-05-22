/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.network.packet.s2c.login.UnknownLoginQueryRequestPayload;
import net.minecraft.util.Identifier;

public record LoginQueryRequestS2CPacket(int queryId, LoginQueryRequestPayload payload) implements Packet<ClientLoginPacketListener>
{
    public static final PacketCodec<PacketByteBuf, LoginQueryRequestS2CPacket> CODEC = Packet.createCodec(LoginQueryRequestS2CPacket::write, LoginQueryRequestS2CPacket::new);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private LoginQueryRequestS2CPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), LoginQueryRequestS2CPacket.readPayload(buf.readIdentifier(), buf));
    }

    private static LoginQueryRequestPayload readPayload(Identifier id, PacketByteBuf buf) {
        return LoginQueryRequestS2CPacket.readUnknownPayload(id, buf);
    }

    private static UnknownLoginQueryRequestPayload readUnknownPayload(Identifier id, PacketByteBuf buf) {
        int i = buf.readableBytes();
        if (i < 0 || i > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        buf.skipBytes(i);
        return new UnknownLoginQueryRequestPayload(id);
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.queryId);
        buf.writeIdentifier(this.payload.id());
        this.payload.write(buf);
    }

    @Override
    public PacketType<LoginQueryRequestS2CPacket> getPacketId() {
        return LoginPackets.CUSTOM_QUERY;
    }

    @Override
    public void apply(ClientLoginPacketListener arg) {
        arg.onQueryRequest(this);
    }
}

