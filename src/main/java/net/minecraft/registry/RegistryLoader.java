package net.minecraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.network.message.MessageType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;

public class RegistryLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final List DYNAMIC_REGISTRIES;
   public static final List DIMENSION_REGISTRIES;

   public static DynamicRegistryManager.Immutable load(ResourceManager resourceManager, DynamicRegistryManager baseRegistryManager, List entries) {
      Map map = new HashMap();
      List list2 = entries.stream().map((entry) -> {
         return entry.getLoader(Lifecycle.stable(), map);
      }).toList();
      RegistryOps.RegistryInfoGetter lv = createInfoGetter(baseRegistryManager, list2);
      list2.forEach((loader) -> {
         ((RegistryLoadable)loader.getSecond()).load(resourceManager, lv);
      });
      list2.forEach((loader) -> {
         Registry lv = (Registry)loader.getFirst();

         try {
            lv.freeze();
         } catch (Exception var4) {
            map.put(lv.getKey(), var4);
         }

      });
      if (!map.isEmpty()) {
         writeLoadingError(map);
         throw new IllegalStateException("Failed to load registries due to above errors");
      } else {
         return (new DynamicRegistryManager.ImmutableImpl(list2.stream().map(Pair::getFirst).toList())).toImmutable();
      }
   }

   private static RegistryOps.RegistryInfoGetter createInfoGetter(DynamicRegistryManager baseRegistryManager, List additionalRegistries) {
      final Map map = new HashMap();
      baseRegistryManager.streamAllRegistries().forEach((entry) -> {
         map.put(entry.key(), createInfo(entry.value()));
      });
      additionalRegistries.forEach((pair) -> {
         map.put(((MutableRegistry)pair.getFirst()).getKey(), createInfo((MutableRegistry)pair.getFirst()));
      });
      return new RegistryOps.RegistryInfoGetter() {
         public Optional getRegistryInfo(RegistryKey registryRef) {
            return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(registryRef));
         }
      };
   }

   private static RegistryOps.RegistryInfo createInfo(MutableRegistry registry) {
      return new RegistryOps.RegistryInfo(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
   }

   private static RegistryOps.RegistryInfo createInfo(Registry registry) {
      return new RegistryOps.RegistryInfo(registry.getReadOnlyWrapper(), registry.getTagCreatingWrapper(), registry.getLifecycle());
   }

   private static void writeLoadingError(Map exceptions) {
      StringWriter stringWriter = new StringWriter();
      PrintWriter printWriter = new PrintWriter(stringWriter);
      Map map2 = (Map)exceptions.entrySet().stream().collect(Collectors.groupingBy((entry) -> {
         return ((RegistryKey)entry.getKey()).getRegistry();
      }, Collectors.toMap((entry) -> {
         return ((RegistryKey)entry.getKey()).getValue();
      }, Map.Entry::getValue)));
      map2.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach((entry) -> {
         printWriter.printf("> Errors in registry %s:%n", entry.getKey());
         ((Map)entry.getValue()).entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach((elementEntry) -> {
            printWriter.printf(">> Errors in element %s:%n", elementEntry.getKey());
            ((Exception)elementEntry.getValue()).printStackTrace(printWriter);
         });
      });
      printWriter.flush();
      LOGGER.error("Registry loading errors:\n{}", stringWriter);
   }

   private static String getPath(Identifier id) {
      return id.getPath();
   }

   static void load(RegistryOps.RegistryInfoGetter registryInfoGetter, ResourceManager resourceManager, RegistryKey registryRef, MutableRegistry newRegistry, Decoder decoder, Map exceptions) {
      String string = getPath(registryRef.getValue());
      ResourceFinder lv = ResourceFinder.json(string);
      RegistryOps lv2 = RegistryOps.of(JsonOps.INSTANCE, (RegistryOps.RegistryInfoGetter)registryInfoGetter);
      Iterator var9 = lv.findResources(resourceManager).entrySet().iterator();

      while(var9.hasNext()) {
         Map.Entry entry = (Map.Entry)var9.next();
         Identifier lv3 = (Identifier)entry.getKey();
         RegistryKey lv4 = RegistryKey.of(registryRef, lv.toResourceId(lv3));
         Resource lv5 = (Resource)entry.getValue();

         try {
            Reader reader = lv5.getReader();

            try {
               JsonElement jsonElement = JsonParser.parseReader(reader);
               DataResult dataResult = decoder.parse(lv2, jsonElement);
               Object object = dataResult.getOrThrow(false, (error) -> {
               });
               newRegistry.add(lv4, object, lv5.isAlwaysStable() ? Lifecycle.stable() : dataResult.lifecycle());
            } catch (Throwable var19) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var18) {
                     var19.addSuppressed(var18);
                  }
               }

               throw var19;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (Exception var20) {
            exceptions.put(lv4, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", lv3, lv5.getResourcePackName()), var20));
         }
      }

   }

   static {
      DYNAMIC_REGISTRIES = List.of(new Entry(RegistryKeys.DIMENSION_TYPE, DimensionType.CODEC), new Entry(RegistryKeys.BIOME, Biome.CODEC), new Entry(RegistryKeys.MESSAGE_TYPE, MessageType.CODEC), new Entry(RegistryKeys.CONFIGURED_CARVER, ConfiguredCarver.CODEC), new Entry(RegistryKeys.CONFIGURED_FEATURE, ConfiguredFeature.CODEC), new Entry(RegistryKeys.PLACED_FEATURE, PlacedFeature.CODEC), new Entry(RegistryKeys.STRUCTURE, Structure.STRUCTURE_CODEC), new Entry(RegistryKeys.STRUCTURE_SET, StructureSet.CODEC), new Entry(RegistryKeys.PROCESSOR_LIST, StructureProcessorType.PROCESSORS_CODEC), new Entry(RegistryKeys.TEMPLATE_POOL, StructurePool.CODEC), new Entry(RegistryKeys.CHUNK_GENERATOR_SETTINGS, ChunkGeneratorSettings.CODEC), new Entry(RegistryKeys.NOISE_PARAMETERS, DoublePerlinNoiseSampler.NoiseParameters.CODEC), new Entry(RegistryKeys.DENSITY_FUNCTION, DensityFunction.CODEC), new Entry(RegistryKeys.WORLD_PRESET, WorldPreset.CODEC), new Entry(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.CODEC), new Entry(RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC), new Entry(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC), new Entry(RegistryKeys.DAMAGE_TYPE, DamageType.CODEC), new Entry(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.CODEC));
      DIMENSION_REGISTRIES = List.of(new Entry(RegistryKeys.DIMENSION, DimensionOptions.CODEC));
   }

   private interface RegistryLoadable {
      void load(ResourceManager resourceManager, RegistryOps.RegistryInfoGetter registryInfoGetter);
   }

   public static record Entry(RegistryKey key, Codec elementCodec) {
      public Entry(RegistryKey arg, Codec codec) {
         this.key = arg;
         this.elementCodec = codec;
      }

      Pair getLoader(Lifecycle lifecycle, Map exceptions) {
         MutableRegistry lv = new SimpleRegistry(this.key, lifecycle);
         RegistryLoadable lv2 = (resourceManager, registryInfoGetter) -> {
            RegistryLoader.load(registryInfoGetter, resourceManager, this.key, lv, this.elementCodec, exceptions);
         };
         return Pair.of(lv, lv2);
      }

      public RegistryKey key() {
         return this.key;
      }

      public Codec elementCodec() {
         return this.elementCodec;
      }
   }
}
