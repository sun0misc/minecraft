package net.minecraft.client.world;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientChunkManager extends ChunkManager {
   static final Logger LOGGER = LogUtils.getLogger();
   private final WorldChunk emptyChunk;
   private final LightingProvider lightingProvider;
   volatile ClientChunkMap chunks;
   final ClientWorld world;

   public ClientChunkManager(ClientWorld world, int loadDistance) {
      this.world = world;
      this.emptyChunk = new EmptyChunk(world, new ChunkPos(0, 0), world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS));
      this.lightingProvider = new LightingProvider(this, true, world.getDimension().hasSkyLight());
      this.chunks = new ClientChunkMap(getChunkMapRadius(loadDistance));
   }

   public LightingProvider getLightingProvider() {
      return this.lightingProvider;
   }

   private static boolean positionEquals(@Nullable WorldChunk chunk, int x, int z) {
      if (chunk == null) {
         return false;
      } else {
         ChunkPos lv = chunk.getPos();
         return lv.x == x && lv.z == z;
      }
   }

   public void unload(int chunkX, int chunkZ) {
      if (this.chunks.isInRadius(chunkX, chunkZ)) {
         int k = this.chunks.getIndex(chunkX, chunkZ);
         WorldChunk lv = this.chunks.getChunk(k);
         if (positionEquals(lv, chunkX, chunkZ)) {
            this.chunks.compareAndSet(k, lv, (WorldChunk)null);
         }

      }
   }

   @Nullable
   public WorldChunk getChunk(int i, int j, ChunkStatus arg, boolean bl) {
      if (this.chunks.isInRadius(i, j)) {
         WorldChunk lv = this.chunks.getChunk(this.chunks.getIndex(i, j));
         if (positionEquals(lv, i, j)) {
            return lv;
         }
      }

      return bl ? this.emptyChunk : null;
   }

   public BlockView getWorld() {
      return this.world;
   }

   public void onChunkBiomeData(int x, int z, PacketByteBuf buf) {
      if (!this.chunks.isInRadius(x, z)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", x, z);
      } else {
         int k = this.chunks.getIndex(x, z);
         WorldChunk lv = (WorldChunk)this.chunks.chunks.get(k);
         if (!positionEquals(lv, x, z)) {
            LOGGER.warn("Ignoring chunk since it's not present: {}, {}", x, z);
         } else {
            lv.loadBiomeFromPacket(buf);
         }

      }
   }

   @Nullable
   public WorldChunk loadChunkFromPacket(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer consumer) {
      if (!this.chunks.isInRadius(x, z)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", x, z);
         return null;
      } else {
         int k = this.chunks.getIndex(x, z);
         WorldChunk lv = (WorldChunk)this.chunks.chunks.get(k);
         ChunkPos lv2 = new ChunkPos(x, z);
         if (!positionEquals(lv, x, z)) {
            lv = new WorldChunk(this.world, lv2);
            lv.loadFromPacket(buf, nbt, consumer);
            this.chunks.set(k, lv);
         } else {
            lv.loadFromPacket(buf, nbt, consumer);
         }

         this.world.resetChunkColor(lv2);
         return lv;
      }
   }

   public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {
   }

   public void setChunkMapCenter(int x, int z) {
      this.chunks.centerChunkX = x;
      this.chunks.centerChunkZ = z;
   }

   public void updateLoadDistance(int loadDistance) {
      int j = this.chunks.radius;
      int k = getChunkMapRadius(loadDistance);
      if (j != k) {
         ClientChunkMap lv = new ClientChunkMap(k);
         lv.centerChunkX = this.chunks.centerChunkX;
         lv.centerChunkZ = this.chunks.centerChunkZ;

         for(int l = 0; l < this.chunks.chunks.length(); ++l) {
            WorldChunk lv2 = (WorldChunk)this.chunks.chunks.get(l);
            if (lv2 != null) {
               ChunkPos lv3 = lv2.getPos();
               if (lv.isInRadius(lv3.x, lv3.z)) {
                  lv.set(lv.getIndex(lv3.x, lv3.z), lv2);
               }
            }
         }

         this.chunks = lv;
      }

   }

   private static int getChunkMapRadius(int loadDistance) {
      return Math.max(2, loadDistance) + 3;
   }

   public String getDebugString() {
      int var10000 = this.chunks.chunks.length();
      return "" + var10000 + ", " + this.getLoadedChunkCount();
   }

   public int getLoadedChunkCount() {
      return this.chunks.loadedChunkCount;
   }

   public void onLightUpdate(LightType type, ChunkSectionPos pos) {
      MinecraftClient.getInstance().worldRenderer.scheduleBlockRender(pos.getSectionX(), pos.getSectionY(), pos.getSectionZ());
   }

   // $FF: synthetic method
   @Nullable
   public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
      return this.getChunk(x, z, leastStatus, create);
   }

   @Environment(EnvType.CLIENT)
   private final class ClientChunkMap {
      final AtomicReferenceArray chunks;
      final int radius;
      private final int diameter;
      volatile int centerChunkX;
      volatile int centerChunkZ;
      int loadedChunkCount;

      ClientChunkMap(int radius) {
         this.radius = radius;
         this.diameter = radius * 2 + 1;
         this.chunks = new AtomicReferenceArray(this.diameter * this.diameter);
      }

      int getIndex(int chunkX, int chunkZ) {
         return Math.floorMod(chunkZ, this.diameter) * this.diameter + Math.floorMod(chunkX, this.diameter);
      }

      protected void set(int index, @Nullable WorldChunk chunk) {
         WorldChunk lv = (WorldChunk)this.chunks.getAndSet(index, chunk);
         if (lv != null) {
            --this.loadedChunkCount;
            ClientChunkManager.this.world.unloadBlockEntities(lv);
         }

         if (chunk != null) {
            ++this.loadedChunkCount;
         }

      }

      protected WorldChunk compareAndSet(int index, WorldChunk expect, @Nullable WorldChunk update) {
         if (this.chunks.compareAndSet(index, expect, update) && update == null) {
            --this.loadedChunkCount;
         }

         ClientChunkManager.this.world.unloadBlockEntities(expect);
         return expect;
      }

      boolean isInRadius(int chunkX, int chunkZ) {
         return Math.abs(chunkX - this.centerChunkX) <= this.radius && Math.abs(chunkZ - this.centerChunkZ) <= this.radius;
      }

      @Nullable
      protected WorldChunk getChunk(int index) {
         return (WorldChunk)this.chunks.get(index);
      }

      private void writePositions(String fileName) {
         try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            try {
               int i = ClientChunkManager.this.chunks.radius;

               for(int j = this.centerChunkZ - i; j <= this.centerChunkZ + i; ++j) {
                  for(int k = this.centerChunkX - i; k <= this.centerChunkX + i; ++k) {
                     WorldChunk lv = (WorldChunk)ClientChunkManager.this.chunks.chunks.get(ClientChunkManager.this.chunks.getIndex(k, j));
                     if (lv != null) {
                        ChunkPos lv2 = lv.getPos();
                        fileOutputStream.write((lv2.x + "\t" + lv2.z + "\t" + lv.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                     }
                  }
               }
            } catch (Throwable var9) {
               try {
                  fileOutputStream.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            fileOutputStream.close();
         } catch (IOException var10) {
            ClientChunkManager.LOGGER.error("Failed to dump chunks to file {}", fileName, var10);
         }

      }
   }
}
