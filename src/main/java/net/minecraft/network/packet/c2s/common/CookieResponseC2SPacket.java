/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.c2s.common;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCookieResponsePacketListener;
import net.minecraft.network.packet.CookiePackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record CookieResponseC2SPacket(Identifier key, @Nullable byte[] payload) implements Packet<ServerCookieResponsePacketListener>
{
    public static final PacketCodec<PacketByteBuf, CookieResponseC2SPacket> CODEC = Packet.createCodec(CookieResponseC2SPacket::write, CookieResponseC2SPacket::new);

    private CookieResponseC2SPacket(PacketByteBuf buf) {
        this(buf.readIdentifier(), buf.readNullable(StoreCookieS2CPacket.COOKIE_PACKET_CODEC));
    }

    private void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.key);
        buf.writeNullable(this.payload, StoreCookieS2CPacket.COOKIE_PACKET_CODEC);
    }

    @Override
    public PacketType<CookieResponseC2SPacket> getPacketId() {
        return CookiePackets.COOKIE_RESPONSE;
    }

    @Override
    public void apply(ServerCookieResponsePacketListener arg) {
        arg.onCookieResponse(this);
    }

    @Nullable
    public byte[] payload() {
        return this.payload;
    }
}

