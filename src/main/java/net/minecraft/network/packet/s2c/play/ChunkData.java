package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class ChunkData {
   private static final int MAX_SECTIONS_DATA_SIZE = 2097152;
   private final NbtCompound heightmap;
   private final byte[] sectionsData;
   private final List blockEntities;

   public ChunkData(WorldChunk chunk) {
      this.heightmap = new NbtCompound();
      Iterator var2 = chunk.getHeightmaps().iterator();

      Map.Entry entry;
      while(var2.hasNext()) {
         entry = (Map.Entry)var2.next();
         if (((Heightmap.Type)entry.getKey()).shouldSendToClient()) {
            this.heightmap.put(((Heightmap.Type)entry.getKey()).getName(), new NbtLongArray(((Heightmap)entry.getValue()).asLongArray()));
         }
      }

      this.sectionsData = new byte[getSectionsPacketSize(chunk)];
      writeSections(new PacketByteBuf(this.getWritableSectionsDataBuf()), chunk);
      this.blockEntities = Lists.newArrayList();
      var2 = chunk.getBlockEntities().entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Map.Entry)var2.next();
         this.blockEntities.add(ChunkData.BlockEntityData.of((BlockEntity)entry.getValue()));
      }

   }

   public ChunkData(PacketByteBuf buf, int x, int z) {
      this.heightmap = buf.readNbt();
      if (this.heightmap == null) {
         throw new RuntimeException("Can't read heightmap in packet for [" + x + ", " + z + "]");
      } else {
         int k = buf.readVarInt();
         if (k > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
         } else {
            this.sectionsData = new byte[k];
            buf.readBytes(this.sectionsData);
            this.blockEntities = buf.readList(BlockEntityData::new);
         }
      }
   }

   public void write(PacketByteBuf buf) {
      buf.writeNbt(this.heightmap);
      buf.writeVarInt(this.sectionsData.length);
      buf.writeBytes(this.sectionsData);
      buf.writeCollection(this.blockEntities, (buf2, entry) -> {
         entry.write(buf2);
      });
   }

   private static int getSectionsPacketSize(WorldChunk chunk) {
      int i = 0;
      ChunkSection[] var2 = chunk.getSectionArray();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ChunkSection lv = var2[var4];
         i += lv.getPacketSize();
      }

      return i;
   }

   private ByteBuf getWritableSectionsDataBuf() {
      ByteBuf byteBuf = Unpooled.wrappedBuffer(this.sectionsData);
      byteBuf.writerIndex(0);
      return byteBuf;
   }

   public static void writeSections(PacketByteBuf buf, WorldChunk chunk) {
      ChunkSection[] var2 = chunk.getSectionArray();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ChunkSection lv = var2[var4];
         lv.toPacket(buf);
      }

   }

   public Consumer getBlockEntities(int x, int z) {
      return (visitor) -> {
         this.iterateBlockEntities(visitor, x, z);
      };
   }

   private void iterateBlockEntities(BlockEntityVisitor consumer, int x, int z) {
      int k = 16 * x;
      int l = 16 * z;
      BlockPos.Mutable lv = new BlockPos.Mutable();
      Iterator var7 = this.blockEntities.iterator();

      while(var7.hasNext()) {
         BlockEntityData lv2 = (BlockEntityData)var7.next();
         int m = k + ChunkSectionPos.getLocalCoord(lv2.localXz >> 4);
         int n = l + ChunkSectionPos.getLocalCoord(lv2.localXz);
         lv.set(m, lv2.y, n);
         consumer.accept(lv, lv2.type, lv2.nbt);
      }

   }

   public PacketByteBuf getSectionsDataBuf() {
      return new PacketByteBuf(Unpooled.wrappedBuffer(this.sectionsData));
   }

   public NbtCompound getHeightmap() {
      return this.heightmap;
   }

   static class BlockEntityData {
      final int localXz;
      final int y;
      final BlockEntityType type;
      @Nullable
      final NbtCompound nbt;

      private BlockEntityData(int localXz, int y, BlockEntityType type, @Nullable NbtCompound nbt) {
         this.localXz = localXz;
         this.y = y;
         this.type = type;
         this.nbt = nbt;
      }

      private BlockEntityData(PacketByteBuf buf) {
         this.localXz = buf.readByte();
         this.y = buf.readShort();
         this.type = (BlockEntityType)buf.readRegistryValue(Registries.BLOCK_ENTITY_TYPE);
         this.nbt = buf.readNbt();
      }

      void write(PacketByteBuf buf) {
         buf.writeByte(this.localXz);
         buf.writeShort(this.y);
         buf.writeRegistryValue(Registries.BLOCK_ENTITY_TYPE, this.type);
         buf.writeNbt(this.nbt);
      }

      static BlockEntityData of(BlockEntity blockEntity) {
         NbtCompound lv = blockEntity.toInitialChunkDataNbt();
         BlockPos lv2 = blockEntity.getPos();
         int i = ChunkSectionPos.getLocalCoord(lv2.getX()) << 4 | ChunkSectionPos.getLocalCoord(lv2.getZ());
         return new BlockEntityData(i, lv2.getY(), blockEntity.getType(), lv.isEmpty() ? null : lv);
      }
   }

   @FunctionalInterface
   public interface BlockEntityVisitor {
      void accept(BlockPos pos, BlockEntityType type, @Nullable NbtCompound nbt);
   }
}
