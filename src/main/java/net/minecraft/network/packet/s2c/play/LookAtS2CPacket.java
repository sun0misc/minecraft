/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LookAtS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, LookAtS2CPacket> CODEC = Packet.createCodec(LookAtS2CPacket::write, LookAtS2CPacket::new);
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int entityId;
    private final EntityAnchorArgumentType.EntityAnchor selfAnchor;
    private final EntityAnchorArgumentType.EntityAnchor targetAnchor;
    private final boolean lookAtEntity;

    public LookAtS2CPacket(EntityAnchorArgumentType.EntityAnchor selfAnchor, double targetX, double targetY, double targetZ) {
        this.selfAnchor = selfAnchor;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.entityId = 0;
        this.lookAtEntity = false;
        this.targetAnchor = null;
    }

    public LookAtS2CPacket(EntityAnchorArgumentType.EntityAnchor selfAnchor, Entity entity, EntityAnchorArgumentType.EntityAnchor targetAnchor) {
        this.selfAnchor = selfAnchor;
        this.entityId = entity.getId();
        this.targetAnchor = targetAnchor;
        Vec3d lv = targetAnchor.positionAt(entity);
        this.targetX = lv.x;
        this.targetY = lv.y;
        this.targetZ = lv.z;
        this.lookAtEntity = true;
    }

    private LookAtS2CPacket(PacketByteBuf buf) {
        this.selfAnchor = buf.readEnumConstant(EntityAnchorArgumentType.EntityAnchor.class);
        this.targetX = buf.readDouble();
        this.targetY = buf.readDouble();
        this.targetZ = buf.readDouble();
        this.lookAtEntity = buf.readBoolean();
        if (this.lookAtEntity) {
            this.entityId = buf.readVarInt();
            this.targetAnchor = buf.readEnumConstant(EntityAnchorArgumentType.EntityAnchor.class);
        } else {
            this.entityId = 0;
            this.targetAnchor = null;
        }
    }

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.selfAnchor);
        buf.writeDouble(this.targetX);
        buf.writeDouble(this.targetY);
        buf.writeDouble(this.targetZ);
        buf.writeBoolean(this.lookAtEntity);
        if (this.lookAtEntity) {
            buf.writeVarInt(this.entityId);
            buf.writeEnumConstant(this.targetAnchor);
        }
    }

    @Override
    public PacketType<LookAtS2CPacket> getPacketId() {
        return PlayPackets.PLAYER_LOOK_AT;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onLookAt(this);
    }

    public EntityAnchorArgumentType.EntityAnchor getSelfAnchor() {
        return this.selfAnchor;
    }

    @Nullable
    public Vec3d getTargetPosition(World world) {
        if (this.lookAtEntity) {
            Entity lv = world.getEntityById(this.entityId);
            if (lv == null) {
                return new Vec3d(this.targetX, this.targetY, this.targetZ);
            }
            return this.targetAnchor.positionAt(lv);
        }
        return new Vec3d(this.targetX, this.targetY, this.targetZ);
    }
}

