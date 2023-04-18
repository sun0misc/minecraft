package net.minecraft.structure;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorLists;

public class TrailRuinsGenerator {
   public static final RegistryKey TOWER = StructurePools.of("trail_ruins/tower");

   public static void bootstrap(Registerable poolRegisterable) {
      RegistryEntryLookup lv = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
      RegistryEntry lv2 = lv.getOrThrow(StructurePools.EMPTY);
      RegistryEntryLookup lv3 = poolRegisterable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
      RegistryEntry lv4 = lv3.getOrThrow(StructureProcessorLists.TRAIL_RUINS_SUSPICIOUS_SAND);
      poolRegisterable.register(TOWER, new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/tower_3", lv4), 1)), StructurePool.Projection.RIGID));
      StructurePools.register(poolRegisterable, "trail_ruins/tower/additions", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/large_hall_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/platform_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/hall_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/stable_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/tower/one_room_1", lv4), 1)), StructurePool.Projection.RIGID));
      StructurePools.register(poolRegisterable, "trail_ruins/roads", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/long_road_end"), 1), Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/road_end_1"), 2), Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/road_section_1"), 1), Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/road_section_2"), 1), Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/road_section_3"), 1), Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/road_section_4"), 1), Pair.of(StructurePoolElement.ofSingle("trail_ruins/roads/road_spacer_1"), 1)), StructurePool.Projection.RIGID));
      StructurePools.register(poolRegisterable, "trail_ruins/buildings", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_entrance_three_1", lv4), 3), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_entrance_two_1", lv4), 3), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_entrance_two_2", lv4), 3), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/large_room_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/one_room_4", lv4), 1)), StructurePool.Projection.RIGID));
      StructurePools.register(poolRegisterable, "trail_ruins/buildings/grouped", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_one_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_one_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_two_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_two_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_two_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_two_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/buildings/group_room_two_5", lv4), 1)), StructurePool.Projection.RIGID));
      StructurePools.register(poolRegisterable, "trail_ruins/decor", new StructurePool(lv2, List.of(Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_1", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_2", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_3", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_4", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_5", lv4), 1), Pair.of(StructurePoolElement.ofProcessedSingle("trail_ruins/decor/decor_6", lv4), 1)), StructurePool.Projection.RIGID));
   }
}
