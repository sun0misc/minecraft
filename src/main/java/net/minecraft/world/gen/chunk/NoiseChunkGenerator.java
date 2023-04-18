package net.minecraft.world.gen.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public final class NoiseChunkGenerator extends ChunkGenerator {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((generator) -> {
         return generator.biomeSource;
      }), ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter((generator) -> {
         return generator.settings;
      })).apply(instance, instance.stable(NoiseChunkGenerator::new));
   });
   private static final BlockState AIR;
   private final RegistryEntry settings;
   private final Supplier fluidLevelSampler;

   public NoiseChunkGenerator(BiomeSource biomeSource, RegistryEntry settings) {
      super(biomeSource);
      this.settings = settings;
      this.fluidLevelSampler = Suppliers.memoize(() -> {
         return createFluidLevelSampler((ChunkGeneratorSettings)settings.value());
      });
   }

   private static AquiferSampler.FluidLevelSampler createFluidLevelSampler(ChunkGeneratorSettings settings) {
      AquiferSampler.FluidLevel lv = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
      int i = settings.seaLevel();
      AquiferSampler.FluidLevel lv2 = new AquiferSampler.FluidLevel(i, settings.defaultFluid());
      AquiferSampler.FluidLevel lv3 = new AquiferSampler.FluidLevel(DimensionType.MIN_HEIGHT * 2, Blocks.AIR.getDefaultState());
      return (x, y, z) -> {
         return y < Math.min(-54, i) ? lv : lv2;
      };
   }

   public CompletableFuture populateBiomes(Executor executor, NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
      return CompletableFuture.supplyAsync(Util.debugSupplier("init_biomes", () -> {
         this.populateBiomes(blender, noiseConfig, structureAccessor, chunk);
         return chunk;
      }), Util.getMainWorkerExecutor());
   }

   private void populateBiomes(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
      ChunkNoiseSampler lv = chunk.getOrCreateChunkNoiseSampler((chunkx) -> {
         return this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig);
      });
      BiomeSupplier lv2 = BelowZeroRetrogen.getBiomeSupplier(blender.getBiomeSupplier(this.biomeSource), chunk);
      chunk.populateBiomes(lv2, lv.createMultiNoiseSampler(noiseConfig.getNoiseRouter(), ((ChunkGeneratorSettings)this.settings.value()).spawnTarget()));
   }

   private ChunkNoiseSampler createChunkNoiseSampler(Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig) {
      return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.createStructureWeightSampler(world, chunk.getPos()), (ChunkGeneratorSettings)this.settings.value(), (AquiferSampler.FluidLevelSampler)this.fluidLevelSampler.get(), blender);
   }

   protected Codec getCodec() {
      return CODEC;
   }

   public RegistryEntry getSettings() {
      return this.settings;
   }

   public boolean matchesSettings(RegistryKey settings) {
      return this.settings.matchesKey(settings);
   }

   public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
      return this.sampleHeightmap(world, noiseConfig, x, z, (MutableObject)null, heightmap.getBlockPredicate()).orElse(world.getBottomY());
   }

   public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
      MutableObject mutableObject = new MutableObject();
      this.sampleHeightmap(world, noiseConfig, x, z, mutableObject, (Predicate)null);
      return (VerticalBlockSample)mutableObject.getValue();
   }

   public void getDebugHudText(List text, NoiseConfig noiseConfig, BlockPos pos) {
      DecimalFormat decimalFormat = new DecimalFormat("0.000");
      NoiseRouter lv = noiseConfig.getNoiseRouter();
      DensityFunction.UnblendedNoisePos lv2 = new DensityFunction.UnblendedNoisePos(pos.getX(), pos.getY(), pos.getZ());
      double d = lv.ridges().sample(lv2);
      String var10001 = decimalFormat.format(lv.temperature().sample(lv2));
      text.add("NoiseRouter T: " + var10001 + " V: " + decimalFormat.format(lv.vegetation().sample(lv2)) + " C: " + decimalFormat.format(lv.continents().sample(lv2)) + " E: " + decimalFormat.format(lv.erosion().sample(lv2)) + " D: " + decimalFormat.format(lv.depth().sample(lv2)) + " W: " + decimalFormat.format(d) + " PV: " + decimalFormat.format((double)DensityFunctions.getPeaksValleysNoise((float)d)) + " AS: " + decimalFormat.format(lv.initialDensityWithoutJaggedness().sample(lv2)) + " N: " + decimalFormat.format(lv.finalDensity().sample(lv2)));
   }

   private OptionalInt sampleHeightmap(HeightLimitView world, NoiseConfig noiseConfig, int x, int z, @Nullable MutableObject columnSample, @Nullable Predicate stopPredicate) {
      GenerationShapeConfig lv = ((ChunkGeneratorSettings)this.settings.value()).generationShapeConfig().trimHeight(world);
      int k = lv.verticalCellBlockCount();
      int l = lv.minimumY();
      int m = MathHelper.floorDiv(l, k);
      int n = MathHelper.floorDiv(lv.height(), k);
      if (n <= 0) {
         return OptionalInt.empty();
      } else {
         BlockState[] lvs;
         if (columnSample == null) {
            lvs = null;
         } else {
            lvs = new BlockState[lv.height()];
            columnSample.setValue(new VerticalBlockSample(l, lvs));
         }

         int o = lv.horizontalCellBlockCount();
         int p = Math.floorDiv(x, o);
         int q = Math.floorDiv(z, o);
         int r = Math.floorMod(x, o);
         int s = Math.floorMod(z, o);
         int t = p * o;
         int u = q * o;
         double d = (double)r / (double)o;
         double e = (double)s / (double)o;
         ChunkNoiseSampler lv2 = new ChunkNoiseSampler(1, noiseConfig, t, u, lv, DensityFunctionTypes.Beardifier.INSTANCE, (ChunkGeneratorSettings)this.settings.value(), (AquiferSampler.FluidLevelSampler)this.fluidLevelSampler.get(), Blender.getNoBlending());
         lv2.sampleStartDensity();
         lv2.sampleEndDensity(0);

         for(int v = n - 1; v >= 0; --v) {
            lv2.onSampledCellCorners(v, 0);

            for(int w = k - 1; w >= 0; --w) {
               int x = (m + v) * k + w;
               double f = (double)w / (double)k;
               lv2.interpolateY(x, f);
               lv2.interpolateX(x, d);
               lv2.interpolateZ(z, e);
               BlockState lv3 = lv2.sampleBlockState();
               BlockState lv4 = lv3 == null ? ((ChunkGeneratorSettings)this.settings.value()).defaultBlock() : lv3;
               if (lvs != null) {
                  int y = v * k + w;
                  lvs[y] = lv4;
               }

               if (stopPredicate != null && stopPredicate.test(lv4)) {
                  lv2.stopInterpolation();
                  return OptionalInt.of(x + 1);
               }
            }
         }

         lv2.stopInterpolation();
         return OptionalInt.empty();
      }
   }

   public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
      if (!SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
         HeightContext lv = new HeightContext(this, region);
         this.buildSurface(chunk, lv, noiseConfig, structures, region.getBiomeAccess(), region.getRegistryManager().get(RegistryKeys.BIOME), Blender.getBlender(region));
      }
   }

   @VisibleForTesting
   public void buildSurface(Chunk chunk, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry biomeRegistry, Blender blender) {
      ChunkNoiseSampler lv = chunk.getOrCreateChunkNoiseSampler((chunkx) -> {
         return this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig);
      });
      ChunkGeneratorSettings lv2 = (ChunkGeneratorSettings)this.settings.value();
      noiseConfig.getSurfaceBuilder().buildSurface(noiseConfig, biomeAccess, biomeRegistry, lv2.usesLegacyRandom(), heightContext, chunk, lv, lv2.surfaceRule());
   }

   public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
      BiomeAccess lv = biomeAccess.withSource((biomeX, biomeY, biomeZ) -> {
         return this.biomeSource.getBiome(biomeX, biomeY, biomeZ, noiseConfig.getMultiNoiseSampler());
      });
      ChunkRandom lv2 = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
      int i = true;
      ChunkPos lv3 = chunk.getPos();
      ChunkNoiseSampler lv4 = chunk.getOrCreateChunkNoiseSampler((chunkx) -> {
         return this.createChunkNoiseSampler(chunkx, structureAccessor, Blender.getBlender(chunkRegion), noiseConfig);
      });
      AquiferSampler lv5 = lv4.getAquiferSampler();
      CarverContext lv6 = new CarverContext(this, chunkRegion.getRegistryManager(), chunk.getHeightLimitView(), lv4, noiseConfig, ((ChunkGeneratorSettings)this.settings.value()).surfaceRule());
      CarvingMask lv7 = ((ProtoChunk)chunk).getOrCreateCarvingMask(carverStep);

      for(int j = -8; j <= 8; ++j) {
         for(int k = -8; k <= 8; ++k) {
            ChunkPos lv8 = new ChunkPos(lv3.x + j, lv3.z + k);
            Chunk lv9 = chunkRegion.getChunk(lv8.x, lv8.z);
            GenerationSettings lv10 = lv9.getOrCreateGenerationSettings(() -> {
               return this.getGenerationSettings(this.biomeSource.getBiome(BiomeCoords.fromBlock(lv8.getStartX()), 0, BiomeCoords.fromBlock(lv8.getStartZ()), noiseConfig.getMultiNoiseSampler()));
            });
            Iterable iterable = lv10.getCarversForStep(carverStep);
            int m = 0;

            for(Iterator var24 = iterable.iterator(); var24.hasNext(); ++m) {
               RegistryEntry lv11 = (RegistryEntry)var24.next();
               ConfiguredCarver lv12 = (ConfiguredCarver)lv11.value();
               lv2.setCarverSeed(seed + (long)m, lv8.x, lv8.z);
               if (lv12.shouldCarve(lv2)) {
                  Objects.requireNonNull(lv);
                  lv12.carve(lv6, chunk, lv::getBiome, lv2, lv5, lv8, lv7);
               }
            }
         }
      }

   }

   public CompletableFuture populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
      GenerationShapeConfig lv = ((ChunkGeneratorSettings)this.settings.value()).generationShapeConfig().trimHeight(chunk.getHeightLimitView());
      int i = lv.minimumY();
      int j = MathHelper.floorDiv(i, lv.verticalCellBlockCount());
      int k = MathHelper.floorDiv(lv.height(), lv.verticalCellBlockCount());
      if (k <= 0) {
         return CompletableFuture.completedFuture(chunk);
      } else {
         int l = chunk.getSectionIndex(k * lv.verticalCellBlockCount() - 1 + i);
         int m = chunk.getSectionIndex(i);
         Set set = Sets.newHashSet();

         for(int n = l; n >= m; --n) {
            ChunkSection lv2 = chunk.getSection(n);
            lv2.lock();
            set.add(lv2);
         }

         return CompletableFuture.supplyAsync(Util.debugSupplier("wgen_fill_noise", () -> {
            return this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k);
         }), Util.getMainWorkerExecutor()).whenCompleteAsync((arg, throwable) -> {
            Iterator var3 = set.iterator();

            while(var3.hasNext()) {
               ChunkSection lv = (ChunkSection)var3.next();
               lv.unlock();
            }

         }, executor);
      }
   }

   private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
      ChunkNoiseSampler lv = chunk.getOrCreateChunkNoiseSampler((chunkx) -> {
         return this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig);
      });
      Heightmap lv2 = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
      Heightmap lv3 = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
      ChunkPos lv4 = chunk.getPos();
      int k = lv4.getStartX();
      int l = lv4.getStartZ();
      AquiferSampler lv5 = lv.getAquiferSampler();
      lv.sampleStartDensity();
      BlockPos.Mutable lv6 = new BlockPos.Mutable();
      int m = lv.getHorizontalCellBlockCount();
      int n = lv.getVerticalCellBlockCount();
      int o = 16 / m;
      int p = 16 / m;

      for(int q = 0; q < o; ++q) {
         lv.sampleEndDensity(q);

         for(int r = 0; r < p; ++r) {
            ChunkSection lv7 = chunk.getSection(chunk.countVerticalSections() - 1);

            for(int s = cellHeight - 1; s >= 0; --s) {
               lv.onSampledCellCorners(s, r);

               for(int t = n - 1; t >= 0; --t) {
                  int u = (minimumCellY + s) * n + t;
                  int v = u & 15;
                  int w = chunk.getSectionIndex(u);
                  if (chunk.getSectionIndex(lv7.getYOffset()) != w) {
                     lv7 = chunk.getSection(w);
                  }

                  double d = (double)t / (double)n;
                  lv.interpolateY(u, d);

                  for(int x = 0; x < m; ++x) {
                     int y = k + q * m + x;
                     int z = y & 15;
                     double e = (double)x / (double)m;
                     lv.interpolateX(y, e);

                     for(int aa = 0; aa < m; ++aa) {
                        int ab = l + r * m + aa;
                        int ac = ab & 15;
                        double f = (double)aa / (double)m;
                        lv.interpolateZ(ab, f);
                        BlockState lv8 = lv.sampleBlockState();
                        if (lv8 == null) {
                           lv8 = ((ChunkGeneratorSettings)this.settings.value()).defaultBlock();
                        }

                        lv8 = this.getBlockState(lv, y, u, ab, lv8);
                        if (lv8 != AIR && !SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
                           if (lv8.getLuminance() != 0 && chunk instanceof ProtoChunk) {
                              lv6.set(y, u, ab);
                              ((ProtoChunk)chunk).addLightSource(lv6);
                           }

                           lv7.setBlockState(z, v, ac, lv8, false);
                           lv2.trackUpdate(z, u, ac, lv8);
                           lv3.trackUpdate(z, u, ac, lv8);
                           if (lv5.needsFluidTick() && !lv8.getFluidState().isEmpty()) {
                              lv6.set(y, u, ab);
                              chunk.markBlockForPostProcessing(lv6);
                           }
                        }
                     }
                  }
               }
            }
         }

         lv.swapBuffers();
      }

      lv.stopInterpolation();
      return chunk;
   }

   private BlockState getBlockState(ChunkNoiseSampler chunkNoiseSampler, int x, int y, int z, BlockState state) {
      return state;
   }

   public int getWorldHeight() {
      return ((ChunkGeneratorSettings)this.settings.value()).generationShapeConfig().height();
   }

   public int getSeaLevel() {
      return ((ChunkGeneratorSettings)this.settings.value()).seaLevel();
   }

   public int getMinimumY() {
      return ((ChunkGeneratorSettings)this.settings.value()).generationShapeConfig().minimumY();
   }

   public void populateEntities(ChunkRegion region) {
      if (!((ChunkGeneratorSettings)this.settings.value()).mobGenerationDisabled()) {
         ChunkPos lv = region.getCenterPos();
         RegistryEntry lv2 = region.getBiome(lv.getStartPos().withY(region.getTopY() - 1));
         ChunkRandom lv3 = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
         lv3.setPopulationSeed(region.getSeed(), lv.getStartX(), lv.getStartZ());
         SpawnHelper.populateEntities(region, lv2, lv, lv3);
      }
   }

   static {
      AIR = Blocks.AIR.getDefaultState();
   }
}
