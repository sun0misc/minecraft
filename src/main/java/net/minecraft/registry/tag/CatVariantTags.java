package net.minecraft.registry.tag;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class CatVariantTags {
   public static final TagKey DEFAULT_SPAWNS = of("default_spawns");
   public static final TagKey FULL_MOON_SPAWNS = of("full_moon_spawns");

   private CatVariantTags() {
   }

   private static TagKey of(String id) {
      return TagKey.of(RegistryKeys.CAT_VARIANT, new Identifier(id));
   }
}
