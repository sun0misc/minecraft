package net.minecraft.registry.tag;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public interface StructureTags {
   TagKey EYE_OF_ENDER_LOCATED = of("eye_of_ender_located");
   TagKey DOLPHIN_LOCATED = of("dolphin_located");
   TagKey ON_WOODLAND_EXPLORER_MAPS = of("on_woodland_explorer_maps");
   TagKey ON_OCEAN_EXPLORER_MAPS = of("on_ocean_explorer_maps");
   TagKey ON_TREASURE_MAPS = of("on_treasure_maps");
   TagKey CATS_SPAWN_IN = of("cats_spawn_in");
   TagKey CATS_SPAWN_AS_BLACK = of("cats_spawn_as_black");
   TagKey VILLAGE = of("village");
   TagKey MINESHAFT = of("mineshaft");
   TagKey SHIPWRECK = of("shipwreck");
   TagKey RUINED_PORTAL = of("ruined_portal");
   TagKey OCEAN_RUIN = of("ocean_ruin");

   private static TagKey of(String id) {
      return TagKey.of(RegistryKeys.STRUCTURE, new Identifier(id));
   }
}
