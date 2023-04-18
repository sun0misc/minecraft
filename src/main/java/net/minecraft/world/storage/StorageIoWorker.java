package net.minecraft.world.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.scanner.SelectiveNbtCollector;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StorageIoWorker implements NbtScannable, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final AtomicBoolean closed = new AtomicBoolean();
   private final TaskExecutor executor;
   private final RegionBasedStorage storage;
   private final Map results = Maps.newLinkedHashMap();
   private final Long2ObjectLinkedOpenHashMap blendingStatusCaches = new Long2ObjectLinkedOpenHashMap();
   private static final int MAX_CACHE_SIZE = 1024;

   protected StorageIoWorker(Path directory, boolean dsync, String name) {
      this.storage = new RegionBasedStorage(directory, dsync);
      this.executor = new TaskExecutor(new TaskQueue.Prioritized(StorageIoWorker.Priority.values().length), Util.getIoWorkerExecutor(), "IOWorker-" + name);
   }

   public boolean needsBlending(ChunkPos chunkPos, int checkRadius) {
      ChunkPos lv = new ChunkPos(chunkPos.x - checkRadius, chunkPos.z - checkRadius);
      ChunkPos lv2 = new ChunkPos(chunkPos.x + checkRadius, chunkPos.z + checkRadius);

      for(int j = lv.getRegionX(); j <= lv2.getRegionX(); ++j) {
         for(int k = lv.getRegionZ(); k <= lv2.getRegionZ(); ++k) {
            BitSet bitSet = (BitSet)this.getOrComputeBlendingStatus(j, k).join();
            if (!bitSet.isEmpty()) {
               ChunkPos lv3 = ChunkPos.fromRegion(j, k);
               int l = Math.max(lv.x - lv3.x, 0);
               int m = Math.max(lv.z - lv3.z, 0);
               int n = Math.min(lv2.x - lv3.x, 31);
               int o = Math.min(lv2.z - lv3.z, 31);

               for(int p = l; p <= n; ++p) {
                  for(int q = m; q <= o; ++q) {
                     int r = q * 32 + p;
                     if (bitSet.get(r)) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private CompletableFuture getOrComputeBlendingStatus(int chunkX, int chunkZ) {
      long l = ChunkPos.toLong(chunkX, chunkZ);
      synchronized(this.blendingStatusCaches) {
         CompletableFuture completableFuture = (CompletableFuture)this.blendingStatusCaches.getAndMoveToFirst(l);
         if (completableFuture == null) {
            completableFuture = this.computeBlendingStatus(chunkX, chunkZ);
            this.blendingStatusCaches.putAndMoveToFirst(l, completableFuture);
            if (this.blendingStatusCaches.size() > 1024) {
               this.blendingStatusCaches.removeLast();
            }
         }

         return completableFuture;
      }
   }

   private CompletableFuture computeBlendingStatus(int chunkX, int chunkZ) {
      return CompletableFuture.supplyAsync(() -> {
         ChunkPos lv = ChunkPos.fromRegion(chunkX, chunkZ);
         ChunkPos lv2 = ChunkPos.fromRegionCenter(chunkX, chunkZ);
         BitSet bitSet = new BitSet();
         ChunkPos.stream(lv, lv2).forEach((chunkPos) -> {
            SelectiveNbtCollector lv = new SelectiveNbtCollector(new NbtScanQuery[]{new NbtScanQuery(NbtInt.TYPE, "DataVersion"), new NbtScanQuery(NbtCompound.TYPE, "blending_data")});

            try {
               this.scanChunk(chunkPos, lv).join();
            } catch (Exception var7) {
               LOGGER.warn("Failed to scan chunk {}", chunkPos, var7);
               return;
            }

            NbtElement lv2 = lv.getRoot();
            if (lv2 instanceof NbtCompound lv3) {
               if (this.needsBlending(lv3)) {
                  int i = chunkPos.getRegionRelativeZ() * 32 + chunkPos.getRegionRelativeX();
                  bitSet.set(i);
               }
            }

         });
         return bitSet;
      }, Util.getMainWorkerExecutor());
   }

   private boolean needsBlending(NbtCompound nbt) {
      return nbt.contains("DataVersion", NbtElement.NUMBER_TYPE) && nbt.getInt("DataVersion") >= 3441 ? nbt.contains("blending_data", NbtElement.COMPOUND_TYPE) : true;
   }

   public CompletableFuture setResult(ChunkPos pos, @Nullable NbtCompound nbt) {
      return this.run(() -> {
         Result lv = (Result)this.results.computeIfAbsent(pos, (pos2) -> {
            return new Result(nbt);
         });
         lv.nbt = nbt;
         return Either.left(lv.future);
      }).thenCompose(Function.identity());
   }

   public CompletableFuture readChunkData(ChunkPos pos) {
      return this.run(() -> {
         Result lv = (Result)this.results.get(pos);
         if (lv != null) {
            return Either.left(Optional.ofNullable(lv.nbt));
         } else {
            try {
               NbtCompound lv2 = this.storage.getTagAt(pos);
               return Either.left(Optional.ofNullable(lv2));
            } catch (Exception var4) {
               LOGGER.warn("Failed to read chunk {}", pos, var4);
               return Either.right(var4);
            }
         }
      });
   }

   public CompletableFuture completeAll(boolean sync) {
      CompletableFuture completableFuture = this.run(() -> {
         return Either.left(CompletableFuture.allOf((CompletableFuture[])this.results.values().stream().map((arg) -> {
            return arg.future;
         }).toArray((i) -> {
            return new CompletableFuture[i];
         })));
      }).thenCompose(Function.identity());
      return sync ? completableFuture.thenCompose((void_) -> {
         return this.run(() -> {
            try {
               this.storage.sync();
               return Either.left((Object)null);
            } catch (Exception var2) {
               LOGGER.warn("Failed to synchronize chunks", var2);
               return Either.right(var2);
            }
         });
      }) : completableFuture.thenCompose((void_) -> {
         return this.run(() -> {
            return Either.left((Object)null);
         });
      });
   }

   public CompletableFuture scanChunk(ChunkPos pos, NbtScanner scanner) {
      return this.run(() -> {
         try {
            Result lv = (Result)this.results.get(pos);
            if (lv != null) {
               if (lv.nbt != null) {
                  lv.nbt.accept(scanner);
               }
            } else {
               this.storage.scanChunk(pos, scanner);
            }

            return Either.left((Object)null);
         } catch (Exception var4) {
            LOGGER.warn("Failed to bulk scan chunk {}", pos, var4);
            return Either.right(var4);
         }
      });
   }

   private CompletableFuture run(Supplier task) {
      return this.executor.askFallible((listener) -> {
         return new TaskQueue.PrioritizedTask(StorageIoWorker.Priority.FOREGROUND.ordinal(), () -> {
            if (!this.closed.get()) {
               listener.send((Either)task.get());
            }

            this.writeRemainingResults();
         });
      });
   }

   private void writeResult() {
      if (!this.results.isEmpty()) {
         Iterator iterator = this.results.entrySet().iterator();
         Map.Entry entry = (Map.Entry)iterator.next();
         iterator.remove();
         this.write((ChunkPos)entry.getKey(), (Result)entry.getValue());
         this.writeRemainingResults();
      }
   }

   private void writeRemainingResults() {
      this.executor.send(new TaskQueue.PrioritizedTask(StorageIoWorker.Priority.BACKGROUND.ordinal(), this::writeResult));
   }

   private void write(ChunkPos pos, Result result) {
      try {
         this.storage.write(pos, result.nbt);
         result.future.complete((Object)null);
      } catch (Exception var4) {
         LOGGER.error("Failed to store chunk {}", pos, var4);
         result.future.completeExceptionally(var4);
      }

   }

   public void close() throws IOException {
      if (this.closed.compareAndSet(false, true)) {
         this.executor.ask((listener) -> {
            return new TaskQueue.PrioritizedTask(StorageIoWorker.Priority.SHUTDOWN.ordinal(), () -> {
               listener.send(Unit.INSTANCE);
            });
         }).join();
         this.executor.close();

         try {
            this.storage.close();
         } catch (Exception var2) {
            LOGGER.error("Failed to close storage", var2);
         }

      }
   }

   private static enum Priority {
      FOREGROUND,
      BACKGROUND,
      SHUTDOWN;

      // $FF: synthetic method
      private static Priority[] method_36744() {
         return new Priority[]{FOREGROUND, BACKGROUND, SHUTDOWN};
      }
   }

   private static class Result {
      @Nullable
      NbtCompound nbt;
      final CompletableFuture future = new CompletableFuture();

      public Result(@Nullable NbtCompound nbt) {
         this.nbt = nbt;
      }
   }
}
