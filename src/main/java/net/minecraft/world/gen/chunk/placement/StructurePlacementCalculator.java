package net.minecraft.world.gen.chunk.placement;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructurePlacementCalculator {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final NoiseConfig noiseConfig;
   private final BiomeSource biomeSource;
   private final long structureSeed;
   private final long concentricRingSeed;
   private final Map structuresToPlacements = new Object2ObjectOpenHashMap();
   private final Map concentricPlacementsToPositions = new Object2ObjectArrayMap();
   private boolean calculated;
   private final List structureSets;

   public static StructurePlacementCalculator create(NoiseConfig noiseConfig, long seed, BiomeSource biomeSource, Stream structureSets) {
      List list = structureSets.filter((structureSet) -> {
         return hasValidBiome((StructureSet)structureSet.value(), biomeSource);
      }).toList();
      return new StructurePlacementCalculator(noiseConfig, biomeSource, seed, 0L, list);
   }

   public static StructurePlacementCalculator create(NoiseConfig noiseConfig, long seed, BiomeSource biomeSource, RegistryWrapper structureSetRegistry) {
      List list = (List)structureSetRegistry.streamEntries().filter((structureSet) -> {
         return hasValidBiome((StructureSet)structureSet.value(), biomeSource);
      }).collect(Collectors.toUnmodifiableList());
      return new StructurePlacementCalculator(noiseConfig, biomeSource, seed, seed, list);
   }

   private static boolean hasValidBiome(StructureSet structureSet, BiomeSource biomeSource) {
      Stream stream = structureSet.structures().stream().flatMap((structure) -> {
         Structure lv = (Structure)structure.structure().value();
         return lv.getValidBiomes().stream();
      });
      Set var10001 = biomeSource.getBiomes();
      Objects.requireNonNull(var10001);
      return stream.anyMatch(var10001::contains);
   }

   private StructurePlacementCalculator(NoiseConfig noiseConfig, BiomeSource biomeSource, long structureSeed, long concentricRingSeed, List structureSets) {
      this.noiseConfig = noiseConfig;
      this.structureSeed = structureSeed;
      this.biomeSource = biomeSource;
      this.concentricRingSeed = concentricRingSeed;
      this.structureSets = structureSets;
   }

   public List getStructureSets() {
      return this.structureSets;
   }

   private void calculate() {
      Set set = this.biomeSource.getBiomes();
      this.getStructureSets().forEach((structureSet) -> {
         StructureSet lv = (StructureSet)structureSet.value();
         boolean bl = false;
         Iterator var5 = lv.structures().iterator();

         while(var5.hasNext()) {
            StructureSet.WeightedEntry lv2 = (StructureSet.WeightedEntry)var5.next();
            Structure lv3 = (Structure)lv2.structure().value();
            Stream var10000 = lv3.getValidBiomes().stream();
            Objects.requireNonNull(set);
            if (var10000.anyMatch(set::contains)) {
               ((List)this.structuresToPlacements.computeIfAbsent(lv3, (structure) -> {
                  return new ArrayList();
               })).add(lv.placement());
               bl = true;
            }
         }

         if (bl) {
            StructurePlacement lv4 = lv.placement();
            if (lv4 instanceof ConcentricRingsStructurePlacement) {
               ConcentricRingsStructurePlacement lv5 = (ConcentricRingsStructurePlacement)lv4;
               this.concentricPlacementsToPositions.put(lv5, this.calculateConcentricsRingPlacementPos(structureSet, lv5));
            }
         }

      });
   }

   private CompletableFuture calculateConcentricsRingPlacementPos(RegistryEntry structureSetEntry, ConcentricRingsStructurePlacement placement) {
      if (placement.getCount() == 0) {
         return CompletableFuture.completedFuture(List.of());
      } else {
         Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
         int i = placement.getDistance();
         int j = placement.getCount();
         List list = new ArrayList(j);
         int k = placement.getSpread();
         RegistryEntryList lv = placement.getPreferredBiomes();
         Random lv2 = Random.create();
         lv2.setSeed(this.concentricRingSeed);
         double d = lv2.nextDouble() * Math.PI * 2.0;
         int l = 0;
         int m = 0;

         for(int n = 0; n < j; ++n) {
            double e = (double)(4 * i + i * m * 6) + (lv2.nextDouble() - 0.5) * (double)i * 2.5;
            int o = (int)Math.round(Math.cos(d) * e);
            int p = (int)Math.round(Math.sin(d) * e);
            Random lv3 = lv2.split();
            list.add(CompletableFuture.supplyAsync(() -> {
               BiomeSource var10000 = this.biomeSource;
               int var10001 = ChunkSectionPos.getOffsetPos(o, 8);
               int var10003 = ChunkSectionPos.getOffsetPos(p, 8);
               Objects.requireNonNull(lv);
               Pair pair = var10000.locateBiome(var10001, 0, var10003, 112, lv::contains, lv3, this.noiseConfig.getMultiNoiseSampler());
               if (pair != null) {
                  BlockPos lvx = (BlockPos)pair.getFirst();
                  return new ChunkPos(ChunkSectionPos.getSectionCoord(lvx.getX()), ChunkSectionPos.getSectionCoord(lvx.getZ()));
               } else {
                  return new ChunkPos(o, p);
               }
            }, Util.getMainWorkerExecutor()));
            d += 6.283185307179586 / (double)k;
            ++l;
            if (l == k) {
               ++m;
               l = 0;
               k += 2 * k / (m + 1);
               k = Math.min(k, j - n);
               d += lv2.nextDouble() * Math.PI * 2.0;
            }
         }

         return Util.combineSafe(list).thenApply((positions) -> {
            double d = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            LOGGER.debug("Calculation for {} took {}s", structureSetEntry, d);
            return positions;
         });
      }
   }

   public void tryCalculate() {
      if (!this.calculated) {
         this.calculate();
         this.calculated = true;
      }

   }

   @Nullable
   public List getPlacementPositions(ConcentricRingsStructurePlacement placement) {
      this.tryCalculate();
      CompletableFuture completableFuture = (CompletableFuture)this.concentricPlacementsToPositions.get(placement);
      return completableFuture != null ? (List)completableFuture.join() : null;
   }

   public List getPlacements(RegistryEntry structureEntry) {
      this.tryCalculate();
      return (List)this.structuresToPlacements.getOrDefault(structureEntry.value(), List.of());
   }

   public NoiseConfig getNoiseConfig() {
      return this.noiseConfig;
   }

   public boolean canGenerate(RegistryEntry structureSetEntry, int centerChunkX, int centerChunkZ, int chunkCount) {
      StructurePlacement lv = ((StructureSet)structureSetEntry.value()).placement();

      for(int l = centerChunkX - chunkCount; l <= centerChunkX + chunkCount; ++l) {
         for(int m = centerChunkZ - chunkCount; m <= centerChunkZ + chunkCount; ++m) {
            if (lv.shouldGenerate(this, l, m)) {
               return true;
            }
         }
      }

      return false;
   }

   public long getStructureSeed() {
      return this.structureSeed;
   }
}
