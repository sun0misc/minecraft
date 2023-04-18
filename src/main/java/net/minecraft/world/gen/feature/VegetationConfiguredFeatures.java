package net.minecraft.world.gen.feature;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.DualNoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseThresholdBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

public class VegetationConfiguredFeatures {
   public static final RegistryKey BAMBOO_NO_PODZOL = ConfiguredFeatures.of("bamboo_no_podzol");
   public static final RegistryKey BAMBOO_SOME_PODZOL = ConfiguredFeatures.of("bamboo_some_podzol");
   public static final RegistryKey VINES = ConfiguredFeatures.of("vines");
   public static final RegistryKey PATCH_BROWN_MUSHROOM = ConfiguredFeatures.of("patch_brown_mushroom");
   public static final RegistryKey PATCH_RED_MUSHROOM = ConfiguredFeatures.of("patch_red_mushroom");
   public static final RegistryKey PATCH_SUNFLOWER = ConfiguredFeatures.of("patch_sunflower");
   public static final RegistryKey PATCH_PUMPKIN = ConfiguredFeatures.of("patch_pumpkin");
   public static final RegistryKey PATCH_BERRY_BUSH = ConfiguredFeatures.of("patch_berry_bush");
   public static final RegistryKey PATCH_TAIGA_GRASS = ConfiguredFeatures.of("patch_taiga_grass");
   public static final RegistryKey PATCH_GRASS = ConfiguredFeatures.of("patch_grass");
   public static final RegistryKey PATCH_GRASS_JUNGLE = ConfiguredFeatures.of("patch_grass_jungle");
   public static final RegistryKey SINGLE_PIECE_OF_GRASS = ConfiguredFeatures.of("single_piece_of_grass");
   public static final RegistryKey PATCH_DEAD_BUSH = ConfiguredFeatures.of("patch_dead_bush");
   public static final RegistryKey PATCH_MELON = ConfiguredFeatures.of("patch_melon");
   public static final RegistryKey PATCH_WATERLILY = ConfiguredFeatures.of("patch_waterlily");
   public static final RegistryKey PATCH_TALL_GRASS = ConfiguredFeatures.of("patch_tall_grass");
   public static final RegistryKey PATCH_LARGE_FERN = ConfiguredFeatures.of("patch_large_fern");
   public static final RegistryKey PATCH_CACTUS = ConfiguredFeatures.of("patch_cactus");
   public static final RegistryKey PATCH_SUGAR_CANE = ConfiguredFeatures.of("patch_sugar_cane");
   public static final RegistryKey FLOWER_DEFAULT = ConfiguredFeatures.of("flower_default");
   public static final RegistryKey FLOWER_FLOWER_FOREST = ConfiguredFeatures.of("flower_flower_forest");
   public static final RegistryKey FLOWER_SWAMP = ConfiguredFeatures.of("flower_swamp");
   public static final RegistryKey FLOWER_PLAIN = ConfiguredFeatures.of("flower_plain");
   public static final RegistryKey FLOWER_MEADOW = ConfiguredFeatures.of("flower_meadow");
   public static final RegistryKey FLOWER_CHERRY = ConfiguredFeatures.of("flower_cherry");
   public static final RegistryKey FOREST_FLOWERS = ConfiguredFeatures.of("forest_flowers");
   public static final RegistryKey DARK_FOREST_VEGETATION = ConfiguredFeatures.of("dark_forest_vegetation");
   public static final RegistryKey TREES_FLOWER_FOREST = ConfiguredFeatures.of("trees_flower_forest");
   public static final RegistryKey MEADOW_TREES = ConfiguredFeatures.of("meadow_trees");
   public static final RegistryKey TREES_TAIGA = ConfiguredFeatures.of("trees_taiga");
   public static final RegistryKey TREES_GROVE = ConfiguredFeatures.of("trees_grove");
   public static final RegistryKey TREES_SAVANNA = ConfiguredFeatures.of("trees_savanna");
   public static final RegistryKey BIRCH_TALL = ConfiguredFeatures.of("birch_tall");
   public static final RegistryKey TREES_WINDSWEPT_HILLS = ConfiguredFeatures.of("trees_windswept_hills");
   public static final RegistryKey TREES_WATER = ConfiguredFeatures.of("trees_water");
   public static final RegistryKey TREES_BIRCH_AND_OAK = ConfiguredFeatures.of("trees_birch_and_oak");
   public static final RegistryKey TREES_PLAINS = ConfiguredFeatures.of("trees_plains");
   public static final RegistryKey TREES_SPARSE_JUNGLE = ConfiguredFeatures.of("trees_sparse_jungle");
   public static final RegistryKey TREES_OLD_GROWTH_SPRUCE_TAIGA = ConfiguredFeatures.of("trees_old_growth_spruce_taiga");
   public static final RegistryKey TREES_OLD_GROWTH_PINE_TAIGA = ConfiguredFeatures.of("trees_old_growth_pine_taiga");
   public static final RegistryKey TREES_JUNGLE = ConfiguredFeatures.of("trees_jungle");
   public static final RegistryKey BAMBOO_VEGETATION = ConfiguredFeatures.of("bamboo_vegetation");
   public static final RegistryKey MUSHROOM_ISLAND_VEGETATION = ConfiguredFeatures.of("mushroom_island_vegetation");
   public static final RegistryKey MANGROVE_VEGETATION = ConfiguredFeatures.of("mangrove_vegetation");

