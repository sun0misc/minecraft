/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.structure.pool;

import com.google.common.collect.ImmutableList;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.AncientCityGenerator;
import net.minecraft.structure.BastionRemnantGenerator;
import net.minecraft.structure.PillagerOutpostGenerator;
import net.minecraft.structure.TrailRuinsGenerator;
import net.minecraft.structure.TrialChamberData;
import net.minecraft.structure.VillageGenerator;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;

public class StructurePools {
    public static final RegistryKey<StructurePool> EMPTY = StructurePools.of("empty");

    public static RegistryKey<StructurePool> of(String id) {
        return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.method_60656(id));
    }

    public static RegistryKey<StructurePool> method_60923(String string) {
        return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.method_60654(string));
    }

    public static void register(Registerable<StructurePool> structurePoolsRegisterable, String id, StructurePool pool) {
        structurePoolsRegisterable.register(StructurePools.of(id), pool);
    }

    public static void bootstrap(Registerable<StructurePool> structurePoolsRegisterable) {
        RegistryEntryLookup<StructurePool> lv = structurePoolsRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv2 = lv.getOrThrow(EMPTY);
        structurePoolsRegisterable.register(EMPTY, new StructurePool(lv2, ImmutableList.of(), StructurePool.Projection.RIGID));
        BastionRemnantGenerator.bootstrap(structurePoolsRegisterable);
        PillagerOutpostGenerator.bootstrap(structurePoolsRegisterable);
        VillageGenerator.bootstrap(structurePoolsRegisterable);
        AncientCityGenerator.bootstrap(structurePoolsRegisterable);
        TrailRuinsGenerator.bootstrap(structurePoolsRegisterable);
        TrialChamberData.bootstrap(structurePoolsRegisterable);
    }
}

