package net.minecraft.registry.tag;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class EntityTypeTags {
   public static final TagKey SKELETONS = of("skeletons");
   public static final TagKey RAIDERS = of("raiders");
   public static final TagKey BEEHIVE_INHABITORS = of("beehive_inhabitors");
   public static final TagKey ARROWS = of("arrows");
   public static final TagKey IMPACT_PROJECTILES = of("impact_projectiles");
   public static final TagKey POWDER_SNOW_WALKABLE_MOBS = of("powder_snow_walkable_mobs");
   public static final TagKey AXOLOTL_ALWAYS_HOSTILES = of("axolotl_always_hostiles");
   public static final TagKey AXOLOTL_HUNT_TARGETS = of("axolotl_hunt_targets");
   public static final TagKey FREEZE_IMMUNE_ENTITY_TYPES = of("freeze_immune_entity_types");
   public static final TagKey FREEZE_HURTS_EXTRA_TYPES = of("freeze_hurts_extra_types");
   public static final TagKey FROG_FOOD = of("frog_food");
   public static final TagKey FALL_DAMAGE_IMMUNE = of("fall_damage_immune");
   public static final TagKey DISMOUNTS_UNDERWATER = of("dismounts_underwater");

   private EntityTypeTags() {
   }

   private static TagKey of(String id) {
      return TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(id));
   }
}
