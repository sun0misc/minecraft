package net.minecraft.world.gen;

import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public class WorldPresets {
   public static final RegistryKey DEFAULT = of("normal");
   public static final RegistryKey FLAT = of("flat");
   public static final RegistryKey LARGE_BIOMES = of("large_biomes");
   public static final RegistryKey AMPLIFIED = of("amplified");
   public static final RegistryKey SINGLE_BIOME_SURFACE = of("single_biome_surface");
   public static final RegistryKey DEBUG_ALL_BLOCK_STATES = of("debug_all_block_states");

   public static void bootstrap(Registerable presetRegisterable) {
      (new Registrar(presetRegisterable)).bootstrap();
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.WORLD_PRESET, new Identifier(id));
   }

   public static Optional getWorldPreset(Registry registry) {
      return registry.getOrEmpty(DimensionOptions.OVERWORLD).flatMap((overworld) -> {
         ChunkGenerator lv = overworld.chunkGenerator();
         if (lv instanceof FlatChunkGenerator) {
            return Optional.of(FLAT);
         } else {
            return lv instanceof DebugChunkGenerator ? Optional.of(DEBUG_ALL_BLOCK_STATES) : Optional.empty();
         }
      });
   }

   public static DimensionOptionsRegistryHolder createDemoOptions(DynamicRegistryManager dynamicRegistryManager) {
      return ((WorldPreset)dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(DEFAULT).value()).createDimensionsRegistryHolder();
   }

   public static DimensionOptions getDefaultOverworldOptions(DynamicRegistryManager dynamicRegistryManager) {
      return (DimensionOptions)((WorldPreset)dynamicRegistryManager.get(RegistryKeys.WORLD_PRESET).entryOf(DEFAULT).value()).getOverworld().orElseThrow();
   }

   private static class Registrar {
      private final Registerable presetRegisterable;
      private final RegistryEntryLookup chunkGeneratorSettingsLookup;
      private final RegistryEntryLookup biomeLookup;
      private final RegistryEntryLookup featureLookup;
      private final RegistryEntryLookup structureSetLookup;
      private final RegistryEntryLookup multiNoisePresetLookup;
      private final RegistryEntry overworldDimensionType;
      private final DimensionOptions netherDimensionOptions;
      private final DimensionOptions endDimensionOptions;

      Registrar(Registerable presetRegisterable) {
         this.presetRegisterable = presetRegisterable;
         RegistryEntryLookup lv = presetRegisterable.getRegistryLookup(RegistryKeys.DIMENSION_TYPE);
         this.chunkGeneratorSettingsLookup = presetRegisterable.getRegistryLookup(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
         this.biomeLookup = presetRegisterable.getRegistryLookup(RegistryKeys.BIOME);
         this.featureLookup = presetRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
         this.structureSetLookup = presetRegisterable.getRegistryLookup(RegistryKeys.STRUCTURE_SET);
         this.multiNoisePresetLookup = presetRegisterable.getRegistryLookup(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
         this.overworldDimensionType = lv.getOrThrow(DimensionTypes.OVERWORLD);
         RegistryEntry lv2 = lv.getOrThrow(DimensionTypes.THE_NETHER);
         RegistryEntry lv3 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.NETHER);
         RegistryEntry.Reference lv4 = this.multiNoisePresetLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
         this.netherDimensionOptions = new DimensionOptions(lv2, new NoiseChunkGenerator(MultiNoiseBiomeSource.create((RegistryEntry)lv4), lv3));
         RegistryEntry lv5 = lv.getOrThrow(DimensionTypes.THE_END);
         RegistryEntry lv6 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.END);
         this.endDimensionOptions = new DimensionOptions(lv5, new NoiseChunkGenerator(TheEndBiomeSource.createVanilla(this.biomeLookup), lv6));
      }

      private DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator) {
         return new DimensionOptions(this.overworldDimensionType, chunkGenerator);
      }

      private DimensionOptions createOverworldOptions(BiomeSource biomeSource, RegistryEntry chunkGeneratorSettings) {
         return this.createOverworldOptions(new NoiseChunkGenerator(biomeSource, chunkGeneratorSettings));
      }

      private WorldPreset createPreset(DimensionOptions dimensionOptions) {
         return new WorldPreset(Map.of(DimensionOptions.OVERWORLD, dimensionOptions, DimensionOptions.NETHER, this.netherDimensionOptions, DimensionOptions.END, this.endDimensionOptions));
      }

      private void register(RegistryKey key, DimensionOptions dimensionOptions) {
         this.presetRegisterable.register(key, this.createPreset(dimensionOptions));
      }

      private void bootstrap(BiomeSource biomeSource) {
         RegistryEntry lv = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
         this.register(WorldPresets.DEFAULT, this.createOverworldOptions(biomeSource, lv));
         RegistryEntry lv2 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.LARGE_BIOMES);
         this.register(WorldPresets.LARGE_BIOMES, this.createOverworldOptions(biomeSource, lv2));
         RegistryEntry lv3 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.AMPLIFIED);
         this.register(WorldPresets.AMPLIFIED, this.createOverworldOptions(biomeSource, lv3));
      }

      public void bootstrap() {
         RegistryEntry.Reference lv = this.multiNoisePresetLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
         this.bootstrap(MultiNoiseBiomeSource.create((RegistryEntry)lv));
         RegistryEntry lv2 = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);
         RegistryEntry.Reference lv3 = this.biomeLookup.getOrThrow(BiomeKeys.PLAINS);
         this.register(WorldPresets.SINGLE_BIOME_SURFACE, this.createOverworldOptions(new FixedBiomeSource(lv3), lv2));
         this.register(WorldPresets.FLAT, this.createOverworldOptions(new FlatChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig(this.biomeLookup, this.structureSetLookup, this.featureLookup))));
         this.register(WorldPresets.DEBUG_ALL_BLOCK_STATES, this.createOverworldOptions(new DebugChunkGenerator(lv3)));
      }
   }
}
