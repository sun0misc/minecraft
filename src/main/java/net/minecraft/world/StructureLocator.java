package net.minecraft.world;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.SelectiveNbtCollector;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.storage.NbtScannable;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructureLocator {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int START_NOT_PRESENT_REFERENCE = -1;
   private final NbtScannable chunkIoWorker;
   private final DynamicRegistryManager registryManager;
   private final Registry biomeRegistry;
   private final Registry structureRegistry;
   private final StructureTemplateManager structureTemplateManager;
   private final RegistryKey worldKey;
   private final ChunkGenerator chunkGenerator;
   private final NoiseConfig noiseConfig;
   private final HeightLimitView world;
   private final BiomeSource biomeSource;
   private final long seed;
   private final DataFixer dataFixer;
   private final Long2ObjectMap cachedStructuresByChunkPos = new Long2ObjectOpenHashMap();
   private final Map generationPossibilityByStructure = new HashMap();

   public StructureLocator(NbtScannable chunkIoWorker, DynamicRegistryManager registryManager, StructureTemplateManager structureTemplateManager, RegistryKey worldKey, ChunkGenerator chunkGenerator, NoiseConfig noiseConfig, HeightLimitView world, BiomeSource biomeSource, long seed, DataFixer dataFixer) {
      this.chunkIoWorker = chunkIoWorker;
      this.registryManager = registryManager;
      this.structureTemplateManager = structureTemplateManager;
      this.worldKey = worldKey;
      this.chunkGenerator = chunkGenerator;
      this.noiseConfig = noiseConfig;
      this.world = world;
      this.biomeSource = biomeSource;
      this.seed = seed;
      this.dataFixer = dataFixer;
      this.biomeRegistry = registryManager.get(RegistryKeys.BIOME);
      this.structureRegistry = registryManager.get(RegistryKeys.STRUCTURE);
   }

   public StructurePresence getStructurePresence(ChunkPos pos, Structure type, boolean skipReferencedStructures) {
      long l = pos.toLong();
      Object2IntMap object2IntMap = (Object2IntMap)this.cachedStructuresByChunkPos.get(l);
      if (object2IntMap != null) {
         return this.getStructurePresence(object2IntMap, type, skipReferencedStructures);
      } else {
         StructurePresence lv = this.getStructurePresence(pos, type, skipReferencedStructures, l);
         if (lv != null) {
            return lv;
         } else {
            boolean bl2 = ((Long2BooleanMap)this.generationPossibilityByStructure.computeIfAbsent(type, (structure2) -> {
               return new Long2BooleanOpenHashMap();
            })).computeIfAbsent(l, (chunkPos) -> {
               return this.isGenerationPossible(pos, type);
            });
            return !bl2 ? StructurePresence.START_NOT_PRESENT : StructurePresence.CHUNK_LOAD_NEEDED;
         }
      }
   }

   private boolean isGenerationPossible(ChunkPos pos, Structure structure) {
      DynamicRegistryManager var10003 = this.registryManager;
      ChunkGenerator var10004 = this.chunkGenerator;
      BiomeSource var10005 = this.biomeSource;
      NoiseConfig var10006 = this.noiseConfig;
      StructureTemplateManager var10007 = this.structureTemplateManager;
      long var10008 = this.seed;
      HeightLimitView var10010 = this.world;
      RegistryEntryList var10011 = structure.getValidBiomes();
      Objects.requireNonNull(var10011);
      return structure.getValidStructurePosition(new Structure.Context(var10003, var10004, var10005, var10006, var10007, var10008, pos, var10010, var10011::contains)).isPresent();
   }

   @Nullable
   private StructurePresence getStructurePresence(ChunkPos pos, Structure structure, boolean skipReferencedStructures, long posLong) {
      SelectiveNbtCollector lv = new SelectiveNbtCollector(new NbtScanQuery[]{new NbtScanQuery(NbtInt.TYPE, "DataVersion"), new NbtScanQuery("Level", "Structures", NbtCompound.TYPE, "Starts"), new NbtScanQuery("structures", NbtCompound.TYPE, "starts")});

      try {
         this.chunkIoWorker.scanChunk(pos, lv).join();
      } catch (Exception var13) {
         LOGGER.warn("Failed to read chunk {}", pos, var13);
         return StructurePresence.CHUNK_LOAD_NEEDED;
      }

      NbtElement lv2 = lv.getRoot();
      if (!(lv2 instanceof NbtCompound lv3)) {
         return null;
      } else {
         int i = VersionedChunkStorage.getDataVersion(lv3);
         if (i <= 1493) {
            return StructurePresence.CHUNK_LOAD_NEEDED;
         } else {
            VersionedChunkStorage.saveContextToNbt(lv3, this.worldKey, this.chunkGenerator.getCodecKey());

            NbtCompound lv4;
            try {
               lv4 = DataFixTypes.CHUNK.update(this.dataFixer, lv3, i);
            } catch (Exception var12) {
               LOGGER.warn("Failed to partially datafix chunk {}", pos, var12);
               return StructurePresence.CHUNK_LOAD_NEEDED;
            }

            Object2IntMap object2IntMap = this.collectStructuresAndReferences(lv4);
            if (object2IntMap == null) {
               return null;
            } else {
               this.cache(posLong, object2IntMap);
               return this.getStructurePresence(object2IntMap, structure, skipReferencedStructures);
            }
         }
      }
   }

   @Nullable
   private Object2IntMap collectStructuresAndReferences(NbtCompound nbt) {
      if (!nbt.contains("structures", NbtElement.COMPOUND_TYPE)) {
         return null;
      } else {
         NbtCompound lv = nbt.getCompound("structures");
         if (!lv.contains("starts", NbtElement.COMPOUND_TYPE)) {
            return null;
         } else {
            NbtCompound lv2 = lv.getCompound("starts");
            if (lv2.isEmpty()) {
               return Object2IntMaps.emptyMap();
            } else {
               Object2IntMap object2IntMap = new Object2IntOpenHashMap();
               Registry lv3 = this.registryManager.get(RegistryKeys.STRUCTURE);
               Iterator var6 = lv2.getKeys().iterator();

               while(var6.hasNext()) {
                  String string = (String)var6.next();
                  Identifier lv4 = Identifier.tryParse(string);
                  if (lv4 != null) {
                     Structure lv5 = (Structure)lv3.get(lv4);
                     if (lv5 != null) {
                        NbtCompound lv6 = lv2.getCompound(string);
                        if (!lv6.isEmpty()) {
                           String string2 = lv6.getString("id");
                           if (!"INVALID".equals(string2)) {
                              int i = lv6.getInt("references");
                              object2IntMap.put(lv5, i);
                           }
                        }
                     }
                  }
               }

               return object2IntMap;
            }
         }
      }
   }

   private static Object2IntMap createMapIfEmpty(Object2IntMap map) {
      return map.isEmpty() ? Object2IntMaps.emptyMap() : map;
   }

   private StructurePresence getStructurePresence(Object2IntMap referencesByStructure, Structure structure, boolean skipReferencedStructures) {
      int i = referencesByStructure.getOrDefault(structure, -1);
      return i == -1 || skipReferencedStructures && i != 0 ? StructurePresence.START_NOT_PRESENT : StructurePresence.START_PRESENT;
   }

   public void cache(ChunkPos pos, Map structureStarts) {
      long l = pos.toLong();
      Object2IntMap object2IntMap = new Object2IntOpenHashMap();
      structureStarts.forEach((start, arg2) -> {
         if (arg2.hasChildren()) {
            object2IntMap.put(start, arg2.getReferences());
         }

      });
      this.cache(l, object2IntMap);
   }

   private void cache(long pos, Object2IntMap referencesByStructure) {
      this.cachedStructuresByChunkPos.put(pos, createMapIfEmpty(referencesByStructure));
      this.generationPossibilityByStructure.values().forEach((generationPossibilityByChunkPos) -> {
         generationPossibilityByChunkPos.remove(pos);
      });
   }

   public void incrementReferences(ChunkPos pos, Structure structure) {
      this.cachedStructuresByChunkPos.compute(pos.toLong(), (posx, referencesByStructure) -> {
         if (referencesByStructure == null || ((Object2IntMap)referencesByStructure).isEmpty()) {
            referencesByStructure = new Object2IntOpenHashMap();
         }

         ((Object2IntMap)referencesByStructure).computeInt(structure, (feature, references) -> {
            return references == null ? 1 : references + 1;
         });
         return (Object2IntMap)referencesByStructure;
      });
   }
}
