/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;

public class BastionTreasureData {
    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructureProcessorList> lv = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        RegistryEntry.Reference<StructureProcessorList> lv2 = lv.getOrThrow(StructureProcessorLists.TREASURE_ROOMS);
        RegistryEntry.Reference<StructureProcessorList> lv3 = lv.getOrThrow(StructureProcessorLists.HIGH_WALL);
        RegistryEntry.Reference<StructureProcessorList> lv4 = lv.getOrThrow(StructureProcessorLists.BOTTOM_RAMPART);
        RegistryEntry.Reference<StructureProcessorList> lv5 = lv.getOrThrow(StructureProcessorLists.HIGH_RAMPART);
        RegistryEntry.Reference<StructureProcessorList> lv6 = lv.getOrThrow(StructureProcessorLists.ROOF);
        RegistryEntryLookup<StructurePool> lv7 = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv8 = lv7.getOrThrow(StructurePools.EMPTY);
        StructurePools.register(poolRegisterable, "bastion/treasure/bases", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/bases/lava_basin", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/stairs", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/stairs/lower_stairs", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/bases/centers", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/bases/centers/center_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/bases/centers/center_1", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/bases/centers/center_2", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/bases/centers/center_3", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/brains", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/brains/center_brain", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/walls", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/lava_wall", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/entrance_wall", lv3), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/walls/outer", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/outer/top_corner", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/outer/mid_corner", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/outer/bottom_corner", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/outer/outer_wall", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/outer/medium_outer_wall", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/outer/tall_outer_wall", lv3), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/walls/bottom", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/bottom/wall_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/bottom/wall_1", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/bottom/wall_2", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/bottom/wall_3", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/walls/mid", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/mid/wall_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/mid/wall_1", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/mid/wall_2", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/walls/top", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/top/main_entrance", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/top/wall_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/walls/top/wall_1", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/connectors", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/connectors/center_to_wall_middle", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/connectors/center_to_wall_top", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/connectors/center_to_wall_top_entrance", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/entrances", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/entrances/entrance_0", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/ramparts", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/ramparts/mid_wall_main", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/ramparts/mid_wall_side", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/ramparts/bottom_wall_0", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/ramparts/top_wall", lv5), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/ramparts/lava_basin_side", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/ramparts/lava_basin_main", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/corners/bottom", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/bottom/corner_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/bottom/corner_1", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/corners/edges", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/edges/bottom", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/edges/middle", lv3), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/edges/top", lv3), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/corners/middle", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/middle/corner_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/middle/corner_1", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/corners/top", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/top/corner_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/corners/top/corner_1", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/extensions/large_pool", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/empty", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/empty", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/fire_room", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/large_bridge_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/large_bridge_1", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/large_bridge_2", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/large_bridge_3", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/roofed_bridge", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/empty", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/extensions/small_pool", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/empty", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/fire_room", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/empty", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/small_bridge_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/small_bridge_1", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/small_bridge_2", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/small_bridge_3", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/extensions/houses", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/house_0", lv2), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/extensions/house_1", lv2), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/treasure/roofs", new StructurePool(lv8, ImmutableList.of(Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/roofs/wall_roof", lv6), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/roofs/corner_roof", lv6), 1), Pair.of(StructurePoolElement.ofProcessedSingle("bastion/treasure/roofs/center_roof", lv6), 1)), StructurePool.Projection.RIGID));
    }
}

