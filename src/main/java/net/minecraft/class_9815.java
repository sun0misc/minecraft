/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.class_9782;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record class_9815(class_9782 links) implements Packet<ClientCommonPacketListener>
{
    public static final PacketCodec<ByteBuf, class_9815> field_52190 = PacketCodec.tuple(class_9782.field_51978, class_9815::links, class_9815::new);

    @Override
    public PacketType<class_9815> getPacketId() {
        return CommonPackets.SERVER_LINKS;
    }

    @Override
    public void apply(ClientCommonPacketListener arg) {
        arg.method_60884(this);
    }
}

