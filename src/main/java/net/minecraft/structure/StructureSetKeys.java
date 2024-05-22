/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;

public interface StructureSetKeys {
    public static final RegistryKey<StructureSet> VILLAGES = StructureSetKeys.of("villages");
    public static final RegistryKey<StructureSet> DESERT_PYRAMIDS = StructureSetKeys.of("desert_pyramids");
    public static final RegistryKey<StructureSet> IGLOOS = StructureSetKeys.of("igloos");
    public static final RegistryKey<StructureSet> JUNGLE_TEMPLES = StructureSetKeys.of("jungle_temples");
    public static final RegistryKey<StructureSet> SWAMP_HUTS = StructureSetKeys.of("swamp_huts");
    public static final RegistryKey<StructureSet> PILLAGER_OUTPOSTS = StructureSetKeys.of("pillager_outposts");
    public static final RegistryKey<StructureSet> OCEAN_MONUMENTS = StructureSetKeys.of("ocean_monuments");
    public static final RegistryKey<StructureSet> WOODLAND_MANSIONS = StructureSetKeys.of("woodland_mansions");
    public static final RegistryKey<StructureSet> BURIED_TREASURES = StructureSetKeys.of("buried_treasures");
    public static final RegistryKey<StructureSet> MINESHAFTS = StructureSetKeys.of("mineshafts");
    public static final RegistryKey<StructureSet> RUINED_PORTALS = StructureSetKeys.of("ruined_portals");
    public static final RegistryKey<StructureSet> SHIPWRECKS = StructureSetKeys.of("shipwrecks");
    public static final RegistryKey<StructureSet> OCEAN_RUINS = StructureSetKeys.of("ocean_ruins");
    public static final RegistryKey<StructureSet> NETHER_COMPLEXES = StructureSetKeys.of("nether_complexes");
    public static final RegistryKey<StructureSet> NETHER_FOSSILS = StructureSetKeys.of("nether_fossils");
    public static final RegistryKey<StructureSet> END_CITIES = StructureSetKeys.of("end_cities");
    public static final RegistryKey<StructureSet> ANCIENT_CITIES = StructureSetKeys.of("ancient_cities");
    public static final RegistryKey<StructureSet> STRONGHOLDS = StructureSetKeys.of("strongholds");
    public static final RegistryKey<StructureSet> TRAIL_RUINS = StructureSetKeys.of("trail_ruins");
    public static final RegistryKey<StructureSet> TRIAL_CHAMBERS = StructureSetKeys.of("trial_chambers");

    private static RegistryKey<StructureSet> of(String id) {
        return RegistryKey.of(RegistryKeys.STRUCTURE_SET, Identifier.method_60656(id));
    }
}

