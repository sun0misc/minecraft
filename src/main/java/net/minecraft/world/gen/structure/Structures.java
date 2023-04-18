package net.minecraft.world.gen.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.AncientCityGenerator;
import net.minecraft.structure.BastionRemnantGenerator;
import net.minecraft.structure.DesertVillageData;
import net.minecraft.structure.PillagerOutpostGenerator;
import net.minecraft.structure.PlainsVillageData;
import net.minecraft.structure.RuinedPortalStructurePiece;
import net.minecraft.structure.SavannaVillageData;
import net.minecraft.structure.SnowyVillageData;
import net.minecraft.structure.TaigaVillageData;
import net.minecraft.structure.TrailRuinsGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.ConstantHeightProvider;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;

public class Structures {
   private static Structure.Config createConfig(RegistryEntryList biomes, Map spawns, GenerationStep.Feature featureStep, StructureTerrainAdaptation terrainAdaptation) {
      return new Structure.Config(biomes, spawns, featureStep, terrainAdaptation);
   }

   private static Structure.Config createConfig(RegistryEntryList biomes, GenerationStep.Feature featureStep, StructureTerrainAdaptation terrainAdaptation) {
      return createConfig(biomes, Map.of(), featureStep, terrainAdaptation);
   }

   private static Structure.Config createConfig(RegistryEntryList biomes, StructureTerrainAdaptation terrainAdaptation) {
      return createConfig(biomes, Map.of(), GenerationStep.Feature.SURFACE_STRUCTURES, terrainAdaptation);
   }

