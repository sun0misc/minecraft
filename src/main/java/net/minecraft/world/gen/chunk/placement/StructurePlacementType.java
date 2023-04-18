package net.minecraft.world.gen.chunk.placement;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface StructurePlacementType {
   StructurePlacementType RANDOM_SPREAD = register("random_spread", RandomSpreadStructurePlacement.CODEC);
   StructurePlacementType CONCENTRIC_RINGS = register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

   Codec codec();

   private static StructurePlacementType register(String id, Codec codec) {
      return (StructurePlacementType)Registry.register(Registries.STRUCTURE_PLACEMENT, (String)id, () -> {
         return codec;
      });
   }
}
