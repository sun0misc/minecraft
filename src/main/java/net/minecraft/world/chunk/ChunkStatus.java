package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.jfr.Finishable;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class ChunkStatus {
   public static final int field_35470 = 8;
   private static final EnumSet PRE_CARVER_HEIGHTMAPS;
   public static final EnumSet POST_CARVER_HEIGHTMAPS;
   private static final LoadTask STATUS_BUMP_LOAD_TASK;
   public static final ChunkStatus EMPTY;
   public static final ChunkStatus STRUCTURE_STARTS;
   public static final ChunkStatus STRUCTURE_REFERENCES;
   public static final ChunkStatus BIOMES;
   public static final ChunkStatus NOISE;
   public static final ChunkStatus SURFACE;
   public static final ChunkStatus CARVERS;
   public static final ChunkStatus LIQUID_CARVERS;
   public static final ChunkStatus FEATURES;
   public static final ChunkStatus LIGHT;
   public static final ChunkStatus SPAWN;
   public static final ChunkStatus HEIGHTMAPS;
   public static final ChunkStatus FULL;
   private static final List DISTANCE_TO_STATUS;
   private static final IntList STATUS_TO_DISTANCE;
   private final String id;
   private final int index;
   private final ChunkStatus previous;
   private final GenerationTask generationTask;
   private final LoadTask loadTask;
   private final int taskMargin;
   private final ChunkType chunkType;
   private final EnumSet heightMapTypes;

   private static CompletableFuture getLightingFuture(ChunkStatus status, ServerLightingProvider lightingProvider, Chunk chunk) {
      boolean bl = shouldExcludeBlockLight(status, chunk);
      if (!chunk.getStatus().isAtLeast(status)) {
         ((ProtoChunk)chunk).setStatus(status);
      }

      return lightingProvider.light(chunk, bl).thenApply(Either::left);
   }

   private static ChunkStatus register(String id, @Nullable ChunkStatus previous, int taskMargin, EnumSet heightMapTypes, ChunkType chunkType, SimpleGenerationTask task) {
      return register(id, previous, taskMargin, heightMapTypes, chunkType, (GenerationTask)task);
   }

   private static ChunkStatus register(String id, @Nullable ChunkStatus previous, int taskMargin, EnumSet heightMapTypes, ChunkType chunkType, GenerationTask task) {
      return register(id, previous, taskMargin, heightMapTypes, chunkType, task, STATUS_BUMP_LOAD_TASK);
   }

   private static ChunkStatus register(String id, @Nullable ChunkStatus previous, int taskMargin, EnumSet heightMapTypes, ChunkType chunkType, GenerationTask task, LoadTask loadTask) {
      return (ChunkStatus)Registry.register(Registries.CHUNK_STATUS, (String)id, new ChunkStatus(id, previous, taskMargin, heightMapTypes, chunkType, task, loadTask));
   }

   public static List createOrderedList() {
      List list = Lists.newArrayList();

      ChunkStatus lv;
      for(lv = FULL; lv.getPrevious() != lv; lv = lv.getPrevious()) {
         list.add(lv);
      }

      list.add(lv);
      Collections.reverse(list);
      return list;
   }

   private static boolean shouldExcludeBlockLight(ChunkStatus status, Chunk chunk) {
      return chunk.getStatus().isAtLeast(status) && chunk.isLightOn();
   }

   public static ChunkStatus byDistanceFromFull(int level) {
      if (level >= DISTANCE_TO_STATUS.size()) {
         return EMPTY;
      } else {
         return level < 0 ? FULL : (ChunkStatus)DISTANCE_TO_STATUS.get(level);
      }
   }

   public static int getMaxDistanceFromFull() {
      return DISTANCE_TO_STATUS.size();
   }

   public static int getDistanceFromFull(ChunkStatus status) {
      return STATUS_TO_DISTANCE.getInt(status.getIndex());
   }

   ChunkStatus(String id, @Nullable ChunkStatus previous, int taskMargin, EnumSet heightMapTypes, ChunkType chunkType, GenerationTask generationTask, LoadTask loadTask) {
      this.id = id;
      this.previous = previous == null ? this : previous;
      this.generationTask = generationTask;
      this.loadTask = loadTask;
      this.taskMargin = taskMargin;
      this.chunkType = chunkType;
      this.heightMapTypes = heightMapTypes;
      this.index = previous == null ? 0 : previous.getIndex() + 1;
   }

   public int getIndex() {
      return this.index;
   }

   public String getId() {
      return this.id;
   }

   public ChunkStatus getPrevious() {
      return this.previous;
   }

   public CompletableFuture runGenerationTask(Executor executor, ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureTemplateManager, ServerLightingProvider lightingProvider, Function fullChunkConverter, List chunks, boolean regenerate) {
      Chunk lv = (Chunk)chunks.get(chunks.size() / 2);
      Finishable lv2 = FlightProfiler.INSTANCE.startChunkGenerationProfiling(lv.getPos(), world.getRegistryKey(), this.id);
      CompletableFuture completableFuture = this.generationTask.doWork(this, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, lv, regenerate);
      return lv2 != null ? completableFuture.thenApply((either) -> {
         lv2.finish();
         return either;
      }) : completableFuture;
   }

   public CompletableFuture runLoadTask(ServerWorld world, StructureTemplateManager structureTemplateManager, ServerLightingProvider lightingProvider, Function fullChunkConverter, Chunk chunk) {
      return this.loadTask.doWork(this, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk);
   }

   public int getTaskMargin() {
      return this.taskMargin;
   }

   public ChunkType getChunkType() {
      return this.chunkType;
   }

   public static ChunkStatus byId(String id) {
      return (ChunkStatus)Registries.CHUNK_STATUS.get(Identifier.tryParse(id));
   }

   public EnumSet getHeightmapTypes() {
      return this.heightMapTypes;
   }

   public boolean isAtLeast(ChunkStatus chunkStatus) {
      return this.getIndex() >= chunkStatus.getIndex();
   }

   public String toString() {
      return Registries.CHUNK_STATUS.getId(this).toString();
   }

   static {
      PRE_CARVER_HEIGHTMAPS = EnumSet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG);
      POST_CARVER_HEIGHTMAPS = EnumSet.of(Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE, Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);
      STATUS_BUMP_LOAD_TASK = (targetStatus, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk) -> {
         if (chunk instanceof ProtoChunk lv) {
            if (!chunk.getStatus().isAtLeast(targetStatus)) {
               lv.setStatus(targetStatus);
            }
         }

         return CompletableFuture.completedFuture(Either.left(chunk));
      };
      EMPTY = register("empty", (ChunkStatus)null, -1, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
      }));
      STRUCTURE_STARTS = register("structure_starts", EMPTY, 0, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk, regenerate) -> {
         if (!chunk.getStatus().isAtLeast(targetStatus)) {
            if (world.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures()) {
               generator.setStructureStarts(world.getRegistryManager(), world.getChunkManager().getStructurePlacementCalculator(), world.getStructureAccessor(), chunk, structureTemplateManager);
            }

            if (chunk instanceof ProtoChunk) {
               ProtoChunk lv = (ProtoChunk)chunk;
               lv.setStatus(targetStatus);
            }

            world.cacheStructures(chunk);
         }

         return CompletableFuture.completedFuture(Either.left(chunk));
      }, (targetStatus, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk) -> {
         if (!chunk.getStatus().isAtLeast(targetStatus)) {
            if (chunk instanceof ProtoChunk) {
               ProtoChunk lv = (ProtoChunk)chunk;
               lv.setStatus(targetStatus);
            }

            world.cacheStructures(chunk);
         }

         return CompletableFuture.completedFuture(Either.left(chunk));
      });
      STRUCTURE_REFERENCES = register("structure_references", STRUCTURE_STARTS, 8, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
         ChunkRegion lv = new ChunkRegion(world, chunks, targetStatus, -1);
         generator.addStructureReferences(lv, world.getStructureAccessor().forRegion(lv), chunk);
      }));
      BIOMES = register("biomes", STRUCTURE_REFERENCES, 8, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (GenerationTask)((targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk, regenerate) -> {
         if (!regenerate && chunk.getStatus().isAtLeast(targetStatus)) {
            return CompletableFuture.completedFuture(Either.left(chunk));
         } else {
            ChunkRegion lv = new ChunkRegion(world, chunks, targetStatus, -1);
            return generator.populateBiomes(executor, world.getChunkManager().getNoiseConfig(), Blender.getBlender(lv), world.getStructureAccessor().forRegion(lv), chunk).thenApply((chunkx) -> {
               if (chunkx instanceof ProtoChunk) {
                  ((ProtoChunk)chunkx).setStatus(targetStatus);
               }

               return Either.left(chunkx);
            });
         }
      }));
      NOISE = register("noise", BIOMES, 8, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (GenerationTask)((targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk, regenerate) -> {
         if (!regenerate && chunk.getStatus().isAtLeast(targetStatus)) {
            return CompletableFuture.completedFuture(Either.left(chunk));
         } else {
            ChunkRegion lv = new ChunkRegion(world, chunks, targetStatus, 0);
            return generator.populateNoise(executor, Blender.getBlender(lv), world.getChunkManager().getNoiseConfig(), world.getStructureAccessor().forRegion(lv), chunk).thenApply((chunkx) -> {
               if (chunkx instanceof ProtoChunk lv) {
                  BelowZeroRetrogen lv2 = lv.getBelowZeroRetrogen();
                  if (lv2 != null) {
                     BelowZeroRetrogen.replaceOldBedrock(lv);
                     if (lv2.hasMissingBedrock()) {
                        lv2.fillColumnsWithAirIfMissingBedrock(lv);
                     }
                  }

                  lv.setStatus(targetStatus);
               }

               return Either.left(chunkx);
            });
         }
      }));
      SURFACE = register("surface", NOISE, 8, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
         ChunkRegion lv = new ChunkRegion(world, chunks, targetStatus, 0);
         generator.buildSurface(lv, world.getStructureAccessor().forRegion(lv), world.getChunkManager().getNoiseConfig(), chunk);
      }));
      CARVERS = register("carvers", SURFACE, 8, PRE_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
         ChunkRegion lv = new ChunkRegion(world, chunks, targetStatus, 0);
         if (chunk instanceof ProtoChunk lv2) {
            Blender.createCarvingMasks(lv, lv2);
         }

         generator.carve(lv, world.getSeed(), world.getChunkManager().getNoiseConfig(), world.getBiomeAccess(), world.getStructureAccessor().forRegion(lv), chunk, GenerationStep.Carver.AIR);
      }));
      LIQUID_CARVERS = register("liquid_carvers", CARVERS, 8, POST_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
      }));
      FEATURES = register("features", LIQUID_CARVERS, 8, POST_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk, regenerate) -> {
         ProtoChunk lv = (ProtoChunk)chunk;
         lv.setLightingProvider(lightingProvider);
         if (regenerate || !chunk.getStatus().isAtLeast(targetStatus)) {
            Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
            ChunkRegion lv2 = new ChunkRegion(world, chunks, targetStatus, 1);
            generator.generateFeatures(lv2, chunk, world.getStructureAccessor().forRegion(lv2));
            Blender.tickLeavesAndFluids(lv2, chunk);
            lv.setStatus(targetStatus);
         }

         return lightingProvider.retainData(chunk).thenApply(Either::left);
      }, (status, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk) -> {
         return lightingProvider.retainData(chunk).thenApply(Either::left);
      });
      LIGHT = register("light", FEATURES, 1, POST_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk, regenerate) -> {
         return getLightingFuture(targetStatus, lightingProvider, chunk);
      }, (targetStatus, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk) -> {
         return getLightingFuture(targetStatus, lightingProvider, chunk);
      });
      SPAWN = register("spawn", LIGHT, 0, POST_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
         if (!chunk.hasBelowZeroRetrogen()) {
            generator.populateEntities(new ChunkRegion(world, chunks, targetStatus, -1));
         }

      }));
      HEIGHTMAPS = register("heightmaps", SPAWN, 0, POST_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.PROTOCHUNK, (SimpleGenerationTask)((targetStatus, world, generator, chunks, chunk) -> {
      }));
      FULL = register("full", HEIGHTMAPS, 0, POST_CARVER_HEIGHTMAPS, ChunkStatus.ChunkType.LEVELCHUNK, (targetStatus, executor, world, generator, structureTemplateManager, lightingProvider, fullChunkConverter, chunks, chunk, regenerate) -> {
         return (CompletableFuture)fullChunkConverter.apply(chunk);
      }, (targetStatus, world, structureTemplateManager, lightingProvider, fullChunkConverter, chunk) -> {
         return (CompletableFuture)fullChunkConverter.apply(chunk);
      });
      DISTANCE_TO_STATUS = ImmutableList.of(FULL, FEATURES, LIQUID_CARVERS, BIOMES, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, new ChunkStatus[0]);
      STATUS_TO_DISTANCE = (IntList)Util.make(new IntArrayList(createOrderedList().size()), (statusToDistance) -> {
         int i = 0;

         for(int j = createOrderedList().size() - 1; j >= 0; --j) {
            while(i + 1 < DISTANCE_TO_STATUS.size() && j <= ((ChunkStatus)DISTANCE_TO_STATUS.get(i + 1)).getIndex()) {
               ++i;
            }

            statusToDistance.add(0, i);
         }

      });
   }

   public static enum ChunkType {
      PROTOCHUNK,
      LEVELCHUNK;

      // $FF: synthetic method
      private static ChunkType[] method_36741() {
         return new ChunkType[]{PROTOCHUNK, LEVELCHUNK};
      }
   }

   private interface GenerationTask {
      CompletableFuture doWork(ChunkStatus targetStatus, Executor executor, ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureTemplateManager, ServerLightingProvider lightingProvider, Function fullChunkConverter, List chunks, Chunk chunk, boolean regenerate);
   }

   interface LoadTask {
      CompletableFuture doWork(ChunkStatus targetStatus, ServerWorld world, StructureTemplateManager structureTemplateManager, ServerLightingProvider lightingProvider, Function fullChunkConverter, Chunk chunk);
   }

   interface SimpleGenerationTask extends GenerationTask {
      default CompletableFuture doWork(ChunkStatus arg, Executor executor, ServerWorld arg2, ChunkGenerator arg3, StructureTemplateManager arg4, ServerLightingProvider arg5, Function function, List list, Chunk arg6, boolean bl) {
         if (bl || !arg6.getStatus().isAtLeast(arg)) {
            this.doWork(arg, arg2, arg3, list, arg6);
            if (arg6 instanceof ProtoChunk) {
               ProtoChunk lv = (ProtoChunk)arg6;
               lv.setStatus(arg);
            }
         }

         return CompletableFuture.completedFuture(Either.left(arg6));
      }

      void doWork(ChunkStatus targetStatus, ServerWorld world, ChunkGenerator chunkGenerator, List chunks, Chunk chunk);
   }
}
