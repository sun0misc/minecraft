/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.Set;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.PositionFlag;

public class PlayerPositionLookS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, PlayerPositionLookS2CPacket> CODEC = Packet.createCodec(PlayerPositionLookS2CPacket::write, PlayerPositionLookS2CPacket::new);
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final Set<PositionFlag> flags;
    private final int teleportId;

    public PlayerPositionLookS2CPacket(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags, int teleportId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.flags = flags;
        this.teleportId = teleportId;
    }

    private PlayerPositionLookS2CPacket(PacketByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.flags = PositionFlag.getFlags(buf.readUnsignedByte());
        this.teleportId = buf.readVarInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeByte(PositionFlag.getBitfield(this.flags));
        buf.writeVarInt(this.teleportId);
    }

    @Override
    public PacketType<PlayerPositionLookS2CPacket> getPacketId() {
        return PlayPackets.PLAYER_POSITION;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onPlayerPositionLook(this);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getTeleportId() {
        return this.teleportId;
    }

    public Set<PositionFlag> getFlags() {
        return this.flags;
    }
}

