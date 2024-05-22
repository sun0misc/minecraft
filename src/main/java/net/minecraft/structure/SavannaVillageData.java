/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VillagePlacedFeatures;

public class SavannaVillageData {
    public static final RegistryKey<StructurePool> TOWN_CENTERS_KEY = StructurePools.of("village/savanna/town_centers");
    private static final RegistryKey<StructurePool> TERMINATORS_KEY = StructurePools.of("village/savanna/terminators");
    private static final RegistryKey<StructurePool> ZOMBIE_TERMINATORS_KEY = StructurePools.of("village/savanna/zombie/terminators");

    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<PlacedFeature> lv = poolRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntry.Reference<PlacedFeature> lv2 = lv.getOrThrow(VillagePlacedFeatures.ACACIA);
        RegistryEntry.Reference<PlacedFeature> lv3 = lv.getOrThrow(VillagePlacedFeatures.PILE_HAY);
        RegistryEntry.Reference<PlacedFeature> lv4 = lv.getOrThrow(VillagePlacedFeatures.PILE_MELON);
        RegistryEntryLookup<StructureProcessorList> lv5 = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv6 = lv5.getOrThrow(StructureProcessorLists.ZOMBIE_SAVANNA);
        RegistryEntry.Reference<StructureProcessorList> lv7 = lv5.getOrThrow(StructureProcessorLists.STREET_SAVANNA);
        RegistryEntry.Reference<StructureProcessorList> lv8 = lv5.getOrThrow(StructureProcessorLists.FARM_SAVANNA);
        RegistryEntryLookup<StructurePool> lv9 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv10 = lv9.getOrThrow(StructurePools.EMPTY);
        RegistryEntry.Reference<StructurePool> lv11 = lv9.getOrThrow(TERMINATORS_KEY);
        RegistryEntry.Reference<StructurePool> lv12 = lv9.getOrThrow(ZOMBIE_TERMINATORS_KEY);
        poolRegisterable.register(TOWN_CENTERS_KEY, new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/town_centers/savanna_meeting_point_1"), 100), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/town_centers/savanna_meeting_point_2"), 50), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/town_centers/savanna_meeting_point_3"), 150), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/town_centers/savanna_meeting_point_4"), 150), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/town_centers/savanna_meeting_point_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/town_centers/savanna_meeting_point_2", lv6), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/town_centers/savanna_meeting_point_3", lv6), 3), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/town_centers/savanna_meeting_point_4", lv6), 3)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/savanna/streets", new StructurePool(lv11, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/corner_01", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/corner_03", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_02", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_04", lv7), 7), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_05", lv7), 3), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_06", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_08", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_09", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_10", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/straight_11", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/crossroad_02", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/crossroad_03", lv7), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/crossroad_04", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/crossroad_05", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/crossroad_06", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/crossroad_07", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/split_01", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/split_02", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/streets/turn_01", lv7), 3)}), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "village/savanna/zombie/streets", new StructurePool(lv12, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/corner_01", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/corner_03", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_02", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_04", lv7), 7), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_05", lv7), 3), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_06", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_08", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_09", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_10", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/straight_11", lv7), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/crossroad_02", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/crossroad_03", lv7), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/crossroad_04", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/crossroad_05", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/crossroad_06", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/crossroad_07", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/split_01", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/split_02", lv7), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/streets/turn_01", lv7), 3)}), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "village/savanna/houses", new StructurePool(lv11, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_2"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_3"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_4"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_5"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_6"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_7"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_small_house_8"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_medium_house_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_medium_house_2"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_butchers_shop_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_butchers_shop_2"), 2), new Pair[]{Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_tool_smith_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_fletcher_house_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_shepherd_1"), 7), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_armorer_1"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_fisher_cottage_1"), 3), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_tannery_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_cartographer_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_library_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_mason_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_weaponsmith_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_weaponsmith_2"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_temple_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_temple_2"), 3), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_large_farm_1", lv8), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_large_farm_2", lv8), 6), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_small_farm", lv8), 4), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_animal_pen_1"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_animal_pen_2"), 2), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/houses/savanna_animal_pen_3"), 2), Pair.of(StructurePoolElement.ofEmpty(), 5)}), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/savanna/zombie/houses", new StructurePool(lv12, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_2", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_3", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_4", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_5", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_6", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_7", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_small_house_8", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_medium_house_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_medium_house_2", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_butchers_shop_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_butchers_shop_2", lv6), 2), new Pair[]{Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_tool_smith_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_fletcher_house_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_shepherd_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_armorer_1", lv6), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_fisher_cottage_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_tannery_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_cartographer_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_library_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_mason_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_weaponsmith_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_weaponsmith_2", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_temple_1", lv6), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_temple_2", lv6), 3), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_large_farm_1", lv6), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_large_farm_2", lv6), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_small_farm", lv6), 4), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/houses/savanna_animal_pen_1", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_animal_pen_2", lv6), 2), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/houses/savanna_animal_pen_3", lv6), 2), Pair.of(StructurePoolElement.ofEmpty(), 5)}), StructurePool.Projection.RIGID));
        poolRegisterable.register(TERMINATORS_KEY, new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_01", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_02", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_03", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_04", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/terminators/terminator_05", lv7), 1)), StructurePool.Projection.TERRAIN_MATCHING));
        poolRegisterable.register(ZOMBIE_TERMINATORS_KEY, new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_01", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_02", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_03", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/plains/terminators/terminator_04", lv7), 1), Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/zombie/terminators/terminator_05", lv7), 1)), StructurePool.Projection.TERRAIN_MATCHING));
        StructurePools.register(poolRegisterable, "village/savanna/trees", new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofFeature(lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/savanna/decor", new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/savanna_lamp_post_01"), 4), Pair.of(StructurePoolElement.ofFeature(lv2), 4), Pair.of(StructurePoolElement.ofFeature(lv3), 4), Pair.of(StructurePoolElement.ofFeature(lv4), 1), Pair.of(StructurePoolElement.ofEmpty(), 4)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/savanna/zombie/decor", new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedLegacySingle("village/savanna/savanna_lamp_post_01", lv6), 4), Pair.of(StructurePoolElement.ofFeature(lv2), 4), Pair.of(StructurePoolElement.ofFeature(lv3), 4), Pair.of(StructurePoolElement.ofFeature(lv4), 1), Pair.of(StructurePoolElement.ofEmpty(), 4)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/savanna/villagers", new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/villagers/nitwit"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/villagers/baby"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/villagers/unemployed"), 10)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "village/savanna/zombie/villagers", new StructurePool(lv10, ImmutableList.of(Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/zombie/villagers/nitwit"), 1), Pair.of(StructurePoolElement.ofLegacySingle("village/savanna/zombie/villagers/unemployed"), 10)), StructurePool.Projection.RIGID));
    }
}

