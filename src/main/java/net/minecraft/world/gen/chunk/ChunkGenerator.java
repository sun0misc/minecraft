package net.minecraft.world.gen.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkGenerator {
   public static final Codec CODEC;
   protected final BiomeSource biomeSource;
   private final Supplier indexedFeaturesListSupplier;
   private final Function generationSettingsGetter;

   public ChunkGenerator(BiomeSource biomeSource) {
      this(biomeSource, (biomeEntry) -> {
         return ((Biome)biomeEntry.value()).getGenerationSettings();
      });
   }

   public ChunkGenerator(BiomeSource biomeSource, Function generationSettingsGetter) {
      this.biomeSource = biomeSource;
      this.generationSettingsGetter = generationSettingsGetter;
      this.indexedFeaturesListSupplier = Suppliers.memoize(() -> {
         return PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), (biomeEntry) -> {
            return ((GenerationSettings)generationSettingsGetter.apply(biomeEntry)).getFeatures();
         }, true);
      });
   }

   protected abstract Codec getCodec();

   public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper structureSetRegistry, NoiseConfig noiseConfig, long seed) {
      return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, structureSetRegistry);
   }

   public Optional getCodecKey() {
      return Registries.CHUNK_GENERATOR.getKey(this.getCodec());
   }

   public CompletableFuture populateBiomes(Executor executor, NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
      return CompletableFuture.supplyAsync(Util.debugSupplier("init_biomes", () -> {
         chunk.populateBiomes(this.biomeSource, noiseConfig.getMultiNoiseSampler());
         return chunk;
      }), Util.getMainWorkerExecutor());
   }

   public abstract void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep);

   @Nullable
   public Pair locateStructure(ServerWorld world, RegistryEntryList structures, BlockPos center, int radius, boolean skipReferencedStructures) {
      StructurePlacementCalculator lv = world.getChunkManager().getStructurePlacementCalculator();
      Map map = new Object2ObjectArrayMap();
      Iterator var8 = structures.iterator();

      while(var8.hasNext()) {
         RegistryEntry lv2 = (RegistryEntry)var8.next();
         Iterator var10 = lv.getPlacements(lv2).iterator();

         while(var10.hasNext()) {
            StructurePlacement lv3 = (StructurePlacement)var10.next();
            ((Set)map.computeIfAbsent(lv3, (placement) -> {
               return new ObjectArraySet();
            })).add(lv2);
         }
      }

      if (map.isEmpty()) {
         return null;
      } else {
         Pair pair = null;
         double d = Double.MAX_VALUE;
         StructureAccessor lv4 = world.getStructureAccessor();
         List list = new ArrayList(map.size());
         Iterator var13 = map.entrySet().iterator();

         while(var13.hasNext()) {
            Map.Entry entry = (Map.Entry)var13.next();
            StructurePlacement lv5 = (StructurePlacement)entry.getKey();
            if (lv5 instanceof ConcentricRingsStructurePlacement) {
               ConcentricRingsStructurePlacement lv6 = (ConcentricRingsStructurePlacement)lv5;
               Pair pair2 = this.locateConcentricRingsStructure((Set)entry.getValue(), world, lv4, center, skipReferencedStructures, lv6);
               if (pair2 != null) {
                  BlockPos lv7 = (BlockPos)pair2.getFirst();
                  double e = center.getSquaredDistance(lv7);
                  if (e < d) {
                     d = e;
                     pair = pair2;
                  }
               }
            } else if (lv5 instanceof RandomSpreadStructurePlacement) {
               list.add(entry);
            }
         }

         if (!list.isEmpty()) {
            int j = ChunkSectionPos.getSectionCoord(center.getX());
            int k = ChunkSectionPos.getSectionCoord(center.getZ());

            for(int l = 0; l <= radius; ++l) {
               boolean bl2 = false;
               Iterator var30 = list.iterator();

               while(var30.hasNext()) {
                  Map.Entry entry2 = (Map.Entry)var30.next();
                  RandomSpreadStructurePlacement lv8 = (RandomSpreadStructurePlacement)entry2.getKey();
                  Pair pair3 = locateRandomSpreadStructure((Set)entry2.getValue(), world, lv4, j, k, l, skipReferencedStructures, lv.getStructureSeed(), lv8);
                  if (pair3 != null) {
                     bl2 = true;
                     double f = center.getSquaredDistance((Vec3i)pair3.getFirst());
                     if (f < d) {
                        d = f;
                        pair = pair3;
                     }
                  }
               }

               if (bl2) {
                  return pair;
               }
            }
         }

         return pair;
      }
   }

   @Nullable
   private Pair locateConcentricRingsStructure(Set structures, ServerWorld world, StructureAccessor structureAccessor, BlockPos center, boolean skipReferencedStructures, ConcentricRingsStructurePlacement placement) {
      List list = world.getChunkManager().getStructurePlacementCalculator().getPlacementPositions(placement);
      if (list == null) {
         throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
      } else {
         Pair pair = null;
         double d = Double.MAX_VALUE;
         BlockPos.Mutable lv = new BlockPos.Mutable();
         Iterator var12 = list.iterator();

         while(var12.hasNext()) {
            ChunkPos lv2 = (ChunkPos)var12.next();
            lv.set(ChunkSectionPos.getOffsetPos(lv2.x, 8), 32, ChunkSectionPos.getOffsetPos(lv2.z, 8));
            double e = lv.getSquaredDistance(center);
            boolean bl2 = pair == null || e < d;
            if (bl2) {
               Pair pair2 = locateStructure(structures, world, structureAccessor, skipReferencedStructures, placement, lv2);
               if (pair2 != null) {
                  pair = pair2;
                  d = e;
               }
            }
         }

         return pair;
      }
   }

   @Nullable
   private static Pair locateRandomSpreadStructure(Set structures, WorldView world, StructureAccessor structureAccessor, int centerChunkX, int centerChunkZ, int radius, boolean skipReferencedStructures, long seed, RandomSpreadStructurePlacement placement) {
      int m = placement.getSpacing();

      for(int n = -radius; n <= radius; ++n) {
         boolean bl2 = n == -radius || n == radius;

         for(int o = -radius; o <= radius; ++o) {
            boolean bl3 = o == -radius || o == radius;
            if (bl2 || bl3) {
               int p = centerChunkX + m * n;
               int q = centerChunkZ + m * o;
               ChunkPos lv = placement.getStartChunk(seed, p, q);
               Pair pair = locateStructure(structures, world, structureAccessor, skipReferencedStructures, placement, lv);
               if (pair != null) {
                  return pair;
               }
            }
         }
      }

      return null;
   }

   @Nullable
   private static Pair locateStructure(Set structures, WorldView world, StructureAccessor structureAccessor, boolean skipReferencedStructures, StructurePlacement placement, ChunkPos pos) {
      Iterator var6 = structures.iterator();

      RegistryEntry lv;
      StructureStart lv4;
      do {
         do {
            do {
               StructurePresence lv2;
               do {
                  if (!var6.hasNext()) {
                     return null;
                  }

                  lv = (RegistryEntry)var6.next();
                  lv2 = structureAccessor.getStructurePresence(pos, (Structure)lv.value(), skipReferencedStructures);
               } while(lv2 == StructurePresence.START_NOT_PRESENT);

               if (!skipReferencedStructures && lv2 == StructurePresence.START_PRESENT) {
                  return Pair.of(placement.getLocatePos(pos), lv);
               }

               Chunk lv3 = world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS);
               lv4 = structureAccessor.getStructureStart(ChunkSectionPos.from(lv3), (Structure)lv.value(), lv3);
            } while(lv4 == null);
         } while(!lv4.hasChildren());
      } while(skipReferencedStructures && !checkNotReferenced(structureAccessor, lv4));

      return Pair.of(placement.getLocatePos(lv4.getPos()), lv);
   }

   private static boolean checkNotReferenced(StructureAccessor structureAccessor, StructureStart start) {
      if (start.isNeverReferenced()) {
         structureAccessor.incrementReferences(start);
         return true;
      } else {
         return false;
      }
   }

   public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
      ChunkPos lv = chunk.getPos();
      if (!SharedConstants.isOutsideGenerationArea(lv)) {
         ChunkSectionPos lv2 = ChunkSectionPos.from(lv, world.getBottomSectionCoord());
         BlockPos lv3 = lv2.getMinPos();
         Registry lv4 = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
         Map map = (Map)lv4.stream().collect(Collectors.groupingBy((structureType) -> {
            return structureType.getFeatureGenerationStep().ordinal();
         }));
         List list = (List)this.indexedFeaturesListSupplier.get();
         ChunkRandom lv5 = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
         long l = lv5.setPopulationSeed(world.getSeed(), lv3.getX(), lv3.getZ());
         Set set = new ObjectArraySet();
         ChunkPos.stream(lv2.toChunkPos(), 1).forEach((arg2) -> {
            Chunk lv = world.getChunk(arg2.x, arg2.z);
            ChunkSection[] var4 = lv.getSectionArray();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               ChunkSection lv2 = var4[var6];
               ReadableContainer var10000 = lv2.getBiomeContainer();
               Objects.requireNonNull(set);
               var10000.forEachValue(set::add);
            }

         });
         set.retainAll(this.biomeSource.getBiomes());
         int i = list.size();

         try {
            Registry lv6 = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE);
            int j = Math.max(GenerationStep.Feature.values().length, i);

            for(int k = 0; k < j; ++k) {
               int m = 0;
               CrashReportSection var10000;
               Iterator var20;
               if (structureAccessor.shouldGenerateStructures()) {
                  List list2 = (List)map.getOrDefault(k, Collections.emptyList());

                  for(var20 = list2.iterator(); var20.hasNext(); ++m) {
                     Structure lv7 = (Structure)var20.next();
                     lv5.setDecoratorSeed(l, m, k);
                     Supplier supplier = () -> {
                        Optional var10000 = lv4.getKey(lv7).map(Object::toString);
                        Objects.requireNonNull(lv7);
                        return (String)var10000.orElseGet(lv7::toString);
                     };

                     try {
                        world.setCurrentlyGeneratingStructureName(supplier);
                        structureAccessor.getStructureStarts(lv2, lv7).forEach((start) -> {
                           start.place(world, structureAccessor, this, lv5, getBlockBoxForChunk(chunk), lv);
                        });
                     } catch (Exception var29) {
                        CrashReport lv8 = CrashReport.create(var29, "Feature placement");
                        var10000 = lv8.addElement("Feature");
                        Objects.requireNonNull(supplier);
                        var10000.add("Description", supplier::get);
                        throw new CrashException(lv8);
                     }
                  }
               }

               if (k < i) {
                  IntSet intSet = new IntArraySet();
                  var20 = set.iterator();

                  while(var20.hasNext()) {
                     RegistryEntry lv9 = (RegistryEntry)var20.next();
                     List list3 = ((GenerationSettings)this.generationSettingsGetter.apply(lv9)).getFeatures();
                     if (k < list3.size()) {
                        RegistryEntryList lv10 = (RegistryEntryList)list3.get(k);
                        PlacedFeatureIndexer.IndexedFeatures lv11 = (PlacedFeatureIndexer.IndexedFeatures)list.get(k);
                        lv10.stream().map(RegistryEntry::value).forEach((arg2) -> {
                           intSet.add(lv11.indexMapping().applyAsInt(arg2));
                        });
                     }
                  }

                  int n = intSet.size();
                  int[] is = intSet.toIntArray();
                  Arrays.sort(is);
                  PlacedFeatureIndexer.IndexedFeatures lv12 = (PlacedFeatureIndexer.IndexedFeatures)list.get(k);

                  for(int o = 0; o < n; ++o) {
                     int p = is[o];
                     PlacedFeature lv13 = (PlacedFeature)lv12.features().get(p);
                     Supplier supplier2 = () -> {
                        Optional var10000 = lv6.getKey(lv13).map(Object::toString);
                        Objects.requireNonNull(lv13);
                        return (String)var10000.orElseGet(lv13::toString);
                     };
                     lv5.setDecoratorSeed(l, p, k);

                     try {
                        world.setCurrentlyGeneratingStructureName(supplier2);
                        lv13.generate(world, this, lv5, lv3);
                     } catch (Exception var30) {
                        CrashReport lv14 = CrashReport.create(var30, "Feature placement");
                        var10000 = lv14.addElement("Feature");
                        Objects.requireNonNull(supplier2);
                        var10000.add("Description", supplier2::get);
                        throw new CrashException(lv14);
                     }
                  }
               }
            }

            world.setCurrentlyGeneratingStructureName((Supplier)null);
         } catch (Exception var31) {
            CrashReport lv15 = CrashReport.create(var31, "Biome decoration");
            lv15.addElement("Generation").add("CenterX", (Object)lv.x).add("CenterZ", (Object)lv.z).add("Seed", (Object)l);
            throw new CrashException(lv15);
         }
      }
   }

   private static BlockBox getBlockBoxForChunk(Chunk chunk) {
      ChunkPos lv = chunk.getPos();
      int i = lv.getStartX();
      int j = lv.getStartZ();
      HeightLimitView lv2 = chunk.getHeightLimitView();
      int k = lv2.getBottomY() + 1;
      int l = lv2.getTopY() - 1;
      return new BlockBox(i, k, j, i + 15, l, j + 15);
   }

   public abstract void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk);

   public abstract void populateEntities(ChunkRegion region);

   public int getSpawnHeight(HeightLimitView world) {
      return 64;
   }

   public BiomeSource getBiomeSource() {
      return this.biomeSource;
   }

   public abstract int getWorldHeight();

   public Pool getEntitySpawnList(RegistryEntry biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
      Map map = accessor.getStructureReferences(pos);
      Iterator var6 = map.entrySet().iterator();

      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         Structure lv = (Structure)entry.getKey();
         StructureSpawns lv2 = (StructureSpawns)lv.getStructureSpawns().get(group);
         if (lv2 != null) {
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate predicate = lv2.boundingBox() == StructureSpawns.BoundingBox.PIECE ? (start) -> {
               return accessor.structureContains(pos, start);
            } : (start) -> {
               return start.getBoundingBox().contains(pos);
            };
            accessor.acceptStructureStarts(lv, (LongSet)entry.getValue(), (start) -> {
               if (mutableBoolean.isFalse() && predicate.test(start)) {
                  mutableBoolean.setTrue();
               }

            });
            if (mutableBoolean.isTrue()) {
               return lv2.spawns();
            }
         }
      }

      return ((Biome)biome.value()).getSpawnSettings().getSpawnEntries(group);
   }

   public void setStructureStarts(DynamicRegistryManager registryManager, StructurePlacementCalculator placementCalculator, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager) {
      ChunkPos lv = chunk.getPos();
      ChunkSectionPos lv2 = ChunkSectionPos.from(chunk);
      NoiseConfig lv3 = placementCalculator.getNoiseConfig();
      placementCalculator.getStructureSets().forEach((structureSet) -> {
         StructurePlacement lvx = ((StructureSet)structureSet.value()).placement();
         List list = ((StructureSet)structureSet.value()).structures();
         Iterator var12 = list.iterator();

         while(var12.hasNext()) {
            StructureSet.WeightedEntry lv2x = (StructureSet.WeightedEntry)var12.next();
            StructureStart lv3x = structureAccessor.getStructureStart(lv2, (Structure)lv2x.structure().value(), chunk);
            if (lv3x != null && lv3x.hasChildren()) {
               return;
            }
         }

         if (lvx.shouldGenerate(placementCalculator, lv.x, lv.z)) {
            if (list.size() == 1) {
               this.trySetStructureStart((StructureSet.WeightedEntry)list.get(0), structureAccessor, registryManager, lv3, structureTemplateManager, placementCalculator.getStructureSeed(), chunk, lv, lv2);
            } else {
               ArrayList arrayList = new ArrayList(list.size());
               arrayList.addAll(list);
               ChunkRandom lv4 = new ChunkRandom(new CheckedRandom(0L));
               lv4.setCarverSeed(placementCalculator.getStructureSeed(), lv.x, lv.z);
               int i = 0;

               StructureSet.WeightedEntry lv5;
               for(Iterator var15 = arrayList.iterator(); var15.hasNext(); i += lv5.weight()) {
                  lv5 = (StructureSet.WeightedEntry)var15.next();
               }

               while(!arrayList.isEmpty()) {
                  int j = lv4.nextInt(i);
                  int k = 0;

                  for(Iterator var17 = arrayList.iterator(); var17.hasNext(); ++k) {
                     StructureSet.WeightedEntry lv6 = (StructureSet.WeightedEntry)var17.next();
                     j -= lv6.weight();
                     if (j < 0) {
                        break;
                     }
                  }

                  StructureSet.WeightedEntry lv7 = (StructureSet.WeightedEntry)arrayList.get(k);
                  if (this.trySetStructureStart(lv7, structureAccessor, registryManager, lv3, structureTemplateManager, placementCalculator.getStructureSeed(), chunk, lv, lv2)) {
                     return;
                  }

                  arrayList.remove(k);
                  i -= lv7.weight();
               }

            }
         }
      });
   }

   private boolean trySetStructureStart(StructureSet.WeightedEntry weightedEntry, StructureAccessor structureAccessor, DynamicRegistryManager dynamicRegistryManager, NoiseConfig noiseConfig, StructureTemplateManager structureManager, long seed, Chunk chunk, ChunkPos pos, ChunkSectionPos sectionPos) {
      Structure lv = (Structure)weightedEntry.structure().value();
      int i = getStructureReferences(structureAccessor, chunk, sectionPos, lv);
      RegistryEntryList lv2 = lv.getValidBiomes();
      Objects.requireNonNull(lv2);
      Predicate predicate = lv2::contains;
      StructureStart lv3 = lv.createStructureStart(dynamicRegistryManager, this, this.biomeSource, noiseConfig, structureManager, seed, pos, i, chunk, predicate);
      if (lv3.hasChildren()) {
         structureAccessor.setStructureStart(sectionPos, lv, lv3, chunk);
         return true;
      } else {
         return false;
      }
   }

   private static int getStructureReferences(StructureAccessor structureAccessor, Chunk chunk, ChunkSectionPos sectionPos, Structure structure) {
      StructureStart lv = structureAccessor.getStructureStart(sectionPos, structure, chunk);
      return lv != null ? lv.getReferences() : 0;
   }

   public void addStructureReferences(StructureWorldAccess world, StructureAccessor structureAccessor, Chunk chunk) {
      int i = true;
      ChunkPos lv = chunk.getPos();
      int j = lv.x;
      int k = lv.z;
      int l = lv.getStartX();
      int m = lv.getStartZ();
      ChunkSectionPos lv2 = ChunkSectionPos.from(chunk);

      for(int n = j - 8; n <= j + 8; ++n) {
         for(int o = k - 8; o <= k + 8; ++o) {
            long p = ChunkPos.toLong(n, o);
            Iterator var15 = world.getChunk(n, o).getStructureStarts().values().iterator();

            while(var15.hasNext()) {
               StructureStart lv3 = (StructureStart)var15.next();

               try {
                  if (lv3.hasChildren() && lv3.getBoundingBox().intersectsXZ(l, m, l + 15, m + 15)) {
                     structureAccessor.addStructureReference(lv2, lv3.getStructure(), p, chunk);
                     DebugInfoSender.sendStructureStart(world, lv3);
                  }
               } catch (Exception var21) {
                  CrashReport lv4 = CrashReport.create(var21, "Generating structure reference");
                  CrashReportSection lv5 = lv4.addElement("Structure");
                  Optional optional = world.getRegistryManager().getOptional(RegistryKeys.STRUCTURE);
                  lv5.add("Id", () -> {
                     return (String)optional.map((structureTypeRegistry) -> {
                        return structureTypeRegistry.getId(lv3.getStructure()).toString();
                     }).orElse("UNKNOWN");
                  });
                  lv5.add("Name", () -> {
                     return Registries.STRUCTURE_TYPE.getId(lv3.getStructure().getType()).toString();
                  });
                  lv5.add("Class", () -> {
                     return lv3.getStructure().getClass().getCanonicalName();
                  });
                  throw new CrashException(lv4);
               }
            }
         }
      }

   }

   public abstract CompletableFuture populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk);

   public abstract int getSeaLevel();

   public abstract int getMinimumY();

   public abstract int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig);

   public abstract VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig);

   public int getHeightOnGround(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
      return this.getHeight(x, z, heightmap, world, noiseConfig);
   }

   public int getHeightInGround(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
      return this.getHeight(x, z, heightmap, world, noiseConfig) - 1;
   }

   public abstract void getDebugHudText(List text, NoiseConfig noiseConfig, BlockPos pos);

   /** @deprecated */
   @Deprecated
   public GenerationSettings getGenerationSettings(RegistryEntry biomeEntry) {
      return (GenerationSettings)this.generationSettingsGetter.apply(biomeEntry);
   }

   static {
      CODEC = Registries.CHUNK_GENERATOR.getCodec().dispatchStable(ChunkGenerator::getCodec, Function.identity());
   }
}