   private static RandomPatchFeatureConfig createRandomPatchFeatureConfig(BlockStateProvider block, int tries) {
      return ConfiguredFeatures.createRandomPatchFeatureConfig(tries, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(block))));
   }

   public static void bootstrap(Registerable featureRegisterable) {
      RegistryEntryLookup lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
      RegistryEntry lv2 = lv.getOrThrow(TreeConfiguredFeatures.HUGE_BROWN_MUSHROOM);
      RegistryEntry lv3 = lv.getOrThrow(TreeConfiguredFeatures.HUGE_RED_MUSHROOM);
      RegistryEntry lv4 = lv.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_005);
      RegistryEntry lv5 = lv.getOrThrow(TreeConfiguredFeatures.OAK_BEES_005);
      RegistryEntry lv6 = lv.getOrThrow(PATCH_GRASS_JUNGLE);
      RegistryEntryLookup lv7 = featureRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
      RegistryEntry lv8 = lv7.getOrThrow(TreePlacedFeatures.DARK_OAK_CHECKED);
      RegistryEntry lv9 = lv7.getOrThrow(TreePlacedFeatures.BIRCH_CHECKED);
      RegistryEntry lv10 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_CHECKED);
      RegistryEntry lv11 = lv7.getOrThrow(TreePlacedFeatures.BIRCH_BEES_002);
      RegistryEntry lv12 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES_002);
      RegistryEntry lv13 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES);
      RegistryEntry lv14 = lv7.getOrThrow(TreePlacedFeatures.PINE_CHECKED);
      RegistryEntry lv15 = lv7.getOrThrow(TreePlacedFeatures.SPRUCE_CHECKED);
      RegistryEntry lv16 = lv7.getOrThrow(TreePlacedFeatures.PINE_ON_SNOW);
      RegistryEntry lv17 = lv7.getOrThrow(TreePlacedFeatures.ACACIA_CHECKED);
      RegistryEntry lv18 = lv7.getOrThrow(TreePlacedFeatures.SUPER_BIRCH_BEES_0002);
      RegistryEntry lv19 = lv7.getOrThrow(TreePlacedFeatures.BIRCH_BEES_0002);
      RegistryEntry lv20 = lv7.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES_0002);
      RegistryEntry lv21 = lv7.getOrThrow(TreePlacedFeatures.JUNGLE_BUSH);
      RegistryEntry lv22 = lv7.getOrThrow(TreePlacedFeatures.MEGA_SPRUCE_CHECKED);
      RegistryEntry lv23 = lv7.getOrThrow(TreePlacedFeatures.MEGA_PINE_CHECKED);
      RegistryEntry lv24 = lv7.getOrThrow(TreePlacedFeatures.MEGA_JUNGLE_TREE_CHECKED);
      RegistryEntry lv25 = lv7.getOrThrow(TreePlacedFeatures.TALL_MANGROVE_CHECKED);
      RegistryEntry lv26 = lv7.getOrThrow(TreePlacedFeatures.OAK_CHECKED);
      RegistryEntry lv27 = lv7.getOrThrow(TreePlacedFeatures.OAK_BEES_002);
      RegistryEntry lv28 = lv7.getOrThrow(TreePlacedFeatures.SUPER_BIRCH_BEES);
      RegistryEntry lv29 = lv7.getOrThrow(TreePlacedFeatures.SPRUCE_ON_SNOW);
      RegistryEntry lv30 = lv7.getOrThrow(TreePlacedFeatures.OAK_BEES_0002);
      RegistryEntry lv31 = lv7.getOrThrow(TreePlacedFeatures.JUNGLE_TREE);
      RegistryEntry lv32 = lv7.getOrThrow(TreePlacedFeatures.MANGROVE_CHECKED);
      ConfiguredFeatures.register(featureRegisterable, BAMBOO_NO_PODZOL, Feature.BAMBOO, new ProbabilityConfig(0.0F));
      ConfiguredFeatures.register(featureRegisterable, BAMBOO_SOME_PODZOL, Feature.BAMBOO, new ProbabilityConfig(0.2F));
      ConfiguredFeatures.register(featureRegisterable, VINES, Feature.VINES);
      ConfiguredFeatures.register(featureRegisterable, PATCH_BROWN_MUSHROOM, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BROWN_MUSHROOM))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_RED_MUSHROOM, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.RED_MUSHROOM))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_SUNFLOWER, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.SUNFLOWER))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_PUMPKIN, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.PUMPKIN)), List.of(Blocks.GRASS_BLOCK)));
      ConfiguredFeatures.register(featureRegisterable, PATCH_BERRY_BUSH, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of((BlockState)Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 3))), List.of(Blocks.GRASS_BLOCK)));
      ConfiguredFeatures.register(featureRegisterable, PATCH_TAIGA_GRASS, Feature.RANDOM_PATCH, createRandomPatchFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.GRASS.getDefaultState(), 1).add(Blocks.FERN.getDefaultState(), 4)), 32));
      ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS, Feature.RANDOM_PATCH, createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.GRASS), 32));
      ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS_JUNGLE, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(32, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.GRASS.getDefaultState(), 3).add(Blocks.FERN.getDefaultState(), 1))), (BlockPredicate)BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.not(BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.PODZOL))))));
      ConfiguredFeatures.register(featureRegisterable, SINGLE_PIECE_OF_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.GRASS.getDefaultState())));
      ConfiguredFeatures.register(featureRegisterable, PATCH_DEAD_BUSH, Feature.RANDOM_PATCH, createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.DEAD_BUSH), 4));
      ConfiguredFeatures.register(featureRegisterable, PATCH_MELON, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(64, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.MELON)), (BlockPredicate)BlockPredicate.allOf(BlockPredicate.replaceable(), BlockPredicate.noFluid(), BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.GRASS_BLOCK)))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_WATERLILY, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(10, 7, 3, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILY_PAD))))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_TALL_GRASS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.TALL_GRASS))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_LARGE_FERN, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LARGE_FERN))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_CACTUS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(10, PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, BlockColumnFeatureConfig.create(BiasedToBottomIntProvider.create(1, 3), BlockStateProvider.of(Blocks.CACTUS)), (PlacementModifier[])(BlockFilterPlacementModifier.of(BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.wouldSurvive(Blocks.CACTUS.getDefaultState(), BlockPos.ORIGIN)))))));
      ConfiguredFeatures.register(featureRegisterable, PATCH_SUGAR_CANE, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(20, 4, 0, PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, BlockColumnFeatureConfig.create(BiasedToBottomIntProvider.create(2, 4), BlockStateProvider.of(Blocks.SUGAR_CANE)), (PlacementModifier[])(BlockFilterPlacementModifier.of(BlockPredicate.allOf(BlockPredicate.IS_AIR, BlockPredicate.wouldSurvive(Blocks.SUGAR_CANE.getDefaultState(), BlockPos.ORIGIN), BlockPredicate.anyOf(BlockPredicate.matchingFluids(new BlockPos(1, -1, 0), (Fluid[])(Fluids.WATER, Fluids.FLOWING_WATER)), BlockPredicate.matchingFluids(new BlockPos(-1, -1, 0), (Fluid[])(Fluids.WATER, Fluids.FLOWING_WATER)), BlockPredicate.matchingFluids(new BlockPos(0, -1, 1), (Fluid[])(Fluids.WATER, Fluids.FLOWING_WATER)), BlockPredicate.matchingFluids(new BlockPos(0, -1, -1), (Fluid[])(Fluids.WATER, Fluids.FLOWING_WATER)))))))));
      ConfiguredFeatures.register(featureRegisterable, FLOWER_DEFAULT, Feature.FLOWER, createRandomPatchFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.POPPY.getDefaultState(), 2).add(Blocks.DANDELION.getDefaultState(), 1)), 64));
      ConfiguredFeatures.register(featureRegisterable, FLOWER_FLOWER_FOREST, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(new NoiseBlockStateProvider(2345L, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0, new double[0]), 0.020833334F, List.of(Blocks.DANDELION.getDefaultState(), Blocks.POPPY.getDefaultState(), Blocks.ALLIUM.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.ORANGE_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(), Blocks.LILY_OF_THE_VALLEY.getDefaultState())))))));
      ConfiguredFeatures.register(featureRegisterable, FLOWER_SWAMP, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BLUE_ORCHID))))));
      ConfiguredFeatures.register(featureRegisterable, FLOWER_PLAIN, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(new NoiseThresholdBlockStateProvider(2345L, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0, new double[0]), 0.005F, -0.8F, 0.33333334F, Blocks.DANDELION.getDefaultState(), List.of(Blocks.ORANGE_TULIP.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState()), List.of(Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState())))))));
      ConfiguredFeatures.register(featureRegisterable, FLOWER_MEADOW, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(new DualNoiseBlockStateProvider(new Range(1, 3), new DoublePerlinNoiseSampler.NoiseParameters(-10, 1.0, new double[0]), 1.0F, 2345L, new DoublePerlinNoiseSampler.NoiseParameters(-3, 1.0, new double[0]), 1.0F, List.of(Blocks.TALL_GRASS.getDefaultState(), Blocks.ALLIUM.getDefaultState(), Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.DANDELION.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.GRASS.getDefaultState())))))));
      DataPool.Builder lv33 = DataPool.builder();

      for(int i = 1; i <= 4; ++i) {
         Iterator var35 = Direction.Type.HORIZONTAL.iterator();

         while(var35.hasNext()) {
            Direction lv34 = (Direction)var35.next();
            lv33.add((BlockState)((BlockState)Blocks.PINK_PETALS.getDefaultState().with(FlowerbedBlock.FLOWER_AMOUNT, i)).with(FlowerbedBlock.FACING, lv34), 1);
         }
      }

      ConfiguredFeatures.register(featureRegisterable, FLOWER_CHERRY, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry((Feature)Feature.SIMPLE_BLOCK, (FeatureConfig)(new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(lv33))))));
      ConfiguredFeatures.register(featureRegisterable, FOREST_FLOWERS, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfig(RegistryEntryList.of(PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILAC))), (PlacementModifier[])()), PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.ROSE_BUSH))), (PlacementModifier[])()), PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.PEONY))), (PlacementModifier[])()), PlacedFeatures.createEntry(Feature.NO_BONEMEAL_FLOWER, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILY_OF_THE_VALLEY))), (PlacementModifier[])()))));
      ConfiguredFeatures.register(featureRegisterable, DARK_FOREST_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry((RegistryEntry)lv2, (PlacementModifier[])()), 0.025F), new RandomFeatureEntry(PlacedFeatures.createEntry((RegistryEntry)lv3, (PlacementModifier[])()), 0.05F), new RandomFeatureEntry(lv8, 0.6666667F), new RandomFeatureEntry(lv9, 0.2F), new RandomFeatureEntry(lv10, 0.1F)), lv26));
      ConfiguredFeatures.register(featureRegisterable, TREES_FLOWER_FOREST, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv11, 0.2F), new RandomFeatureEntry(lv12, 0.1F)), lv27));
      ConfiguredFeatures.register(featureRegisterable, MEADOW_TREES, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv13, 0.5F)), lv28));
      ConfiguredFeatures.register(featureRegisterable, TREES_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv14, 0.33333334F)), lv15));
      ConfiguredFeatures.register(featureRegisterable, TREES_GROVE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv16, 0.33333334F)), lv29));
      ConfiguredFeatures.register(featureRegisterable, TREES_SAVANNA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv17, 0.8F)), lv26));
      ConfiguredFeatures.register(featureRegisterable, BIRCH_TALL, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv18, 0.5F)), lv19));
      ConfiguredFeatures.register(featureRegisterable, TREES_WINDSWEPT_HILLS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv15, 0.666F), new RandomFeatureEntry(lv10, 0.1F)), lv26));
      ConfiguredFeatures.register(featureRegisterable, TREES_WATER, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.1F)), lv26));
      ConfiguredFeatures.register(featureRegisterable, TREES_BIRCH_AND_OAK, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv19, 0.2F), new RandomFeatureEntry(lv20, 0.1F)), lv30));
      ConfiguredFeatures.register(featureRegisterable, TREES_PLAINS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry((RegistryEntry)lv4, (PlacementModifier[])()), 0.33333334F)), PlacedFeatures.createEntry((RegistryEntry)lv5, (PlacementModifier[])())));
      ConfiguredFeatures.register(featureRegisterable, TREES_SPARSE_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.1F), new RandomFeatureEntry(lv21, 0.5F)), lv31));
      ConfiguredFeatures.register(featureRegisterable, TREES_OLD_GROWTH_SPRUCE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv22, 0.33333334F), new RandomFeatureEntry(lv14, 0.33333334F)), lv15));
      ConfiguredFeatures.register(featureRegisterable, TREES_OLD_GROWTH_PINE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv22, 0.025641026F), new RandomFeatureEntry(lv23, 0.30769232F), new RandomFeatureEntry(lv14, 0.33333334F)), lv15));
      ConfiguredFeatures.register(featureRegisterable, TREES_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.1F), new RandomFeatureEntry(lv21, 0.5F), new RandomFeatureEntry(lv24, 0.33333334F)), lv31));
      ConfiguredFeatures.register(featureRegisterable, BAMBOO_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv10, 0.05F), new RandomFeatureEntry(lv21, 0.15F), new RandomFeatureEntry(lv24, 0.7F)), PlacedFeatures.createEntry((RegistryEntry)lv6, (PlacementModifier[])())));
      ConfiguredFeatures.register(featureRegisterable, MUSHROOM_ISLAND_VEGETATION, Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfig(PlacedFeatures.createEntry((RegistryEntry)lv3, (PlacementModifier[])()), PlacedFeatures.createEntry((RegistryEntry)lv2, (PlacementModifier[])())));
      ConfiguredFeatures.register(featureRegisterable, MANGROVE_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(lv25, 0.85F)), lv32));
   }
}
