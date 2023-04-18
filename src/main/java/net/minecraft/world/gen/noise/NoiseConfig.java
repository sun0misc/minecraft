package net.minecraft.world.gen.noise;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public final class NoiseConfig {
   final RandomSplitter randomDeriver;
   private final RegistryEntryLookup noiseParametersRegistry;
   private final NoiseRouter noiseRouter;
   private final MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler;
   private final SurfaceBuilder surfaceBuilder;
   private final RandomSplitter aquiferRandomDeriver;
   private final RandomSplitter oreRandomDeriver;
   private final Map noises;
   private final Map randomDerivers;

   public static NoiseConfig create(RegistryEntryLookup.RegistryLookup registryLookup, RegistryKey chunkGeneratorSettingsKey, long legacyWorldSeed) {
      return create((ChunkGeneratorSettings)registryLookup.getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS).getOrThrow(chunkGeneratorSettingsKey).value(), registryLookup.getOrThrow(RegistryKeys.NOISE_PARAMETERS), legacyWorldSeed);
   }

   public static NoiseConfig create(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup noiseParametersLookup, long legacyWorldSeed) {
      return new NoiseConfig(chunkGeneratorSettings, noiseParametersLookup, legacyWorldSeed);
   }

   private NoiseConfig(ChunkGeneratorSettings chunkGeneratorSettings, RegistryEntryLookup noiseParametersLookup, final long seed) {
      this.randomDeriver = chunkGeneratorSettings.getRandomProvider().create(seed).nextSplitter();
      this.noiseParametersRegistry = noiseParametersLookup;
      this.aquiferRandomDeriver = this.randomDeriver.split(new Identifier("aquifer")).nextSplitter();
      this.oreRandomDeriver = this.randomDeriver.split(new Identifier("ore")).nextSplitter();
      this.noises = new ConcurrentHashMap();
      this.randomDerivers = new ConcurrentHashMap();
      this.surfaceBuilder = new SurfaceBuilder(this, chunkGeneratorSettings.defaultBlock(), chunkGeneratorSettings.seaLevel(), this.randomDeriver);
      final boolean bl = chunkGeneratorSettings.usesLegacyRandom();

      class LegacyNoiseDensityFunctionVisitor implements DensityFunction.DensityFunctionVisitor {
         private final Map cache = new HashMap();

         private Random createRandom(long seedx) {
            return new CheckedRandom(seed + seedx);
         }

         public DensityFunction.Noise apply(DensityFunction.Noise noiseDensityFunction) {
            RegistryEntry lv = noiseDensityFunction.noiseData();
            DoublePerlinNoiseSampler lv2;
            if (bl) {
               if (lv.matchesKey(NoiseParametersKeys.TEMPERATURE)) {
                  lv2 = DoublePerlinNoiseSampler.createLegacy(this.createRandom(0L), new DoublePerlinNoiseSampler.NoiseParameters(-7, 1.0, new double[]{1.0}));
                  return new DensityFunction.Noise(lv, lv2);
               }

               if (lv.matchesKey(NoiseParametersKeys.VEGETATION)) {
                  lv2 = DoublePerlinNoiseSampler.createLegacy(this.createRandom(1L), new DoublePerlinNoiseSampler.NoiseParameters(-7, 1.0, new double[]{1.0}));
                  return new DensityFunction.Noise(lv, lv2);
               }

               if (lv.matchesKey(NoiseParametersKeys.OFFSET)) {
                  lv2 = DoublePerlinNoiseSampler.create(NoiseConfig.this.randomDeriver.split(NoiseParametersKeys.OFFSET.getValue()), new DoublePerlinNoiseSampler.NoiseParameters(0, 0.0, new double[0]));
                  return new DensityFunction.Noise(lv, lv2);
               }
            }

            lv2 = NoiseConfig.this.getOrCreateSampler((RegistryKey)lv.getKey().orElseThrow());
            return new DensityFunction.Noise(lv, lv2);
         }

         private DensityFunction applyNotCached(DensityFunction densityFunction) {
            if (densityFunction instanceof InterpolatedNoiseSampler lv) {
               Random lv2 = bl ? this.createRandom(0L) : NoiseConfig.this.randomDeriver.split(new Identifier("terrain"));
               return lv.copyWithRandom(lv2);
            } else {
               return (DensityFunction)(densityFunction instanceof DensityFunctionTypes.EndIslands ? new DensityFunctionTypes.EndIslands(seed) : densityFunction);
            }
         }

         public DensityFunction apply(DensityFunction arg) {
            return (DensityFunction)this.cache.computeIfAbsent(arg, this::applyNotCached);
         }
      }

      this.noiseRouter = chunkGeneratorSettings.noiseRouter().apply(new LegacyNoiseDensityFunctionVisitor());
      DensityFunction.DensityFunctionVisitor lv = new DensityFunction.DensityFunctionVisitor() {
         private final Map unwrapped = new HashMap();

         private DensityFunction unwrap(DensityFunction densityFunction) {
            if (densityFunction instanceof DensityFunctionTypes.RegistryEntryHolder lv) {
               return (DensityFunction)lv.function().value();
            } else if (densityFunction instanceof DensityFunctionTypes.Wrapping lv2) {
               return lv2.wrapped();
            } else {
               return densityFunction;
            }
         }

         public DensityFunction apply(DensityFunction arg) {
            return (DensityFunction)this.unwrapped.computeIfAbsent(arg, this::unwrap);
         }
      };
      this.multiNoiseSampler = new MultiNoiseUtil.MultiNoiseSampler(this.noiseRouter.temperature().apply(lv), this.noiseRouter.vegetation().apply(lv), this.noiseRouter.continents().apply(lv), this.noiseRouter.erosion().apply(lv), this.noiseRouter.depth().apply(lv), this.noiseRouter.ridges().apply(lv), chunkGeneratorSettings.spawnTarget());
   }

   public DoublePerlinNoiseSampler getOrCreateSampler(RegistryKey noiseParametersKey) {
      return (DoublePerlinNoiseSampler)this.noises.computeIfAbsent(noiseParametersKey, (key) -> {
         return NoiseParametersKeys.createNoiseSampler(this.noiseParametersRegistry, this.randomDeriver, noiseParametersKey);
      });
   }

   public RandomSplitter getOrCreateRandomDeriver(Identifier id) {
      return (RandomSplitter)this.randomDerivers.computeIfAbsent(id, (id2) -> {
         return this.randomDeriver.split(id).nextSplitter();
      });
   }

   public NoiseRouter getNoiseRouter() {
      return this.noiseRouter;
   }

   public MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler() {
      return this.multiNoiseSampler;
   }

   public SurfaceBuilder getSurfaceBuilder() {
      return this.surfaceBuilder;
   }

   public RandomSplitter getAquiferRandomDeriver() {
      return this.aquiferRandomDeriver;
   }

   public RandomSplitter getOreRandomDeriver() {
      return this.oreRandomDeriver;
   }
}
