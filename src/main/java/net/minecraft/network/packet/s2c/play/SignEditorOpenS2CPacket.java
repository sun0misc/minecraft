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
import net.minecraft.util.math.BlockPos;

public class SignEditorOpenS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, SignEditorOpenS2CPacket> CODEC = Packet.createCodec(SignEditorOpenS2CPacket::write, SignEditorOpenS2CPacket::new);
    private final BlockPos pos;
    private final boolean front;

    public SignEditorOpenS2CPacket(BlockPos pos, boolean front) {
        this.pos = pos;
        this.front = front;
    }

    private SignEditorOpenS2CPacket(PacketByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.front = buf.readBoolean();
    }

    private void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.front);
    }

    @Override
    public PacketType<SignEditorOpenS2CPacket> getPacketId() {
        return PlayPackets.OPEN_SIGN_EDITOR;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onSignEditorOpen(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean isFront() {
        return this.front;
    }
}

