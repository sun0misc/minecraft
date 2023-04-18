package net.minecraft.world.biome;

import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.OceanPlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import org.jetbrains.annotations.Nullable;

public class OverworldBiomeCreator {
   protected static final int DEFAULT_WATER_COLOR = 4159204;
   protected static final int DEFAULT_WATER_FOG_COLOR = 329011;
   private static final int DEFAULT_FOG_COLOR = 12638463;
   @Nullable
   private static final MusicSound DEFAULT_MUSIC = null;

   protected static int getSkyColor(float temperature) {
      float g = temperature / 3.0F;
      g = MathHelper.clamp(g, -1.0F, 1.0F);
      return MathHelper.hsvToRgb(0.62222224F - g * 0.05F, 0.5F + g * 0.1F, 1.0F);
   }

   private static Biome createBiome(boolean precipitation, float temperature, float downfall, SpawnSettings.Builder spawnSettings, GenerationSettings.LookupBackedBuilder generationSettings, @Nullable MusicSound music) {
      return createBiome(precipitation, temperature, downfall, 4159204, 329011, (Integer)null, (Integer)null, spawnSettings, generationSettings, music);
   }

   private static Biome createBiome(boolean precipitation, float temperature, float downfall, int waterColor, int waterFogColor, @Nullable Integer grassColor, @Nullable Integer foliageColor, SpawnSettings.Builder spawnSettings, GenerationSettings.LookupBackedBuilder generationSettings, @Nullable MusicSound music) {
      BiomeEffects.Builder lv = (new BiomeEffects.Builder()).waterColor(waterColor).waterFogColor(waterFogColor).fogColor(12638463).skyColor(getSkyColor(temperature)).moodSound(BiomeMoodSound.CAVE).music(music);
      if (grassColor != null) {
         lv.grassColor(grassColor);
      }

      if (foliageColor != null) {
         lv.foliageColor(foliageColor);
      }

      return (new Biome.Builder()).precipitation(precipitation).temperature(temperature).downfall(downfall).effects(lv.build()).spawnSettings(spawnSettings.build()).generationSettings(generationSettings.build()).build();
   }

   private static void addBasicFeatures(GenerationSettings.LookupBackedBuilder generationSettings) {
      DefaultBiomeFeatures.addLandCarvers(generationSettings);
      DefaultBiomeFeatures.addAmethystGeodes(generationSettings);
      DefaultBiomeFeatures.addDungeons(generationSettings);
      DefaultBiomeFeatures.addMineables(generationSettings);
      DefaultBiomeFeatures.addSprings(generationSettings);
      DefaultBiomeFeatures.addFrozenTopLayer(generationSettings);
   }

