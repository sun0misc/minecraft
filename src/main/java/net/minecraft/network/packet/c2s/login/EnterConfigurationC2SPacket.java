/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.login;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public class EnterConfigurationC2SPacket
implements Packet<ServerLoginPacketListener> {
    public static final EnterConfigurationC2SPacket INSTANCE = new EnterConfigurationC2SPacket();
    public static final PacketCodec<ByteBuf, EnterConfigurationC2SPacket> CODEC = PacketCodec.unit(INSTANCE);

    private EnterConfigurationC2SPacket() {
    }

    @Override
    public PacketType<EnterConfigurationC2SPacket> getPacketId() {
        return LoginPackets.LOGIN_ACKNOWLEDGED;
    }

    @Override
    public void apply(ServerLoginPacketListener arg) {
        arg.onEnterConfiguration(this);
    }

    @Override
    public boolean transitionsNetworkState() {
        return true;
    }
}

