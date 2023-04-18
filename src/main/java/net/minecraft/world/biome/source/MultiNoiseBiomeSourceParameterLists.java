package net.minecraft.world.biome.source;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class MultiNoiseBiomeSourceParameterLists {
   public static final RegistryKey NETHER = of("nether");
   public static final RegistryKey OVERWORLD = of("overworld");

   public static void bootstrap(Registerable registry) {
      RegistryEntryLookup lv = registry.getRegistryLookup(RegistryKeys.BIOME);
      registry.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, lv));
      registry.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, lv));
   }

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, new Identifier(id));
   }
}
