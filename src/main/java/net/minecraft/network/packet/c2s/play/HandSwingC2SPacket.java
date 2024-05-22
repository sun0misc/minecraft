/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.Hand;

public class HandSwingC2SPacket
implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, HandSwingC2SPacket> CODEC = Packet.createCodec(HandSwingC2SPacket::write, HandSwingC2SPacket::new);
    private final Hand hand;

    public HandSwingC2SPacket(Hand hand) {
        this.hand = hand;
    }

    private HandSwingC2SPacket(PacketByteBuf buf) {
        this.hand = buf.readEnumConstant(Hand.class);
    }

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.hand);
    }

    @Override
    public PacketType<HandSwingC2SPacket> getPacketId() {
        return PlayPackets.SWING;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onHandSwing(this);
    }

    public Hand getHand() {
        return this.hand;
    }
}

