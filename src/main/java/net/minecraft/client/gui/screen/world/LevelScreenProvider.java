package net.minecraft.client.gui.screen.world;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.CustomizeBuffetLevelScreen;
import net.minecraft.client.gui.screen.CustomizeFlatLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

@Environment(EnvType.CLIENT)
public interface LevelScreenProvider {
   Map WORLD_PRESET_TO_SCREEN_PROVIDER = Map.of(Optional.of(WorldPresets.FLAT), (parent, generatorOptionsHolder) -> {
      ChunkGenerator lv = generatorOptionsHolder.selectedDimensions().getChunkGenerator();
      DynamicRegistryManager lv2 = generatorOptionsHolder.getCombinedRegistryManager();
      RegistryEntryLookup lv3 = lv2.getWrapperOrThrow(RegistryKeys.BIOME);
      RegistryEntryLookup lv4 = lv2.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET);
      RegistryEntryLookup lv5 = lv2.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE);
      return new CustomizeFlatLevelScreen(parent, (config) -> {
         parent.getWorldCreator().applyModifier(createModifier(config));
      }, lv instanceof FlatChunkGenerator ? ((FlatChunkGenerator)lv).getConfig() : FlatChunkGeneratorConfig.getDefaultConfig(lv3, lv4, lv5));
   }, Optional.of(WorldPresets.SINGLE_BIOME_SURFACE), (parent, generatorOptionsHolder) -> {
      return new CustomizeBuffetLevelScreen(parent, generatorOptionsHolder, (biomeEntry) -> {
         parent.getWorldCreator().applyModifier(createModifier(biomeEntry));
      });
   });

   Screen createEditScreen(CreateWorldScreen parent, GeneratorOptionsHolder generatorOptionsHolder);

   private static GeneratorOptionsHolder.RegistryAwareModifier createModifier(FlatChunkGeneratorConfig config) {
      return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
         ChunkGenerator lv = new FlatChunkGenerator(config);
         return dimensionsRegistryHolder.with(dynamicRegistryManager, lv);
      };
   }

   private static GeneratorOptionsHolder.RegistryAwareModifier createModifier(RegistryEntry biomeEntry) {
      return (dynamicRegistryManager, dimensionsRegistryHolder) -> {
         Registry lv = dynamicRegistryManager.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
         RegistryEntry lv2 = lv.entryOf(ChunkGeneratorSettings.OVERWORLD);
         BiomeSource lv3 = new FixedBiomeSource(biomeEntry);
         ChunkGenerator lv4 = new NoiseChunkGenerator(lv3, lv2);
         return dimensionsRegistryHolder.with(dynamicRegistryManager, lv4);
      };
   }
}
