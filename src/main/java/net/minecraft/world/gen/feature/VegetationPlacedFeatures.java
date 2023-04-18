package net.minecraft.world.gen.feature;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.NoiseBasedCountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.NoiseThresholdCountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SurfaceWaterDepthFilterPlacementModifier;
import org.jetbrains.annotations.Nullable;

public class VegetationPlacedFeatures {
   public static final RegistryKey BAMBOO_LIGHT = PlacedFeatures.of("bamboo_light");
   public static final RegistryKey BAMBOO = PlacedFeatures.of("bamboo");
   public static final RegistryKey VINES = PlacedFeatures.of("vines");
   public static final RegistryKey PATCH_SUNFLOWER = PlacedFeatures.of("patch_sunflower");
   public static final RegistryKey PATCH_PUMPKIN = PlacedFeatures.of("patch_pumpkin");
   public static final RegistryKey PATCH_GRASS_PLAIN = PlacedFeatures.of("patch_grass_plain");
   public static final RegistryKey PATCH_GRASS_FOREST = PlacedFeatures.of("patch_grass_forest");
   public static final RegistryKey PATCH_GRASS_BADLANDS = PlacedFeatures.of("patch_grass_badlands");
   public static final RegistryKey PATCH_GRASS_SAVANNA = PlacedFeatures.of("patch_grass_savanna");
   public static final RegistryKey PATCH_GRASS_NORMAL = PlacedFeatures.of("patch_grass_normal");
   public static final RegistryKey PATCH_GRASS_TAIGA_2 = PlacedFeatures.of("patch_grass_taiga_2");
   public static final RegistryKey PATCH_GRASS_TAIGA = PlacedFeatures.of("patch_grass_taiga");
   public static final RegistryKey PATCH_GRASS_JUNGLE = PlacedFeatures.of("patch_grass_jungle");
   public static final RegistryKey GRASS_BONEMEAL = PlacedFeatures.of("grass_bonemeal");
   public static final RegistryKey PATCH_DEAD_BUSH_2 = PlacedFeatures.of("patch_dead_bush_2");
   public static final RegistryKey PATCH_DEAD_BUSH = PlacedFeatures.of("patch_dead_bush");
   public static final RegistryKey PATCH_DEAD_BUSH_BADLANDS = PlacedFeatures.of("patch_dead_bush_badlands");
   public static final RegistryKey PATCH_MELON = PlacedFeatures.of("patch_melon");
   public static final RegistryKey PATCH_MELON_SPARSE = PlacedFeatures.of("patch_melon_sparse");
   public static final RegistryKey PATCH_BERRY_COMMON = PlacedFeatures.of("patch_berry_common");
   public static final RegistryKey PATCH_BERRY_RARE = PlacedFeatures.of("patch_berry_rare");
   public static final RegistryKey PATCH_WATERLILY = PlacedFeatures.of("patch_waterlily");
   public static final RegistryKey PATCH_TALL_GRASS_2 = PlacedFeatures.of("patch_tall_grass_2");
   public static final RegistryKey PATCH_TALL_GRASS = PlacedFeatures.of("patch_tall_grass");
   public static final RegistryKey PATCH_LARGE_FERN = PlacedFeatures.of("patch_large_fern");
   public static final RegistryKey PATCH_CACTUS_DESERT = PlacedFeatures.of("patch_cactus_desert");
   public static final RegistryKey PATCH_CACTUS_DECORATED = PlacedFeatures.of("patch_cactus_decorated");
   public static final RegistryKey PATCH_SUGAR_CANE_SWAMP = PlacedFeatures.of("patch_sugar_cane_swamp");
   public static final RegistryKey PATCH_SUGAR_CANE_DESERT = PlacedFeatures.of("patch_sugar_cane_desert");
   public static final RegistryKey PATCH_SUGAR_CANE_BADLANDS = PlacedFeatures.of("patch_sugar_cane_badlands");
   public static final RegistryKey PATCH_SUGAR_CANE = PlacedFeatures.of("patch_sugar_cane");
   public static final RegistryKey BROWN_MUSHROOM_NETHER = PlacedFeatures.of("brown_mushroom_nether");
   public static final RegistryKey RED_MUSHROOM_NETHER = PlacedFeatures.of("red_mushroom_nether");
   public static final RegistryKey BROWN_MUSHROOM_NORMAL = PlacedFeatures.of("brown_mushroom_normal");
   public static final RegistryKey RED_MUSHROOM_NORMAL = PlacedFeatures.of("red_mushroom_normal");
   public static final RegistryKey BROWN_MUSHROOM_TAIGA = PlacedFeatures.of("brown_mushroom_taiga");
   public static final RegistryKey RED_MUSHROOM_TAIGA = PlacedFeatures.of("red_mushroom_taiga");
   public static final RegistryKey BROWN_MUSHROOM_OLD_GROWTH = PlacedFeatures.of("brown_mushroom_old_growth");
   public static final RegistryKey RED_MUSHROOM_OLD_GROWTH = PlacedFeatures.of("red_mushroom_old_growth");
   public static final RegistryKey BROWN_MUSHROOM_SWAMP = PlacedFeatures.of("brown_mushroom_swamp");
   public static final RegistryKey RED_MUSHROOM_SWAMP = PlacedFeatures.of("red_mushroom_swamp");
   public static final RegistryKey FLOWER_WARM = PlacedFeatures.of("flower_warm");
   public static final RegistryKey FLOWER_DEFAULT = PlacedFeatures.of("flower_default");
   public static final RegistryKey FLOWER_FLOWER_FOREST = PlacedFeatures.of("flower_flower_forest");
   public static final RegistryKey FLOWER_SWAMP = PlacedFeatures.of("flower_swamp");
   public static final RegistryKey FLOWER_PLAIN = PlacedFeatures.of("flower_plains");
   public static final RegistryKey FLOWER_MEADOW = PlacedFeatures.of("flower_meadow");
   public static final RegistryKey FLOWER_CHERRY = PlacedFeatures.of("flower_cherry");
   public static final RegistryKey TREES_PLAINS = PlacedFeatures.of("trees_plains");
   public static final RegistryKey DARK_FOREST_VEGETATION = PlacedFeatures.of("dark_forest_vegetation");
   public static final RegistryKey FLOWER_FOREST_FLOWERS = PlacedFeatures.of("flower_forest_flowers");
   public static final RegistryKey FOREST_FLOWERS = PlacedFeatures.of("forest_flowers");
   public static final RegistryKey TREES_FLOWER_FOREST = PlacedFeatures.of("trees_flower_forest");
   public static final RegistryKey TREES_MEADOW = PlacedFeatures.of("trees_meadow");
   public static final RegistryKey TREES_CHERRY = PlacedFeatures.of("trees_cherry");
   public static final RegistryKey TREES_TAIGA = PlacedFeatures.of("trees_taiga");
   public static final RegistryKey TREES_GROVE = PlacedFeatures.of("trees_grove");
   public static final RegistryKey TREES_BADLANDS = PlacedFeatures.of("trees_badlands");
   public static final RegistryKey TREES_SNOWY = PlacedFeatures.of("trees_snowy");
   public static final RegistryKey TREES_SWAMP = PlacedFeatures.of("trees_swamp");
   public static final RegistryKey TREES_WINDSWEPT_SAVANNA = PlacedFeatures.of("trees_windswept_savanna");
   public static final RegistryKey TREES_SAVANNA = PlacedFeatures.of("trees_savanna");
   public static final RegistryKey BIRCH_TALL = PlacedFeatures.of("birch_tall");
   public static final RegistryKey TREES_BIRCH = PlacedFeatures.of("trees_birch");
   public static final RegistryKey TREES_WINDSWEPT_FOREST = PlacedFeatures.of("trees_windswept_forest");
   public static final RegistryKey TREES_WINDSWEPT_HILLS = PlacedFeatures.of("trees_windswept_hills");
   public static final RegistryKey TREES_WATER = PlacedFeatures.of("trees_water");
   public static final RegistryKey TREES_BIRCH_AND_OAK = PlacedFeatures.of("trees_birch_and_oak");
   public static final RegistryKey TREES_SPARSE_JUNGLE = PlacedFeatures.of("trees_sparse_jungle");
   public static final RegistryKey TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacedFeatures.of("trees_old_growth_spruce_taiga");
   public static final RegistryKey TREES_OLD_GROWTH_PINE_TAIGA = PlacedFeatures.of("trees_old_growth_pine_taiga");
   public static final RegistryKey TREES_JUNGLE = PlacedFeatures.of("trees_jungle");
   public static final RegistryKey BAMBOO_VEGETATION = PlacedFeatures.of("bamboo_vegetation");
   public static final RegistryKey MUSHROOM_ISLAND_VEGETATION = PlacedFeatures.of("mushroom_island_vegetation");
   public static final RegistryKey TREES_MANGROVE = PlacedFeatures.of("trees_mangrove");
   private static final PlacementModifier NOT_IN_SURFACE_WATER_MODIFIER = SurfaceWaterDepthFilterPlacementModifier.of(0);

