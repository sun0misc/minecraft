package net.minecraft.world.gen.feature;

import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

public class VillagePlacedFeatures {
   public static final RegistryKey PILE_HAY = PlacedFeatures.of("pile_hay");
   public static final RegistryKey PILE_MELON = PlacedFeatures.of("pile_melon");
   public static final RegistryKey PILE_SNOW = PlacedFeatures.of("pile_snow");
   public static final RegistryKey PILE_ICE = PlacedFeatures.of("pile_ice");
   public static final RegistryKey PILE_PUMPKIN = PlacedFeatures.of("pile_pumpkin");
   public static final RegistryKey OAK = PlacedFeatures.of("oak");
   public static final RegistryKey ACACIA = PlacedFeatures.of("acacia");
   public static final RegistryKey SPRUCE = PlacedFeatures.of("spruce");
   public static final RegistryKey PINE = PlacedFeatures.of("pine");
   public static final RegistryKey PATCH_CACTUS = PlacedFeatures.of("patch_cactus");
   public static final RegistryKey FLOWER_PLAIN = PlacedFeatures.of("flower_plain");
   public static final RegistryKey PATCH_TAIGA_GRASS = PlacedFeatures.of("patch_taiga_grass");
   public static final RegistryKey PATCH_BERRY_BUSH = PlacedFeatures.of("patch_berry_bush");

   public static void bootstrap(Registerable featureRegisterable) {
      RegistryEntryLookup lv = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
      RegistryEntry lv2 = lv.getOrThrow(PileConfiguredFeatures.PILE_HAY);
      RegistryEntry lv3 = lv.getOrThrow(PileConfiguredFeatures.PILE_MELON);
      RegistryEntry lv4 = lv.getOrThrow(PileConfiguredFeatures.PILE_SNOW);
      RegistryEntry lv5 = lv.getOrThrow(PileConfiguredFeatures.PILE_ICE);
      RegistryEntry lv6 = lv.getOrThrow(PileConfiguredFeatures.PILE_PUMPKIN);
      RegistryEntry lv7 = lv.getOrThrow(TreeConfiguredFeatures.OAK);
      RegistryEntry lv8 = lv.getOrThrow(TreeConfiguredFeatures.ACACIA);
      RegistryEntry lv9 = lv.getOrThrow(TreeConfiguredFeatures.SPRUCE);
      RegistryEntry lv10 = lv.getOrThrow(TreeConfiguredFeatures.PINE);
      RegistryEntry lv11 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_CACTUS);
      RegistryEntry lv12 = lv.getOrThrow(VegetationConfiguredFeatures.FLOWER_PLAIN);
      RegistryEntry lv13 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_TAIGA_GRASS);
      RegistryEntry lv14 = lv.getOrThrow(VegetationConfiguredFeatures.PATCH_BERRY_BUSH);
      PlacedFeatures.register(featureRegisterable, PILE_HAY, lv2, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, PILE_MELON, lv3, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, PILE_SNOW, lv4, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, PILE_ICE, lv5, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, PILE_PUMPKIN, lv6, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, OAK, lv7, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.OAK_SAPLING)));
      PlacedFeatures.register(featureRegisterable, ACACIA, lv8, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.ACACIA_SAPLING)));
      PlacedFeatures.register(featureRegisterable, SPRUCE, lv9, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, PINE, lv10, (PlacementModifier[])(PlacedFeatures.wouldSurvive(Blocks.SPRUCE_SAPLING)));
      PlacedFeatures.register(featureRegisterable, PATCH_CACTUS, lv11, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, FLOWER_PLAIN, lv12, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, PATCH_TAIGA_GRASS, lv13, (PlacementModifier[])());
      PlacedFeatures.register(featureRegisterable, PATCH_BERRY_BUSH, lv14, (PlacementModifier[])());
   }
}
