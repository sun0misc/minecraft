/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.Vec3d;

public class EntityPositionS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, EntityPositionS2CPacket> CODEC = Packet.createCodec(EntityPositionS2CPacket::write, EntityPositionS2CPacket::new);
    private final int id;
    private final double x;
    private final double y;
    private final double z;
    private final byte yaw;
    private final byte pitch;
    private final boolean onGround;

    public EntityPositionS2CPacket(Entity entity) {
        this.id = entity.getId();
        Vec3d lv = entity.getSyncedPos();
        this.x = lv.x;
        this.y = lv.y;
        this.z = lv.z;
        this.yaw = (byte)(entity.getYaw() * 256.0f / 360.0f);
        this.pitch = (byte)(entity.getPitch() * 256.0f / 360.0f);
        this.onGround = entity.isOnGround();
    }

    private EntityPositionS2CPacket(PacketByteBuf buf) {
        this.id = buf.readVarInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readByte();
        this.pitch = buf.readByte();
        this.onGround = buf.readBoolean();
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeByte(this.yaw);
        buf.writeByte(this.pitch);
        buf.writeBoolean(this.onGround);
    }

    @Override
    public PacketType<EntityPositionS2CPacket> getPacketId() {
        return PlayPackets.TELEPORT_ENTITY;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onEntityPosition(this);
    }

    public int getId() {
        return this.id;
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

    public byte getYaw() {
        return this.yaw;
    }

    public byte getPitch() {
        return this.pitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }
}

