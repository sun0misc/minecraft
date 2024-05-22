/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import java.util.List;
import java.util.Optional;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

public interface StructureSets {
    public static void bootstrap(Registerable<StructureSet> structureSetRegisterable) {
        RegistryEntryLookup<Structure> lv = structureSetRegisterable.getRegistryLookup(RegistryKeys.STRUCTURE);
        RegistryEntryLookup<Biome> lv2 = structureSetRegisterable.getRegistryLookup(RegistryKeys.BIOME);
        RegistryEntry.Reference<StructureSet> lv3 = structureSetRegisterable.register(StructureSetKeys.VILLAGES, new StructureSet(List.of(StructureSet.createEntry(lv.getOrThrow(StructureKeys.VILLAGE_PLAINS)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.VILLAGE_DESERT)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.VILLAGE_SAVANNA)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.VILLAGE_SNOWY)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.VILLAGE_TAIGA))), (StructurePlacement)new RandomSpreadStructurePlacement(34, 8, SpreadType.LINEAR, 10387312)));
        structureSetRegisterable.register(StructureSetKeys.DESERT_PYRAMIDS, new StructureSet(lv.getOrThrow(StructureKeys.DESERT_PYRAMID), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, SpreadType.LINEAR, 14357617)));
        structureSetRegisterable.register(StructureSetKeys.IGLOOS, new StructureSet(lv.getOrThrow(StructureKeys.IGLOO), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, SpreadType.LINEAR, 14357618)));
        structureSetRegisterable.register(StructureSetKeys.JUNGLE_TEMPLES, new StructureSet(lv.getOrThrow(StructureKeys.JUNGLE_PYRAMID), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, SpreadType.LINEAR, 14357619)));
        structureSetRegisterable.register(StructureSetKeys.SWAMP_HUTS, new StructureSet(lv.getOrThrow(StructureKeys.SWAMP_HUT), (StructurePlacement)new RandomSpreadStructurePlacement(32, 8, SpreadType.LINEAR, 14357620)));
        structureSetRegisterable.register(StructureSetKeys.PILLAGER_OUTPOSTS, new StructureSet(lv.getOrThrow(StructureKeys.PILLAGER_OUTPOST), (StructurePlacement)new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_1, 0.2f, 165745296, Optional.of(new StructurePlacement.ExclusionZone(lv3, 10)), 32, 8, SpreadType.LINEAR)));
        structureSetRegisterable.register(StructureSetKeys.ANCIENT_CITIES, new StructureSet(lv.getOrThrow(StructureKeys.ANCIENT_CITY), (StructurePlacement)new RandomSpreadStructurePlacement(24, 8, SpreadType.LINEAR, 20083232)));
        structureSetRegisterable.register(StructureSetKeys.OCEAN_MONUMENTS, new StructureSet(lv.getOrThrow(StructureKeys.MONUMENT), (StructurePlacement)new RandomSpreadStructurePlacement(32, 5, SpreadType.TRIANGULAR, 10387313)));
        structureSetRegisterable.register(StructureSetKeys.WOODLAND_MANSIONS, new StructureSet(lv.getOrThrow(StructureKeys.MANSION), (StructurePlacement)new RandomSpreadStructurePlacement(80, 20, SpreadType.TRIANGULAR, 10387319)));
        structureSetRegisterable.register(StructureSetKeys.BURIED_TREASURES, new StructureSet(lv.getOrThrow(StructureKeys.BURIED_TREASURE), (StructurePlacement)new RandomSpreadStructurePlacement(new Vec3i(9, 0, 9), StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2, 0.01f, 0, Optional.empty(), 1, 0, SpreadType.LINEAR)));
        structureSetRegisterable.register(StructureSetKeys.MINESHAFTS, new StructureSet(List.of(StructureSet.createEntry(lv.getOrThrow(StructureKeys.MINESHAFT)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.MINESHAFT_MESA))), (StructurePlacement)new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_3, 0.004f, 0, Optional.empty(), 1, 0, SpreadType.LINEAR)));
        structureSetRegisterable.register(StructureSetKeys.RUINED_PORTALS, new StructureSet(List.of(StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL_DESERT)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL_JUNGLE)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL_SWAMP)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL_MOUNTAIN)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL_OCEAN)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.RUINED_PORTAL_NETHER))), (StructurePlacement)new RandomSpreadStructurePlacement(40, 15, SpreadType.LINEAR, 34222645)));
        structureSetRegisterable.register(StructureSetKeys.SHIPWRECKS, new StructureSet(List.of(StructureSet.createEntry(lv.getOrThrow(StructureKeys.SHIPWRECK)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.SHIPWRECK_BEACHED))), (StructurePlacement)new RandomSpreadStructurePlacement(24, 4, SpreadType.LINEAR, 165745295)));
        structureSetRegisterable.register(StructureSetKeys.OCEAN_RUINS, new StructureSet(List.of(StructureSet.createEntry(lv.getOrThrow(StructureKeys.OCEAN_RUIN_COLD)), StructureSet.createEntry(lv.getOrThrow(StructureKeys.OCEAN_RUIN_WARM))), (StructurePlacement)new RandomSpreadStructurePlacement(20, 8, SpreadType.LINEAR, 14357621)));
        structureSetRegisterable.register(StructureSetKeys.NETHER_COMPLEXES, new StructureSet(List.of(StructureSet.createEntry(lv.getOrThrow(StructureKeys.FORTRESS), 2), StructureSet.createEntry(lv.getOrThrow(StructureKeys.BASTION_REMNANT), 3)), (StructurePlacement)new RandomSpreadStructurePlacement(27, 4, SpreadType.LINEAR, 30084232)));
        structureSetRegisterable.register(StructureSetKeys.NETHER_FOSSILS, new StructureSet(lv.getOrThrow(StructureKeys.NETHER_FOSSIL), (StructurePlacement)new RandomSpreadStructurePlacement(2, 1, SpreadType.LINEAR, 14357921)));
        structureSetRegisterable.register(StructureSetKeys.END_CITIES, new StructureSet(lv.getOrThrow(StructureKeys.END_CITY), (StructurePlacement)new RandomSpreadStructurePlacement(20, 11, SpreadType.TRIANGULAR, 10387313)));
        structureSetRegisterable.register(StructureSetKeys.STRONGHOLDS, new StructureSet(lv.getOrThrow(StructureKeys.STRONGHOLD), (StructurePlacement)new ConcentricRingsStructurePlacement(32, 3, 128, lv2.getOrThrow(BiomeTags.STRONGHOLD_BIASED_TO))));
        structureSetRegisterable.register(StructureSetKeys.TRAIL_RUINS, new StructureSet(lv.getOrThrow(StructureKeys.TRAIL_RUINS), (StructurePlacement)new RandomSpreadStructurePlacement(34, 8, SpreadType.LINEAR, 83469867)));
        structureSetRegisterable.register(StructureSetKeys.TRIAL_CHAMBERS, new StructureSet(lv.getOrThrow(StructureKeys.TRIAL_CHAMBERS), (StructurePlacement)new RandomSpreadStructurePlacement(34, 12, SpreadType.LINEAR, 94251327)));
    }
}

