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

public class PickFromInventoryC2SPacket
implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, PickFromInventoryC2SPacket> CODEC = Packet.createCodec(PickFromInventoryC2SPacket::write, PickFromInventoryC2SPacket::new);
    private final int slot;

    public PickFromInventoryC2SPacket(int slot) {
        this.slot = slot;
    }

    private PickFromInventoryC2SPacket(PacketByteBuf buf) {
        this.slot = buf.readVarInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.slot);
    }

    @Override
    public PacketType<PickFromInventoryC2SPacket> getPacketId() {
        return PlayPackets.PICK_ITEM;
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onPickFromInventory(this);
    }

    public int getSlot() {
        return this.slot;
    }
}