   public static void bootstrap(Registerable structureRegisterable) {
      RegistryEntryLookup lv = structureRegisterable.getRegistryLookup(RegistryKeys.BIOME);
      RegistryEntryLookup lv2 = structureRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
      structureRegisterable.register(StructureKeys.PILLAGER_OUTPOST, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.PILLAGER_OUTPOST_HAS_STRUCTURE), Map.of(SpawnGroup.MONSTER, new StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, Pool.of((Weighted[])(new SpawnSettings.SpawnEntry(EntityType.PILLAGER, 1, 1, 1))))), GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.BEARD_THIN), lv2.getOrThrow(PillagerOutpostGenerator.STRUCTURE_POOLS), 7, ConstantHeightProvider.create(YOffset.fixed(0)), true, Heightmap.Type.WORLD_SURFACE_WG));
      structureRegisterable.register(StructureKeys.MINESHAFT, new MineshaftStructure(createConfig(lv.getOrThrow(BiomeTags.MINESHAFT_HAS_STRUCTURE), GenerationStep.Feature.UNDERGROUND_STRUCTURES, StructureTerrainAdaptation.NONE), MineshaftStructure.Type.NORMAL));
      structureRegisterable.register(StructureKeys.MINESHAFT_MESA, new MineshaftStructure(createConfig(lv.getOrThrow(BiomeTags.MINESHAFT_MESA_HAS_STRUCTURE), GenerationStep.Feature.UNDERGROUND_STRUCTURES, StructureTerrainAdaptation.NONE), MineshaftStructure.Type.MESA));
      structureRegisterable.register(StructureKeys.MANSION, new WoodlandMansionStructure(createConfig(lv.getOrThrow(BiomeTags.WOODLAND_MANSION_HAS_STRUCTURE), StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.JUNGLE_PYRAMID, new JungleTempleStructure(createConfig(lv.getOrThrow(BiomeTags.JUNGLE_TEMPLE_HAS_STRUCTURE), StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.DESERT_PYRAMID, new DesertPyramidStructure(createConfig(lv.getOrThrow(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE), StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.IGLOO, new IglooStructure(createConfig(lv.getOrThrow(BiomeTags.IGLOO_HAS_STRUCTURE), StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.SHIPWRECK, new ShipwreckStructure(createConfig(lv.getOrThrow(BiomeTags.SHIPWRECK_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), false));
      structureRegisterable.register(StructureKeys.SHIPWRECK_BEACHED, new ShipwreckStructure(createConfig(lv.getOrThrow(BiomeTags.SHIPWRECK_BEACHED_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), true));
      structureRegisterable.register(StructureKeys.SWAMP_HUT, new SwampHutStructure(createConfig(lv.getOrThrow(BiomeTags.SWAMP_HUT_HAS_STRUCTURE), Map.of(SpawnGroup.MONSTER, new StructureSpawns(StructureSpawns.BoundingBox.PIECE, Pool.of((Weighted[])(new SpawnSettings.SpawnEntry(EntityType.WITCH, 1, 1, 1)))), SpawnGroup.CREATURE, new StructureSpawns(StructureSpawns.BoundingBox.PIECE, Pool.of((Weighted[])(new SpawnSettings.SpawnEntry(EntityType.CAT, 1, 1, 1))))), GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.STRONGHOLD, new StrongholdStructure(createConfig(lv.getOrThrow(BiomeTags.STRONGHOLD_HAS_STRUCTURE), StructureTerrainAdaptation.BURY)));
      structureRegisterable.register(StructureKeys.MONUMENT, new OceanMonumentStructure(createConfig(lv.getOrThrow(BiomeTags.OCEAN_MONUMENT_HAS_STRUCTURE), Map.of(SpawnGroup.MONSTER, new StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, Pool.of((Weighted[])(new SpawnSettings.SpawnEntry(EntityType.GUARDIAN, 1, 2, 4)))), SpawnGroup.UNDERGROUND_WATER_CREATURE, new StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, SpawnSettings.EMPTY_ENTRY_POOL), SpawnGroup.AXOLOTLS, new StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, SpawnSettings.EMPTY_ENTRY_POOL)), GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.OCEAN_RUIN_COLD, new OceanRuinStructure(createConfig(lv.getOrThrow(BiomeTags.OCEAN_RUIN_COLD_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), OceanRuinStructure.BiomeTemperature.COLD, 0.3F, 0.9F));
      structureRegisterable.register(StructureKeys.OCEAN_RUIN_WARM, new OceanRuinStructure(createConfig(lv.getOrThrow(BiomeTags.OCEAN_RUIN_WARM_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), OceanRuinStructure.BiomeTemperature.WARM, 0.3F, 0.9F));
      structureRegisterable.register(StructureKeys.FORTRESS, new NetherFortressStructure(createConfig(lv.getOrThrow(BiomeTags.NETHER_FORTRESS_HAS_STRUCTURE), Map.of(SpawnGroup.MONSTER, new StructureSpawns(StructureSpawns.BoundingBox.PIECE, NetherFortressStructure.MONSTER_SPAWNS)), GenerationStep.Feature.UNDERGROUND_DECORATION, StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.NETHER_FOSSIL, new NetherFossilStructure(createConfig(lv.getOrThrow(BiomeTags.NETHER_FOSSIL_HAS_STRUCTURE), GenerationStep.Feature.UNDERGROUND_DECORATION, StructureTerrainAdaptation.BEARD_THIN), UniformHeightProvider.create(YOffset.fixed(32), YOffset.belowTop(2))));
      structureRegisterable.register(StructureKeys.END_CITY, new EndCityStructure(createConfig(lv.getOrThrow(BiomeTags.END_CITY_HAS_STRUCTURE), StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.BURIED_TREASURE, new BuriedTreasureStructure(createConfig(lv.getOrThrow(BiomeTags.BURIED_TREASURE_HAS_STRUCTURE), GenerationStep.Feature.UNDERGROUND_STRUCTURES, StructureTerrainAdaptation.NONE)));
      structureRegisterable.register(StructureKeys.BASTION_REMNANT, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.BASTION_REMNANT_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), lv2.getOrThrow(BastionRemnantGenerator.STRUCTURE_POOLS), 6, ConstantHeightProvider.create(YOffset.fixed(33)), false));
      structureRegisterable.register(StructureKeys.VILLAGE_PLAINS, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE), StructureTerrainAdaptation.BEARD_THIN), lv2.getOrThrow(PlainsVillageData.TOWN_CENTERS_KEY), 6, ConstantHeightProvider.create(YOffset.fixed(0)), true, Heightmap.Type.WORLD_SURFACE_WG));
      structureRegisterable.register(StructureKeys.VILLAGE_DESERT, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.VILLAGE_DESERT_HAS_STRUCTURE), StructureTerrainAdaptation.BEARD_THIN), lv2.getOrThrow(DesertVillageData.TOWN_CENTERS_KEY), 6, ConstantHeightProvider.create(YOffset.fixed(0)), true, Heightmap.Type.WORLD_SURFACE_WG));
      structureRegisterable.register(StructureKeys.VILLAGE_SAVANNA, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.VILLAGE_SAVANNA_HAS_STRUCTURE), StructureTerrainAdaptation.BEARD_THIN), lv2.getOrThrow(SavannaVillageData.TOWN_CENTERS_KEY), 6, ConstantHeightProvider.create(YOffset.fixed(0)), true, Heightmap.Type.WORLD_SURFACE_WG));
      structureRegisterable.register(StructureKeys.VILLAGE_SNOWY, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.VILLAGE_SNOWY_HAS_STRUCTURE), StructureTerrainAdaptation.BEARD_THIN), lv2.getOrThrow(SnowyVillageData.TOWN_CENTERS_KEY), 6, ConstantHeightProvider.create(YOffset.fixed(0)), true, Heightmap.Type.WORLD_SURFACE_WG));
      structureRegisterable.register(StructureKeys.VILLAGE_TAIGA, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.VILLAGE_TAIGA_HAS_STRUCTURE), StructureTerrainAdaptation.BEARD_THIN), lv2.getOrThrow(TaigaVillageData.TOWN_CENTERS_KEY), 6, ConstantHeightProvider.create(YOffset.fixed(0)), true, Heightmap.Type.WORLD_SURFACE_WG));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_STANDARD_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), List.of(new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.UNDERGROUND, 1.0F, 0.2F, false, false, true, false, 0.5F), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F))));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL_DESERT, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_DESERT_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.PARTLY_BURIED, 0.0F, 0.0F, false, false, false, false, 1.0F)));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL_JUNGLE, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_JUNGLE_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.8F, true, true, false, false, 1.0F)));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL_SWAMP, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.5F, false, true, false, false, 1.0F)));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL_MOUNTAIN, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_MOUNTAIN_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), List.of(new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.IN_MOUNTAIN, 1.0F, 0.2F, false, false, true, false, 0.5F), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F))));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL_OCEAN, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_OCEAN_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.8F, false, false, true, false, 1.0F)));
      structureRegisterable.register(StructureKeys.RUINED_PORTAL_NETHER, new RuinedPortalStructure(createConfig(lv.getOrThrow(BiomeTags.RUINED_PORTAL_NETHER_HAS_STRUCTURE), StructureTerrainAdaptation.NONE), new RuinedPortalStructure.Setup(RuinedPortalStructurePiece.VerticalPlacement.IN_NETHER, 0.5F, 0.0F, false, false, false, true, 1.0F)));
      structureRegisterable.register(StructureKeys.ANCIENT_CITY, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.ANCIENT_CITY_HAS_STRUCTURE), (Map)Arrays.stream(SpawnGroup.values()).collect(Collectors.toMap((arg) -> {
         return arg;
      }, (arg) -> {
         return new StructureSpawns(StructureSpawns.BoundingBox.STRUCTURE, Pool.empty());
      })), GenerationStep.Feature.UNDERGROUND_DECORATION, StructureTerrainAdaptation.BEARD_BOX), lv2.getOrThrow(AncientCityGenerator.CITY_CENTER), Optional.of(new Identifier("city_anchor")), 7, ConstantHeightProvider.create(YOffset.fixed(-27)), false, Optional.empty(), 116));
      structureRegisterable.register(StructureKeys.TRAIL_RUINS, new JigsawStructure(createConfig(lv.getOrThrow(BiomeTags.TRAIL_RUINS_HAS_STRUCTURE), Map.of(), GenerationStep.Feature.UNDERGROUND_STRUCTURES, StructureTerrainAdaptation.BURY), lv2.getOrThrow(TrailRuinsGenerator.TOWER), 7, ConstantHeightProvider.create(YOffset.fixed(-15)), false, Heightmap.Type.WORLD_SURFACE_WG));
   }
}
