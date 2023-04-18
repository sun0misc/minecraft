package net.minecraft.world.biome;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.EndPlacedFeatures;

public class TheEndBiomeCreator {
   private static Biome createEndBiome(GenerationSettings.LookupBackedBuilder builder) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addEndMobs(lv);
      return (new Biome.Builder()).precipitation(false).temperature(0.5F).downfall(0.5F).effects((new BiomeEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).moodSound(BiomeMoodSound.CAVE).build()).spawnSettings(lv.build()).generationSettings(builder.build()).build();
   }

   public static Biome createEndBarrens(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      return createEndBiome(lv);
   }

   public static Biome createTheEnd(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = (new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup)).feature(GenerationStep.Feature.SURFACE_STRUCTURES, EndPlacedFeatures.END_SPIKE);
      return createEndBiome(lv);
   }

   public static Biome createEndMidlands(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      return createEndBiome(lv);
   }

   public static Biome createEndHighlands(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = (new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup)).feature(GenerationStep.Feature.SURFACE_STRUCTURES, EndPlacedFeatures.END_GATEWAY_RETURN).feature(GenerationStep.Feature.VEGETAL_DECORATION, EndPlacedFeatures.CHORUS_PLANT);
      return createEndBiome(lv);
   }

   public static Biome createSmallEndIslands(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = (new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup)).feature(GenerationStep.Feature.RAW_GENERATION, EndPlacedFeatures.END_ISLAND_DECORATED);
      return createEndBiome(lv);
   }
}
