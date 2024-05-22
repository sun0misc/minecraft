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

public class BastionData {
    public static void bootstrap(Registerable<StructurePool> poolRegisterable) {
        RegistryEntryLookup<StructurePool> lv = poolRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> lv2 = lv.getOrThrow(StructurePools.EMPTY);
        StructurePools.register(poolRegisterable, "bastion/mobs/piglin", new StructurePool(lv2, ImmutableList.of(Pair.of(StructurePoolElement.ofSingle("bastion/mobs/melee_piglin"), 1), Pair.of(StructurePoolElement.ofSingle("bastion/mobs/sword_piglin"), 4), Pair.of(StructurePoolElement.ofSingle("bastion/mobs/crossbow_piglin"), 4), Pair.of(StructurePoolElement.ofSingle("bastion/mobs/empty"), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/mobs/hoglin", new StructurePool(lv2, ImmutableList.of(Pair.of(StructurePoolElement.ofSingle("bastion/mobs/hoglin"), 2), Pair.of(StructurePoolElement.ofSingle("bastion/mobs/empty"), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/blocks/gold", new StructurePool(lv2, ImmutableList.of(Pair.of(StructurePoolElement.ofSingle("bastion/blocks/air"), 3), Pair.of(StructurePoolElement.ofSingle("bastion/blocks/gold"), 1)), StructurePool.Projection.RIGID));
        StructurePools.register(poolRegisterable, "bastion/mobs/piglin_melee", new StructurePool(lv2, ImmutableList.of(Pair.of(StructurePoolElement.ofSingle("bastion/mobs/melee_piglin_always"), 1), Pair.of(StructurePoolElement.ofSingle("bastion/mobs/melee_piglin"), 5), Pair.of(StructurePoolElement.ofSingle("bastion/mobs/sword_piglin"), 1)), StructurePool.Projection.RIGID));
    }
}

