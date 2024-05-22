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

public class WorldTimeUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, WorldTimeUpdateS2CPacket> CODEC = Packet.createCodec(WorldTimeUpdateS2CPacket::write, WorldTimeUpdateS2CPacket::new);
    private final long time;
    private final long timeOfDay;

    public WorldTimeUpdateS2CPacket(long time, long timeOfDay, boolean doDaylightCycle) {
        this.time = time;
        long n = timeOfDay;
        if (!doDaylightCycle && (n = -n) == 0L) {
            n = -1L;
        }
        this.timeOfDay = n;
    }

    private WorldTimeUpdateS2CPacket(PacketByteBuf buf) {
        this.time = buf.readLong();
        this.timeOfDay = buf.readLong();
    }

    private void write(PacketByteBuf buf) {
        buf.writeLong(this.time);
        buf.writeLong(this.timeOfDay);
    }

    @Override
    public PacketType<WorldTimeUpdateS2CPacket> getPacketId() {
        return PlayPackets.SET_TIME;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onWorldTimeUpdate(this);
    }

    public long getTime() {
        return this.time;
    }

    public long getTimeOfDay() {
        return this.timeOfDay;
    }
}

