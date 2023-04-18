package net.minecraft.world.biome;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public abstract class BiomeKeys {
   public static final RegistryKey THE_VOID = register("the_void");
   public static final RegistryKey PLAINS = register("plains");
   public static final RegistryKey SUNFLOWER_PLAINS = register("sunflower_plains");
   public static final RegistryKey SNOWY_PLAINS = register("snowy_plains");
   public static final RegistryKey ICE_SPIKES = register("ice_spikes");
   public static final RegistryKey DESERT = register("desert");
   public static final RegistryKey SWAMP = register("swamp");
   public static final RegistryKey MANGROVE_SWAMP = register("mangrove_swamp");
   public static final RegistryKey FOREST = register("forest");
   public static final RegistryKey FLOWER_FOREST = register("flower_forest");
   public static final RegistryKey BIRCH_FOREST = register("birch_forest");
   public static final RegistryKey DARK_FOREST = register("dark_forest");
   public static final RegistryKey OLD_GROWTH_BIRCH_FOREST = register("old_growth_birch_forest");
   public static final RegistryKey OLD_GROWTH_PINE_TAIGA = register("old_growth_pine_taiga");
   public static final RegistryKey OLD_GROWTH_SPRUCE_TAIGA = register("old_growth_spruce_taiga");
   public static final RegistryKey TAIGA = register("taiga");
   public static final RegistryKey SNOWY_TAIGA = register("snowy_taiga");
   public static final RegistryKey SAVANNA = register("savanna");
   public static final RegistryKey SAVANNA_PLATEAU = register("savanna_plateau");
   public static final RegistryKey WINDSWEPT_HILLS = register("windswept_hills");
   public static final RegistryKey WINDSWEPT_GRAVELLY_HILLS = register("windswept_gravelly_hills");
   public static final RegistryKey WINDSWEPT_FOREST = register("windswept_forest");
   public static final RegistryKey WINDSWEPT_SAVANNA = register("windswept_savanna");
   public static final RegistryKey JUNGLE = register("jungle");
   public static final RegistryKey SPARSE_JUNGLE = register("sparse_jungle");
   public static final RegistryKey BAMBOO_JUNGLE = register("bamboo_jungle");
   public static final RegistryKey BADLANDS = register("badlands");
   public static final RegistryKey ERODED_BADLANDS = register("eroded_badlands");
   public static final RegistryKey WOODED_BADLANDS = register("wooded_badlands");
   public static final RegistryKey MEADOW = register("meadow");
   public static final RegistryKey CHERRY_GROVE = register("cherry_grove");
   public static final RegistryKey GROVE = register("grove");
   public static final RegistryKey SNOWY_SLOPES = register("snowy_slopes");
   public static final RegistryKey FROZEN_PEAKS = register("frozen_peaks");
   public static final RegistryKey JAGGED_PEAKS = register("jagged_peaks");
   public static final RegistryKey STONY_PEAKS = register("stony_peaks");
   public static final RegistryKey RIVER = register("river");
   public static final RegistryKey FROZEN_RIVER = register("frozen_river");
   public static final RegistryKey BEACH = register("beach");
   public static final RegistryKey SNOWY_BEACH = register("snowy_beach");
   public static final RegistryKey STONY_SHORE = register("stony_shore");
   public static final RegistryKey WARM_OCEAN = register("warm_ocean");
   public static final RegistryKey LUKEWARM_OCEAN = register("lukewarm_ocean");
   public static final RegistryKey DEEP_LUKEWARM_OCEAN = register("deep_lukewarm_ocean");
   public static final RegistryKey OCEAN = register("ocean");
   public static final RegistryKey DEEP_OCEAN = register("deep_ocean");
   public static final RegistryKey COLD_OCEAN = register("cold_ocean");
   public static final RegistryKey DEEP_COLD_OCEAN = register("deep_cold_ocean");
   public static final RegistryKey FROZEN_OCEAN = register("frozen_ocean");
   public static final RegistryKey DEEP_FROZEN_OCEAN = register("deep_frozen_ocean");
   public static final RegistryKey MUSHROOM_FIELDS = register("mushroom_fields");
   public static final RegistryKey DRIPSTONE_CAVES = register("dripstone_caves");
   public static final RegistryKey LUSH_CAVES = register("lush_caves");
   public static final RegistryKey DEEP_DARK = register("deep_dark");
   public static final RegistryKey NETHER_WASTES = register("nether_wastes");
   public static final RegistryKey WARPED_FOREST = register("warped_forest");
   public static final RegistryKey CRIMSON_FOREST = register("crimson_forest");
   public static final RegistryKey SOUL_SAND_VALLEY = register("soul_sand_valley");
   public static final RegistryKey BASALT_DELTAS = register("basalt_deltas");
   public static final RegistryKey THE_END = register("the_end");
   public static final RegistryKey END_HIGHLANDS = register("end_highlands");
   public static final RegistryKey END_MIDLANDS = register("end_midlands");
   public static final RegistryKey SMALL_END_ISLANDS = register("small_end_islands");
   public static final RegistryKey END_BARRENS = register("end_barrens");

   private static RegistryKey register(String name) {
      return RegistryKey.of(RegistryKeys.BIOME, new Identifier(name));
   }
}
