/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.util.Objects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.Nullable;

public class ScoreboardDisplayS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, ScoreboardDisplayS2CPacket> CODEC = Packet.createCodec(ScoreboardDisplayS2CPacket::write, ScoreboardDisplayS2CPacket::new);
    private final ScoreboardDisplaySlot slot;
    private final String name;

    public ScoreboardDisplayS2CPacket(ScoreboardDisplaySlot slot, @Nullable ScoreboardObjective objective) {
        this.slot = slot;
        this.name = objective == null ? "" : objective.getName();
    }

    private ScoreboardDisplayS2CPacket(PacketByteBuf buf) {
        this.slot = buf.decode(ScoreboardDisplaySlot.FROM_ID);
        this.name = buf.readString();
    }

    private void write(PacketByteBuf buf) {
        buf.encode(ScoreboardDisplaySlot::getId, this.slot);
        buf.writeString(this.name);
    }

    @Override
    public PacketType<ScoreboardDisplayS2CPacket> getPacketId() {
        return PlayPackets.SET_DISPLAY_OBJECTIVE;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onScoreboardDisplay(this);
    }

    public ScoreboardDisplaySlot getSlot() {
        return this.slot;
    }

    @Nullable
    public String getName() {
        return Objects.equals(this.name, "") ? null : this.name;
    }
}

