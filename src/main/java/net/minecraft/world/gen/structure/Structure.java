package net.minecraft.world.gen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;

public abstract class Structure {
   public static final Codec STRUCTURE_CODEC;
   public static final Codec ENTRY_CODEC;
   protected final Config config;

   public static RecordCodecBuilder configCodecBuilder(RecordCodecBuilder.Instance instance) {
      return Structure.Config.CODEC.forGetter((feature) -> {
         return feature.config;
      });
   }

   public static Codec createCodec(Function featureCreator) {
      return RecordCodecBuilder.create((instance) -> {
         return instance.group(configCodecBuilder(instance)).apply(instance, featureCreator);
      });
   }

   protected Structure(Config config) {
      this.config = config;
   }

   public RegistryEntryList getValidBiomes() {
      return this.config.biomes;
   }

   public Map getStructureSpawns() {
      return this.config.spawnOverrides;
   }

   public GenerationStep.Feature getFeatureGenerationStep() {
      return this.config.step;
   }

   public StructureTerrainAdaptation getTerrainAdaptation() {
      return this.config.terrainAdaptation;
   }

   public BlockBox expandBoxIfShouldAdaptNoise(BlockBox box) {
      return this.getTerrainAdaptation() != StructureTerrainAdaptation.NONE ? box.expand(12) : box;
   }

   public StructureStart createStructureStart(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, int references, HeightLimitView world, Predicate validBiomes) {
      Context lv = new Context(dynamicRegistryManager, chunkGenerator, biomeSource, noiseConfig, structureTemplateManager, seed, chunkPos, world, validBiomes);
      Optional optional = this.getValidStructurePosition(lv);
      if (optional.isPresent()) {
         StructurePiecesCollector lv2 = ((StructurePosition)optional.get()).generate();
         StructureStart lv3 = new StructureStart(this, chunkPos, references, lv2.toList());
         if (lv3.hasChildren()) {
            return lv3;
         }
      }

      return StructureStart.DEFAULT;
   }

   protected static Optional getStructurePosition(Context context, Heightmap.Type heightmap, Consumer generator) {
      ChunkPos lv = context.chunkPos();
      int i = lv.getCenterX();
      int j = lv.getCenterZ();
      int k = context.chunkGenerator().getHeightInGround(i, j, heightmap, context.world(), context.noiseConfig());
      return Optional.of(new StructurePosition(new BlockPos(i, k, j), generator));
   }

   private static boolean isBiomeValid(StructurePosition result, Context context) {
      BlockPos lv = result.position();
      return context.biomePredicate.test(context.chunkGenerator.getBiomeSource().getBiome(BiomeCoords.fromBlock(lv.getX()), BiomeCoords.fromBlock(lv.getY()), BiomeCoords.fromBlock(lv.getZ()), context.noiseConfig.getMultiNoiseSampler()));
   }