   public static Biome createOldGrowthTaiga(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean spruce) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv);
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 4, 4));
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3));
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FOX, 8, 2, 4));
      if (spruce) {
         DefaultBiomeFeatures.addBatsAndMonsters(lv);
      } else {
         DefaultBiomeFeatures.addCaveMobs(lv);
         DefaultBiomeFeatures.addMonsters(lv, 100, 25, 100, false);
      }

      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addMossyRocks(lv2);
      DefaultBiomeFeatures.addLargeFerns(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, spruce ? VegetationPlacedFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacedFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addGiantTaigaGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      DefaultBiomeFeatures.addSweetBerryBushes(lv2);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_OLD_GROWTH_TAIGA);
      return createBiome(true, spruce ? 0.25F : 0.3F, 0.8F, lv, lv2, lv3);
   }

   public static Biome createSparseJungle(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addJungleMobs(lv);
      return createJungleFeatures(featureLookup, carverLookup, 0.8F, false, true, false, lv);
   }

   public static Biome createJungle(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addJungleMobs(lv);
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PARROT, 40, 1, 2)).spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.OCELOT, 2, 1, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PANDA, 1, 1, 2));
      return createJungleFeatures(featureLookup, carverLookup, 0.9F, false, false, true, lv);
   }

   public static Biome createNormalBambooJungle(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addJungleMobs(lv);
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PARROT, 40, 1, 2)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.PANDA, 80, 1, 2)).spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.OCELOT, 2, 1, 1));
      return createJungleFeatures(featureLookup, carverLookup, 0.9F, true, false, true, lv);
   }

   private static Biome createJungleFeatures(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, float depth, boolean bamboo, boolean sparse, boolean unmodified, SpawnSettings.Builder spawnSettings) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      if (bamboo) {
         DefaultBiomeFeatures.addBambooJungleTrees(lv);
      } else {
         if (unmodified) {
            DefaultBiomeFeatures.addBamboo(lv);
         }

         if (sparse) {
            DefaultBiomeFeatures.addSparseJungleTrees(lv);
         } else {
            DefaultBiomeFeatures.addJungleTrees(lv);
         }
      }

      DefaultBiomeFeatures.addExtraDefaultFlowers(lv);
      DefaultBiomeFeatures.addJungleGrass(lv);
      DefaultBiomeFeatures.addDefaultMushrooms(lv);
      DefaultBiomeFeatures.addDefaultVegetation(lv);
      DefaultBiomeFeatures.addVines(lv);
      if (sparse) {
         DefaultBiomeFeatures.addSparseMelons(lv);
      } else {
         DefaultBiomeFeatures.addMelons(lv);
      }

      MusicSound lv2 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_JUNGLE_AND_FOREST);
      return createBiome(true, 0.95F, depth, spawnSettings, lv, lv2);
   }

   public static Biome createWindsweptHills(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean forest) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv);
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.LLAMA, 5, 4, 6));
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      if (forest) {
         DefaultBiomeFeatures.addWindsweptForestTrees(lv2);
      } else {
         DefaultBiomeFeatures.addWindsweptHillsTrees(lv2);
      }

      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addDefaultGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      DefaultBiomeFeatures.addEmeraldOre(lv2);
      DefaultBiomeFeatures.addInfestedStone(lv2);
      return createBiome(true, 0.2F, 0.3F, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createDesert(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addDesertMobs(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      DefaultBiomeFeatures.addFossils(lv2);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addDefaultGrass(lv2);
      DefaultBiomeFeatures.addDesertDeadBushes(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDesertVegetation(lv2);
      DefaultBiomeFeatures.addDesertFeatures(lv2);
      return createBiome(false, 2.0F, 0.0F, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createPlains(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean sunflower, boolean snowy, boolean iceSpikes) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      if (snowy) {
         lv.creatureSpawnProbability(0.07F);
         DefaultBiomeFeatures.addSnowyMobs(lv);
         if (iceSpikes) {
            lv2.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.ICE_SPIKE);
            lv2.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.ICE_PATCH);
         }
      } else {
         DefaultBiomeFeatures.addPlainsMobs(lv);
         DefaultBiomeFeatures.addPlainsTallGrass(lv2);
         if (sunflower) {
            lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUNFLOWER);
         }
      }

      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      if (snowy) {
         DefaultBiomeFeatures.addSnowySpruceTrees(lv2);
         DefaultBiomeFeatures.addDefaultFlowers(lv2);
         DefaultBiomeFeatures.addDefaultGrass(lv2);
      } else {
         DefaultBiomeFeatures.addPlainsFeatures(lv2);
      }

      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      if (sunflower) {
         lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUGAR_CANE);
         lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_PUMPKIN);
      } else {
         DefaultBiomeFeatures.addDefaultVegetation(lv2);
      }

      float f = snowy ? 0.0F : 0.8F;
      return createBiome(true, f, snowy ? 0.5F : 0.4F, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createMushroomFields(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addMushroomMobs(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addMushroomFieldsFeatures(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      return createBiome(true, 0.9F, 1.0F, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createSavanna(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean windswept, boolean plateau) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv);
      if (!windswept) {
         DefaultBiomeFeatures.addSavannaTallGrass(lv);
      }

      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      if (windswept) {
         DefaultBiomeFeatures.addExtraSavannaTrees(lv);
         DefaultBiomeFeatures.addDefaultFlowers(lv);
         DefaultBiomeFeatures.addWindsweptSavannaGrass(lv);
      } else {
         DefaultBiomeFeatures.addSavannaTrees(lv);
         DefaultBiomeFeatures.addExtraDefaultFlowers(lv);
         DefaultBiomeFeatures.addSavannaGrass(lv);
      }

      DefaultBiomeFeatures.addDefaultMushrooms(lv);
      DefaultBiomeFeatures.addDefaultVegetation(lv);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv2);
      lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.HORSE, 1, 2, 6)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.DONKEY, 1, 1, 1));
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      if (plateau) {
         lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.LLAMA, 8, 4, 4));
      }

      return createBiome(false, 2.0F, 0.0F, lv2, lv, DEFAULT_MUSIC);
   }

   public static Biome createBadlands(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean plateau) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addExtraGoldOre(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      if (plateau) {
         DefaultBiomeFeatures.addBadlandsPlateauTrees(lv2);
      }

      DefaultBiomeFeatures.addBadlandsGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addBadlandsVegetation(lv2);
      return (new Biome.Builder()).precipitation(false).temperature(2.0F).downfall(0.0F).effects((new BiomeEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(getSkyColor(2.0F)).foliageColor(10387789).grassColor(9470285).moodSound(BiomeMoodSound.CAVE).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
   }

   private static Biome createOcean(SpawnSettings.Builder spawnSettings, int waterColor, int waterFogColor, GenerationSettings.LookupBackedBuilder generationSettings) {
      return createBiome(true, 0.5F, 0.5F, waterColor, waterFogColor, (Integer)null, (Integer)null, spawnSettings, generationSettings, DEFAULT_MUSIC);
   }

   private static GenerationSettings.LookupBackedBuilder createOceanGenerationSettings(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      DefaultBiomeFeatures.addWaterBiomeOakTrees(lv);
      DefaultBiomeFeatures.addDefaultFlowers(lv);
      DefaultBiomeFeatures.addDefaultGrass(lv);
      DefaultBiomeFeatures.addDefaultMushrooms(lv);
      DefaultBiomeFeatures.addDefaultVegetation(lv);
      return lv;
   }

   public static Biome createColdOcean(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean deep) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addOceanMobs(lv, 3, 4, 15);
      lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.SALMON, 15, 1, 5));
      GenerationSettings.LookupBackedBuilder lv2 = createOceanGenerationSettings(featureLookup, carverLookup);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP_COLD : OceanPlacedFeatures.SEAGRASS_COLD);
      DefaultBiomeFeatures.addSeagrassOnStone(lv2);
      DefaultBiomeFeatures.addKelp(lv2);
      return createOcean(lv, 4020182, 329011, lv2);
   }

   public static Biome createNormalOcean(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean deep) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addOceanMobs(lv, 1, 4, 10);
      lv.spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 1, 1, 2));
      GenerationSettings.LookupBackedBuilder lv2 = createOceanGenerationSettings(featureLookup, carverLookup);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP : OceanPlacedFeatures.SEAGRASS_NORMAL);
      DefaultBiomeFeatures.addSeagrassOnStone(lv2);
      DefaultBiomeFeatures.addKelp(lv2);
      return createOcean(lv, 4159204, 329011, lv2);
   }

   public static Biome createLukewarmOcean(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean deep) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      if (deep) {
         DefaultBiomeFeatures.addOceanMobs(lv, 8, 4, 8);
      } else {
         DefaultBiomeFeatures.addOceanMobs(lv, 10, 2, 15);
      }

      lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.PUFFERFISH, 5, 1, 3)).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8)).spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 2, 1, 2));
      GenerationSettings.LookupBackedBuilder lv2 = createOceanGenerationSettings(featureLookup, carverLookup);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP_WARM : OceanPlacedFeatures.SEAGRASS_WARM);
      if (deep) {
         DefaultBiomeFeatures.addSeagrassOnStone(lv2);
      }

      DefaultBiomeFeatures.addLessKelp(lv2);
      return createOcean(lv, 4566514, 267827, lv2);
   }

   public static Biome createWarmOcean(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = (new SpawnSettings.Builder()).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.PUFFERFISH, 15, 1, 3));
      DefaultBiomeFeatures.addWarmOceanMobs(lv, 10, 4);
      GenerationSettings.LookupBackedBuilder lv2 = createOceanGenerationSettings(featureLookup, carverLookup).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.WARM_OCEAN_VEGETATION).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_WARM).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEA_PICKLE);
      return createOcean(lv, 4445678, 270131, lv2);
   }

   public static Biome createFrozenOcean(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean deep) {
      SpawnSettings.Builder lv = (new SpawnSettings.Builder()).spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.SQUID, 1, 1, 4)).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.SALMON, 15, 1, 5)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.POLAR_BEAR, 1, 1, 2));
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 5, 1, 1));
      float f = deep ? 0.5F : 0.0F;
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      DefaultBiomeFeatures.addIcebergs(lv2);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addBlueIce(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addWaterBiomeOakTrees(lv2);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addDefaultGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      return (new Biome.Builder()).precipitation(true).temperature(f).temperatureModifier(Biome.TemperatureModifier.FROZEN).downfall(0.5F).effects((new BiomeEffects.Builder()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(getSkyColor(f)).moodSound(BiomeMoodSound.CAVE).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
   }

   public static Biome createNormalForest(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean birch, boolean oldGrowth, boolean flower) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv);
      if (flower) {
         lv.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_FOREST_FLOWERS);
      } else {
         DefaultBiomeFeatures.addForestFlowers(lv);
      }

      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      if (flower) {
         lv.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_FLOWER_FOREST);
         lv.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_FLOWER_FOREST);
         DefaultBiomeFeatures.addDefaultGrass(lv);
      } else {
         if (birch) {
            if (oldGrowth) {
               DefaultBiomeFeatures.addTallBirchTrees(lv);
            } else {
               DefaultBiomeFeatures.addBirchTrees(lv);
            }
         } else {
            DefaultBiomeFeatures.addForestTrees(lv);
         }

         DefaultBiomeFeatures.addDefaultFlowers(lv);
         DefaultBiomeFeatures.addForestGrass(lv);
      }

      DefaultBiomeFeatures.addDefaultMushrooms(lv);
      DefaultBiomeFeatures.addDefaultVegetation(lv);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv2);
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      if (flower) {
         lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3));
      } else if (!birch) {
         lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 5, 4, 4));
      }

      float f = birch ? 0.6F : 0.7F;
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_JUNGLE_AND_FOREST);
      return createBiome(true, f, birch ? 0.6F : 0.8F, lv2, lv, lv3);
   }

   public static Biome createTaiga(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean snowy) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv);
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 4, 4)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FOX, 8, 2, 4));
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      float f = snowy ? -0.5F : 0.25F;
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addLargeFerns(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addTaigaTrees(lv2);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addTaigaGrass(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      if (snowy) {
         DefaultBiomeFeatures.addSweetBerryBushesSnowy(lv2);
      } else {
         DefaultBiomeFeatures.addSweetBerryBushes(lv2);
      }

      return createBiome(true, f, snowy ? 0.4F : 0.8F, snowy ? 4020182 : 4159204, 329011, (Integer)null, (Integer)null, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createDarkForest(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv);
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.DARK_FOREST_VEGETATION);
      DefaultBiomeFeatures.addForestFlowers(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addForestGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_JUNGLE_AND_FOREST);
      return (new Biome.Builder()).precipitation(true).temperature(0.7F).downfall(0.8F).effects((new BiomeEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(getSkyColor(0.7F)).grassColorModifier(BiomeEffects.GrassColorModifier.DARK_FOREST).moodSound(BiomeMoodSound.CAVE).music(lv3).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
   }

   public static Biome createSwamp(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv);
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1, 1));
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FROG, 10, 2, 5));
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      DefaultBiomeFeatures.addFossils(lv2);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addClayDisk(lv2);
      DefaultBiomeFeatures.addSwampFeatures(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addSwampVegetation(lv2);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SWAMP);
      return (new Biome.Builder()).precipitation(true).temperature(0.8F).downfall(0.9F).effects((new BiomeEffects.Builder()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(getSkyColor(0.8F)).foliageColor(6975545).grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP).moodSound(BiomeMoodSound.CAVE).music(lv3).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
   }

   public static Biome createMangroveSwamp(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1, 1));
      lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FROG, 10, 2, 5));
      lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      DefaultBiomeFeatures.addFossils(lv2);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addGrassAndClayDisks(lv2);
      DefaultBiomeFeatures.addMangroveSwampFeatures(lv2);
      lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SWAMP);
      return (new Biome.Builder()).precipitation(true).temperature(0.8F).downfall(0.9F).effects((new BiomeEffects.Builder()).waterColor(3832426).waterFogColor(5077600).fogColor(12638463).skyColor(getSkyColor(0.8F)).foliageColor(9285927).grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP).moodSound(BiomeMoodSound.CAVE).music(lv3).build()).spawnSettings(lv.build()).generationSettings(lv2.build()).build();
   }

   public static Biome createRiver(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean frozen) {
      SpawnSettings.Builder lv = (new SpawnSettings.Builder()).spawn(SpawnGroup.WATER_CREATURE, new SpawnSettings.SpawnEntry(EntityType.SQUID, 2, 1, 4)).spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.SALMON, 5, 1, 5));
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      lv.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.DROWNED, frozen ? 1 : 100, 1, 1));
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addWaterBiomeOakTrees(lv2);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addDefaultGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      if (!frozen) {
         lv2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER);
      }

      float f = frozen ? 0.0F : 0.5F;
      return createBiome(true, f, 0.5F, frozen ? 3750089 : 4159204, 329011, (Integer)null, (Integer)null, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createBeach(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean snowy, boolean stony) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      boolean bl3 = !stony && !snowy;
      if (bl3) {
         lv.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.TURTLE, 5, 2, 5));
      }

      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addDefaultFlowers(lv2);
      DefaultBiomeFeatures.addDefaultGrass(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      float f;
      if (snowy) {
         f = 0.05F;
      } else if (stony) {
         f = 0.2F;
      } else {
         f = 0.8F;
      }

      return createBiome(true, f, bl3 ? 0.4F : 0.3F, snowy ? 4020182 : 4159204, 329011, (Integer)null, (Integer)null, lv, lv2, DEFAULT_MUSIC);
   }

   public static Biome createTheVoid(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      lv.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, MiscPlacedFeatures.VOID_START_PLATFORM);
      return createBiome(false, 0.5F, 0.5F, new SpawnSettings.Builder(), lv, DEFAULT_MUSIC);
   }

   public static Biome createMeadow(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup, boolean cherryGrove) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(cherryGrove ? EntityType.PIG : EntityType.DONKEY, 1, 1, 2)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 2, 6)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.SHEEP, 2, 2, 4));
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addPlainsTallGrass(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      if (cherryGrove) {
         DefaultBiomeFeatures.addCherryGroveFeatures(lv);
      } else {
         DefaultBiomeFeatures.addMeadowFlowers(lv);
      }

      DefaultBiomeFeatures.addEmeraldOre(lv);
      DefaultBiomeFeatures.addInfestedStone(lv);
      MusicSound lv3 = MusicType.createIngameMusic(cherryGrove ? SoundEvents.MUSIC_OVERWORLD_CHERRY_GROVE : SoundEvents.MUSIC_OVERWORLD_MEADOW);
      return cherryGrove ? createBiome(true, 0.5F, 0.8F, 6141935, 6141935, 11983713, 11983713, lv2, lv, lv3) : createBiome(true, 0.5F, 0.8F, 937679, 329011, (Integer)null, (Integer)null, lv2, lv, lv3);
   }

   public static Biome createFrozenPeaks(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.GOAT, 5, 1, 3));
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addFrozenLavaSpring(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      DefaultBiomeFeatures.addEmeraldOre(lv);
      DefaultBiomeFeatures.addInfestedStone(lv);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FROZEN_PEAKS);
      return createBiome(true, -0.7F, 0.9F, lv2, lv, lv3);
   }

   public static Biome createJaggedPeaks(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.GOAT, 5, 1, 3));
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addFrozenLavaSpring(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      DefaultBiomeFeatures.addEmeraldOre(lv);
      DefaultBiomeFeatures.addInfestedStone(lv);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_JAGGED_PEAKS);
      return createBiome(true, -0.7F, 0.9F, lv2, lv, lv3);
   }

   public static Biome createStonyPeaks(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      DefaultBiomeFeatures.addEmeraldOre(lv);
      DefaultBiomeFeatures.addInfestedStone(lv);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_STONY_PEAKS);
      return createBiome(true, 1.0F, 0.3F, lv2, lv, lv3);
   }

   public static Biome createSnowySlopes(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.GOAT, 5, 1, 3));
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addFrozenLavaSpring(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      DefaultBiomeFeatures.addDefaultVegetation(lv);
      DefaultBiomeFeatures.addEmeraldOre(lv);
      DefaultBiomeFeatures.addInfestedStone(lv);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_SNOWY_SLOPES);
      return createBiome(true, -0.3F, 0.9F, lv2, lv, lv3);
   }

   public static Biome createGrove(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      GenerationSettings.LookupBackedBuilder lv = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      SpawnSettings.Builder lv2 = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addFarmAnimals(lv2);
      lv2.spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 8, 4, 4)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3)).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.FOX, 8, 2, 4));
      DefaultBiomeFeatures.addBatsAndMonsters(lv2);
      addBasicFeatures(lv);
      DefaultBiomeFeatures.addFrozenLavaSpring(lv);
      DefaultBiomeFeatures.addDefaultOres(lv);
      DefaultBiomeFeatures.addDefaultDisks(lv);
      DefaultBiomeFeatures.addGroveTrees(lv);
      DefaultBiomeFeatures.addDefaultVegetation(lv);
      DefaultBiomeFeatures.addEmeraldOre(lv);
      DefaultBiomeFeatures.addInfestedStone(lv);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_GROVE);
      return createBiome(true, -0.2F, 0.8F, lv2, lv, lv3);
   }

   public static Biome createLushCaves(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      lv.spawn(SpawnGroup.AXOLOTLS, new SpawnSettings.SpawnEntry(EntityType.AXOLOTL, 10, 4, 6));
      lv.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
      DefaultBiomeFeatures.addBatsAndMonsters(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addPlainsTallGrass(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addClayOre(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addLushCavesDecoration(lv2);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_LUSH_CAVES);
      return createBiome(true, 0.5F, 0.5F, lv, lv2, lv3);
   }

   public static Biome createDripstoneCaves(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      DefaultBiomeFeatures.addDripstoneCaveMobs(lv);
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      addBasicFeatures(lv2);
      DefaultBiomeFeatures.addPlainsTallGrass(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2, true);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addPlainsFeatures(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      DefaultBiomeFeatures.addDripstone(lv2);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_DRIPSTONE_CAVES);
      return createBiome(true, 0.8F, 0.4F, lv, lv2, lv3);
   }

   public static Biome createDeepDark(RegistryEntryLookup featureLookup, RegistryEntryLookup carverLookup) {
      SpawnSettings.Builder lv = new SpawnSettings.Builder();
      GenerationSettings.LookupBackedBuilder lv2 = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
      lv2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE);
      lv2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND);
      lv2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CANYON);
      DefaultBiomeFeatures.addAmethystGeodes(lv2);
      DefaultBiomeFeatures.addDungeons(lv2);
      DefaultBiomeFeatures.addMineables(lv2);
      DefaultBiomeFeatures.addFrozenTopLayer(lv2);
      DefaultBiomeFeatures.addPlainsTallGrass(lv2);
      DefaultBiomeFeatures.addDefaultOres(lv2);
      DefaultBiomeFeatures.addDefaultDisks(lv2);
      DefaultBiomeFeatures.addPlainsFeatures(lv2);
      DefaultBiomeFeatures.addDefaultMushrooms(lv2);
      DefaultBiomeFeatures.addDefaultVegetation(lv2);
      DefaultBiomeFeatures.addSculk(lv2);
      MusicSound lv3 = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_DEEP_DARK);
      return createBiome(true, 0.8F, 0.4F, lv, lv2, lv3);
   }
}
