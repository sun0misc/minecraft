package net.minecraft.entity.passive;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public record CatVariant(Identifier texture) {
   public static final RegistryKey TABBY = of("tabby");
   public static final RegistryKey BLACK = of("black");
   public static final RegistryKey RED = of("red");
   public static final RegistryKey SIAMESE = of("siamese");
   public static final RegistryKey BRITISH_SHORTHAIR = of("british_shorthair");
   public static final RegistryKey CALICO = of("calico");
   public static final RegistryKey PERSIAN = of("persian");
   public static final RegistryKey RAGDOLL = of("ragdoll");
   public static final RegistryKey WHITE = of("white");
   public static final RegistryKey JELLIE = of("jellie");
   public static final RegistryKey ALL_BLACK = of("all_black");

   public CatVariant(Identifier arg) {
      this.texture = arg;
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.CAT_VARIANT, new Identifier(id));
   }

   public static CatVariant registerAndGetDefault(Registry registry) {
      register(registry, TABBY, "textures/entity/cat/tabby.png");
      register(registry, BLACK, "textures/entity/cat/black.png");
      register(registry, RED, "textures/entity/cat/red.png");
      register(registry, SIAMESE, "textures/entity/cat/siamese.png");
      register(registry, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
      register(registry, CALICO, "textures/entity/cat/calico.png");
      register(registry, PERSIAN, "textures/entity/cat/persian.png");
      register(registry, RAGDOLL, "textures/entity/cat/ragdoll.png");
      register(registry, WHITE, "textures/entity/cat/white.png");
      register(registry, JELLIE, "textures/entity/cat/jellie.png");
      return register(registry, ALL_BLACK, "textures/entity/cat/all_black.png");
   }

   private static CatVariant register(Registry registry, RegistryKey key, String textureId) {
      return (CatVariant)Registry.register(registry, (RegistryKey)key, new CatVariant(new Identifier(textureId)));
   }

   public Identifier texture() {
      return this.texture;
   }
}
