package net.minecraft.entity.decoration.painting;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class PaintingVariants {
   public static final RegistryKey KEBAB = of("kebab");
   public static final RegistryKey AZTEC = of("aztec");
   public static final RegistryKey ALBAN = of("alban");
   public static final RegistryKey AZTEC2 = of("aztec2");
   public static final RegistryKey BOMB = of("bomb");
   public static final RegistryKey PLANT = of("plant");
   public static final RegistryKey WASTELAND = of("wasteland");
   public static final RegistryKey POOL = of("pool");
   public static final RegistryKey COURBET = of("courbet");
   public static final RegistryKey SEA = of("sea");
   public static final RegistryKey SUNSET = of("sunset");
   public static final RegistryKey CREEBET = of("creebet");
   public static final RegistryKey WANDERER = of("wanderer");
   public static final RegistryKey GRAHAM = of("graham");
   public static final RegistryKey MATCH = of("match");
   public static final RegistryKey BUST = of("bust");
   public static final RegistryKey STAGE = of("stage");
   public static final RegistryKey VOID = of("void");
   public static final RegistryKey SKULL_AND_ROSES = of("skull_and_roses");
   public static final RegistryKey WITHER = of("wither");
   public static final RegistryKey FIGHTERS = of("fighters");
   public static final RegistryKey POINTER = of("pointer");
   public static final RegistryKey PIGSCENE = of("pigscene");
   public static final RegistryKey BURNING_SKULL = of("burning_skull");
   public static final RegistryKey SKELETON = of("skeleton");
   public static final RegistryKey DONKEY_KONG = of("donkey_kong");
   public static final RegistryKey EARTH = of("earth");
   public static final RegistryKey WIND = of("wind");
   public static final RegistryKey WATER = of("water");
   public static final RegistryKey FIRE = of("fire");

   public static PaintingVariant registerAndGetDefault(Registry registry) {
      Registry.register(registry, (RegistryKey)KEBAB, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)AZTEC, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)ALBAN, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)AZTEC2, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)BOMB, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)PLANT, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)WASTELAND, new PaintingVariant(16, 16));
      Registry.register(registry, (RegistryKey)POOL, new PaintingVariant(32, 16));
      Registry.register(registry, (RegistryKey)COURBET, new PaintingVariant(32, 16));
      Registry.register(registry, (RegistryKey)SEA, new PaintingVariant(32, 16));
      Registry.register(registry, (RegistryKey)SUNSET, new PaintingVariant(32, 16));
      Registry.register(registry, (RegistryKey)CREEBET, new PaintingVariant(32, 16));
      Registry.register(registry, (RegistryKey)WANDERER, new PaintingVariant(16, 32));
      Registry.register(registry, (RegistryKey)GRAHAM, new PaintingVariant(16, 32));
      Registry.register(registry, (RegistryKey)MATCH, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)BUST, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)STAGE, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)VOID, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)SKULL_AND_ROSES, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)WITHER, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)FIGHTERS, new PaintingVariant(64, 32));
      Registry.register(registry, (RegistryKey)POINTER, new PaintingVariant(64, 64));
      Registry.register(registry, (RegistryKey)PIGSCENE, new PaintingVariant(64, 64));
      Registry.register(registry, (RegistryKey)BURNING_SKULL, new PaintingVariant(64, 64));
      Registry.register(registry, (RegistryKey)SKELETON, new PaintingVariant(64, 48));
      Registry.register(registry, (RegistryKey)EARTH, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)WIND, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)WATER, new PaintingVariant(32, 32));
      Registry.register(registry, (RegistryKey)FIRE, new PaintingVariant(32, 32));
      return (PaintingVariant)Registry.register(registry, (RegistryKey)DONKEY_KONG, new PaintingVariant(64, 48));
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.PAINTING_VARIANT, new Identifier(id));
   }
}
