package net.minecraft.world.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import org.slf4j.Logger;

public class EntityChunkDataAccess implements ChunkDataAccess {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String ENTITIES_KEY = "Entities";
   private static final String POSITION_KEY = "Position";
   private final ServerWorld world;
   private final StorageIoWorker dataLoadWorker;
   private final LongSet emptyChunks = new LongOpenHashSet();
   private final TaskExecutor taskExecutor;
   protected final DataFixer dataFixer;

   public EntityChunkDataAccess(ServerWorld world, Path path, DataFixer dataFixer, boolean dsync, Executor executor) {
      this.world = world;
      this.dataFixer = dataFixer;
      this.taskExecutor = TaskExecutor.create(executor, "entity-deserializer");
      this.dataLoadWorker = new StorageIoWorker(path, dsync, "entities");
   }

   public CompletableFuture readChunkData(ChunkPos pos) {
      if (this.emptyChunks.contains(pos.toLong())) {
         return CompletableFuture.completedFuture(emptyDataList(pos));
      } else {
         CompletableFuture var10000 = this.dataLoadWorker.readChunkData(pos);
         Function var10001 = (nbt) -> {
            if (nbt.isEmpty()) {
               this.emptyChunks.add(pos.toLong());
               return emptyDataList(pos);
            } else {
               try {
                  ChunkPos lv = getChunkPos((NbtCompound)nbt.get());
                  if (!Objects.equals(pos, lv)) {
                     LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", new Object[]{pos, pos, lv});
                  }
               } catch (Exception var6) {
                  LOGGER.warn("Failed to parse chunk {} position info", pos, var6);
               }

               NbtCompound lv2 = this.fixChunkData((NbtCompound)nbt.get());
               NbtList lv3 = lv2.getList("Entities", NbtElement.COMPOUND_TYPE);
               List list = (List)EntityType.streamFromNbt(lv3, this.world).collect(ImmutableList.toImmutableList());
               return new ChunkDataList(pos, list);
            }
         };
         TaskExecutor var10002 = this.taskExecutor;
         Objects.requireNonNull(var10002);
         return var10000.thenApplyAsync(var10001, var10002::send);
      }
   }

   private static ChunkPos getChunkPos(NbtCompound chunkNbt) {
      int[] is = chunkNbt.getIntArray("Position");
      return new ChunkPos(is[0], is[1]);
   }

   private static void putChunkPos(NbtCompound chunkNbt, ChunkPos pos) {
      chunkNbt.put("Position", new NbtIntArray(new int[]{pos.x, pos.z}));
   }

   private static ChunkDataList emptyDataList(ChunkPos pos) {
      return new ChunkDataList(pos, ImmutableList.of());
   }

   public void writeChunkData(ChunkDataList dataList) {
      ChunkPos lv = dataList.getChunkPos();
      if (dataList.isEmpty()) {
         if (this.emptyChunks.add(lv.toLong())) {
            this.dataLoadWorker.setResult(lv, (NbtCompound)null);
         }

      } else {
         NbtList lv2 = new NbtList();
         dataList.stream().forEach((entity) -> {
            NbtCompound lv = new NbtCompound();
            if (entity.saveNbt(lv)) {
               lv2.add(lv);
            }

         });
         NbtCompound lv3 = NbtHelper.putDataVersion(new NbtCompound());
         lv3.put("Entities", lv2);
         putChunkPos(lv3, lv);
         this.dataLoadWorker.setResult(lv, lv3).exceptionally((ex) -> {
            LOGGER.error("Failed to store chunk {}", lv, ex);
            return null;
         });
         this.emptyChunks.remove(lv.toLong());
      }
   }

   public void awaitAll(boolean sync) {
      this.dataLoadWorker.completeAll(sync).join();
      this.taskExecutor.awaitAll();
   }

   private NbtCompound fixChunkData(NbtCompound chunkNbt) {
      int i = NbtHelper.getDataVersion(chunkNbt, -1);
      return DataFixTypes.ENTITY_CHUNK.update(this.dataFixer, chunkNbt, i);
   }

   public void close() throws IOException {
      this.dataLoadWorker.close();
   }
}