   public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
   }

   private static int[] getCornerHeights(Context context, int x, int width, int z, int height) {
      ChunkGenerator lv = context.chunkGenerator();
      HeightLimitView lv2 = context.world();
      NoiseConfig lv3 = context.noiseConfig();
      return new int[]{lv.getHeightInGround(x, z, Heightmap.Type.WORLD_SURFACE_WG, lv2, lv3), lv.getHeightInGround(x, z + height, Heightmap.Type.WORLD_SURFACE_WG, lv2, lv3), lv.getHeightInGround(x + width, z, Heightmap.Type.WORLD_SURFACE_WG, lv2, lv3), lv.getHeightInGround(x + width, z + height, Heightmap.Type.WORLD_SURFACE_WG, lv2, lv3)};
   }

   protected static int getMinCornerHeight(Context context, int width, int height) {
      ChunkPos lv = context.chunkPos();
      int k = lv.getStartX();
      int l = lv.getStartZ();
      return getMinCornerHeight(context, k, l, width, height);
   }

   protected static int getMinCornerHeight(Context context, int x, int z, int width, int height) {
      int[] is = getCornerHeights(context, x, width, z, height);
      return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
   }

   /** @deprecated */
   @Deprecated
   protected BlockPos getShiftedPos(Context context, BlockRotation rotation) {
      int i = 5;
      int j = 5;
      if (rotation == BlockRotation.CLOCKWISE_90) {
         i = -5;
      } else if (rotation == BlockRotation.CLOCKWISE_180) {
         i = -5;
         j = -5;
      } else if (rotation == BlockRotation.COUNTERCLOCKWISE_90) {
         j = -5;
      }

      ChunkPos lv = context.chunkPos();
      int k = lv.getOffsetX(7);
      int l = lv.getOffsetZ(7);
      return new BlockPos(k, getMinCornerHeight(context, k, l, i, j), l);
   }

   protected abstract Optional getStructurePosition(Context context);

   public Optional getValidStructurePosition(Context context) {
      return this.getStructurePosition(context).filter((position) -> {
         return isBiomeValid(position, context);
      });
   }

   public abstract StructureType getType();

   static {
      STRUCTURE_CODEC = Registries.STRUCTURE_TYPE.getCodec().dispatch(Structure::getType, StructureType::codec);
      ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.STRUCTURE, STRUCTURE_CODEC);
   }

   public static record Config(RegistryEntryList biomes, Map spawnOverrides, GenerationStep.Feature step, StructureTerrainAdaptation terrainAdaptation) {
      final RegistryEntryList biomes;
      final Map spawnOverrides;
      final GenerationStep.Feature step;
      final StructureTerrainAdaptation terrainAdaptation;
      public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> {
         return instance.group(RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biomes").forGetter(Config::biomes), Codec.simpleMap(SpawnGroup.CODEC, StructureSpawns.CODEC, StringIdentifiable.toKeyable(SpawnGroup.values())).fieldOf("spawn_overrides").forGetter(Config::spawnOverrides), GenerationStep.Feature.CODEC.fieldOf("step").forGetter(Config::step), StructureTerrainAdaptation.CODEC.optionalFieldOf("terrain_adaptation", StructureTerrainAdaptation.NONE).forGetter(Config::terrainAdaptation)).apply(instance, Config::new);
      });

      public Config(RegistryEntryList arg, Map map, GenerationStep.Feature arg2, StructureTerrainAdaptation arg3) {
         this.biomes = arg;
         this.spawnOverrides = map;
         this.step = arg2;
         this.terrainAdaptation = arg3;
      }

      public RegistryEntryList biomes() {
         return this.biomes;
      }

      public Map spawnOverrides() {
         return this.spawnOverrides;
      }

      public GenerationStep.Feature step() {
         return this.step;
      }

      public StructureTerrainAdaptation terrainAdaptation() {
         return this.terrainAdaptation;
      }
   }

   public static record Context(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, StructureTemplateManager structureTemplateManager, ChunkRandom random, long seed, ChunkPos chunkPos, HeightLimitView world, Predicate biomePredicate) {
      final ChunkGenerator chunkGenerator;
      final NoiseConfig noiseConfig;
      final Predicate biomePredicate;

      public Context(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, HeightLimitView world, Predicate biomePredicate) {
         this(dynamicRegistryManager, chunkGenerator, biomeSource, noiseConfig, structureTemplateManager, createChunkRandom(seed, chunkPos), seed, chunkPos, world, biomePredicate);
      }

      public Context(DynamicRegistryManager arg, ChunkGenerator arg2, BiomeSource arg3, NoiseConfig arg4, StructureTemplateManager arg5, ChunkRandom arg6, long l, ChunkPos arg7, HeightLimitView arg8, Predicate predicate) {
         this.dynamicRegistryManager = arg;
         this.chunkGenerator = arg2;
         this.biomeSource = arg3;
         this.noiseConfig = arg4;
         this.structureTemplateManager = arg5;
         this.random = arg6;
         this.seed = l;
         this.chunkPos = arg7;
         this.world = arg8;
         this.biomePredicate = predicate;
      }

      private static ChunkRandom createChunkRandom(long seed, ChunkPos chunkPos) {
         ChunkRandom lv = new ChunkRandom(new CheckedRandom(0L));
         lv.setCarverSeed(seed, chunkPos.x, chunkPos.z);
         return lv;
      }

      public DynamicRegistryManager dynamicRegistryManager() {
         return this.dynamicRegistryManager;
      }

      public ChunkGenerator chunkGenerator() {
         return this.chunkGenerator;
      }

      public BiomeSource biomeSource() {
         return this.biomeSource;
      }

      public NoiseConfig noiseConfig() {
         return this.noiseConfig;
      }

      public StructureTemplateManager structureTemplateManager() {
         return this.structureTemplateManager;
      }

      public ChunkRandom random() {
         return this.random;
      }

      public long seed() {
         return this.seed;
      }

      public ChunkPos chunkPos() {
         return this.chunkPos;
      }

      public HeightLimitView world() {
         return this.world;
      }

      public Predicate biomePredicate() {
         return this.biomePredicate;
      }
   }

   public static record StructurePosition(BlockPos position, Either generator) {
      public StructurePosition(BlockPos pos, Consumer generator) {
         this(pos, Either.left(generator));
      }

      public StructurePosition(BlockPos arg, Either either) {
         this.position = arg;
         this.generator = either;
      }

      public StructurePiecesCollector generate() {
         return (StructurePiecesCollector)this.generator.map((generator) -> {
            StructurePiecesCollector lv = new StructurePiecesCollector();
            generator.accept(lv);
            return lv;
         }, (collector) -> {
            return collector;
         });
      }

      public BlockPos position() {
         return this.position;
      }

      public Either generator() {
         return this.generator;
      }
   }
}
