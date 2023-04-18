package net.minecraft.registry.tag;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class BannerPatternTags {
   public static final TagKey NO_ITEM_REQUIRED = of("no_item_required");
   public static final TagKey FLOWER_PATTERN_ITEM = of("pattern_item/flower");
   public static final TagKey CREEPER_PATTERN_ITEM = of("pattern_item/creeper");
   public static final TagKey SKULL_PATTERN_ITEM = of("pattern_item/skull");
   public static final TagKey MOJANG_PATTERN_ITEM = of("pattern_item/mojang");
   public static final TagKey GLOBE_PATTERN_ITEM = of("pattern_item/globe");
   public static final TagKey PIGLIN_PATTERN_ITEM = of("pattern_item/piglin");

   private BannerPatternTags() {
   }

   private static TagKey of(String id) {
      return TagKey.of(RegistryKeys.BANNER_PATTERN, new Identifier(id));
   }
}
