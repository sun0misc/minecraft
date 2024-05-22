/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record DamageTiltS2CPacket(int id, float yaw) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, DamageTiltS2CPacket> CODEC = Packet.createCodec(DamageTiltS2CPacket::write, DamageTiltS2CPacket::new);

    public DamageTiltS2CPacket(LivingEntity entity) {
        this(entity.getId(), entity.getDamageTiltYaw());
    }

    private DamageTiltS2CPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readFloat());
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeFloat(this.yaw);
    }

    @Override
    public PacketType<DamageTiltS2CPacket> getPacketId() {
        return PlayPackets.HURT_ANIMATION;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onDamageTilt(this);
    }
}