   public static List modifiers(int count) {
      return List.of(CountPlacementModifier.of(count), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of());
   }

   private static List mushroomModifiers(int chance, @Nullable PlacementModifier modifier) {
      ImmutableList.Builder builder = ImmutableList.builder();
      if (modifier != null) {
         builder.add(modifier);
      }

      if (chance != 0) {
         builder.add(RarityFilterPlacementModifier.of(chance));
      }

      builder.add(SquarePlacementModifier.of());
      builder.add(PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP);
      builder.add(BiomePlacementModifier.of());
      return builder.build();
   }

   private static ImmutableList.Builder treeModifiersBuilder(PlacementModifier countModifier) {
      return ImmutableList.builder().add(countModifier).add(SquarePlacementModifier.of()).add(NOT_IN_SURFACE_WATER_MODIFIER).add(PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP).add(BiomePlacementModifier.of());
   }

   public static List treeModifiers(PlacementModifier modifier) {
      return treeModifiersBuilder(modifier).build();
   }

   public static List treeModifiersWithWouldSurvive(PlacementModifier modifier, Block block) {
      return treeModifiersBuilder(modifier).add(BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(block.getDefaultState(), BlockPos.ORIGIN))).build();
   }

   public static void bootstrap(Registerable featureRegisterable) {
      RegistryEntryLookup lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
      RegistryEntry lv2 = lv.getOrThrow(VegetationConfiguredFeatures.BAMBOO_NO_PODZOL);
      RegistryEntry lv3 = lv.getOrThrow(VegetationConfiguredFeatures.BAMBOO_SOME_PODZOL);
      RegistryEntry lv4 = lv.getOrThrow(VegetationConfiguredFeatures.VINES);
      RegistryEntry lv5 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_SUNFLOWER);
      RegistryEntry lv6 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_PUMPKIN);
      RegistryEntry lv7 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_GRASS);
      RegistryEntry lv8 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_TAIGA_GRASS);
      RegistryEntry lv9 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_GRASS_JUNGLE);
      RegistryEntry lv10 = lv.getOrThrow(VegetationConfiguredFeatures.SINGLE_PIECE_OF_GRASS);
      RegistryEntry lv11 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_DEAD_BUSH);
      RegistryEntry lv12 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_MELON);
      RegistryEntry lv13 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_BERRY_BUSH);
      RegistryEntry lv14 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_WATERLILY);
      RegistryEntry lv15 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_TALL_GRASS);
      RegistryEntry lv16 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_LARGE_FERN);
      RegistryEntry lv17 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_CACTUS);
      RegistryEntry lv18 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_SUGAR_CANE);
      RegistryEntry lv19 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_BROWN_MUSHROOM);
      RegistryEntry lv20 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_RED_MUSHROOM);
      RegistryEntry lv21 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_DEFAULT);
      RegistryEntry lv22 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_FLOWER_FOREST);
      RegistryEntry lv23 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_SWAMP);
      RegistryEntry lv24 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_PLAIN);
      RegistryEntry lv25 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_MEADOW);
      RegistryEntry lv26 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_CHERRY);
      RegistryEntry lv27 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_PLAINS);
      RegistryEntry lv28 = lv.getOrThrow(VegetationConfiguredFeatures.DARK_FOREST_VEGETATION);
      RegistryEntry lv29 = lv.getOrThrow(VegetationConfiguredFeatures.FOREST_FLOWERS);
      RegistryEntry lv30 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_FLOWER_FOREST);
      RegistryEntry lv31 = lv.getOrThrow(VegetationConfiguredFeatures.MEADOW_TREES);
      RegistryEntry lv32 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_TAIGA);
      RegistryEntry lv33 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_GROVE);
      RegistryEntry lv34 = lv.getOrThrow(TreeConfiguredFeatures.OAK);
      RegistryEntry lv35 = lv.getOrThrow(TreeConfiguredFeatures.SPRUCE);
      RegistryEntry lv36 = lv.getOrThrow(TreeConfiguredFeatures.CHERRY_BEES_005);
      RegistryEntry lv37 = lv.getOrThrow(TreeConfiguredFeatures.SWAMP_OAK);
      RegistryEntry lv38 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_SAVANNA);
      RegistryEntry lv39 = lv.getOrThrow(VegetationConfiguredFeatures.BIRCH_TALL);
      RegistryEntry lv40 = lv.getOrThrow(TreeConfiguredFeatures.BIRCH_BEES_0002);
      RegistryEntry lv41 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_WINDSWEPT_HILLS);
      RegistryEntry lv42 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_WATER);
      RegistryEntry lv43 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_BIRCH_AND_OAK);
      RegistryEntry lv44 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_SPARSE_JUNGLE);
      RegistryEntry lv45 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
      RegistryEntry lv46 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
      RegistryEntry lv47 = lv.getOrThrow(VegetationConfiguredFeatures.TREES_JUNGLE);
      RegistryEntry lv48 = lv.getOrThrow(VegetationConfiguredFeatures.BAMBOO_VEGETATION);
      RegistryEntry lv49 = lv.getOrThrow(VegetationConfiguredFeatures.MUSHROOM_ISLAND_VEGETATION);
      RegistryEntry lv50 = lv.getOrThrow(VegetationConfiguredFeatures.MANGROVE_VEGETATION);
      PlacedFeatures.register(featureRegisterable, BAMBOO_LIGHT, lv2, (PlacementModifier[])(RarityFilterPlacementModifier.of(4), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, BAMBOO, lv3, (PlacementModifier[])(NoiseBasedCountPlacementModifier.of(160, 80.0, 0.3), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, VINES, lv4, (PlacementModifier[])(CountPlacementModifier.of(127), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.fixed(64), YOffset.fixed(100)), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_SUNFLOWER, lv5, (PlacementModifier[])(RarityFilterPlacementModifier.of(3), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_PUMPKIN, lv6, (PlacementModifier[])(RarityFilterPlacementModifier.of(300), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_PLAIN, lv7, (PlacementModifier[])(NoiseThresholdCountPlacementModifier.of(-0.8, 5, 10), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_FOREST, lv7, (List)modifiers(2));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_BADLANDS, lv7, (PlacementModifier[])(SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_SAVANNA, lv7, (List)modifiers(20));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_NORMAL, lv7, (List)modifiers(5));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_TAIGA_2, lv8, (PlacementModifier[])(SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_TAIGA, lv8, (List)modifiers(7));
      PlacedFeatures.register(featureRegisterable, PATCH_GRASS_JUNGLE, lv9, (List)modifiers(25));
      PlacedFeatures.register(featureRegisterable, GRASS_BONEMEAL, lv10, (PlacementModifier[])(PlacedFeatures.isAir()));
      PlacedFeatures.register(featureRegisterable, PATCH_DEAD_BUSH_2, lv11, (List)modifiers(2));
      PlacedFeatures.register(featureRegisterable, PATCH_DEAD_BUSH, lv11, (PlacementModifier[])(SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_DEAD_BUSH_BADLANDS, lv11, (List)modifiers(20));
      PlacedFeatures.register(featureRegisterable, PATCH_MELON, lv12, (PlacementModifier[])(RarityFilterPlacementModifier.of(6), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_MELON_SPARSE, lv12, (PlacementModifier[])(RarityFilterPlacementModifier.of(64), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_BERRY_COMMON, lv13, (PlacementModifier[])(RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_BERRY_RARE, lv13, (PlacementModifier[])(RarityFilterPlacementModifier.of(384), SquarePlacementModifier.of(), PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_WATERLILY, lv14, (List)modifiers(4));
      PlacedFeatures.register(featureRegisterable, PATCH_TALL_GRASS_2, lv15, (PlacementModifier[])(NoiseThresholdCountPlacementModifier.of(-0.8, 0, 7), RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_TALL_GRASS, lv15, (PlacementModifier[])(RarityFilterPlacementModifier.of(5), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_LARGE_FERN, lv16, (PlacementModifier[])(RarityFilterPlacementModifier.of(5), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_CACTUS_DESERT, lv17, (PlacementModifier[])(RarityFilterPlacementModifier.of(6), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_CACTUS_DECORATED, lv17, (PlacementModifier[])(RarityFilterPlacementModifier.of(13), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE_SWAMP, lv18, (PlacementModifier[])(RarityFilterPlacementModifier.of(3), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE_DESERT, lv18, (PlacementModifier[])(SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE_BADLANDS, lv18, (PlacementModifier[])(RarityFilterPlacementModifier.of(5), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, PATCH_SUGAR_CANE, lv18, (PlacementModifier[])(RarityFilterPlacementModifier.of(6), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_NETHER, lv19, (PlacementModifier[])(RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_NETHER, lv20, (PlacementModifier[])(RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.BOTTOM_TO_TOP_RANGE, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_NORMAL, lv19, (List)mushroomModifiers(256, (PlacementModifier)null));
      PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_NORMAL, lv20, (List)mushroomModifiers(512, (PlacementModifier)null));
      PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_TAIGA, lv19, (List)mushroomModifiers(4, (PlacementModifier)null));
      PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_TAIGA, lv20, (List)mushroomModifiers(256, (PlacementModifier)null));
      PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_OLD_GROWTH, lv19, (List)mushroomModifiers(4, CountPlacementModifier.of(3)));
      PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_OLD_GROWTH, lv20, (List)mushroomModifiers(171, (PlacementModifier)null));
      PlacedFeatures.register(featureRegisterable, BROWN_MUSHROOM_SWAMP, lv19, (List)mushroomModifiers(0, CountPlacementModifier.of(2)));
      PlacedFeatures.register(featureRegisterable, RED_MUSHROOM_SWAMP, lv20, (List)mushroomModifiers(64, (PlacementModifier)null));
      PlacedFeatures.register(featureRegisterable, FLOWER_WARM, lv21, (PlacementModifier[])(RarityFilterPlacementModifier.of(16), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_DEFAULT, lv21, (PlacementModifier[])(RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_FLOWER_FOREST, lv22, (PlacementModifier[])(CountPlacementModifier.of(3), RarityFilterPlacementModifier.of(2), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_SWAMP, lv23, (PlacementModifier[])(RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_PLAIN, lv24, (PlacementModifier[])(NoiseThresholdCountPlacementModifier.of(-0.8, 15, 4), RarityFilterPlacementModifier.of(32), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_CHERRY, lv26, (PlacementModifier[])(NoiseThresholdCountPlacementModifier.of(-0.8, 5, 10), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_MEADOW, lv25, (PlacementModifier[])(SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacementModifier lv51 = SurfaceWaterDepthFilterPlacementModifier.of(0);
      PlacedFeatures.register(featureRegisterable, TREES_PLAINS, lv27, (PlacementModifier[])(PlacedFeatures.createCountExtraModifier(0, 0.05F, 1), SquarePlacementModifier.of(), lv51, PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, DARK_FOREST_VEGETATION, lv28, (PlacementModifier[])(CountPlacementModifier.of(16), SquarePlacementModifier.of(), lv51, PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FLOWER_FOREST_FLOWERS, lv29, (PlacementModifier[])(RarityFilterPlacementModifier.of(7), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-1, 3), 0, 3)), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, FOREST_FLOWERS, lv29, (PlacementModifier[])(RarityFilterPlacementModifier.of(7), SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, CountPlacementModifier.of(ClampedIntProvider.create(UniformIntProvider.create(-3, 1), 0, 1)), BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, TREES_FLOWER_FOREST, lv30, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(6, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_MEADOW, lv31, (List)treeModifiers(RarityFilterPlacementModifier.of(100)));
      PlacedFeatures.register(featureRegisterable, TREES_CHERRY, lv36, (List)treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1), Blocks.CHERRY_SAPLING));
      PlacedFeatures.register(featureRegisterable, TREES_TAIGA, lv32, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_GROVE, lv33, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_BADLANDS, lv34, (List)treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(5, 0.1F, 1), Blocks.OAK_SAPLING));
      PlacedFeatures.register(featureRegisterable, TREES_SNOWY, lv35, (List)treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.1F, 1), Blocks.SPRUCE_SAPLING));
      PlacedFeatures.register(featureRegisterable, TREES_SWAMP, lv37, (PlacementModifier[])(PlacedFeatures.createCountExtraModifier(2, 0.1F, 1), SquarePlacementModifier.of(), SurfaceWaterDepthFilterPlacementModifier.of(2), PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of(), BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN))));
      PlacedFeatures.register(featureRegisterable, TREES_WINDSWEPT_SAVANNA, lv38, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(2, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_SAVANNA, lv38, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(1, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, BIRCH_TALL, lv39, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_BIRCH, lv40, (List)treeModifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1), Blocks.BIRCH_SAPLING));
      PlacedFeatures.register(featureRegisterable, TREES_WINDSWEPT_FOREST, lv41, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(3, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_WINDSWEPT_HILLS, lv41, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(0, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_WATER, lv42, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(0, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_BIRCH_AND_OAK, lv43, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_SPARSE_JUNGLE, lv44, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(2, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_OLD_GROWTH_SPRUCE_TAIGA, lv45, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_OLD_GROWTH_PINE_TAIGA, lv46, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, TREES_JUNGLE, lv47, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(50, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, BAMBOO_VEGETATION, lv48, (List)treeModifiers(PlacedFeatures.createCountExtraModifier(30, 0.1F, 1)));
      PlacedFeatures.register(featureRegisterable, MUSHROOM_ISLAND_VEGETATION, lv49, (PlacementModifier[])(SquarePlacementModifier.of(), PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP, BiomePlacementModifier.of()));
      PlacedFeatures.register(featureRegisterable, TREES_MANGROVE, lv50, (PlacementModifier[])(CountPlacementModifier.of(25), SquarePlacementModifier.of(), SurfaceWaterDepthFilterPlacementModifier.of(5), PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of(), BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.getDefaultState(), BlockPos.ORIGIN))));
   }
}
