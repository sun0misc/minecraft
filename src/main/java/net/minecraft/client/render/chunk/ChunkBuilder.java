package net.minecraft.client.render.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ChunkBuilder {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_32831 = 4;
   private static final VertexFormat POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
   private static final int field_35300 = 2;
   private final PriorityBlockingQueue prioritizedTaskQueue = Queues.newPriorityBlockingQueue();
   private final Queue taskQueue = Queues.newLinkedBlockingDeque();
   private int processablePrioritizedTaskCount = 2;
   private final Queue threadBuffers;
   private final Queue uploadQueue = Queues.newConcurrentLinkedQueue();
   private volatile int queuedTaskCount;
   private volatile int bufferCount;
   final BlockBufferBuilderStorage buffers;
   private final TaskExecutor mailbox;
   private final Executor executor;
   ClientWorld world;
   final WorldRenderer worldRenderer;
   private Vec3d cameraPosition;

   public ChunkBuilder(ClientWorld world, WorldRenderer worldRenderer, Executor executor, boolean is64Bits, BlockBufferBuilderStorage buffers) {
      this.cameraPosition = Vec3d.ZERO;
      this.world = world;
      this.worldRenderer = worldRenderer;
      int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderLayer.getBlockLayers().stream().mapToInt(RenderLayer::getExpectedBufferSize).sum() * 4) - 1);
      int j = Runtime.getRuntime().availableProcessors();
      int k = is64Bits ? j : Math.min(j, 4);
      int l = Math.max(1, Math.min(k, i));
      this.buffers = buffers;
      List list = Lists.newArrayListWithExpectedSize(l);

      try {
         for(int m = 0; m < l; ++m) {
            list.add(new BlockBufferBuilderStorage());
         }
      } catch (OutOfMemoryError var14) {
         LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
         int n = Math.min(list.size() * 2 / 3, list.size() - 1);

         for(int o = 0; o < n; ++o) {
            list.remove(list.size() - 1);
         }

         System.gc();
      }

      this.threadBuffers = Queues.newArrayDeque(list);
      this.bufferCount = this.threadBuffers.size();
      this.executor = executor;
      this.mailbox = TaskExecutor.create(executor, "Chunk Renderer");
      this.mailbox.send(this::scheduleRunTasks);
   }

   public void setWorld(ClientWorld world) {
      this.world = world;
   }

   private void scheduleRunTasks() {
      if (!this.threadBuffers.isEmpty()) {
         BuiltChunk.Task lv = this.pollTask();
         if (lv != null) {
            BlockBufferBuilderStorage lv2 = (BlockBufferBuilderStorage)this.threadBuffers.poll();
            this.queuedTaskCount = this.prioritizedTaskQueue.size() + this.taskQueue.size();
            this.bufferCount = this.threadBuffers.size();
            CompletableFuture.supplyAsync(Util.debugSupplier(lv.getName(), () -> {
               return lv.run(lv2);
            }), this.executor).thenCompose((future) -> {
               return future;
            }).whenComplete((result, throwable) -> {
               if (throwable != null) {
                  MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Batching chunks"));
               } else {
                  this.mailbox.send(() -> {
                     if (result == ChunkBuilder.Result.SUCCESSFUL) {
                        lv2.clear();
                     } else {
                        lv2.reset();
                     }

                     this.threadBuffers.add(lv2);
                     this.bufferCount = this.threadBuffers.size();
                     this.scheduleRunTasks();
                  });
               }
            });
         }
      }
   }

   @Nullable
   private BuiltChunk.Task pollTask() {
      BuiltChunk.Task lv;
      if (this.processablePrioritizedTaskCount <= 0) {
         lv = (BuiltChunk.Task)this.taskQueue.poll();
         if (lv != null) {
            this.processablePrioritizedTaskCount = 2;
            return lv;
         }
      }

      lv = (BuiltChunk.Task)this.prioritizedTaskQueue.poll();
      if (lv != null) {
         --this.processablePrioritizedTaskCount;
         return lv;
      } else {
         this.processablePrioritizedTaskCount = 2;
         return (BuiltChunk.Task)this.taskQueue.poll();
      }
   }

   public String getDebugString() {
      return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.queuedTaskCount, this.uploadQueue.size(), this.bufferCount);
   }

   public int getToBatchCount() {
      return this.queuedTaskCount;
   }

   public int getChunksToUpload() {
      return this.uploadQueue.size();
   }

   public int getFreeBufferCount() {
      return this.bufferCount;
   }

   public void setCameraPosition(Vec3d cameraPosition) {
      this.cameraPosition = cameraPosition;
   }

   public Vec3d getCameraPosition() {
      return this.cameraPosition;
   }

   public void upload() {
      Runnable runnable;
      while((runnable = (Runnable)this.uploadQueue.poll()) != null) {
         runnable.run();
      }

   }

   public void rebuild(BuiltChunk chunk, ChunkRendererRegionBuilder builder) {
      chunk.rebuild(builder);
   }

   public void reset() {
      this.clear();
   }

   public void send(BuiltChunk.Task task) {
      this.mailbox.send(() -> {
         if (task.prioritized) {
            this.prioritizedTaskQueue.offer(task);
         } else {
            this.taskQueue.offer(task);
         }

         this.queuedTaskCount = this.prioritizedTaskQueue.size() + this.taskQueue.size();
         this.scheduleRunTasks();
      });
   }

   public CompletableFuture scheduleUpload(BufferBuilder.BuiltBuffer arg, VertexBuffer glBuffer) {
      Runnable var10000 = () -> {
         if (!glBuffer.isClosed()) {
            glBuffer.bind();
            glBuffer.upload(arg);
            VertexBuffer.unbind();
         }
      };
      Queue var10001 = this.uploadQueue;
      Objects.requireNonNull(var10001);
      return CompletableFuture.runAsync(var10000, var10001::add);
   }

   private void clear() {
      BuiltChunk.Task lv;
      while(!this.prioritizedTaskQueue.isEmpty()) {
         lv = (BuiltChunk.Task)this.prioritizedTaskQueue.poll();
         if (lv != null) {
            lv.cancel();
         }
      }

      while(!this.taskQueue.isEmpty()) {
         lv = (BuiltChunk.Task)this.taskQueue.poll();
         if (lv != null) {
            lv.cancel();
         }
      }

      this.queuedTaskCount = 0;
   }

   public boolean isEmpty() {
      return this.queuedTaskCount == 0 && this.uploadQueue.isEmpty();
   }

   public void stop() {
      this.clear();
      this.mailbox.close();
      this.threadBuffers.clear();
   }

   static {
      POSITION_COLOR_TEXTURE_LIGHT_NORMAL = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
   }

   @Environment(EnvType.CLIENT)
   public class BuiltChunk {
      public static final int field_32832 = 16;
      public final int index;
      public final AtomicReference data;
      final AtomicInteger field_36374;
      @Nullable
      private RebuildTask rebuildTask;
      @Nullable
      private SortTask sortTask;
      private final Set blockEntities;
      private final Map buffers;
      private Box boundingBox;
      private boolean needsRebuild;
      final BlockPos.Mutable origin;
      private final BlockPos.Mutable[] neighborPositions;
      private boolean needsImportantRebuild;

      public BuiltChunk(int index, int originX, int originY, int originZ) {
         this.data = new AtomicReference(ChunkBuilder.ChunkData.EMPTY);
         this.field_36374 = new AtomicInteger(0);
         this.blockEntities = Sets.newHashSet();
         this.buffers = (Map)RenderLayer.getBlockLayers().stream().collect(Collectors.toMap((argx) -> {
            return argx;
         }, (argx) -> {
            return new VertexBuffer();
         }));
         this.needsRebuild = true;
         this.origin = new BlockPos.Mutable(-1, -1, -1);
         this.neighborPositions = (BlockPos.Mutable[])Util.make(new BlockPos.Mutable[6], (neighborPositions) -> {
            for(int i = 0; i < neighborPositions.length; ++i) {
               neighborPositions[i] = new BlockPos.Mutable();
            }

         });
         this.index = index;
         this.setOrigin(originX, originY, originZ);
      }

      private boolean isChunkNonEmpty(BlockPos pos) {
         return ChunkBuilder.this.world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false) != null;
      }

      public boolean shouldBuild() {
         int i = true;
         if (!(this.getSquaredCameraDistance() > 576.0)) {
            return true;
         } else {
            return this.isChunkNonEmpty(this.neighborPositions[Direction.WEST.ordinal()]) && this.isChunkNonEmpty(this.neighborPositions[Direction.NORTH.ordinal()]) && this.isChunkNonEmpty(this.neighborPositions[Direction.EAST.ordinal()]) && this.isChunkNonEmpty(this.neighborPositions[Direction.SOUTH.ordinal()]);
         }
      }

      public Box getBoundingBox() {
         return this.boundingBox;
      }

      public VertexBuffer getBuffer(RenderLayer layer) {
         return (VertexBuffer)this.buffers.get(layer);
      }

      public void setOrigin(int x, int y, int z) {
         this.clear();
         this.origin.set(x, y, z);
         this.boundingBox = new Box((double)x, (double)y, (double)z, (double)(x + 16), (double)(y + 16), (double)(z + 16));
         Direction[] var4 = Direction.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Direction lv = var4[var6];
            this.neighborPositions[lv.ordinal()].set(this.origin).move(lv, 16);
         }

      }

      protected double getSquaredCameraDistance() {
         Camera lv = MinecraftClient.getInstance().gameRenderer.getCamera();
         double d = this.boundingBox.minX + 8.0 - lv.getPos().x;
         double e = this.boundingBox.minY + 8.0 - lv.getPos().y;
         double f = this.boundingBox.minZ + 8.0 - lv.getPos().z;
         return d * d + e * e + f * f;
      }

      void beginBufferBuilding(BufferBuilder buffer) {
         buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
      }

      public ChunkData getData() {
         return (ChunkData)this.data.get();
      }

      private void clear() {
         this.cancel();
         this.data.set(ChunkBuilder.ChunkData.EMPTY);
         this.needsRebuild = true;
      }

      public void delete() {
         this.clear();
         this.buffers.values().forEach(VertexBuffer::close);
      }

      public BlockPos getOrigin() {
         return this.origin;
      }

      public void scheduleRebuild(boolean important) {
         boolean bl2 = this.needsRebuild;
         this.needsRebuild = true;
         this.needsImportantRebuild = important | (bl2 && this.needsImportantRebuild);
      }

      public void cancelRebuild() {
         this.needsRebuild = false;
         this.needsImportantRebuild = false;
      }

      public boolean needsRebuild() {
         return this.needsRebuild;
      }

      public boolean needsImportantRebuild() {
         return this.needsRebuild && this.needsImportantRebuild;
      }

      public BlockPos getNeighborPosition(Direction direction) {
         return this.neighborPositions[direction.ordinal()];
      }

      public boolean scheduleSort(RenderLayer layer, ChunkBuilder chunkRenderer) {
         ChunkData lv = this.getData();
         if (this.sortTask != null) {
            this.sortTask.cancel();
         }

         if (!lv.nonEmptyLayers.contains(layer)) {
            return false;
         } else {
            this.sortTask = new SortTask(this.getSquaredCameraDistance(), lv);
            chunkRenderer.send(this.sortTask);
            return true;
         }
      }

      protected boolean cancel() {
         boolean bl = false;
         if (this.rebuildTask != null) {
            this.rebuildTask.cancel();
            this.rebuildTask = null;
            bl = true;
         }

         if (this.sortTask != null) {
            this.sortTask.cancel();
            this.sortTask = null;
         }

         return bl;
      }

      public Task createRebuildTask(ChunkRendererRegionBuilder builder) {
         boolean bl = this.cancel();
         BlockPos lv = this.origin.toImmutable();
         int i = true;
         ChunkRendererRegion lv2 = builder.build(ChunkBuilder.this.world, lv.add(-1, -1, -1), lv.add(16, 16, 16), 1);
         boolean bl2 = this.data.get() == ChunkBuilder.ChunkData.EMPTY;
         if (bl2 && bl) {
            this.field_36374.incrementAndGet();
         }

         this.rebuildTask = new RebuildTask(this.getSquaredCameraDistance(), lv2, !bl2 || this.field_36374.get() > 2);
         return this.rebuildTask;
      }

      public void scheduleRebuild(ChunkBuilder chunkRenderer, ChunkRendererRegionBuilder builder) {
         Task lv = this.createRebuildTask(builder);
         chunkRenderer.send(lv);
      }

      void setNoCullingBlockEntities(Collection collection) {
         Set set = Sets.newHashSet(collection);
         HashSet set2;
         synchronized(this.blockEntities) {
            set2 = Sets.newHashSet(this.blockEntities);
            set.removeAll(this.blockEntities);
            set2.removeAll(collection);
            this.blockEntities.clear();
            this.blockEntities.addAll(collection);
         }

         ChunkBuilder.this.worldRenderer.updateNoCullingBlockEntities(set2, set);
      }

      public void rebuild(ChunkRendererRegionBuilder builder) {
         Task lv = this.createRebuildTask(builder);
         lv.run(ChunkBuilder.this.buffers);
      }

      @Environment(EnvType.CLIENT)
      private class SortTask extends Task {
         private final ChunkData data;

         public SortTask(double distance, ChunkData data) {
            super(distance, true);
            this.data = data;
         }

         protected String getName() {
            return "rend_chk_sort";
         }

         public CompletableFuture run(BlockBufferBuilderStorage buffers) {
            if (this.cancelled.get()) {
               return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
            } else if (!BuiltChunk.this.shouldBuild()) {
               this.cancelled.set(true);
               return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
            } else if (this.cancelled.get()) {
               return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
            } else {
               Vec3d lv = ChunkBuilder.this.getCameraPosition();
               float f = (float)lv.x;
               float g = (float)lv.y;
               float h = (float)lv.z;
               BufferBuilder.TransparentSortingData lv2 = this.data.transparentSortingData;
               if (lv2 != null && !this.data.isEmpty(RenderLayer.getTranslucent())) {
                  BufferBuilder lv3 = buffers.get(RenderLayer.getTranslucent());
                  BuiltChunk.this.beginBufferBuilding(lv3);
                  lv3.beginSortedIndexBuffer(lv2);
                  lv3.setSorter(VertexSorter.byDistance(f - (float)BuiltChunk.this.origin.getX(), g - (float)BuiltChunk.this.origin.getY(), h - (float)BuiltChunk.this.origin.getZ()));
                  this.data.transparentSortingData = lv3.getSortingData();
                  BufferBuilder.BuiltBuffer lv4 = lv3.end();
                  if (this.cancelled.get()) {
                     lv4.release();
                     return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
                  } else {
                     CompletableFuture completableFuture = ChunkBuilder.this.scheduleUpload(lv4, BuiltChunk.this.getBuffer(RenderLayer.getTranslucent())).thenApply((void_) -> {
                        return ChunkBuilder.Result.CANCELLED;
                     });
                     return completableFuture.handle((result, throwable) -> {
                        if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                           MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Rendering chunk"));
                        }

                        return this.cancelled.get() ? ChunkBuilder.Result.CANCELLED : ChunkBuilder.Result.SUCCESSFUL;
                     });
                  }
               } else {
                  return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
               }
            }
         }

         public void cancel() {
            this.cancelled.set(true);
         }
      }

      @Environment(EnvType.CLIENT)
      private abstract class Task implements Comparable {
         protected final double distance;
         protected final AtomicBoolean cancelled = new AtomicBoolean(false);
         protected final boolean prioritized;

         public Task(double distance, boolean prioritized) {
            this.distance = distance;
            this.prioritized = prioritized;
         }

         public abstract CompletableFuture run(BlockBufferBuilderStorage buffers);

         public abstract void cancel();

         protected abstract String getName();

         public int compareTo(Task arg) {
            return Doubles.compare(this.distance, arg.distance);
         }

         // $FF: synthetic method
         public int compareTo(Object other) {
            return this.compareTo((Task)other);
         }
      }

      @Environment(EnvType.CLIENT)
      private class RebuildTask extends Task {
         @Nullable
         protected ChunkRendererRegion region;

         public RebuildTask(double distance, @Nullable ChunkRendererRegion region, boolean prioritized) {
            super(distance, prioritized);
            this.region = region;
         }

         protected String getName() {
            return "rend_chk_rebuild";
         }

         public CompletableFuture run(BlockBufferBuilderStorage buffers) {
            if (this.cancelled.get()) {
               return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
            } else if (!BuiltChunk.this.shouldBuild()) {
               this.region = null;
               BuiltChunk.this.scheduleRebuild(false);
               this.cancelled.set(true);
               return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
            } else if (this.cancelled.get()) {
               return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
            } else {
               Vec3d lv = ChunkBuilder.this.getCameraPosition();
               float f = (float)lv.x;
               float g = (float)lv.y;
               float h = (float)lv.z;
               RenderData lv2 = this.render(f, g, h, buffers);
               BuiltChunk.this.setNoCullingBlockEntities(lv2.noCullingBlockEntities);
               if (this.cancelled.get()) {
                  lv2.field_39081.values().forEach(BufferBuilder.BuiltBuffer::release);
                  return CompletableFuture.completedFuture(ChunkBuilder.Result.CANCELLED);
               } else {
                  ChunkData lv3 = new ChunkData();
                  lv3.occlusionGraph = lv2.chunkOcclusionData;
                  lv3.blockEntities.addAll(lv2.blockEntities);
                  lv3.transparentSortingData = lv2.translucencySortingData;
                  List list = Lists.newArrayList();
                  lv2.field_39081.forEach((arg2, arg3) -> {
                     list.add(ChunkBuilder.this.scheduleUpload(arg3, BuiltChunk.this.getBuffer(arg2)));
                     lv3.nonEmptyLayers.add(arg2);
                  });
                  return Util.combine(list).handle((results, throwable) -> {
                     if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Rendering chunk"));
                     }

                     if (this.cancelled.get()) {
                        return ChunkBuilder.Result.CANCELLED;
                     } else {
                        BuiltChunk.this.data.set(lv3);
                        BuiltChunk.this.field_36374.set(0);
                        ChunkBuilder.this.worldRenderer.addBuiltChunk(BuiltChunk.this);
                        return ChunkBuilder.Result.SUCCESSFUL;
                     }
                  });
               }
            }
         }

         private RenderData render(float cameraX, float cameraY, float cameraZ, BlockBufferBuilderStorage arg) {
            RenderData lv = new RenderData();
            int i = true;
            BlockPos lv2 = BuiltChunk.this.origin.toImmutable();
            BlockPos lv3 = lv2.add(15, 15, 15);
            ChunkOcclusionDataBuilder lv4 = new ChunkOcclusionDataBuilder();
            ChunkRendererRegion lv5 = this.region;
            this.region = null;
            MatrixStack lv6 = new MatrixStack();
            if (lv5 != null) {
               BlockModelRenderer.enableBrightnessCache();
               Set set = new ReferenceArraySet(RenderLayer.getBlockLayers().size());
               Random lv7 = Random.create();
               BlockRenderManager lv8 = MinecraftClient.getInstance().getBlockRenderManager();
               Iterator var15 = BlockPos.iterate(lv2, lv3).iterator();

               while(var15.hasNext()) {
                  BlockPos lv9 = (BlockPos)var15.next();
                  BlockState lv10 = lv5.getBlockState(lv9);
                  if (lv10.isOpaqueFullCube(lv5, lv9)) {
                     lv4.markClosed(lv9);
                  }

                  if (lv10.hasBlockEntity()) {
                     BlockEntity lv11 = lv5.getBlockEntity(lv9);
                     if (lv11 != null) {
                        this.addBlockEntity(lv, lv11);
                     }
                  }

                  BlockState lv12 = lv5.getBlockState(lv9);
                  FluidState lv13 = lv12.getFluidState();
                  RenderLayer lv14;
                  BufferBuilder lv15;
                  if (!lv13.isEmpty()) {
                     lv14 = RenderLayers.getFluidLayer(lv13);
                     lv15 = arg.get(lv14);
                     if (set.add(lv14)) {
                        BuiltChunk.this.beginBufferBuilding(lv15);
                     }

                     lv8.renderFluid(lv9, lv5, lv15, lv12, lv13);
                  }

                  if (lv10.getRenderType() != BlockRenderType.INVISIBLE) {
                     lv14 = RenderLayers.getBlockLayer(lv10);
                     lv15 = arg.get(lv14);
                     if (set.add(lv14)) {
                        BuiltChunk.this.beginBufferBuilding(lv15);
                     }

                     lv6.push();
                     lv6.translate((float)(lv9.getX() & 15), (float)(lv9.getY() & 15), (float)(lv9.getZ() & 15));
                     lv8.renderBlock(lv10, lv9, lv5, lv6, lv15, true, lv7);
                     lv6.pop();
                  }
               }

               if (set.contains(RenderLayer.getTranslucent())) {
                  BufferBuilder lv16 = arg.get(RenderLayer.getTranslucent());
                  if (!lv16.isBatchEmpty()) {
                     lv16.setSorter(VertexSorter.byDistance(cameraX - (float)lv2.getX(), cameraY - (float)lv2.getY(), cameraZ - (float)lv2.getZ()));
                     lv.translucencySortingData = lv16.getSortingData();
                  }
               }

               var15 = set.iterator();

               while(var15.hasNext()) {
                  RenderLayer lv17 = (RenderLayer)var15.next();
                  BufferBuilder.BuiltBuffer lv18 = arg.get(lv17).endNullable();
                  if (lv18 != null) {
                     lv.field_39081.put(lv17, lv18);
                  }
               }

               BlockModelRenderer.disableBrightnessCache();
            }

            lv.chunkOcclusionData = lv4.build();
            return lv;
         }

         private void addBlockEntity(RenderData renderData, BlockEntity blockEntity) {
            BlockEntityRenderer lv = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(blockEntity);
            if (lv != null) {
               renderData.blockEntities.add(blockEntity);
               if (lv.rendersOutsideBoundingBox(blockEntity)) {
                  renderData.noCullingBlockEntities.add(blockEntity);
               }
            }

         }

         public void cancel() {
            this.region = null;
            if (this.cancelled.compareAndSet(false, true)) {
               BuiltChunk.this.scheduleRebuild(false);
            }

         }

         @Environment(EnvType.CLIENT)
         private static final class RenderData {
            public final List noCullingBlockEntities = new ArrayList();
            public final List blockEntities = new ArrayList();
            public final Map field_39081 = new Reference2ObjectArrayMap();
            public ChunkOcclusionData chunkOcclusionData = new ChunkOcclusionData();
            @Nullable
            public BufferBuilder.TransparentSortingData translucencySortingData;

            RenderData() {
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static enum Result {
      SUCCESSFUL,
      CANCELLED;

      // $FF: synthetic method
      private static Result[] method_36923() {
         return new Result[]{SUCCESSFUL, CANCELLED};
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ChunkData {
      public static final ChunkData EMPTY = new ChunkData() {
         public boolean isVisibleThrough(Direction from, Direction to) {
            return false;
         }
      };
      final Set nonEmptyLayers = new ObjectArraySet(RenderLayer.getBlockLayers().size());
      final List blockEntities = Lists.newArrayList();
      ChunkOcclusionData occlusionGraph = new ChunkOcclusionData();
      @Nullable
      BufferBuilder.TransparentSortingData transparentSortingData;

      public boolean isEmpty() {
         return this.nonEmptyLayers.isEmpty();
      }

      public boolean isEmpty(RenderLayer layer) {
         return !this.nonEmptyLayers.contains(layer);
      }

      public List getBlockEntities() {
         return this.blockEntities;
      }

      public boolean isVisibleThrough(Direction from, Direction to) {
         return this.occlusionGraph.isVisibleThrough(from, to);
      }
   }
}
