package net.minecraft.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GravityField;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.NetherFortressStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class SpawnHelper {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MIN_SPAWN_DISTANCE = 24;
   public static final int field_30972 = 8;
   public static final int field_30973 = 128;
   static final int CHUNK_AREA = (int)Math.pow(17.0, 2.0);
   private static final SpawnGroup[] SPAWNABLE_GROUPS = (SpawnGroup[])Stream.of(SpawnGroup.values()).filter((spawnGroup) -> {
      return spawnGroup != SpawnGroup.MISC;
   }).toArray((i) -> {
      return new SpawnGroup[i];
   });

   private SpawnHelper() {
   }

   public static Info setupSpawn(int spawningChunkCount, Iterable entities, ChunkSource chunkSource, SpawnDensityCapper densityCapper) {
      GravityField lv = new GravityField();
      Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
      Iterator var6 = entities.iterator();

      while(true) {
         Entity lv2;
         MobEntity lv3;
         do {
            if (!var6.hasNext()) {
               return new Info(spawningChunkCount, object2IntOpenHashMap, lv, densityCapper);
            }

            lv2 = (Entity)var6.next();
            if (!(lv2 instanceof MobEntity)) {
               break;
            }

            lv3 = (MobEntity)lv2;
         } while(lv3.isPersistent() || lv3.cannotDespawn());

         SpawnGroup lv4 = lv2.getType().getSpawnGroup();
         if (lv4 != SpawnGroup.MISC) {
            BlockPos lv5 = lv2.getBlockPos();
            chunkSource.query(ChunkPos.toLong(lv5), (chunk) -> {
               SpawnSettings.SpawnDensity lvx = getBiomeDirectly(lv5, chunk).getSpawnSettings().getSpawnDensity(lv2.getType());
               if (lvx != null) {
                  lv.addPoint(lv2.getBlockPos(), lvx.mass());
               }

               if (lv2 instanceof MobEntity) {
                  densityCapper.increaseDensity(chunk.getPos(), lv4);
               }

               object2IntOpenHashMap.addTo(lv4, 1);
            });
         }
      }
   }

   static Biome getBiomeDirectly(BlockPos pos, Chunk chunk) {
      return (Biome)chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getY()), BiomeCoords.fromBlock(pos.getZ())).value();
   }

   public static void spawn(ServerWorld world, WorldChunk chunk, Info info, boolean spawnAnimals, boolean spawnMonsters, boolean rareSpawn) {
      world.getProfiler().push("spawner");
      SpawnGroup[] var6 = SPAWNABLE_GROUPS;
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         SpawnGroup lv = var6[var8];
         if ((spawnAnimals || !lv.isPeaceful()) && (spawnMonsters || lv.isPeaceful()) && (rareSpawn || !lv.isRare()) && info.isBelowCap(lv, chunk.getPos())) {
            Objects.requireNonNull(info);
            Checker var10003 = info::test;
            Objects.requireNonNull(info);
            spawnEntitiesInChunk(lv, world, chunk, var10003, info::run);
         }
      }

      world.getProfiler().pop();
   }

   public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, WorldChunk chunk, Checker checker, Runner runner) {
      BlockPos lv = getRandomPosInChunkSection(world, chunk);
      if (lv.getY() >= world.getBottomY() + 1) {
         spawnEntitiesInChunk(group, world, chunk, lv, checker, runner);
      }
   }

   @Debug
   public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, BlockPos pos) {
      spawnEntitiesInChunk(group, world, world.getChunk(pos), pos, (type, posx, chunk) -> {
         return true;
      }, (entity, chunk) -> {
      });
   }

   public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, Checker checker, Runner runner) {
      StructureAccessor lv = world.getStructureAccessor();
      ChunkGenerator lv2 = world.getChunkManager().getChunkGenerator();
      int i = pos.getY();
      BlockState lv3 = chunk.getBlockState(pos);
      if (!lv3.isSolidBlock(chunk, pos)) {
         BlockPos.Mutable lv4 = new BlockPos.Mutable();
         int j = 0;

         for(int k = 0; k < 3; ++k) {
            int l = pos.getX();
            int m = pos.getZ();
            int n = true;
            SpawnSettings.SpawnEntry lv5 = null;
            EntityData lv6 = null;
            int o = MathHelper.ceil(world.random.nextFloat() * 4.0F);
            int p = 0;

            for(int q = 0; q < o; ++q) {
               l += world.random.nextInt(6) - world.random.nextInt(6);
               m += world.random.nextInt(6) - world.random.nextInt(6);
               lv4.set(l, i, m);
               double d = (double)l + 0.5;
               double e = (double)m + 0.5;
               PlayerEntity lv7 = world.getClosestPlayer(d, (double)i, e, -1.0, false);
               if (lv7 != null) {
                  double f = lv7.squaredDistanceTo(d, (double)i, e);
                  if (isAcceptableSpawnPosition(world, chunk, lv4, f)) {
                     if (lv5 == null) {
                        Optional optional = pickRandomSpawnEntry(world, lv, lv2, group, world.random, lv4);
                        if (optional.isEmpty()) {
                           break;
                        }

                        lv5 = (SpawnSettings.SpawnEntry)optional.get();
                        o = lv5.minGroupSize + world.random.nextInt(1 + lv5.maxGroupSize - lv5.minGroupSize);
                     }

                     if (canSpawn(world, group, lv, lv2, lv5, lv4, f) && checker.test(lv5.type, lv4, chunk)) {
                        MobEntity lv8 = createMob(world, lv5.type);
                        if (lv8 == null) {
                           return;
                        }

                        lv8.refreshPositionAndAngles(d, (double)i, e, world.random.nextFloat() * 360.0F, 0.0F);
                        if (isValidSpawn(world, lv8, f)) {
                           lv6 = lv8.initialize(world, world.getLocalDifficulty(lv8.getBlockPos()), SpawnReason.NATURAL, lv6, (NbtCompound)null);
                           ++j;
                           ++p;
                           world.spawnEntityAndPassengers(lv8);
                           runner.run(lv8, chunk);
                           if (j >= lv8.getLimitPerChunk()) {
                              return;
                           }

                           if (lv8.spawnsTooManyForEachTry(p)) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static boolean isAcceptableSpawnPosition(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance) {
      if (squaredDistance <= 576.0) {
         return false;
      } else if (world.getSpawnPos().isWithinDistance(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5), 24.0)) {
         return false;
      } else {
         return Objects.equals(new ChunkPos(pos), chunk.getPos()) || world.shouldTick((BlockPos)pos);
      }
   }

   private static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance) {
      EntityType lv = spawnEntry.type;
      if (lv.getSpawnGroup() == SpawnGroup.MISC) {
         return false;
      } else if (!lv.isSpawnableFarFromPlayer() && squaredDistance > (double)(lv.getSpawnGroup().getImmediateDespawnRange() * lv.getSpawnGroup().getImmediateDespawnRange())) {
         return false;
      } else if (lv.isSummonable() && containsSpawnEntry(world, structureAccessor, chunkGenerator, group, spawnEntry, pos)) {
         SpawnRestriction.Location lv2 = SpawnRestriction.getLocation(lv);
         if (!canSpawn(lv2, world, pos, lv)) {
            return false;
         } else if (!SpawnRestriction.canSpawn(lv, world, SpawnReason.NATURAL, pos, world.random)) {
            return false;
         } else {
            return world.isSpaceEmpty(lv.createSimpleBoundingBox((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5));
         }
      } else {
         return false;
      }
   }

   @Nullable
   private static MobEntity createMob(ServerWorld world, EntityType type) {
      try {
         Entity var3 = type.create(world);
         if (var3 instanceof MobEntity lv) {
            return lv;
         }

         LOGGER.warn("Can't spawn entity of type: {}", Registries.ENTITY_TYPE.getId(type));
      } catch (Exception var4) {
         LOGGER.warn("Failed to create mob", var4);
      }

      return null;
   }

   private static boolean isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance) {
      if (squaredDistance > (double)(entity.getType().getSpawnGroup().getImmediateDespawnRange() * entity.getType().getSpawnGroup().getImmediateDespawnRange()) && entity.canImmediatelyDespawn(squaredDistance)) {
         return false;
      } else {
         return entity.canSpawn(world, SpawnReason.NATURAL) && entity.canSpawn(world);
      }
   }

   private static Optional pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
      RegistryEntry lv = world.getBiome(pos);
      return spawnGroup == SpawnGroup.WATER_AMBIENT && lv.isIn(BiomeTags.REDUCE_WATER_AMBIENT_SPAWNS) && random.nextFloat() < 0.98F ? Optional.empty() : getSpawnEntries(world, structureAccessor, chunkGenerator, spawnGroup, pos, lv).getOrEmpty(random);
   }

   private static boolean containsSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry, BlockPos pos) {
      return getSpawnEntries(world, structureAccessor, chunkGenerator, spawnGroup, pos, (RegistryEntry)null).getEntries().contains(spawnEntry);
   }

   private static Pool getSpawnEntries(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos pos, @Nullable RegistryEntry biomeEntry) {
      return shouldUseNetherFortressSpawns(pos, world, spawnGroup, structureAccessor) ? NetherFortressStructure.MONSTER_SPAWNS : chunkGenerator.getEntitySpawnList(biomeEntry != null ? biomeEntry : world.getBiome(pos), structureAccessor, spawnGroup, pos);
   }

   public static boolean shouldUseNetherFortressSpawns(BlockPos pos, ServerWorld world, SpawnGroup spawnGroup, StructureAccessor structureAccessor) {
      if (spawnGroup == SpawnGroup.MONSTER && world.getBlockState(pos.down()).isOf(Blocks.NETHER_BRICKS)) {
         Structure lv = (Structure)structureAccessor.getRegistryManager().get(RegistryKeys.STRUCTURE).get(StructureKeys.FORTRESS);
         return lv == null ? false : structureAccessor.getStructureAt(pos, lv).hasChildren();
      } else {
         return false;
      }
   }

   private static BlockPos getRandomPosInChunkSection(World world, WorldChunk chunk) {
      ChunkPos lv = chunk.getPos();
      int i = lv.getStartX() + world.random.nextInt(16);
      int j = lv.getStartZ() + world.random.nextInt(16);
      int k = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, i, j) + 1;
      int l = MathHelper.nextBetween(world.random, world.getBottomY(), k);
      return new BlockPos(i, l, j);
   }

   public static boolean isClearForSpawn(BlockView blockView, BlockPos pos, BlockState state, FluidState fluidState, EntityType entityType) {
      if (state.isFullCube(blockView, pos)) {
         return false;
      } else if (state.emitsRedstonePower()) {
         return false;
      } else if (!fluidState.isEmpty()) {
         return false;
      } else if (state.isIn(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
         return false;
      } else {
         return !entityType.isInvalidSpawn(state);
      }
   }

   public static boolean canSpawn(SpawnRestriction.Location location, WorldView world, BlockPos pos, @Nullable EntityType entityType) {
      if (location == SpawnRestriction.Location.NO_RESTRICTIONS) {
         return true;
      } else if (entityType != null && world.getWorldBorder().contains(pos)) {
         BlockState lv = world.getBlockState(pos);
         FluidState lv2 = world.getFluidState(pos);
         BlockPos lv3 = pos.up();
         BlockPos lv4 = pos.down();
         switch (location) {
            case IN_WATER:
               return lv2.isIn(FluidTags.WATER) && !world.getBlockState(lv3).isSolidBlock(world, lv3);
            case IN_LAVA:
               return lv2.isIn(FluidTags.LAVA);
            case ON_GROUND:
            default:
               BlockState lv5 = world.getBlockState(lv4);
               if (!lv5.allowsSpawning(world, lv4, entityType)) {
                  return false;
               } else {
                  return isClearForSpawn(world, pos, lv, lv2, entityType) && isClearForSpawn(world, lv3, world.getBlockState(lv3), world.getFluidState(lv3), entityType);
               }
         }
      } else {
         return false;
      }
   }

   public static void populateEntities(ServerWorldAccess world, RegistryEntry biomeEntry, ChunkPos chunkPos, Random random) {
      SpawnSettings lv = ((Biome)biomeEntry.value()).getSpawnSettings();
      Pool lv2 = lv.getSpawnEntries(SpawnGroup.CREATURE);
      if (!lv2.isEmpty()) {
         int i = chunkPos.getStartX();
         int j = chunkPos.getStartZ();

         while(true) {
            Optional optional;
            do {
               if (!(random.nextFloat() < lv.getCreatureSpawnProbability())) {
                  return;
               }

               optional = lv2.getOrEmpty(random);
            } while(!optional.isPresent());

            SpawnSettings.SpawnEntry lv3 = (SpawnSettings.SpawnEntry)optional.get();
            int k = lv3.minGroupSize + random.nextInt(1 + lv3.maxGroupSize - lv3.minGroupSize);
            EntityData lv4 = null;
            int l = i + random.nextInt(16);
            int m = j + random.nextInt(16);
            int n = l;
            int o = m;

            for(int p = 0; p < k; ++p) {
               boolean bl = false;

               for(int q = 0; !bl && q < 4; ++q) {
                  BlockPos lv5 = getEntitySpawnPos(world, lv3.type, l, m);
                  if (lv3.type.isSummonable() && canSpawn(SpawnRestriction.getLocation(lv3.type), world, lv5, lv3.type)) {
                     float f = lv3.type.getWidth();
                     double d = MathHelper.clamp((double)l, (double)i + (double)f, (double)i + 16.0 - (double)f);
                     double e = MathHelper.clamp((double)m, (double)j + (double)f, (double)j + 16.0 - (double)f);
                     if (!world.isSpaceEmpty(lv3.type.createSimpleBoundingBox(d, (double)lv5.getY(), e)) || !SpawnRestriction.canSpawn(lv3.type, world, SpawnReason.CHUNK_GENERATION, BlockPos.ofFloored(d, (double)lv5.getY(), e), world.getRandom())) {
                        continue;
                     }

                     Entity lv6;
                     try {
                        lv6 = lv3.type.create(world.toServerWorld());
                     } catch (Exception var27) {
                        LOGGER.warn("Failed to create mob", var27);
                        continue;
                     }

                     if (lv6 == null) {
                        continue;
                     }

                     lv6.refreshPositionAndAngles(d, (double)lv5.getY(), e, random.nextFloat() * 360.0F, 0.0F);
                     if (lv6 instanceof MobEntity) {
                        MobEntity lv7 = (MobEntity)lv6;
                        if (lv7.canSpawn(world, SpawnReason.CHUNK_GENERATION) && lv7.canSpawn(world)) {
                           lv4 = lv7.initialize(world, world.getLocalDifficulty(lv7.getBlockPos()), SpawnReason.CHUNK_GENERATION, lv4, (NbtCompound)null);
                           world.spawnEntityAndPassengers(lv7);
                           bl = true;
                        }
                     }
                  }

                  l += random.nextInt(5) - random.nextInt(5);

                  for(m += random.nextInt(5) - random.nextInt(5); l < i || l >= i + 16 || m < j || m >= j + 16; m = o + random.nextInt(5) - random.nextInt(5)) {
                     l = n + random.nextInt(5) - random.nextInt(5);
                  }
               }
            }
         }
      }
   }

   private static BlockPos getEntitySpawnPos(WorldView world, EntityType entityType, int x, int z) {
      int k = world.getTopY(SpawnRestriction.getHeightmapType(entityType), x, z);
      BlockPos.Mutable lv = new BlockPos.Mutable(x, k, z);
      if (world.getDimension().hasCeiling()) {
         do {
            lv.move(Direction.DOWN);
         } while(!world.getBlockState(lv).isAir());

         do {
            lv.move(Direction.DOWN);
         } while(world.getBlockState(lv).isAir() && lv.getY() > world.getBottomY());
      }

      if (SpawnRestriction.getLocation(entityType) == SpawnRestriction.Location.ON_GROUND) {
         BlockPos lv2 = lv.down();
         if (world.getBlockState(lv2).canPathfindThrough(world, lv2, NavigationType.LAND)) {
            return lv2;
         }
      }

      return lv.toImmutable();
   }

   @FunctionalInterface
   public interface ChunkSource {
      void query(long pos, Consumer chunkConsumer);
   }

   public static class Info {
      private final int spawningChunkCount;
      private final Object2IntOpenHashMap groupToCount;
      private final GravityField densityField;
      private final Object2IntMap groupToCountView;
      private final SpawnDensityCapper densityCapper;
      @Nullable
      private BlockPos cachedPos;
      @Nullable
      private EntityType cachedEntityType;
      private double cachedDensityMass;

      Info(int spawningChunkCount, Object2IntOpenHashMap groupToCount, GravityField densityField, SpawnDensityCapper densityCapper) {
         this.spawningChunkCount = spawningChunkCount;
         this.groupToCount = groupToCount;
         this.densityField = densityField;
         this.densityCapper = densityCapper;
         this.groupToCountView = Object2IntMaps.unmodifiable(groupToCount);
      }

      private boolean test(EntityType type, BlockPos pos, Chunk chunk) {
         this.cachedPos = pos;
         this.cachedEntityType = type;
         SpawnSettings.SpawnDensity lv = SpawnHelper.getBiomeDirectly(pos, chunk).getSpawnSettings().getSpawnDensity(type);
         if (lv == null) {
            this.cachedDensityMass = 0.0;
            return true;
         } else {
            double d = lv.mass();
            this.cachedDensityMass = d;
            double e = this.densityField.calculate(pos, d);
            return e <= lv.gravityLimit();
         }
      }

      private void run(MobEntity entity, Chunk chunk) {
         EntityType lv = entity.getType();
         BlockPos lv2 = entity.getBlockPos();
         double d;
         if (lv2.equals(this.cachedPos) && lv == this.cachedEntityType) {
            d = this.cachedDensityMass;
         } else {
            SpawnSettings.SpawnDensity lv3 = SpawnHelper.getBiomeDirectly(lv2, chunk).getSpawnSettings().getSpawnDensity(lv);
            if (lv3 != null) {
               d = lv3.mass();
            } else {
               d = 0.0;
            }
         }

         this.densityField.addPoint(lv2, d);
         SpawnGroup lv4 = lv.getSpawnGroup();
         this.groupToCount.addTo(lv4, 1);
         this.densityCapper.increaseDensity(new ChunkPos(lv2), lv4);
      }

      public int getSpawningChunkCount() {
         return this.spawningChunkCount;
      }

      public Object2IntMap getGroupToCount() {
         return this.groupToCountView;
      }

      boolean isBelowCap(SpawnGroup group, ChunkPos chunkPos) {
         int i = group.getCapacity() * this.spawningChunkCount / SpawnHelper.CHUNK_AREA;
         if (this.groupToCount.getInt(group) >= i) {
            return false;
         } else {
            return this.densityCapper.canSpawn(group, chunkPos);
         }
      }
   }

   @FunctionalInterface
   public interface Checker {
      boolean test(EntityType type, BlockPos pos, Chunk chunk);
   }

   @FunctionalInterface
   public interface Runner {
      void run(MobEntity entity, Chunk chunk);
   }
}
