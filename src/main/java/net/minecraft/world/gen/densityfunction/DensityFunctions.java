package net.minecraft.world.gen.densityfunction;

import java.util.stream.Stream;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.biome.source.util.VanillaTerrainParametersCreator;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.OreVeinSampler;
import net.minecraft.world.gen.noise.NoiseParametersKeys;
import net.minecraft.world.gen.noise.NoiseRouter;

public class DensityFunctions {
   public static final float field_37690 = -0.50375F;
   private static final float field_36614 = 0.08F;
   private static final double field_36615 = 1.5;
   private static final double field_36616 = 1.5;
   private static final double field_36617 = 1.5625;
   private static final double field_38250 = -0.703125;
   public static final int field_37691 = 64;
   public static final long field_37692 = 4096L;
   private static final DensityFunction TEN_FUNCTION = DensityFunctionTypes.constant(10.0);
   private static final DensityFunction ZERO_FUNCTION = DensityFunctionTypes.zero();
   private static final RegistryKey ZERO = of("zero");
   private static final RegistryKey Y = of("y");
   private static final RegistryKey SHIFT_X = of("shift_x");
   private static final RegistryKey SHIFT_Z = of("shift_z");
   private static final RegistryKey BASE_3D_NOISE_OVERWORLD = of("overworld/base_3d_noise");
   private static final RegistryKey BASE_3D_NOISE_NETHER = of("nether/base_3d_noise");
   private static final RegistryKey BASE_3D_NOISE_END = of("end/base_3d_noise");
   public static final RegistryKey CONTINENTS_OVERWORLD = of("overworld/continents");
   public static final RegistryKey EROSION_OVERWORLD = of("overworld/erosion");
   public static final RegistryKey RIDGES_OVERWORLD = of("overworld/ridges");
   public static final RegistryKey RIDGES_FOLDED_OVERWORLD = of("overworld/ridges_folded");
   public static final RegistryKey OFFSET_OVERWORLD = of("overworld/offset");
   public static final RegistryKey FACTOR_OVERWORLD = of("overworld/factor");
   public static final RegistryKey JAGGEDNESS_OVERWORLD = of("overworld/jaggedness");
   public static final RegistryKey DEPTH_OVERWORLD = of("overworld/depth");
   private static final RegistryKey SLOPED_CHEESE_OVERWORLD = of("overworld/sloped_cheese");
   public static final RegistryKey CONTINENTS_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/continents");
   public static final RegistryKey EROSION_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/erosion");
   private static final RegistryKey OFFSET_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/offset");
   private static final RegistryKey FACTOR_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/factor");
   private static final RegistryKey JAGGEDNESS_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/jaggedness");
   private static final RegistryKey DEPTH_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/depth");
   private static final RegistryKey SLOPED_CHEESE_OVERWORLD_LARGE_BIOME = of("overworld_large_biomes/sloped_cheese");
   private static final RegistryKey OFFSET_OVERWORLD_AMPLIFIED = of("overworld_amplified/offset");
   private static final RegistryKey FACTOR_OVERWORLD_AMPLIFIED = of("overworld_amplified/factor");
   private static final RegistryKey JAGGEDNESS_OVERWORLD_AMPLIFIED = of("overworld_amplified/jaggedness");
   private static final RegistryKey DEPTH_OVERWORLD_AMPLIFIED = of("overworld_amplified/depth");
   private static final RegistryKey SLOPED_CHEESE_OVERWORLD_AMPLIFIED = of("overworld_amplified/sloped_cheese");
   private static final RegistryKey SLOPED_CHEESE_END = of("end/sloped_cheese");
   private static final RegistryKey CAVES_SPAGHETTI_ROUGHNESS_FUNCTION_OVERWORLD = of("overworld/caves/spaghetti_roughness_function");
   private static final RegistryKey CAVES_ENTRANCES_OVERWORLD = of("overworld/caves/entrances");
   private static final RegistryKey CAVES_NOODLE_OVERWORLD = of("overworld/caves/noodle");
   private static final RegistryKey CAVES_PILLARS_OVERWORLD = of("overworld/caves/pillars");
   private static final RegistryKey CAVES_SPAGHETTI_2D_THICKNESS_MODULATOR_OVERWORLD = of("overworld/caves/spaghetti_2d_thickness_modulator");
   private static final RegistryKey CAVES_SPAGHETTI_2D_OVERWORLD = of("overworld/caves/spaghetti_2d");

