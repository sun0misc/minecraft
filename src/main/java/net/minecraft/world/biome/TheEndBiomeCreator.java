/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.biome;

import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.EndPlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;

public class TheEndBiomeCreator {
    private static Biome createEndBiome(GenerationSettings.LookupBackedBuilder builder) {
        SpawnSettings.Builder lv = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addEndMobs(lv);
        return new Biome.Builder().precipitation(false).temperature(0.5f).downfall(0.5f).effects(new BiomeEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(0xA080A0).skyColor(0).moodSound(BiomeMoodSound.CAVE).build()).spawnSettings(lv.build()).generationSettings(builder.build()).build();
    }

    public static Biome createEndBarrens(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        return TheEndBiomeCreator.createEndBiome(lv);
    }

    public static Biome createTheEnd(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).feature(GenerationStep.Feature.SURFACE_STRUCTURES, EndPlacedFeatures.END_SPIKE);
        return TheEndBiomeCreator.createEndBiome(lv);
    }

    public static Biome createEndMidlands(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        return TheEndBiomeCreator.createEndBiome(lv);
    }

    public static Biome createEndHighlands(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).feature(GenerationStep.Feature.SURFACE_STRUCTURES, EndPlacedFeatures.END_GATEWAY_RETURN).feature(GenerationStep.Feature.VEGETAL_DECORATION, EndPlacedFeatures.CHORUS_PLANT);
        return TheEndBiomeCreator.createEndBiome(lv);
    }

    public static Biome createSmallEndIslands(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).feature(GenerationStep.Feature.RAW_GENERATION, EndPlacedFeatures.END_ISLAND_DECORATED);
        return TheEndBiomeCreator.createEndBiome(lv);
    }
}

