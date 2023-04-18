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
import net.minecraft.structure.VillageGenerator;
import net.minecraft.util.Identifier;

public class StructurePools {
   public static final RegistryKey EMPTY = of("empty");

   public static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, new Identifier(id));
   }

   public static void register(Registerable structurePoolsRegisterable, String id, StructurePool pool) {
      structurePoolsRegisterable.register(of(id), pool);
   }

   public static void bootstrap(Registerable structurePoolsRegisterable) {
      RegistryEntryLookup lv = structurePoolsRegisterable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
      RegistryEntry lv2 = lv.getOrThrow(EMPTY);
      structurePoolsRegisterable.register(EMPTY, new StructurePool(lv2, ImmutableList.of(), StructurePool.Projection.RIGID));
      BastionRemnantGenerator.bootstrap(structurePoolsRegisterable);
      PillagerOutpostGenerator.bootstrap(structurePoolsRegisterable);
      VillageGenerator.bootstrap(structurePoolsRegisterable);
      AncientCityGenerator.bootstrap(structurePoolsRegisterable);
      TrailRuinsGenerator.bootstrap(structurePoolsRegisterable);
   }
}
