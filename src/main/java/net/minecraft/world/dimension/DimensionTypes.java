package net.minecraft.world.dimension;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class DimensionTypes {
   public static final RegistryKey OVERWORLD = of("overworld");
   public static final RegistryKey THE_NETHER = of("the_nether");
   public static final RegistryKey THE_END = of("the_end");
   public static final RegistryKey OVERWORLD_CAVES = of("overworld_caves");
   public static final Identifier OVERWORLD_ID = new Identifier("overworld");
   public static final Identifier THE_NETHER_ID = new Identifier("the_nether");
   public static final Identifier THE_END_ID = new Identifier("the_end");

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier(id));
   }
}
