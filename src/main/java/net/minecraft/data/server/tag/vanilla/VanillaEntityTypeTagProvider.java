package net.minecraft.data.server.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.ValueLookupTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EntityTypeTags;

public class VanillaEntityTypeTagProvider extends ValueLookupTagProvider {
   public VanillaEntityTypeTagProvider(DataOutput output, CompletableFuture registryLookupFuture) {
      super(output, RegistryKeys.ENTITY_TYPE, registryLookupFuture, (entityType) -> {
         return entityType.getRegistryEntry().registryKey();
      });
   }

   protected void configure(RegistryWrapper.WrapperLookup lookup) {
      this.getOrCreateTagBuilder(EntityTypeTags.SKELETONS).add((Object[])(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON));
      this.getOrCreateTagBuilder(EntityTypeTags.RAIDERS).add((Object[])(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH));
      this.getOrCreateTagBuilder(EntityTypeTags.BEEHIVE_INHABITORS).add((Object)EntityType.BEE);
      this.getOrCreateTagBuilder(EntityTypeTags.ARROWS).add((Object[])(EntityType.ARROW, EntityType.SPECTRAL_ARROW));
      this.getOrCreateTagBuilder(EntityTypeTags.IMPACT_PROJECTILES).addTag(EntityTypeTags.ARROWS).add((Object[])(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL));
      this.getOrCreateTagBuilder(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add((Object[])(EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.FOX));
      this.getOrCreateTagBuilder(EntityTypeTags.AXOLOTL_HUNT_TARGETS).add((Object[])(EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.TADPOLE));
      this.getOrCreateTagBuilder(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES).add((Object[])(EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN));
      this.getOrCreateTagBuilder(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add((Object[])(EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM, EntityType.WITHER));
      this.getOrCreateTagBuilder(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES).add((Object[])(EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE));
      this.getOrCreateTagBuilder(EntityTypeTags.FROG_FOOD).add((Object[])(EntityType.SLIME, EntityType.MAGMA_CUBE));
      this.getOrCreateTagBuilder(EntityTypeTags.FALL_DAMAGE_IMMUNE).add((Object[])(EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.SHULKER, EntityType.ALLAY, EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.CAT, EntityType.CHICKEN, EntityType.GHAST, EntityType.PHANTOM, EntityType.MAGMA_CUBE, EntityType.OCELOT, EntityType.PARROT, EntityType.WITHER));
      this.getOrCreateTagBuilder(EntityTypeTags.DISMOUNTS_UNDERWATER).add((Object[])(EntityType.CAMEL, EntityType.CHICKEN, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.RAVAGER, EntityType.SPIDER, EntityType.STRIDER, EntityType.TRADER_LLAMA, EntityType.ZOMBIE_HORSE));
   }
}
