/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class UpdateSelectedSlotS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, UpdateSelectedSlotS2CPacket> CODEC = Packet.createCodec(UpdateSelectedSlotS2CPacket::write, UpdateSelectedSlotS2CPacket::new);
    private final int slot;

    public UpdateSelectedSlotS2CPacket(int slot) {
        this.slot = slot;
    }

    private UpdateSelectedSlotS2CPacket(PacketByteBuf buf) {
        this.slot = buf.readByte();
    }

    private void write(PacketByteBuf buf) {
        buf.writeByte(this.slot);
    }

    @Override
    public PacketType<UpdateSelectedSlotS2CPacket> getPacketId() {
        return PlayPackets.SET_CARRIED_ITEM_S2C;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onUpdateSelectedSlot(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

