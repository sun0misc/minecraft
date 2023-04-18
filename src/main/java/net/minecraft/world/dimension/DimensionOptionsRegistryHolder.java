package net.minecraft.world.dimension;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.level.LevelProperties;

public record DimensionOptionsRegistryHolder(Registry dimensions) {
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      return instance.group(RegistryCodecs.createKeyedRegistryCodec(RegistryKeys.DIMENSION, Lifecycle.stable(), DimensionOptions.CODEC).fieldOf("dimensions").forGetter(DimensionOptionsRegistryHolder::dimensions)).apply(instance, instance.stable(DimensionOptionsRegistryHolder::new));
   });
   private static final Set VANILLA_KEYS;
   private static final int VANILLA_KEY_COUNT;

   public DimensionOptionsRegistryHolder(Registry arg) {
      DimensionOptions lv = (DimensionOptions)arg.get(DimensionOptions.OVERWORLD);
      if (lv == null) {
         throw new IllegalStateException("Overworld settings missing");
      } else {
         this.dimensions = arg;
      }
   }

   public static Stream streamAll(Stream otherKeys) {
      return Stream.concat(VANILLA_KEYS.stream(), otherKeys.filter((key) -> {
         return !VANILLA_KEYS.contains(key);
      }));
   }

   public DimensionOptionsRegistryHolder with(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator) {
      Registry lv = dynamicRegistryManager.get(RegistryKeys.DIMENSION_TYPE);
      Registry lv2 = createRegistry(lv, this.dimensions, chunkGenerator);
      return new DimensionOptionsRegistryHolder(lv2);
   }

   public static Registry createRegistry(Registry dynamicRegistry, Registry currentRegistry, ChunkGenerator chunkGenerator) {
      DimensionOptions lv = (DimensionOptions)currentRegistry.get(DimensionOptions.OVERWORLD);
      RegistryEntry lv2 = lv == null ? dynamicRegistry.entryOf(DimensionTypes.OVERWORLD) : lv.dimensionTypeEntry();
      return createRegistry(currentRegistry, (RegistryEntry)lv2, chunkGenerator);
   }

   public static Registry createRegistry(Registry currentRegistry, RegistryEntry overworldEntry, ChunkGenerator chunkGenerator) {
      MutableRegistry lv = new SimpleRegistry(RegistryKeys.DIMENSION, Lifecycle.experimental());
      lv.add(DimensionOptions.OVERWORLD, new DimensionOptions(overworldEntry, chunkGenerator), Lifecycle.stable());
      Iterator var4 = currentRegistry.getEntrySet().iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         RegistryKey lv2 = (RegistryKey)entry.getKey();
         if (lv2 != DimensionOptions.OVERWORLD) {
            lv.add(lv2, (DimensionOptions)entry.getValue(), currentRegistry.getEntryLifecycle((DimensionOptions)entry.getValue()));
         }
      }

      return lv.freeze();
   }

   public ChunkGenerator getChunkGenerator() {
      DimensionOptions lv = (DimensionOptions)this.dimensions.get(DimensionOptions.OVERWORLD);
      if (lv == null) {
         throw new IllegalStateException("Overworld settings missing");
      } else {
         return lv.chunkGenerator();
      }
   }

   public Optional getOrEmpty(RegistryKey key) {
      return this.dimensions.getOrEmpty(key);
   }

   public ImmutableSet getWorldKeys() {
      return (ImmutableSet)this.dimensions().getEntrySet().stream().map(Map.Entry::getKey).map(RegistryKeys::toWorldKey).collect(ImmutableSet.toImmutableSet());
   }

   public boolean isDebug() {
      return this.getChunkGenerator() instanceof DebugChunkGenerator;
   }

   private static LevelProperties.SpecialProperty getSpecialProperty(Registry dimensionOptionsRegistry) {
      return (LevelProperties.SpecialProperty)dimensionOptionsRegistry.getOrEmpty(DimensionOptions.OVERWORLD).map((overworldEntry) -> {
         ChunkGenerator lv = overworldEntry.chunkGenerator();
         if (lv instanceof DebugChunkGenerator) {
            return LevelProperties.SpecialProperty.DEBUG;
         } else {
            return lv instanceof FlatChunkGenerator ? LevelProperties.SpecialProperty.FLAT : LevelProperties.SpecialProperty.NONE;
         }
      }).orElse(LevelProperties.SpecialProperty.NONE);
   }

   static Lifecycle getLifecycle(RegistryKey key, DimensionOptions dimensionOptions) {
      return isVanilla(key, dimensionOptions) ? Lifecycle.stable() : Lifecycle.experimental();
   }

   private static boolean isVanilla(RegistryKey key, DimensionOptions dimensionOptions) {
      if (key == DimensionOptions.OVERWORLD) {
         return isOverworldVanilla(dimensionOptions);
      } else if (key == DimensionOptions.NETHER) {
         return isNetherVanilla(dimensionOptions);
      } else {
         return key == DimensionOptions.END ? isTheEndVanilla(dimensionOptions) : false;
      }
   }

   private static boolean isOverworldVanilla(DimensionOptions dimensionOptions) {
      RegistryEntry lv = dimensionOptions.dimensionTypeEntry();
      if (!lv.matchesKey(DimensionTypes.OVERWORLD) && !lv.matchesKey(DimensionTypes.OVERWORLD_CAVES)) {
         return false;
      } else {
         BiomeSource var3 = dimensionOptions.chunkGenerator().getBiomeSource();
         if (var3 instanceof MultiNoiseBiomeSource) {
            MultiNoiseBiomeSource lv2 = (MultiNoiseBiomeSource)var3;
            if (!lv2.matchesInstance(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isNetherVanilla(DimensionOptions dimensionOptions) {
      boolean var10000;
      if (dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.THE_NETHER)) {
         ChunkGenerator var3 = dimensionOptions.chunkGenerator();
         if (var3 instanceof NoiseChunkGenerator) {
            NoiseChunkGenerator lv = (NoiseChunkGenerator)var3;
            if (lv.matchesSettings(ChunkGeneratorSettings.NETHER)) {
               BiomeSource var4 = lv.getBiomeSource();
               if (var4 instanceof MultiNoiseBiomeSource) {
                  MultiNoiseBiomeSource lv2 = (MultiNoiseBiomeSource)var4;
                  if (lv2.matchesInstance(MultiNoiseBiomeSourceParameterLists.NETHER)) {
                     var10000 = true;
                     return var10000;
                  }
               }
            }
         }
      }

      var10000 = false;
      return var10000;
   }

   private static boolean isTheEndVanilla(DimensionOptions dimensionOptions) {
      boolean var10000;
      if (dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.THE_END)) {
         ChunkGenerator var2 = dimensionOptions.chunkGenerator();
         if (var2 instanceof NoiseChunkGenerator) {
            NoiseChunkGenerator lv = (NoiseChunkGenerator)var2;
            if (lv.matchesSettings(ChunkGeneratorSettings.END) && lv.getBiomeSource() instanceof TheEndBiomeSource) {
               var10000 = true;
               return var10000;
            }
         }
      }

      var10000 = false;
      return var10000;
   }

   public DimensionsConfig toConfig(Registry existingRegistry) {
      Stream stream = Stream.concat(existingRegistry.getKeys().stream(), this.dimensions.getKeys().stream()).distinct();
      List list = new ArrayList();
      streamAll(stream).forEach((key) -> {
         existingRegistry.getOrEmpty(key).or(() -> {
            return this.dimensions.getOrEmpty(key);
         }).ifPresent((dimensionOptions) -> {
            record Entry(RegistryKey key, DimensionOptions value) {
               final RegistryKey key;
               final DimensionOptions value;

               Entry(RegistryKey arg, DimensionOptions arg2) {
                  this.key = arg;
                  this.value = arg2;
               }

               Lifecycle getLifecycle() {
                  return DimensionOptionsRegistryHolder.getLifecycle(this.key, this.value);
               }

               public RegistryKey key() {
                  return this.key;
               }

               public DimensionOptions value() {
                  return this.value;
               }
            }

            list.add(new Entry(key, dimensionOptions));
         });
      });
      Lifecycle lifecycle = list.size() == VANILLA_KEY_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
      MutableRegistry lv = new SimpleRegistry(RegistryKeys.DIMENSION, lifecycle);
      list.forEach((entry) -> {
         lv.add(entry.key, entry.value, entry.getLifecycle());
      });
      Registry lv2 = lv.freeze();
      LevelProperties.SpecialProperty lv3 = getSpecialProperty(lv2);
      return new DimensionsConfig(lv2.freeze(), lv3);
   }

   public Registry dimensions() {
      return this.dimensions;
   }

   static {
      VANILLA_KEYS = ImmutableSet.of(DimensionOptions.OVERWORLD, DimensionOptions.NETHER, DimensionOptions.END);
      VANILLA_KEY_COUNT = VANILLA_KEYS.size();
   }

   public static record DimensionsConfig(Registry dimensions, LevelProperties.SpecialProperty specialWorldProperty) {
      public DimensionsConfig(Registry arg, LevelProperties.SpecialProperty arg2) {
         this.dimensions = arg;
         this.specialWorldProperty = arg2;
      }

      public Lifecycle getLifecycle() {
         return this.dimensions.getLifecycle();
      }

      public DynamicRegistryManager.Immutable toDynamicRegistryManager() {
         return (new DynamicRegistryManager.ImmutableImpl(List.of(this.dimensions))).toImmutable();
      }

      public Registry dimensions() {
         return this.dimensions;
      }

      public LevelProperties.SpecialProperty specialWorldProperty() {
         return this.specialWorldProperty;
      }
   }
}
