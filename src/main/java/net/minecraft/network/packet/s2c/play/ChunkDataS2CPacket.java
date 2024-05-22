/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.util.BitSet;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class ChunkDataS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, ChunkDataS2CPacket> CODEC = Packet.createCodec(ChunkDataS2CPacket::write, ChunkDataS2CPacket::new);
    private final int chunkX;
    private final int chunkZ;
    private final ChunkData chunkData;
    private final LightData lightData;

    public ChunkDataS2CPacket(WorldChunk chunk, LightingProvider lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits) {
        ChunkPos lv = chunk.getPos();
        this.chunkX = lv.x;
        this.chunkZ = lv.z;
        this.chunkData = new ChunkData(chunk);
        this.lightData = new LightData(lv, lightProvider, skyBits, blockBits);
    }

    private ChunkDataS2CPacket(RegistryByteBuf buf) {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
        this.chunkData = new ChunkData(buf, this.chunkX, this.chunkZ);
        this.lightData = new LightData(buf, this.chunkX, this.chunkZ);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeInt(this.chunkX);
        buf.writeInt(this.chunkZ);
        this.chunkData.write(buf);
        this.lightData.write(buf);
    }

    @Override
    public PacketType<ChunkDataS2CPacket> getPacketId() {
        return PlayPackets.LEVEL_CHUNK_WITH_LIGHT;
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onChunkData(this);
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public ChunkData getChunkData() {
        return this.chunkData;
    }

    public LightData getLightData() {
        return this.lightData;
    }
}

