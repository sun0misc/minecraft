/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkSection;

public class ChunkDeltaUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, ChunkDeltaUpdateS2CPacket> CODEC = Packet.createCodec(ChunkDeltaUpdateS2CPacket::write, ChunkDeltaUpdateS2CPacket::new);
    private static final int field_33341 = 12;
    private final ChunkSectionPos sectionPos;
    private final short[] positions;
    private final BlockState[] blockStates;

    public ChunkDeltaUpdateS2CPacket(ChunkSectionPos sectionPos, ShortSet positions, ChunkSection section) {
        this.sectionPos = sectionPos;
        int i = positions.size();
        this.positions = new short[i];
        this.blockStates = new BlockState[i];
        int j = 0;
        ShortIterator shortIterator = positions.iterator();
        while (shortIterator.hasNext()) {
            short s;
            this.positions[j] = s = ((Short)shortIterator.next()).shortValue();
            this.blockStates[j] = section.getBlockState(ChunkSectionPos.unpackLocalX(s), ChunkSectionPos.unpackLocalY(s), ChunkSectionPos.unpackLocalZ(s));
            ++j;
        }
    }

    private ChunkDeltaUpdateS2CPacket(PacketByteBuf buf) {
        this.sectionPos = ChunkSectionPos.from(buf.readLong());
        int i = buf.readVarInt();
        this.positions = new short[i];
        this.blockStates = new BlockState[i];
        for (int j = 0; j < i; ++j) {
            long l = buf.readVarLong();
            this.positions[j] = (short)(l & 0xFFFL);
            this.blockStates[j] = Block.STATE_IDS.get((int)(l >>> 12));
        }
    }

    private void write(PacketByteBuf buf) {
        buf.writeLong(this.sectionPos.asLong());
        buf.writeVarInt(this.positions.length);
        for (int i = 0; i < this.positions.length; ++i) {
            buf.writeVarLong((long)Block.getRawIdFromState(this.blockStates[i]) << 12 | (long)this.positions[i]);
        }
    }

    @Override
    public PacketType<ChunkDeltaUpdateS2CPacket> getPacketId() {
        return PlayPackets.SECTION_BLOCKS_UPDATE;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onChunkDeltaUpdate(this);
    }

    public void visitUpdates(BiConsumer<BlockPos, BlockState> visitor) {
        BlockPos.Mutable lv = new BlockPos.Mutable();
        for (int i = 0; i < this.positions.length; ++i) {
            short s = this.positions[i];
            lv.set(this.sectionPos.unpackBlockX(s), this.sectionPos.unpackBlockY(s), this.sectionPos.unpackBlockZ(s));
            visitor.accept(lv, this.blockStates[i]);
        }
    }
}