   private static RegistryKey of(String id) {
      return RegistryKey.of(RegistryKeys.DENSITY_FUNCTION, new Identifier(id));
   }

   public static RegistryEntry bootstrap(Registerable densityFunctionRegisterable) {
      RegistryEntryLookup lv = densityFunctionRegisterable.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS);
      RegistryEntryLookup lv2 = densityFunctionRegisterable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION);
      densityFunctionRegisterable.register(ZERO, DensityFunctionTypes.zero());
      int i = DimensionType.MIN_HEIGHT * 2;
      int j = DimensionType.MAX_COLUMN_HEIGHT * 2;
      densityFunctionRegisterable.register(Y, DensityFunctionTypes.yClampedGradient(i, j, (double)i, (double)j));
      DensityFunction lv3 = registerAndGetHolder(densityFunctionRegisterable, SHIFT_X, DensityFunctionTypes.flatCache(DensityFunctionTypes.cache2d(DensityFunctionTypes.shiftA(lv.getOrThrow(NoiseParametersKeys.OFFSET)))));
      DensityFunction lv4 = registerAndGetHolder(densityFunctionRegisterable, SHIFT_Z, DensityFunctionTypes.flatCache(DensityFunctionTypes.cache2d(DensityFunctionTypes.shiftB(lv.getOrThrow(NoiseParametersKeys.OFFSET)))));
      densityFunctionRegisterable.register(BASE_3D_NOISE_OVERWORLD, InterpolatedNoiseSampler.createBase3dNoiseFunction(0.25, 0.125, 80.0, 160.0, 8.0));
      densityFunctionRegisterable.register(BASE_3D_NOISE_NETHER, InterpolatedNoiseSampler.createBase3dNoiseFunction(0.25, 0.375, 80.0, 60.0, 8.0));
      densityFunctionRegisterable.register(BASE_3D_NOISE_END, InterpolatedNoiseSampler.createBase3dNoiseFunction(0.25, 0.25, 80.0, 160.0, 4.0));
      RegistryEntry lv5 = densityFunctionRegisterable.register(CONTINENTS_OVERWORLD, DensityFunctionTypes.flatCache(DensityFunctionTypes.shiftedNoise(lv3, lv4, 0.25, lv.getOrThrow(NoiseParametersKeys.CONTINENTALNESS))));
      RegistryEntry lv6 = densityFunctionRegisterable.register(EROSION_OVERWORLD, DensityFunctionTypes.flatCache(DensityFunctionTypes.shiftedNoise(lv3, lv4, 0.25, lv.getOrThrow(NoiseParametersKeys.EROSION))));
      DensityFunction lv7 = registerAndGetHolder(densityFunctionRegisterable, RIDGES_OVERWORLD, DensityFunctionTypes.flatCache(DensityFunctionTypes.shiftedNoise(lv3, lv4, 0.25, lv.getOrThrow(NoiseParametersKeys.RIDGE))));
      densityFunctionRegisterable.register(RIDGES_FOLDED_OVERWORLD, createRidgesFoldedOverworldFunction(lv7));
      DensityFunction lv8 = DensityFunctionTypes.noise(lv.getOrThrow(NoiseParametersKeys.JAGGED), 1500.0, 0.0);
      registerSlopedCheeseFunction(densityFunctionRegisterable, lv2, lv8, lv5, lv6, OFFSET_OVERWORLD, FACTOR_OVERWORLD, JAGGEDNESS_OVERWORLD, DEPTH_OVERWORLD, SLOPED_CHEESE_OVERWORLD, false);
      RegistryEntry lv9 = densityFunctionRegisterable.register(CONTINENTS_OVERWORLD_LARGE_BIOME, DensityFunctionTypes.flatCache(DensityFunctionTypes.shiftedNoise(lv3, lv4, 0.25, lv.getOrThrow(NoiseParametersKeys.CONTINENTALNESS_LARGE))));
      RegistryEntry lv10 = densityFunctionRegisterable.register(EROSION_OVERWORLD_LARGE_BIOME, DensityFunctionTypes.flatCache(DensityFunctionTypes.shiftedNoise(lv3, lv4, 0.25, lv.getOrThrow(NoiseParametersKeys.EROSION_LARGE))));
      registerSlopedCheeseFunction(densityFunctionRegisterable, lv2, lv8, lv9, lv10, OFFSET_OVERWORLD_LARGE_BIOME, FACTOR_OVERWORLD_LARGE_BIOME, JAGGEDNESS_OVERWORLD_LARGE_BIOME, DEPTH_OVERWORLD_LARGE_BIOME, SLOPED_CHEESE_OVERWORLD_LARGE_BIOME, false);
      registerSlopedCheeseFunction(densityFunctionRegisterable, lv2, lv8, lv5, lv6, OFFSET_OVERWORLD_AMPLIFIED, FACTOR_OVERWORLD_AMPLIFIED, JAGGEDNESS_OVERWORLD_AMPLIFIED, DEPTH_OVERWORLD_AMPLIFIED, SLOPED_CHEESE_OVERWORLD_AMPLIFIED, true);
      densityFunctionRegisterable.register(SLOPED_CHEESE_END, DensityFunctionTypes.add(DensityFunctionTypes.endIslands(0L), entryHolder(lv2, BASE_3D_NOISE_END)));
      densityFunctionRegisterable.register(CAVES_SPAGHETTI_ROUGHNESS_FUNCTION_OVERWORLD, createCavesSpaghettiRoughnessOverworldFunction(lv));
      densityFunctionRegisterable.register(CAVES_SPAGHETTI_2D_THICKNESS_MODULATOR_OVERWORLD, DensityFunctionTypes.cacheOnce(DensityFunctionTypes.noiseInRange(lv.getOrThrow(NoiseParametersKeys.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3)));
      densityFunctionRegisterable.register(CAVES_SPAGHETTI_2D_OVERWORLD, createCavesSpaghetti2dOverworldFunction(lv2, lv));
      densityFunctionRegisterable.register(CAVES_ENTRANCES_OVERWORLD, createCavesEntrancesOverworldFunction(lv2, lv));
      densityFunctionRegisterable.register(CAVES_NOODLE_OVERWORLD, createCavesNoodleOverworldFunction(lv2, lv));
      return densityFunctionRegisterable.register(CAVES_PILLARS_OVERWORLD, createCavePillarsOverworldFunction(lv));
   }

   private static void registerSlopedCheeseFunction(Registerable densityFunctionRegisterable, RegistryEntryLookup densityFunctionLookup, DensityFunction jaggedNoise, RegistryEntry continents, RegistryEntry erosion, RegistryKey offsetKey, RegistryKey factorKey, RegistryKey jaggednessKey, RegistryKey depthKey, RegistryKey slopedCheeseKey, boolean amplified) {
      DensityFunctionTypes.Spline.DensityFunctionWrapper lv = new DensityFunctionTypes.Spline.DensityFunctionWrapper(continents);
      DensityFunctionTypes.Spline.DensityFunctionWrapper lv2 = new DensityFunctionTypes.Spline.DensityFunctionWrapper(erosion);
      DensityFunctionTypes.Spline.DensityFunctionWrapper lv3 = new DensityFunctionTypes.Spline.DensityFunctionWrapper(densityFunctionLookup.getOrThrow(RIDGES_OVERWORLD));
      DensityFunctionTypes.Spline.DensityFunctionWrapper lv4 = new DensityFunctionTypes.Spline.DensityFunctionWrapper(densityFunctionLookup.getOrThrow(RIDGES_FOLDED_OVERWORLD));
      DensityFunction lv5 = registerAndGetHolder(densityFunctionRegisterable, offsetKey, applyBlending(DensityFunctionTypes.add(DensityFunctionTypes.constant(-0.5037500262260437), DensityFunctionTypes.spline(VanillaTerrainParametersCreator.createOffsetSpline(lv, lv2, lv4, amplified))), DensityFunctionTypes.blendOffset()));
      DensityFunction lv6 = registerAndGetHolder(densityFunctionRegisterable, factorKey, applyBlending(DensityFunctionTypes.spline(VanillaTerrainParametersCreator.createFactorSpline(lv, lv2, lv3, lv4, amplified)), TEN_FUNCTION));
      DensityFunction lv7 = registerAndGetHolder(densityFunctionRegisterable, depthKey, DensityFunctionTypes.add(DensityFunctionTypes.yClampedGradient(-64, 320, 1.5, -1.5), lv5));
      DensityFunction lv8 = registerAndGetHolder(densityFunctionRegisterable, jaggednessKey, applyBlending(DensityFunctionTypes.spline(VanillaTerrainParametersCreator.createJaggednessSpline(lv, lv2, lv3, lv4, amplified)), ZERO_FUNCTION));
      DensityFunction lv9 = DensityFunctionTypes.mul(lv8, jaggedNoise.halfNegative());
      DensityFunction lv10 = createInitialDensityFunction(lv6, DensityFunctionTypes.add(lv7, lv9));
      densityFunctionRegisterable.register(slopedCheeseKey, DensityFunctionTypes.add(lv10, entryHolder(densityFunctionLookup, BASE_3D_NOISE_OVERWORLD)));
   }

   private static DensityFunction registerAndGetHolder(Registerable densityFunctionRegisterable, RegistryKey key, DensityFunction densityFunction) {
      return new DensityFunctionTypes.RegistryEntryHolder(densityFunctionRegisterable.register(key, densityFunction));
   }

   private static DensityFunction entryHolder(RegistryEntryLookup densityFunctionRegisterable, RegistryKey key) {
      return new DensityFunctionTypes.RegistryEntryHolder(densityFunctionRegisterable.getOrThrow(key));
   }

   private static DensityFunction createRidgesFoldedOverworldFunction(DensityFunction input) {
      return DensityFunctionTypes.mul(DensityFunctionTypes.add(DensityFunctionTypes.add(input.abs(), DensityFunctionTypes.constant(-0.6666666666666666)).abs(), DensityFunctionTypes.constant(-0.3333333333333333)), DensityFunctionTypes.constant(-3.0));
   }

   public static float getPeaksValleysNoise(float weirdness) {
      return -(Math.abs(Math.abs(weirdness) - 0.6666667F) - 0.33333334F) * 3.0F;
   }

   private static DensityFunction createCavesSpaghettiRoughnessOverworldFunction(RegistryEntryLookup noiseParametersLookup) {
      DensityFunction lv = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_ROUGHNESS));
      DensityFunction lv2 = DensityFunctionTypes.noiseInRange(noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1);
      return DensityFunctionTypes.cacheOnce(DensityFunctionTypes.mul(lv2, DensityFunctionTypes.add(lv.abs(), DensityFunctionTypes.constant(-0.4))));
   }

   private static DensityFunction createCavesEntrancesOverworldFunction(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup) {
      DensityFunction lv = DensityFunctionTypes.cacheOnce(DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_3D_RARITY), 2.0, 1.0));
      DensityFunction lv2 = DensityFunctionTypes.noiseInRange(noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_3D_THICKNESS), -0.065, -0.088);
      DensityFunction lv3 = DensityFunctionTypes.weirdScaledSampler(lv, noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_3D_1), DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper.TYPE1);
      DensityFunction lv4 = DensityFunctionTypes.weirdScaledSampler(lv, noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_3D_2), DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper.TYPE1);
      DensityFunction lv5 = DensityFunctionTypes.add(DensityFunctionTypes.max(lv3, lv4), lv2).clamp(-1.0, 1.0);
      DensityFunction lv6 = entryHolder(densityFunctionLookup, CAVES_SPAGHETTI_ROUGHNESS_FUNCTION_OVERWORLD);
      DensityFunction lv7 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.CAVE_ENTRANCE), 0.75, 0.5);
      DensityFunction lv8 = DensityFunctionTypes.add(DensityFunctionTypes.add(lv7, DensityFunctionTypes.constant(0.37)), DensityFunctionTypes.yClampedGradient(-10, 30, 0.3, 0.0));
      return DensityFunctionTypes.cacheOnce(DensityFunctionTypes.min(lv8, DensityFunctionTypes.add(lv6, lv5)));
   }

   private static DensityFunction createCavesNoodleOverworldFunction(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup) {
      DensityFunction lv = entryHolder(densityFunctionLookup, Y);
      int i = true;
      int j = true;
      int k = true;
      DensityFunction lv2 = verticalRangeChoice(lv, DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.NOODLE), 1.0, 1.0), -60, 320, -1);
      DensityFunction lv3 = verticalRangeChoice(lv, DensityFunctionTypes.noiseInRange(noiseParametersLookup.getOrThrow(NoiseParametersKeys.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1), -60, 320, 0);
      double d = 2.6666666666666665;
      DensityFunction lv4 = verticalRangeChoice(lv, DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), -60, 320, 0);
      DensityFunction lv5 = verticalRangeChoice(lv, DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), -60, 320, 0);
      DensityFunction lv6 = DensityFunctionTypes.mul(DensityFunctionTypes.constant(1.5), DensityFunctionTypes.max(lv4.abs(), lv5.abs()));
      return DensityFunctionTypes.rangeChoice(lv2, -1000000.0, 0.0, DensityFunctionTypes.constant(64.0), DensityFunctionTypes.add(lv3, lv6));
   }

   private static DensityFunction createCavePillarsOverworldFunction(RegistryEntryLookup noiseParametersLookup) {
      double d = 25.0;
      double e = 0.3;
      DensityFunction lv = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.PILLAR), 25.0, 0.3);
      DensityFunction lv2 = DensityFunctionTypes.noiseInRange(noiseParametersLookup.getOrThrow(NoiseParametersKeys.PILLAR_RARENESS), 0.0, -2.0);
      DensityFunction lv3 = DensityFunctionTypes.noiseInRange(noiseParametersLookup.getOrThrow(NoiseParametersKeys.PILLAR_THICKNESS), 0.0, 1.1);
      DensityFunction lv4 = DensityFunctionTypes.add(DensityFunctionTypes.mul(lv, DensityFunctionTypes.constant(2.0)), lv2);
      return DensityFunctionTypes.cacheOnce(DensityFunctionTypes.mul(lv4, lv3.cube()));
   }

   private static DensityFunction createCavesSpaghetti2dOverworldFunction(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup) {
      DensityFunction lv = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
      DensityFunction lv2 = DensityFunctionTypes.weirdScaledSampler(lv, noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_2D), DensityFunctionTypes.WeirdScaledSampler.RarityValueMapper.TYPE2);
      DensityFunction lv3 = DensityFunctionTypes.noiseInRange(noiseParametersLookup.getOrThrow(NoiseParametersKeys.SPAGHETTI_2D_ELEVATION), 0.0, (double)Math.floorDiv(-64, 8), 8.0);
      DensityFunction lv4 = entryHolder(densityFunctionLookup, CAVES_SPAGHETTI_2D_THICKNESS_MODULATOR_OVERWORLD);
      DensityFunction lv5 = DensityFunctionTypes.add(lv3, DensityFunctionTypes.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
      DensityFunction lv6 = DensityFunctionTypes.add(lv5, lv4).cube();
      double d = 0.083;
      DensityFunction lv7 = DensityFunctionTypes.add(lv2, DensityFunctionTypes.mul(DensityFunctionTypes.constant(0.083), lv4));
      return DensityFunctionTypes.max(lv7, lv6).clamp(-1.0, 1.0);
   }

   private static DensityFunction createCavesFunction(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup, DensityFunction slopedCheese) {
      DensityFunction lv = entryHolder(densityFunctionLookup, CAVES_SPAGHETTI_2D_OVERWORLD);
      DensityFunction lv2 = entryHolder(densityFunctionLookup, CAVES_SPAGHETTI_ROUGHNESS_FUNCTION_OVERWORLD);
      DensityFunction lv3 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.CAVE_LAYER), 8.0);
      DensityFunction lv4 = DensityFunctionTypes.mul(DensityFunctionTypes.constant(4.0), lv3.square());
      DensityFunction lv5 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.CAVE_CHEESE), 0.6666666666666666);
      DensityFunction lv6 = DensityFunctionTypes.add(DensityFunctionTypes.add(DensityFunctionTypes.constant(0.27), lv5).clamp(-1.0, 1.0), DensityFunctionTypes.add(DensityFunctionTypes.constant(1.5), DensityFunctionTypes.mul(DensityFunctionTypes.constant(-0.64), slopedCheese)).clamp(0.0, 0.5));
      DensityFunction lv7 = DensityFunctionTypes.add(lv4, lv6);
      DensityFunction lv8 = DensityFunctionTypes.min(DensityFunctionTypes.min(lv7, entryHolder(densityFunctionLookup, CAVES_ENTRANCES_OVERWORLD)), DensityFunctionTypes.add(lv, lv2));
      DensityFunction lv9 = entryHolder(densityFunctionLookup, CAVES_PILLARS_OVERWORLD);
      DensityFunction lv10 = DensityFunctionTypes.rangeChoice(lv9, -1000000.0, 0.03, DensityFunctionTypes.constant(-1000000.0), lv9);
      return DensityFunctionTypes.max(lv8, lv10);
   }

   private static DensityFunction applyBlendDensity(DensityFunction density) {
      DensityFunction lv = DensityFunctionTypes.blendDensity(density);
      return DensityFunctionTypes.mul(DensityFunctionTypes.interpolated(lv), DensityFunctionTypes.constant(0.64)).squeeze();
   }

   protected static NoiseRouter createSurfaceNoiseRouter(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup, boolean largeBiomes, boolean amplified) {
      DensityFunction lv = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.AQUIFER_BARRIER), 0.5);
      DensityFunction lv2 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
      DensityFunction lv3 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
      DensityFunction lv4 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.AQUIFER_LAVA));
      DensityFunction lv5 = entryHolder(densityFunctionLookup, SHIFT_X);
      DensityFunction lv6 = entryHolder(densityFunctionLookup, SHIFT_Z);
      DensityFunction lv7 = DensityFunctionTypes.shiftedNoise(lv5, lv6, 0.25, noiseParametersLookup.getOrThrow(largeBiomes ? NoiseParametersKeys.TEMPERATURE_LARGE : NoiseParametersKeys.TEMPERATURE));
      DensityFunction lv8 = DensityFunctionTypes.shiftedNoise(lv5, lv6, 0.25, noiseParametersLookup.getOrThrow(largeBiomes ? NoiseParametersKeys.VEGETATION_LARGE : NoiseParametersKeys.VEGETATION));
      DensityFunction lv9 = entryHolder(densityFunctionLookup, largeBiomes ? FACTOR_OVERWORLD_LARGE_BIOME : (amplified ? FACTOR_OVERWORLD_AMPLIFIED : FACTOR_OVERWORLD));
      DensityFunction lv10 = entryHolder(densityFunctionLookup, largeBiomes ? DEPTH_OVERWORLD_LARGE_BIOME : (amplified ? DEPTH_OVERWORLD_AMPLIFIED : DEPTH_OVERWORLD));
      DensityFunction lv11 = createInitialDensityFunction(DensityFunctionTypes.cache2d(lv9), lv10);
      DensityFunction lv12 = entryHolder(densityFunctionLookup, largeBiomes ? SLOPED_CHEESE_OVERWORLD_LARGE_BIOME : (amplified ? SLOPED_CHEESE_OVERWORLD_AMPLIFIED : SLOPED_CHEESE_OVERWORLD));
      DensityFunction lv13 = DensityFunctionTypes.min(lv12, DensityFunctionTypes.mul(DensityFunctionTypes.constant(5.0), entryHolder(densityFunctionLookup, CAVES_ENTRANCES_OVERWORLD)));
      DensityFunction lv14 = DensityFunctionTypes.rangeChoice(lv12, -1000000.0, 1.5625, lv13, createCavesFunction(densityFunctionLookup, noiseParametersLookup, lv12));
      DensityFunction lv15 = DensityFunctionTypes.min(applyBlendDensity(applySurfaceSlides(amplified, lv14)), entryHolder(densityFunctionLookup, CAVES_NOODLE_OVERWORLD));
      DensityFunction lv16 = entryHolder(densityFunctionLookup, Y);
      int i = Stream.of(OreVeinSampler.VeinType.values()).mapToInt((veinType) -> {
         return veinType.minY;
      }).min().orElse(-DimensionType.MIN_HEIGHT * 2);
      int j = Stream.of(OreVeinSampler.VeinType.values()).mapToInt((veinType) -> {
         return veinType.maxY;
      }).max().orElse(-DimensionType.MIN_HEIGHT * 2);
      DensityFunction lv17 = verticalRangeChoice(lv16, DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.ORE_VEININESS), 1.5, 1.5), i, j, 0);
      float f = 4.0F;
      DensityFunction lv18 = verticalRangeChoice(lv16, DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.ORE_VEIN_A), 4.0, 4.0), i, j, 0).abs();
      DensityFunction lv19 = verticalRangeChoice(lv16, DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.ORE_VEIN_B), 4.0, 4.0), i, j, 0).abs();
      DensityFunction lv20 = DensityFunctionTypes.add(DensityFunctionTypes.constant(-0.07999999821186066), DensityFunctionTypes.max(lv18, lv19));
      DensityFunction lv21 = DensityFunctionTypes.noise(noiseParametersLookup.getOrThrow(NoiseParametersKeys.ORE_GAP));
      return new NoiseRouter(lv, lv2, lv3, lv4, lv7, lv8, entryHolder(densityFunctionLookup, largeBiomes ? CONTINENTS_OVERWORLD_LARGE_BIOME : CONTINENTS_OVERWORLD), entryHolder(densityFunctionLookup, largeBiomes ? EROSION_OVERWORLD_LARGE_BIOME : EROSION_OVERWORLD), lv10, entryHolder(densityFunctionLookup, RIDGES_OVERWORLD), applySurfaceSlides(amplified, DensityFunctionTypes.add(lv11, DensityFunctionTypes.constant(-0.703125)).clamp(-64.0, 64.0)), lv15, lv17, lv20, lv21);
   }

   private static NoiseRouter createMultiNoiseDependentNoiseRouter(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup, DensityFunction density) {
      DensityFunction lv = entryHolder(densityFunctionLookup, SHIFT_X);
      DensityFunction lv2 = entryHolder(densityFunctionLookup, SHIFT_Z);
      DensityFunction lv3 = DensityFunctionTypes.shiftedNoise(lv, lv2, 0.25, noiseParametersLookup.getOrThrow(NoiseParametersKeys.TEMPERATURE));
      DensityFunction lv4 = DensityFunctionTypes.shiftedNoise(lv, lv2, 0.25, noiseParametersLookup.getOrThrow(NoiseParametersKeys.VEGETATION));
      DensityFunction lv5 = applyBlendDensity(density);
      return new NoiseRouter(DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), lv3, lv4, DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), lv5, DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero());
   }

   private static DensityFunction applySurfaceSlides(boolean amplified, DensityFunction density) {
      return applySlides(density, -64, 384, amplified ? 16 : 80, amplified ? 0 : 64, -0.078125, 0, 24, amplified ? 0.4 : 0.1171875);
   }

   private static DensityFunction applyCavesSlides(RegistryEntryLookup densityFunctionLookup, int minY, int maxY) {
      return applySlides(entryHolder(densityFunctionLookup, BASE_3D_NOISE_NETHER), minY, maxY, 24, 0, 0.9375, -8, 24, 2.5);
   }

   private static DensityFunction applyFloatingIslandsSlides(DensityFunction function, int minY, int maxY) {
      return applySlides(function, minY, maxY, 72, -184, -23.4375, 4, 32, -0.234375);
   }

   protected static NoiseRouter createNetherNoiseRouter(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup) {
      return createMultiNoiseDependentNoiseRouter(densityFunctionLookup, noiseParametersLookup, applyCavesSlides(densityFunctionLookup, 0, 128));
   }

   protected static NoiseRouter createCavesNoiseRouter(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup) {
      return createMultiNoiseDependentNoiseRouter(densityFunctionLookup, noiseParametersLookup, applyCavesSlides(densityFunctionLookup, -64, 192));
   }

   protected static NoiseRouter createFloatingIslandsNoiseRouter(RegistryEntryLookup densityFunctionLookup, RegistryEntryLookup noiseParametersLookup) {
      return createMultiNoiseDependentNoiseRouter(densityFunctionLookup, noiseParametersLookup, applyFloatingIslandsSlides(entryHolder(densityFunctionLookup, BASE_3D_NOISE_END), 0, 256));
   }

   private static DensityFunction applyEndSlides(DensityFunction slopedCheese) {
      return applyFloatingIslandsSlides(slopedCheese, 0, 128);
   }

   protected static NoiseRouter createEndNoiseRouter(RegistryEntryLookup densityFunctionLookup) {
      DensityFunction lv = DensityFunctionTypes.cache2d(DensityFunctionTypes.endIslands(0L));
      DensityFunction lv2 = applyBlendDensity(applyEndSlides(entryHolder(densityFunctionLookup, SLOPED_CHEESE_END)));
      return new NoiseRouter(DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), lv, DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), applyEndSlides(DensityFunctionTypes.add(lv, DensityFunctionTypes.constant(-0.703125))), lv2, DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero());
   }

   protected static NoiseRouter createMissingNoiseRouter() {
      return new NoiseRouter(DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero(), DensityFunctionTypes.zero());
   }

   private static DensityFunction applyBlending(DensityFunction function, DensityFunction blendOffset) {
      DensityFunction lv = DensityFunctionTypes.lerp(DensityFunctionTypes.blendAlpha(), blendOffset, function);
      return DensityFunctionTypes.flatCache(DensityFunctionTypes.cache2d(lv));
   }

   private static DensityFunction createInitialDensityFunction(DensityFunction factor, DensityFunction depth) {
      DensityFunction lv = DensityFunctionTypes.mul(depth, factor);
      return DensityFunctionTypes.mul(DensityFunctionTypes.constant(4.0), lv.quarterNegative());
   }

   private static DensityFunction verticalRangeChoice(DensityFunction y, DensityFunction whenInRange, int minInclusive, int maxInclusive, int whenOutOfRange) {
      return DensityFunctionTypes.interpolated(DensityFunctionTypes.rangeChoice(y, (double)minInclusive, (double)(maxInclusive + 1), whenInRange, DensityFunctionTypes.constant((double)whenOutOfRange)));
   }

   private static DensityFunction applySlides(DensityFunction density, int minY, int maxY, int topRelativeMinY, int topRelativeMaxY, double topDensity, int bottomRelativeMinY, int bottomRelativeMaxY, double bottomDensity) {
      DensityFunction lv2 = DensityFunctionTypes.yClampedGradient(minY + maxY - topRelativeMinY, minY + maxY - topRelativeMaxY, 1.0, 0.0);
      DensityFunction lv = DensityFunctionTypes.lerp(lv2, topDensity, density);
      DensityFunction lv3 = DensityFunctionTypes.yClampedGradient(minY + bottomRelativeMinY, minY + bottomRelativeMaxY, 0.0, 1.0);
      lv = DensityFunctionTypes.lerp(lv3, bottomDensity, lv);
      return lv;
   }

   protected static final class CaveScaler {
      protected static double scaleCaves(double value) {
         if (value < -0.75) {
            return 0.5;
         } else if (value < -0.5) {
            return 0.75;
         } else if (value < 0.5) {
            return 1.0;
         } else {
            return value < 0.75 ? 2.0 : 3.0;
         }
      }

      protected static double scaleTunnels(double value) {
         if (value < -0.5) {
            return 0.75;
         } else if (value < 0.0) {
            return 1.0;
         } else {
            return value < 0.5 ? 1.5 : 2.0;
         }
      }
   }
}
