/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.SmallDripleafBlock;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.floatprovider.ClampedNormalFloatProvider;
import net.minecraft.util.math.floatprovider.UniformFloatProvider;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.intprovider.WeightedListIntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.BlockColumnFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DripstoneClusterFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FossilFeatureConfig;
import net.minecraft.world.gen.feature.GeodeCrackConfig;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.GeodeLayerConfig;
import net.minecraft.world.gen.feature.GeodeLayerThicknessConfig;
import net.minecraft.world.gen.feature.LargeDripstoneFeatureConfig;
import net.minecraft.world.gen.feature.MultifaceGrowthFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.gen.feature.RootSystemFeatureConfig;
import net.minecraft.world.gen.feature.SculkPatchFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.SimpleRandomFeatureConfig;
import net.minecraft.world.gen.feature.SmallDripstoneFeatureConfig;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.UnderwaterMagmaFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RandomOffsetPlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

public class UndergroundConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> MONSTER_ROOM = ConfiguredFeatures.of("monster_room");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FOSSIL_COAL = ConfiguredFeatures.of("fossil_coal");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FOSSIL_DIAMONDS = ConfiguredFeatures.of("fossil_diamonds");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DRIPSTONE_CLUSTER = ConfiguredFeatures.of("dripstone_cluster");
    public static final RegistryKey<ConfiguredFeature<?, ?>> LARGE_DRIPSTONE = ConfiguredFeatures.of("large_dripstone");
    public static final RegistryKey<ConfiguredFeature<?, ?>> POINTED_DRIPSTONE = ConfiguredFeatures.of("pointed_dripstone");
    public static final RegistryKey<ConfiguredFeature<?, ?>> UNDERWATER_MAGMA = ConfiguredFeatures.of("underwater_magma");
    public static final RegistryKey<ConfiguredFeature<?, ?>> GLOW_LICHEN = ConfiguredFeatures.of("glow_lichen");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ROOTED_AZALEA_TREE = ConfiguredFeatures.of("rooted_azalea_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CAVE_VINE = ConfiguredFeatures.of("cave_vine");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CAVE_VINE_IN_MOSS = ConfiguredFeatures.of("cave_vine_in_moss");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOSS_VEGETATION = ConfiguredFeatures.of("moss_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOSS_PATCH = ConfiguredFeatures.of("moss_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOSS_PATCH_BONEMEAL = ConfiguredFeatures.of("moss_patch_bonemeal");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DRIPLEAF = ConfiguredFeatures.of("dripleaf");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CLAY_WITH_DRIPLEAVES = ConfiguredFeatures.of("clay_with_dripleaves");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CLAY_POOL_WITH_DRIPLEAVES = ConfiguredFeatures.of("clay_pool_with_dripleaves");
    public static final RegistryKey<ConfiguredFeature<?, ?>> LUSH_CAVES_CLAY = ConfiguredFeatures.of("lush_caves_clay");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOSS_PATCH_CEILING = ConfiguredFeatures.of("moss_patch_ceiling");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SPORE_BLOSSOM = ConfiguredFeatures.of("spore_blossom");
    public static final RegistryKey<ConfiguredFeature<?, ?>> AMETHYST_GEODE = ConfiguredFeatures.of("amethyst_geode");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SCULK_PATCH_DEEP_DARK = ConfiguredFeatures.of("sculk_patch_deep_dark");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SCULK_PATCH_ANCIENT_CITY = ConfiguredFeatures.of("sculk_patch_ancient_city");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SCULK_VEIN = ConfiguredFeatures.of("sculk_vein");

    private static RegistryEntry<PlacedFeature> createBigDripleafFeature(Direction direction) {
        return PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, new BlockColumnFeatureConfig(List.of(BlockColumnFeatureConfig.createLayer(new WeightedListIntProvider(DataPool.builder().add(UniformIntProvider.create(0, 4), 2).add((UniformIntProvider)((Object)ConstantIntProvider.create(0)), 1).build()), BlockStateProvider.of((BlockState)Blocks.BIG_DRIPLEAF_STEM.getDefaultState().with(Properties.HORIZONTAL_FACING, direction))), BlockColumnFeatureConfig.createLayer(ConstantIntProvider.create(1), BlockStateProvider.of((BlockState)Blocks.BIG_DRIPLEAF.getDefaultState().with(Properties.HORIZONTAL_FACING, direction)))), Direction.UP, BlockPredicate.IS_AIR_OR_WATER, true), new PlacementModifier[0]);
    }

    private static RegistryEntry<PlacedFeature> createSmallDripleafFeature() {
        return PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add((BlockState)Blocks.SMALL_DRIPLEAF.getDefaultState().with(SmallDripleafBlock.FACING, Direction.EAST), 1).add((BlockState)Blocks.SMALL_DRIPLEAF.getDefaultState().with(SmallDripleafBlock.FACING, Direction.WEST), 1).add((BlockState)Blocks.SMALL_DRIPLEAF.getDefaultState().with(SmallDripleafBlock.FACING, Direction.NORTH), 1).add((BlockState)Blocks.SMALL_DRIPLEAF.getDefaultState().with(SmallDripleafBlock.FACING, Direction.SOUTH), 1))), new PlacementModifier[0]);
    }

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntryLookup<StructureProcessorList> lv2 = featureRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        ConfiguredFeatures.register(featureRegisterable, MONSTER_ROOM, Feature.MONSTER_ROOM);
        List<Identifier> list = List.of(Identifier.method_60656("fossil/spine_1"), Identifier.method_60656("fossil/spine_2"), Identifier.method_60656("fossil/spine_3"), Identifier.method_60656("fossil/spine_4"), Identifier.method_60656("fossil/skull_1"), Identifier.method_60656("fossil/skull_2"), Identifier.method_60656("fossil/skull_3"), Identifier.method_60656("fossil/skull_4"));
        List<Identifier> list2 = List.of(Identifier.method_60656("fossil/spine_1_coal"), Identifier.method_60656("fossil/spine_2_coal"), Identifier.method_60656("fossil/spine_3_coal"), Identifier.method_60656("fossil/spine_4_coal"), Identifier.method_60656("fossil/skull_1_coal"), Identifier.method_60656("fossil/skull_2_coal"), Identifier.method_60656("fossil/skull_3_coal"), Identifier.method_60656("fossil/skull_4_coal"));
        RegistryEntry.Reference<StructureProcessorList> lv3 = lv2.getOrThrow(StructureProcessorLists.FOSSIL_ROT);
        ConfiguredFeatures.register(featureRegisterable, FOSSIL_COAL, Feature.FOSSIL, new FossilFeatureConfig(list, list2, lv3, lv2.getOrThrow(StructureProcessorLists.FOSSIL_COAL), 4));
        ConfiguredFeatures.register(featureRegisterable, FOSSIL_DIAMONDS, Feature.FOSSIL, new FossilFeatureConfig(list, list2, lv3, lv2.getOrThrow(StructureProcessorLists.FOSSIL_DIAMONDS), 4));
        ConfiguredFeatures.register(featureRegisterable, DRIPSTONE_CLUSTER, Feature.DRIPSTONE_CLUSTER, new DripstoneClusterFeatureConfig(12, UniformIntProvider.create(3, 6), UniformIntProvider.create(2, 8), 1, 3, UniformIntProvider.create(2, 4), UniformFloatProvider.create(0.3f, 0.7f), ClampedNormalFloatProvider.create(0.1f, 0.3f, 0.1f, 0.9f), 0.1f, 3, 8));
        ConfiguredFeatures.register(featureRegisterable, LARGE_DRIPSTONE, Feature.LARGE_DRIPSTONE, new LargeDripstoneFeatureConfig(30, UniformIntProvider.create(3, 19), UniformFloatProvider.create(0.4f, 2.0f), 0.33f, UniformFloatProvider.create(0.3f, 0.9f), UniformFloatProvider.create(0.4f, 1.0f), UniformFloatProvider.create(0.0f, 0.3f), 4, 0.6f));
        ConfiguredFeatures.register(featureRegisterable, POINTED_DRIPSTONE, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfig(RegistryEntryList.of(PlacedFeatures.createEntry(Feature.POINTED_DRIPSTONE, new SmallDripstoneFeatureConfig(0.2f, 0.7f, 0.5f, 0.5f), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR_OR_WATER, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(1))), PlacedFeatures.createEntry(Feature.POINTED_DRIPSTONE, new SmallDripstoneFeatureConfig(0.2f, 0.7f, 0.5f, 0.5f), EnvironmentScanPlacementModifier.of(Direction.UP, BlockPredicate.solid(), BlockPredicate.IS_AIR_OR_WATER, 12), RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1))))));
        ConfiguredFeatures.register(featureRegisterable, UNDERWATER_MAGMA, Feature.UNDERWATER_MAGMA, new UnderwaterMagmaFeatureConfig(5, 1, 0.5f));
        MultifaceGrowthBlock lv4 = (MultifaceGrowthBlock)Blocks.GLOW_LICHEN;
        ConfiguredFeatures.register(featureRegisterable, GLOW_LICHEN, Feature.MULTIFACE_GROWTH, new MultifaceGrowthFeatureConfig(lv4, 20, false, true, true, 0.5f, RegistryEntryList.of(Block::getRegistryEntry, Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE)));
        ConfiguredFeatures.register(featureRegisterable, ROOTED_AZALEA_TREE, Feature.ROOT_SYSTEM, new RootSystemFeatureConfig(PlacedFeatures.createEntry(lv.getOrThrow(TreeConfiguredFeatures.AZALEA_TREE), new PlacementModifier[0]), 3, 3, BlockTags.AZALEA_ROOT_REPLACEABLE, BlockStateProvider.of(Blocks.ROOTED_DIRT), 20, 100, 3, 2, BlockStateProvider.of(Blocks.HANGING_ROOTS), 20, 2, BlockPredicate.bothOf(BlockPredicate.eitherOf(BlockPredicate.matchingBlocks(List.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR)), BlockPredicate.matchingBlockTag(BlockTags.REPLACEABLE_BY_TREES)), BlockPredicate.matchingBlockTag(Direction.DOWN.getVector(), BlockTags.AZALEA_GROWS_ON))));
        WeightedBlockStateProvider lv5 = new WeightedBlockStateProvider(DataPool.builder().add(Blocks.CAVE_VINES_PLANT.getDefaultState(), 4).add((BlockState)Blocks.CAVE_VINES_PLANT.getDefaultState().with(CaveVines.BERRIES, true), 1));
        RandomizedIntBlockStateProvider lv6 = new RandomizedIntBlockStateProvider((BlockStateProvider)new WeightedBlockStateProvider(DataPool.builder().add(Blocks.CAVE_VINES.getDefaultState(), 4).add((BlockState)Blocks.CAVE_VINES.getDefaultState().with(CaveVines.BERRIES, true), 1)), CaveVinesHeadBlock.AGE, (IntProvider)UniformIntProvider.create(23, 25));
        ConfiguredFeatures.register(featureRegisterable, CAVE_VINE, Feature.BLOCK_COLUMN, new BlockColumnFeatureConfig(List.of(BlockColumnFeatureConfig.createLayer(new WeightedListIntProvider(DataPool.builder().add(UniformIntProvider.create(0, 19), 2).add(UniformIntProvider.create(0, 2), 3).add(UniformIntProvider.create(0, 6), 10).build()), lv5), BlockColumnFeatureConfig.createLayer(ConstantIntProvider.create(1), lv6)), Direction.DOWN, BlockPredicate.IS_AIR, true));
        ConfiguredFeatures.register(featureRegisterable, CAVE_VINE_IN_MOSS, Feature.BLOCK_COLUMN, new BlockColumnFeatureConfig(List.of(BlockColumnFeatureConfig.createLayer(new WeightedListIntProvider(DataPool.builder().add(UniformIntProvider.create(0, 3), 5).add(UniformIntProvider.create(1, 7), 1).build()), lv5), BlockColumnFeatureConfig.createLayer(ConstantIntProvider.create(1), lv6)), Direction.DOWN, BlockPredicate.IS_AIR, true));
        ConfiguredFeatures.register(featureRegisterable, MOSS_VEGETATION, Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(DataPool.builder().add(Blocks.FLOWERING_AZALEA.getDefaultState(), 4).add(Blocks.AZALEA.getDefaultState(), 7).add(Blocks.MOSS_CARPET.getDefaultState(), 25).add(Blocks.SHORT_GRASS.getDefaultState(), 50).add(Blocks.TALL_GRASS.getDefaultState(), 10))));
        ConfiguredFeatures.register(featureRegisterable, MOSS_PATCH, Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(Blocks.MOSS_BLOCK), PlacedFeatures.createEntry(lv.getOrThrow(MOSS_VEGETATION), new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(1), 0.0f, 5, 0.8f, UniformIntProvider.create(4, 7), 0.3f));
        ConfiguredFeatures.register(featureRegisterable, MOSS_PATCH_BONEMEAL, Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(Blocks.MOSS_BLOCK), PlacedFeatures.createEntry(lv.getOrThrow(MOSS_VEGETATION), new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(1), 0.0f, 5, 0.6f, UniformIntProvider.create(1, 2), 0.75f));
        ConfiguredFeatures.register(featureRegisterable, DRIPLEAF, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfig(RegistryEntryList.of(UndergroundConfiguredFeatures.createSmallDripleafFeature(), UndergroundConfiguredFeatures.createBigDripleafFeature(Direction.EAST), UndergroundConfiguredFeatures.createBigDripleafFeature(Direction.WEST), UndergroundConfiguredFeatures.createBigDripleafFeature(Direction.SOUTH), UndergroundConfiguredFeatures.createBigDripleafFeature(Direction.NORTH))));
        ConfiguredFeatures.register(featureRegisterable, CLAY_WITH_DRIPLEAVES, Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.LUSH_GROUND_REPLACEABLE, BlockStateProvider.of(Blocks.CLAY), PlacedFeatures.createEntry(lv.getOrThrow(DRIPLEAF), new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(3), 0.8f, 2, 0.05f, UniformIntProvider.create(4, 7), 0.7f));
        ConfiguredFeatures.register(featureRegisterable, CLAY_POOL_WITH_DRIPLEAVES, Feature.WATERLOGGED_VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.LUSH_GROUND_REPLACEABLE, BlockStateProvider.of(Blocks.CLAY), PlacedFeatures.createEntry(lv.getOrThrow(DRIPLEAF), new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(3), 0.8f, 5, 0.1f, UniformIntProvider.create(4, 7), 0.7f));
        ConfiguredFeatures.register(featureRegisterable, LUSH_CAVES_CLAY, Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfig(PlacedFeatures.createEntry(lv.getOrThrow(CLAY_WITH_DRIPLEAVES), new PlacementModifier[0]), PlacedFeatures.createEntry(lv.getOrThrow(CLAY_POOL_WITH_DRIPLEAVES), new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, MOSS_PATCH_CEILING, Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(Blocks.MOSS_BLOCK), PlacedFeatures.createEntry(lv.getOrThrow(CAVE_VINE_IN_MOSS), new PlacementModifier[0]), VerticalSurfaceType.CEILING, UniformIntProvider.create(1, 2), 0.0f, 5, 0.08f, UniformIntProvider.create(4, 7), 0.3f));
        ConfiguredFeatures.register(featureRegisterable, SPORE_BLOSSOM, Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.SPORE_BLOSSOM)));
        ConfiguredFeatures.register(featureRegisterable, AMETHYST_GEODE, Feature.GEODE, new GeodeFeatureConfig(new GeodeLayerConfig(BlockStateProvider.of(Blocks.AIR), BlockStateProvider.of(Blocks.AMETHYST_BLOCK), BlockStateProvider.of(Blocks.BUDDING_AMETHYST), BlockStateProvider.of(Blocks.CALCITE), BlockStateProvider.of(Blocks.SMOOTH_BASALT), List.of(Blocks.SMALL_AMETHYST_BUD.getDefaultState(), Blocks.MEDIUM_AMETHYST_BUD.getDefaultState(), Blocks.LARGE_AMETHYST_BUD.getDefaultState(), Blocks.AMETHYST_CLUSTER.getDefaultState()), BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS), new GeodeLayerThicknessConfig(1.7, 2.2, 3.2, 4.2), new GeodeCrackConfig(0.95, 2.0, 2), 0.35, 0.083, true, UniformIntProvider.create(4, 6), UniformIntProvider.create(3, 4), UniformIntProvider.create(1, 2), -16, 16, 0.05, 1));
        ConfiguredFeatures.register(featureRegisterable, SCULK_PATCH_DEEP_DARK, Feature.SCULK_PATCH, new SculkPatchFeatureConfig(10, 32, 64, 0, 1, ConstantIntProvider.create(0), 0.5f));
        ConfiguredFeatures.register(featureRegisterable, SCULK_PATCH_ANCIENT_CITY, Feature.SCULK_PATCH, new SculkPatchFeatureConfig(10, 32, 64, 0, 1, UniformIntProvider.create(1, 3), 0.5f));
        MultifaceGrowthBlock lv7 = (MultifaceGrowthBlock)Blocks.SCULK_VEIN;
        ConfiguredFeatures.register(featureRegisterable, SCULK_VEIN, Feature.MULTIFACE_GROWTH, new MultifaceGrowthFeatureConfig(lv7, 20, true, true, true, 1.0f, RegistryEntryList.of(Block::getRegistryEntry, Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE)));
    }
}

