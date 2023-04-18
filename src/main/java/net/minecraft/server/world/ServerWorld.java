package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InteractionObserver;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.EntityList;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IdCountsState;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventDispatchManager;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.Spawner;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.EntityChunkDataAccess;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerWorld extends World implements StructureWorldAccess {
   public static final BlockPos END_SPAWN_POS = new BlockPos(100, 50, 0);
   public static final IntProvider CLEAR_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(12000, 180000);
   public static final IntProvider RAIN_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(12000, 24000);
   private static final IntProvider CLEAR_THUNDER_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(12000, 180000);
   public static final IntProvider THUNDER_WEATHER_DURATION_PROVIDER = UniformIntProvider.create(3600, 15600);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SERVER_IDLE_COOLDOWN = 300;
   private static final int MAX_TICKS = 65536;
   final List players;
   private final ServerChunkManager chunkManager;
   private final MinecraftServer server;
   private final ServerWorldProperties worldProperties;
   final EntityList entityList;
   private final ServerEntityManager entityManager;
   private final GameEventDispatchManager gameEventDispatchManager;
   public boolean savingDisabled;
   private final SleepManager sleepManager;
   private int idleTimeout;
   private final PortalForcer portalForcer;
   private final WorldTickScheduler blockTickScheduler;
   private final WorldTickScheduler fluidTickScheduler;
   final Set loadedMobs;
   volatile boolean duringListenerUpdate;
   protected final RaidManager raidManager;
   private final ObjectLinkedOpenHashSet syncedBlockEventQueue;
   private final List blockEventQueue;
   private boolean inBlockTick;
   private final List spawners;
   @Nullable
   private final EnderDragonFight enderDragonFight;
   final Int2ObjectMap dragonParts;
   private final StructureAccessor structureAccessor;
   private final StructureLocator structureLocator;
   private final boolean shouldTickTime;

   public ServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime) {
      DynamicRegistryManager.Immutable var10003 = server.getRegistryManager();
      RegistryEntry var10004 = dimensionOptions.dimensionTypeEntry();
      Objects.requireNonNull(server);
      super(properties, worldKey, var10003, var10004, server::getProfiler, false, debugWorld, seed, server.getMaxChainedNeighborUpdates());
      this.players = Lists.newArrayList();
      this.entityList = new EntityList();
      this.blockTickScheduler = new WorldTickScheduler(this::isTickingFutureReady, this.getProfilerSupplier());
      this.fluidTickScheduler = new WorldTickScheduler(this::isTickingFutureReady, this.getProfilerSupplier());
      this.loadedMobs = new ObjectOpenHashSet();
      this.syncedBlockEventQueue = new ObjectLinkedOpenHashSet();
      this.blockEventQueue = new ArrayList(64);
      this.dragonParts = new Int2ObjectOpenHashMap();
      this.shouldTickTime = shouldTickTime;
      this.server = server;
      this.spawners = spawners;
      this.worldProperties = properties;
      ChunkGenerator lv = dimensionOptions.chunkGenerator();
      boolean bl3 = server.syncChunkWrites();
      DataFixer dataFixer = server.getDataFixer();
      ChunkDataAccess lv2 = new EntityChunkDataAccess(this, session.getWorldDirectory(worldKey).resolve("entities"), dataFixer, bl3, server);
      this.entityManager = new ServerEntityManager(Entity.class, new ServerEntityHandler(), lv2);
      StructureTemplateManager var10006 = server.getStructureTemplateManager();
      int var10009 = server.getPlayerManager().getViewDistance();
      int var10010 = server.getPlayerManager().getSimulationDistance();
      ServerEntityManager var10013 = this.entityManager;
      Objects.requireNonNull(var10013);
      this.chunkManager = new ServerChunkManager(this, session, dataFixer, var10006, workerExecutor, lv, var10009, var10010, bl3, worldGenerationProgressListener, var10013::updateTrackingStatus, () -> {
         return server.getOverworld().getPersistentStateManager();
      });
      this.chunkManager.getStructurePlacementCalculator().tryCalculate();
      this.portalForcer = new PortalForcer(this);
      this.calculateAmbientDarkness();
      this.initWeatherGradients();
      this.getWorldBorder().setMaxRadius(server.getMaxWorldBorderRadius());
      this.raidManager = (RaidManager)this.getPersistentStateManager().getOrCreate((nbt) -> {
         return RaidManager.fromNbt(this, nbt);
      }, () -> {
         return new RaidManager(this);
      }, RaidManager.nameFor(this.getDimensionEntry()));
      if (!server.isSingleplayer()) {
         properties.setGameMode(server.getDefaultGameMode());
      }

      long m = server.getSaveProperties().getGeneratorOptions().getSeed();
      this.structureLocator = new StructureLocator(this.chunkManager.getChunkIoWorker(), this.getRegistryManager(), server.getStructureTemplateManager(), worldKey, lv, this.chunkManager.getNoiseConfig(), this, lv.getBiomeSource(), m, dataFixer);
      this.structureAccessor = new StructureAccessor(this, server.getSaveProperties().getGeneratorOptions(), this.structureLocator);
      if (this.getRegistryKey() == World.END && this.getDimensionEntry().matchesKey(DimensionTypes.THE_END)) {
         this.enderDragonFight = new EnderDragonFight(this, m, server.getSaveProperties().getDragonFight());
      } else {
         this.enderDragonFight = null;
      }

      this.sleepManager = new SleepManager();
      this.gameEventDispatchManager = new GameEventDispatchManager(this);
   }

   public void setWeather(int clearDuration, int rainDuration, boolean raining, boolean thundering) {
      this.worldProperties.setClearWeatherTime(clearDuration);
      this.worldProperties.setRainTime(rainDuration);
      this.worldProperties.setThunderTime(rainDuration);
      this.worldProperties.setRaining(raining);
      this.worldProperties.setThundering(thundering);
   }

   public RegistryEntry getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.getChunkManager().getChunkGenerator().getBiomeSource().getBiome(biomeX, biomeY, biomeZ, this.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
   }

   public StructureAccessor getStructureAccessor() {
      return this.structureAccessor;
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      Profiler lv = this.getProfiler();
      this.inBlockTick = true;
      lv.push("world border");
      this.getWorldBorder().tick();
      lv.swap("weather");
      this.tickWeather();
      int i = this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
      long l;
      if (this.sleepManager.canSkipNight(i) && this.sleepManager.canResetTime(i, this.players)) {
         if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            l = this.properties.getTimeOfDay() + 24000L;
            this.setTimeOfDay(l - l % 24000L);
         }

         this.wakeSleepingPlayers();
         if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE) && this.isRaining()) {
            this.resetWeather();
         }
      }

      this.calculateAmbientDarkness();
      this.tickTime();
      lv.swap("tickPending");
      if (!this.isDebugWorld()) {
         l = this.getTime();
         lv.push("blockTicks");
         this.blockTickScheduler.tick(l, 65536, this::tickBlock);
         lv.swap("fluidTicks");
         this.fluidTickScheduler.tick(l, 65536, this::tickFluid);
         lv.pop();
      }

      lv.swap("raid");
      this.raidManager.tick();
      lv.swap("chunkSource");
      this.getChunkManager().tick(shouldKeepTicking, true);
      lv.swap("blockEvents");
      this.processSyncedBlockEvents();
      this.inBlockTick = false;
      lv.pop();
      boolean bl = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
      if (bl) {
         this.resetIdleTimeout();
      }

      if (bl || this.idleTimeout++ < 300) {
         lv.push("entities");
         if (this.enderDragonFight != null) {
            lv.push("dragonFight");
            this.enderDragonFight.tick();
            lv.pop();
         }

         this.entityList.forEach((entity) -> {
            if (!entity.isRemoved()) {
               if (this.shouldCancelSpawn(entity)) {
                  entity.discard();
               } else {
                  lv.push("checkDespawn");
                  entity.checkDespawn();
                  lv.pop();
                  if (this.chunkManager.threadedAnvilChunkStorage.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong())) {
                     Entity lvx = entity.getVehicle();
                     if (lvx != null) {
                        if (!lvx.isRemoved() && lvx.hasPassenger(entity)) {
                           return;
                        }

                        entity.stopRiding();
                     }

                     lv.push("tick");
                     this.tickEntity(this::tickEntity, entity);
                     lv.pop();
                  }
               }
            }
         });
         lv.pop();
         this.tickBlockEntities();
      }

      lv.push("entityManagement");
      this.entityManager.tick();
      lv.pop();
   }

   public boolean shouldTickBlocksInChunk(long chunkPos) {
      return this.chunkManager.threadedAnvilChunkStorage.getTicketManager().shouldTickBlocks(chunkPos);
   }

   protected void tickTime() {
      if (this.shouldTickTime) {
         long l = this.properties.getTime() + 1L;
         this.worldProperties.setTime(l);
         this.worldProperties.getScheduledEvents().processEvents(this.server, l);
         if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
            this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
         }

      }
   }

   public void setTimeOfDay(long timeOfDay) {
      this.worldProperties.setTimeOfDay(timeOfDay);
   }

   public void tickSpawners(boolean spawnMonsters, boolean spawnAnimals) {
      Iterator var3 = this.spawners.iterator();

      while(var3.hasNext()) {
         Spawner lv = (Spawner)var3.next();
         lv.spawn(this, spawnMonsters, spawnAnimals);
      }

   }

   private boolean shouldCancelSpawn(Entity entity) {
      if (this.server.shouldSpawnAnimals() || !(entity instanceof AnimalEntity) && !(entity instanceof WaterCreatureEntity)) {
         return !this.server.shouldSpawnNpcs() && entity instanceof Npc;
      } else {
         return true;
      }
   }

   private void wakeSleepingPlayers() {
      this.sleepManager.clearSleeping();
      ((List)this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList())).forEach((player) -> {
         player.wakeUp(false, false);
      });
   }

   public void tickChunk(WorldChunk chunk, int randomTickSpeed) {
      ChunkPos lv = chunk.getPos();
      boolean bl = this.isRaining();
      int j = lv.getStartX();
      int k = lv.getStartZ();
      Profiler lv2 = this.getProfiler();
      lv2.push("thunder");
      BlockPos lv3;
      if (bl && this.isThundering() && this.random.nextInt(100000) == 0) {
         lv3 = this.getLightningPos(this.getRandomPosInChunk(j, 0, k, 15));
         if (this.hasRain(lv3)) {
            LocalDifficulty lv4 = this.getLocalDifficulty(lv3);
            boolean bl2 = this.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && this.random.nextDouble() < (double)lv4.getLocalDifficulty() * 0.01 && !this.getBlockState(lv3.down()).isOf(Blocks.LIGHTNING_ROD);
            if (bl2) {
               SkeletonHorseEntity lv5 = (SkeletonHorseEntity)EntityType.SKELETON_HORSE.create(this);
               if (lv5 != null) {
                  lv5.setTrapped(true);
                  lv5.setBreedingAge(0);
                  lv5.setPosition((double)lv3.getX(), (double)lv3.getY(), (double)lv3.getZ());
                  this.spawnEntity(lv5);
               }
            }

            LightningEntity lv6 = (LightningEntity)EntityType.LIGHTNING_BOLT.create(this);
            if (lv6 != null) {
               lv6.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(lv3));
               lv6.setCosmetic(bl2);
               this.spawnEntity(lv6);
            }
         }
      }

      lv2.swap("iceandsnow");
      int m;
      if (this.random.nextInt(16) == 0) {
         lv3 = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, this.getRandomPosInChunk(j, 0, k, 15));
         BlockPos lv7 = lv3.down();
         Biome lv8 = (Biome)this.getBiome(lv3).value();
         if (lv8.canSetIce(this, lv7)) {
            this.setBlockState(lv7, Blocks.ICE.getDefaultState());
         }

         if (bl) {
            int l = this.getGameRules().getInt(GameRules.SNOW_ACCUMULATION_HEIGHT);
            if (l > 0 && lv8.canSetSnow(this, lv3)) {
               BlockState lv9 = this.getBlockState(lv3);
               if (lv9.isOf(Blocks.SNOW)) {
                  m = (Integer)lv9.get(SnowBlock.LAYERS);
                  if (m < Math.min(l, 8)) {
                     BlockState lv10 = (BlockState)lv9.with(SnowBlock.LAYERS, m + 1);
                     Block.pushEntitiesUpBeforeBlockChange(lv9, lv10, this, lv3);
                     this.setBlockState(lv3, lv10);
                  }
               } else {
                  this.setBlockState(lv3, Blocks.SNOW.getDefaultState());
               }
            }

            Biome.Precipitation lv11 = lv8.getPrecipitation(lv7);
            if (lv11 != Biome.Precipitation.NONE) {
               BlockState lv12 = this.getBlockState(lv7);
               lv12.getBlock().precipitationTick(lv12, this, lv7, lv11);
            }
         }
      }

      lv2.swap("tickBlocks");
      if (randomTickSpeed > 0) {
         ChunkSection[] var17 = chunk.getSectionArray();
         int var19 = var17.length;

         for(int var21 = 0; var21 < var19; ++var21) {
            ChunkSection lv13 = var17[var21];
            if (lv13.hasRandomTicks()) {
               int n = lv13.getYOffset();

               for(m = 0; m < randomTickSpeed; ++m) {
                  BlockPos lv14 = this.getRandomPosInChunk(j, n, k, 15);
                  lv2.push("randomTick");
                  BlockState lv15 = lv13.getBlockState(lv14.getX() - j, lv14.getY() - n, lv14.getZ() - k);
                  if (lv15.hasRandomTicks()) {
                     lv15.randomTick(this, lv14, this.random);
                  }

                  FluidState lv16 = lv15.getFluidState();
                  if (lv16.hasRandomTicks()) {
                     lv16.onRandomTick(this, lv14, this.random);
                  }

                  lv2.pop();
               }
            }
         }
      }

      lv2.pop();
   }

   private Optional getLightningRodPos(BlockPos pos) {
      Optional optional = this.getPointOfInterestStorage().getNearestPosition((poiType) -> {
         return poiType.matchesKey(PointOfInterestTypes.LIGHTNING_ROD);
      }, (posx) -> {
         return posx.getY() == this.getTopY(Heightmap.Type.WORLD_SURFACE, posx.getX(), posx.getZ()) - 1;
      }, pos, 128, PointOfInterestStorage.OccupationStatus.ANY);
      return optional.map((posx) -> {
         return posx.up(1);
      });
   }

   protected BlockPos getLightningPos(BlockPos pos) {
      BlockPos lv = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
      Optional optional = this.getLightningRodPos(lv);
      if (optional.isPresent()) {
         return (BlockPos)optional.get();
      } else {
         Box lv2 = (new Box(lv, new BlockPos(lv.getX(), this.getTopY(), lv.getZ()))).expand(3.0);
         List list = this.getEntitiesByClass(LivingEntity.class, lv2, (entity) -> {
            return entity != null && entity.isAlive() && this.isSkyVisible(entity.getBlockPos());
         });
         if (!list.isEmpty()) {
            return ((LivingEntity)list.get(this.random.nextInt(list.size()))).getBlockPos();
         } else {
            if (lv.getY() == this.getBottomY() - 1) {
               lv = lv.up(2);
            }

            return lv;
         }
      }
   }

   public boolean isInBlockTick() {
      return this.inBlockTick;
   }

   public boolean isSleepingEnabled() {
      return this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE) <= 100;
   }

   private void sendSleepingStatus() {
      if (this.isSleepingEnabled()) {
         if (!this.getServer().isSingleplayer() || this.getServer().isRemote()) {
            int i = this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
            MutableText lv;
            if (this.sleepManager.canSkipNight(i)) {
               lv = Text.translatable("sleep.skipping_night");
            } else {
               lv = Text.translatable("sleep.players_sleeping", this.sleepManager.getSleeping(), this.sleepManager.getNightSkippingRequirement(i));
            }

            Iterator var3 = this.players.iterator();

            while(var3.hasNext()) {
               ServerPlayerEntity lv2 = (ServerPlayerEntity)var3.next();
               lv2.sendMessage(lv, true);
            }

         }
      }
   }

   public void updateSleepingPlayers() {
      if (!this.players.isEmpty() && this.sleepManager.update(this.players)) {
         this.sendSleepingStatus();
      }

   }

   public ServerScoreboard getScoreboard() {
      return this.server.getScoreboard();
   }

   private void tickWeather() {
      boolean bl = this.isRaining();
      if (this.getDimension().hasSkyLight()) {
         if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
            int i = this.worldProperties.getClearWeatherTime();
            int j = this.worldProperties.getThunderTime();
            int k = this.worldProperties.getRainTime();
            boolean bl2 = this.properties.isThundering();
            boolean bl3 = this.properties.isRaining();
            if (i > 0) {
               --i;
               j = bl2 ? 0 : 1;
               k = bl3 ? 0 : 1;
               bl2 = false;
               bl3 = false;
            } else {
               if (j > 0) {
                  --j;
                  if (j == 0) {
                     bl2 = !bl2;
                  }
               } else if (bl2) {
                  j = THUNDER_WEATHER_DURATION_PROVIDER.get(this.random);
               } else {
                  j = CLEAR_THUNDER_WEATHER_DURATION_PROVIDER.get(this.random);
               }

               if (k > 0) {
                  --k;
                  if (k == 0) {
                     bl3 = !bl3;
                  }
               } else if (bl3) {
                  k = RAIN_WEATHER_DURATION_PROVIDER.get(this.random);
               } else {
                  k = CLEAR_WEATHER_DURATION_PROVIDER.get(this.random);
               }
            }

            this.worldProperties.setThunderTime(j);
            this.worldProperties.setRainTime(k);
            this.worldProperties.setClearWeatherTime(i);
            this.worldProperties.setThundering(bl2);
            this.worldProperties.setRaining(bl3);
         }

         this.thunderGradientPrev = this.thunderGradient;
         if (this.properties.isThundering()) {
            this.thunderGradient += 0.01F;
         } else {
            this.thunderGradient -= 0.01F;
         }

         this.thunderGradient = MathHelper.clamp(this.thunderGradient, 0.0F, 1.0F);
         this.rainGradientPrev = this.rainGradient;
         if (this.properties.isRaining()) {
            this.rainGradient += 0.01F;
         } else {
            this.rainGradient -= 0.01F;
         }

         this.rainGradient = MathHelper.clamp(this.rainGradient, 0.0F, 1.0F);
      }

      if (this.rainGradientPrev != this.rainGradient) {
         this.server.getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, this.rainGradient), this.getRegistryKey());
      }

      if (this.thunderGradientPrev != this.thunderGradient) {
         this.server.getPlayerManager().sendToDimension(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, this.thunderGradient), this.getRegistryKey());
      }

      if (bl != this.isRaining()) {
         if (bl) {
            this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STOPPED, GameStateChangeS2CPacket.field_33328));
         } else {
            this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, GameStateChangeS2CPacket.field_33328));
         }

         this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, this.rainGradient));
         this.server.getPlayerManager().sendToAll(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, this.thunderGradient));
      }

   }

   private void resetWeather() {
      this.worldProperties.setRainTime(0);
      this.worldProperties.setRaining(false);
      this.worldProperties.setThunderTime(0);
      this.worldProperties.setThundering(false);
   }

   public void resetIdleTimeout() {
      this.idleTimeout = 0;
   }

   private void tickFluid(BlockPos pos, Fluid fluid) {
      FluidState lv = this.getFluidState(pos);
      if (lv.isOf(fluid)) {
         lv.onScheduledTick(this, pos);
      }

   }

   private void tickBlock(BlockPos pos, Block block) {
      BlockState lv = this.getBlockState(pos);
      if (lv.isOf(block)) {
         lv.scheduledTick(this, pos, this.random);
      }

   }

   public void tickEntity(Entity entity) {
      entity.resetPosition();
      Profiler lv = this.getProfiler();
      ++entity.age;
      this.getProfiler().push(() -> {
         return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
      });
      lv.visit("tickNonPassenger");
      entity.tick();
      this.getProfiler().pop();
      Iterator var3 = entity.getPassengerList().iterator();

      while(var3.hasNext()) {
         Entity lv2 = (Entity)var3.next();
         this.tickPassenger(entity, lv2);
      }

   }

   private void tickPassenger(Entity vehicle, Entity passenger) {
      if (!passenger.isRemoved() && passenger.getVehicle() == vehicle) {
         if (passenger instanceof PlayerEntity || this.entityList.has(passenger)) {
            passenger.resetPosition();
            ++passenger.age;
            Profiler lv = this.getProfiler();
            lv.push(() -> {
               return Registries.ENTITY_TYPE.getId(passenger.getType()).toString();
            });
            lv.visit("tickPassenger");
            passenger.tickRiding();
            lv.pop();
            Iterator var4 = passenger.getPassengerList().iterator();

            while(var4.hasNext()) {
               Entity lv2 = (Entity)var4.next();
               this.tickPassenger(passenger, lv2);
            }

         }
      } else {
         passenger.stopRiding();
      }
   }

   public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
      return !this.server.isSpawnProtected(this, pos, player) && this.getWorldBorder().contains(pos);
   }

   public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
      ServerChunkManager lv = this.getChunkManager();
      if (!savingDisabled) {
         if (progressListener != null) {
            progressListener.setTitle(Text.translatable("menu.savingLevel"));
         }

         this.saveLevel();
         if (progressListener != null) {
            progressListener.setTask(Text.translatable("menu.savingChunks"));
         }

         lv.save(flush);
         if (flush) {
            this.entityManager.flush();
         } else {
            this.entityManager.save();
         }

      }
   }

   private void saveLevel() {
      if (this.enderDragonFight != null) {
         this.server.getSaveProperties().setDragonFight(this.enderDragonFight.toNbt());
      }

      this.getChunkManager().getPersistentStateManager().save();
   }

   public List getEntitiesByType(TypeFilter filter, Predicate predicate) {
      List list = Lists.newArrayList();
      this.collectEntitiesByType(filter, predicate, list);
      return list;
   }

   public void collectEntitiesByType(TypeFilter filter, Predicate predicate, List result) {
      this.collectEntitiesByType(filter, predicate, result, Integer.MAX_VALUE);
   }

   public void collectEntitiesByType(TypeFilter filter, Predicate predicate, List result, int limit) {
      this.getEntityLookup().forEach(filter, (entity) -> {
         if (predicate.test(entity)) {
            result.add(entity);
            if (result.size() >= limit) {
               return LazyIterationConsumer.NextIteration.ABORT;
            }
         }

         return LazyIterationConsumer.NextIteration.CONTINUE;
      });
   }

   public List getAliveEnderDragons() {
      return this.getEntitiesByType(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
   }

   public List getPlayers(Predicate predicate) {
      return this.getPlayers(predicate, Integer.MAX_VALUE);
   }

   public List getPlayers(Predicate predicate, int limit) {
      List list = Lists.newArrayList();
      Iterator var4 = this.players.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         if (predicate.test(lv)) {
            list.add(lv);
            if (list.size() >= limit) {
               return list;
            }
         }
      }

      return list;
   }

   @Nullable
   public ServerPlayerEntity getRandomAlivePlayer() {
      List list = this.getPlayers(LivingEntity::isAlive);
      return list.isEmpty() ? null : (ServerPlayerEntity)list.get(this.random.nextInt(list.size()));
   }

   public boolean spawnEntity(Entity entity) {
      return this.addEntity(entity);
   }

   public boolean tryLoadEntity(Entity entity) {
      return this.addEntity(entity);
   }

   public void onDimensionChanged(Entity entity) {
      this.addEntity(entity);
   }

   public void onPlayerTeleport(ServerPlayerEntity player) {
      this.addPlayer(player);
   }

   public void onPlayerChangeDimension(ServerPlayerEntity player) {
      this.addPlayer(player);
   }

   public void onPlayerConnected(ServerPlayerEntity player) {
      this.addPlayer(player);
   }

   public void onPlayerRespawned(ServerPlayerEntity player) {
      this.addPlayer(player);
   }

   private void addPlayer(ServerPlayerEntity player) {
      Entity lv = (Entity)this.getEntityLookup().get(player.getUuid());
      if (lv != null) {
         LOGGER.warn("Force-added player with duplicate UUID {}", player.getUuid().toString());
         lv.detach();
         this.removePlayer((ServerPlayerEntity)lv, Entity.RemovalReason.DISCARDED);
      }

      this.entityManager.addEntity(player);
   }

   private boolean addEntity(Entity entity) {
      if (entity.isRemoved()) {
         LOGGER.warn("Tried to add entity {} but it was marked as removed already", EntityType.getId(entity.getType()));
         return false;
      } else {
         return this.entityManager.addEntity(entity);
      }
   }

   public boolean spawnNewEntityAndPassengers(Entity entity) {
      Stream var10000 = entity.streamSelfAndPassengers().map(Entity::getUuid);
      ServerEntityManager var10001 = this.entityManager;
      Objects.requireNonNull(var10001);
      if (var10000.anyMatch(var10001::has)) {
         return false;
      } else {
         this.spawnEntityAndPassengers(entity);
         return true;
      }
   }

   public void unloadEntities(WorldChunk chunk) {
      chunk.clear();
      chunk.removeChunkTickSchedulers(this);
   }

   public void removePlayer(ServerPlayerEntity player, Entity.RemovalReason reason) {
      player.remove(reason);
   }

   public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
      Iterator var4 = this.server.getPlayerManager().getPlayerList().iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         if (lv != null && lv.world == this && lv.getId() != entityId) {
            double d = (double)pos.getX() - lv.getX();
            double e = (double)pos.getY() - lv.getY();
            double f = (double)pos.getZ() - lv.getZ();
            if (d * d + e * e + f * f < 1024.0) {
               lv.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(entityId, pos, progress));
            }
         }
      }

   }

   public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed) {
      this.server.getPlayerManager().sendToAround(except, x, y, z, (double)((SoundEvent)sound.value()).getDistanceToTravel(volume), this.getRegistryKey(), new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed));
   }

   public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed) {
      this.server.getPlayerManager().sendToAround(except, entity.getX(), entity.getY(), entity.getZ(), (double)((SoundEvent)sound.value()).getDistanceToTravel(volume), this.getRegistryKey(), new PlaySoundFromEntityS2CPacket(sound, category, entity, volume, pitch, seed));
   }

   public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
      if (this.getGameRules().getBoolean(GameRules.GLOBAL_SOUND_EVENTS)) {
         this.server.getPlayerManager().sendToAll(new WorldEventS2CPacket(eventId, pos, data, true));
      } else {
         this.syncWorldEvent((PlayerEntity)null, eventId, pos, data);
      }

   }

   public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
      this.server.getPlayerManager().sendToAround(player, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 64.0, this.getRegistryKey(), new WorldEventS2CPacket(eventId, pos, data, false));
   }

   public int getLogicalHeight() {
      return this.getDimension().logicalHeight();
   }

   public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {
      this.gameEventDispatchManager.dispatch(event, emitterPos, emitter);
   }

   public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      if (this.duringListenerUpdate) {
         String string = "recursive call to sendBlockUpdated";
         Util.error("recursive call to sendBlockUpdated", new IllegalStateException("recursive call to sendBlockUpdated"));
      }

      this.getChunkManager().markForUpdate(pos);
      VoxelShape lv = oldState.getCollisionShape(this, pos);
      VoxelShape lv2 = newState.getCollisionShape(this, pos);
      if (VoxelShapes.matchesAnywhere(lv, lv2, BooleanBiFunction.NOT_SAME)) {
         List list = new ObjectArrayList();
         Iterator var8 = this.loadedMobs.iterator();

         while(var8.hasNext()) {
            MobEntity lv3 = (MobEntity)var8.next();
            EntityNavigation lv4 = lv3.getNavigation();
            if (lv4.shouldRecalculatePath(pos)) {
               list.add(lv4);
            }
         }

         try {
            this.duringListenerUpdate = true;
            var8 = list.iterator();

            while(var8.hasNext()) {
               EntityNavigation lv5 = (EntityNavigation)var8.next();
               lv5.recalculatePath();
            }
         } finally {
            this.duringListenerUpdate = false;
         }

      }
   }

   public void updateNeighborsAlways(BlockPos pos, Block sourceBlock) {
      this.neighborUpdater.updateNeighbors(pos, sourceBlock, (Direction)null);
   }

   public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction) {
      this.neighborUpdater.updateNeighbors(pos, sourceBlock, direction);
   }

   public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
      this.neighborUpdater.updateNeighbor(pos, sourceBlock, sourcePos);
   }

   public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      this.neighborUpdater.updateNeighbor(state, pos, sourceBlock, sourcePos, notify);
   }

   public void sendEntityStatus(Entity entity, byte status) {
      this.getChunkManager().sendToNearbyPlayers(entity, new EntityStatusS2CPacket(entity, status));
   }

   public void sendEntityDamage(Entity entity, DamageSource damageSource) {
      this.getChunkManager().sendToNearbyPlayers(entity, new EntityDamageS2CPacket(entity, damageSource));
   }

   public ServerChunkManager getChunkManager() {
      return this.chunkManager;
   }

   public Explosion createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType) {
      Explosion lv = this.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, false);
      if (!lv.shouldDestroy()) {
         lv.clearAffectedBlocks();
      }

      Iterator var14 = this.players.iterator();

      while(var14.hasNext()) {
         ServerPlayerEntity lv2 = (ServerPlayerEntity)var14.next();
         if (lv2.squaredDistanceTo(x, y, z) < 4096.0) {
            lv2.networkHandler.sendPacket(new ExplosionS2CPacket(x, y, z, power, lv.getAffectedBlocks(), (Vec3d)lv.getAffectedPlayers().get(lv2)));
         }
      }

      return lv;
   }

   public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
      this.syncedBlockEventQueue.add(new BlockEvent(pos, block, type, data));
   }

   private void processSyncedBlockEvents() {
      this.blockEventQueue.clear();

      while(!this.syncedBlockEventQueue.isEmpty()) {
         BlockEvent lv = (BlockEvent)this.syncedBlockEventQueue.removeFirst();
         if (this.shouldTickBlockPos(lv.pos())) {
            if (this.processBlockEvent(lv)) {
               this.server.getPlayerManager().sendToAround((PlayerEntity)null, (double)lv.pos().getX(), (double)lv.pos().getY(), (double)lv.pos().getZ(), 64.0, this.getRegistryKey(), new BlockEventS2CPacket(lv.pos(), lv.block(), lv.type(), lv.data()));
            }
         } else {
            this.blockEventQueue.add(lv);
         }
      }

      this.syncedBlockEventQueue.addAll(this.blockEventQueue);
   }

   private boolean processBlockEvent(BlockEvent event) {
      BlockState lv = this.getBlockState(event.pos());
      return lv.isOf(event.block()) ? lv.onSyncedBlockEvent(this, event.pos(), event.type(), event.data()) : false;
   }

   public WorldTickScheduler getBlockTickScheduler() {
      return this.blockTickScheduler;
   }

   public WorldTickScheduler getFluidTickScheduler() {
      return this.fluidTickScheduler;
   }

   @NotNull
   public MinecraftServer getServer() {
      return this.server;
   }

   public PortalForcer getPortalForcer() {
      return this.portalForcer;
   }

   public StructureTemplateManager getStructureTemplateManager() {
      return this.server.getStructureTemplateManager();
   }

   public int spawnParticles(ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
      ParticleS2CPacket lv = new ParticleS2CPacket(particle, false, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
      int l = 0;

      for(int m = 0; m < this.players.size(); ++m) {
         ServerPlayerEntity lv2 = (ServerPlayerEntity)this.players.get(m);
         if (this.sendToPlayerIfNearby(lv2, false, x, y, z, lv)) {
            ++l;
         }
      }

      return l;
   }

   public boolean spawnParticles(ServerPlayerEntity viewer, ParticleEffect particle, boolean force, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
      Packet lv = new ParticleS2CPacket(particle, force, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
      return this.sendToPlayerIfNearby(viewer, force, x, y, z, lv);
   }

   private boolean sendToPlayerIfNearby(ServerPlayerEntity player, boolean force, double x, double y, double z, Packet packet) {
      if (player.getWorld() != this) {
         return false;
      } else {
         BlockPos lv = player.getBlockPos();
         if (lv.isWithinDistance(new Vec3d(x, y, z), force ? 512.0 : 32.0)) {
            player.networkHandler.sendPacket(packet);
            return true;
         } else {
            return false;
         }
      }
   }

   @Nullable
   public Entity getEntityById(int id) {
      return (Entity)this.getEntityLookup().get(id);
   }

   /** @deprecated */
   @Deprecated
   @Nullable
   public Entity getDragonPart(int id) {
      Entity lv = (Entity)this.getEntityLookup().get(id);
      return lv != null ? lv : (Entity)this.dragonParts.get(id);
   }

   @Nullable
   public Entity getEntity(UUID uuid) {
      return (Entity)this.getEntityLookup().get(uuid);
   }

   @Nullable
   public BlockPos locateStructure(TagKey structureTag, BlockPos pos, int radius, boolean skipReferencedStructures) {
      if (!this.server.getSaveProperties().getGeneratorOptions().shouldGenerateStructures()) {
         return null;
      } else {
         Optional optional = this.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntryList(structureTag);
         if (optional.isEmpty()) {
            return null;
         } else {
            Pair pair = this.getChunkManager().getChunkGenerator().locateStructure(this, (RegistryEntryList)optional.get(), pos, radius, skipReferencedStructures);
            return pair != null ? (BlockPos)pair.getFirst() : null;
         }
      }
   }

   @Nullable
   public Pair locateBiome(Predicate predicate, BlockPos pos, int radius, int horizontalBlockCheckInterval, int verticalBlockCheckInterval) {
      return this.getChunkManager().getChunkGenerator().getBiomeSource().locateBiome(pos, radius, horizontalBlockCheckInterval, verticalBlockCheckInterval, predicate, this.getChunkManager().getNoiseConfig().getMultiNoiseSampler(), this);
   }

   public RecipeManager getRecipeManager() {
      return this.server.getRecipeManager();
   }

   public boolean isSavingDisabled() {
      return this.savingDisabled;
   }

   public PersistentStateManager getPersistentStateManager() {
      return this.getChunkManager().getPersistentStateManager();
   }

   @Nullable
   public MapState getMapState(String id) {
      return (MapState)this.getServer().getOverworld().getPersistentStateManager().get(MapState::fromNbt, id);
   }

   public void putMapState(String id, MapState state) {
      this.getServer().getOverworld().getPersistentStateManager().set(id, state);
   }

   public int getNextMapId() {
      return ((IdCountsState)this.getServer().getOverworld().getPersistentStateManager().getOrCreate(IdCountsState::fromNbt, IdCountsState::new, "idcounts")).getNextMapId();
   }

   public void setSpawnPos(BlockPos pos, float angle) {
      ChunkPos lv = new ChunkPos(new BlockPos(this.properties.getSpawnX(), 0, this.properties.getSpawnZ()));
      this.properties.setSpawnPos(pos, angle);
      this.getChunkManager().removeTicket(ChunkTicketType.START, lv, 11, Unit.INSTANCE);
      this.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(pos), 11, Unit.INSTANCE);
      this.getServer().getPlayerManager().sendToAll(new PlayerSpawnPositionS2CPacket(pos, angle));
   }

   public LongSet getForcedChunks() {
      ForcedChunkState lv = (ForcedChunkState)this.getPersistentStateManager().get(ForcedChunkState::fromNbt, "chunks");
      return (LongSet)(lv != null ? LongSets.unmodifiable(lv.getChunks()) : LongSets.EMPTY_SET);
   }

   public boolean setChunkForced(int x, int z, boolean forced) {
      ForcedChunkState lv = (ForcedChunkState)this.getPersistentStateManager().getOrCreate(ForcedChunkState::fromNbt, ForcedChunkState::new, "chunks");
      ChunkPos lv2 = new ChunkPos(x, z);
      long l = lv2.toLong();
      boolean bl2;
      if (forced) {
         bl2 = lv.getChunks().add(l);
         if (bl2) {
            this.getChunk(x, z);
         }
      } else {
         bl2 = lv.getChunks().remove(l);
      }

      lv.setDirty(bl2);
      if (bl2) {
         this.getChunkManager().setChunkForced(lv2, forced);
      }

      return bl2;
   }

   public List getPlayers() {
      return this.players;
   }

   public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
      Optional optional = PointOfInterestTypes.getTypeForState(oldBlock);
      Optional optional2 = PointOfInterestTypes.getTypeForState(newBlock);
      if (!Objects.equals(optional, optional2)) {
         BlockPos lv = pos.toImmutable();
         optional.ifPresent((oldPoiType) -> {
            this.getServer().execute(() -> {
               this.getPointOfInterestStorage().remove(lv);
               DebugInfoSender.sendPoiRemoval(this, lv);
            });
         });
         optional2.ifPresent((newPoiType) -> {
            this.getServer().execute(() -> {
               this.getPointOfInterestStorage().add(lv, newPoiType);
               DebugInfoSender.sendPoiAddition(this, lv);
            });
         });
      }
   }

   public PointOfInterestStorage getPointOfInterestStorage() {
      return this.getChunkManager().getPointOfInterestStorage();
   }

   public boolean isNearOccupiedPointOfInterest(BlockPos pos) {
      return this.isNearOccupiedPointOfInterest(pos, 1);
   }

   public boolean isNearOccupiedPointOfInterest(ChunkSectionPos sectionPos) {
      return this.isNearOccupiedPointOfInterest(sectionPos.getCenterPos());
   }

   public boolean isNearOccupiedPointOfInterest(BlockPos pos, int maxDistance) {
      if (maxDistance > 6) {
         return false;
      } else {
         return this.getOccupiedPointOfInterestDistance(ChunkSectionPos.from(pos)) <= maxDistance;
      }
   }

   public int getOccupiedPointOfInterestDistance(ChunkSectionPos pos) {
      return this.getPointOfInterestStorage().getDistanceFromNearestOccupied(pos);
   }

   public RaidManager getRaidManager() {
      return this.raidManager;
   }

   @Nullable
   public Raid getRaidAt(BlockPos pos) {
      return this.raidManager.getRaidAt(pos, 9216);
   }

   public boolean hasRaidAt(BlockPos pos) {
      return this.getRaidAt(pos) != null;
   }

   public void handleInteraction(EntityInteraction interaction, Entity entity, InteractionObserver observer) {
      observer.onInteractionWith(interaction, entity);
   }

   public void dump(Path path) throws IOException {
      ThreadedAnvilChunkStorage lv = this.getChunkManager().threadedAnvilChunkStorage;
      Writer writer = Files.newBufferedWriter(path.resolve("stats.txt"));

      try {
         writer.write(String.format(Locale.ROOT, "spawning_chunks: %d\n", lv.getTicketManager().getTickedChunkCount()));
         SpawnHelper.Info lv2 = this.getChunkManager().getSpawnInfo();
         if (lv2 != null) {
            ObjectIterator var5 = lv2.getGroupToCount().object2IntEntrySet().iterator();

            while(var5.hasNext()) {
               Object2IntMap.Entry entry = (Object2IntMap.Entry)var5.next();
               writer.write(String.format(Locale.ROOT, "spawn_count.%s: %d\n", ((SpawnGroup)entry.getKey()).getName(), entry.getIntValue()));
            }
         }

         writer.write(String.format(Locale.ROOT, "entities: %s\n", this.entityManager.getDebugString()));
         writer.write(String.format(Locale.ROOT, "block_entity_tickers: %d\n", this.blockEntityTickers.size()));
         writer.write(String.format(Locale.ROOT, "block_ticks: %d\n", this.getBlockTickScheduler().getTickCount()));
         writer.write(String.format(Locale.ROOT, "fluid_ticks: %d\n", this.getFluidTickScheduler().getTickCount()));
         writer.write("distance_manager: " + lv.getTicketManager().toDumpString() + "\n");
         writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getChunkManager().getPendingTasks()));
      } catch (Throwable var22) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var14) {
               var22.addSuppressed(var14);
            }
         }

         throw var22;
      }

      if (writer != null) {
         writer.close();
      }

      CrashReport lv3 = new CrashReport("Level dump", new Exception("dummy"));
      this.addDetailsToCrashReport(lv3);
      Writer writer2 = Files.newBufferedWriter(path.resolve("example_crash.txt"));

      try {
         writer2.write(lv3.asString());
      } catch (Throwable var17) {
         if (writer2 != null) {
            try {
               writer2.close();
            } catch (Throwable var11) {
               var17.addSuppressed(var11);
            }
         }

         throw var17;
      }

      if (writer2 != null) {
         writer2.close();
      }

      Path path2 = path.resolve("chunks.csv");
      Writer writer3 = Files.newBufferedWriter(path2);

      try {
         lv.dump(writer3);
      } catch (Throwable var21) {
         if (writer3 != null) {
            try {
               writer3.close();
            } catch (Throwable var15) {
               var21.addSuppressed(var15);
            }
         }

         throw var21;
      }

      if (writer3 != null) {
         writer3.close();
      }

      Path path3 = path.resolve("entity_chunks.csv");
      Writer writer4 = Files.newBufferedWriter(path3);

      try {
         this.entityManager.dump(writer4);
      } catch (Throwable var19) {
         if (writer4 != null) {
            try {
               writer4.close();
            } catch (Throwable var12) {
               var19.addSuppressed(var12);
            }
         }

         throw var19;
      }

      if (writer4 != null) {
         writer4.close();
      }

      Path path4 = path.resolve("entities.csv");
      Writer writer5 = Files.newBufferedWriter(path4);

      try {
         dumpEntities(writer5, this.getEntityLookup().iterate());
      } catch (Throwable var18) {
         if (writer5 != null) {
            try {
               writer5.close();
            } catch (Throwable var16) {
               var18.addSuppressed(var16);
            }
         }

         throw var18;
      }

      if (writer5 != null) {
         writer5.close();
      }

      Path path5 = path.resolve("block_entities.csv");
      Writer writer6 = Files.newBufferedWriter(path5);

      try {
         this.dumpBlockEntities(writer6);
      } catch (Throwable var20) {
         if (writer6 != null) {
            try {
               writer6.close();
            } catch (Throwable var13) {
               var20.addSuppressed(var13);
            }
         }

         throw var20;
      }

      if (writer6 != null) {
         writer6.close();
      }

   }

   private static void dumpEntities(Writer writer, Iterable entities) throws IOException {
      CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").startBody(writer);
      Iterator var3 = entities.iterator();

      while(var3.hasNext()) {
         Entity lv2 = (Entity)var3.next();
         Text lv3 = lv2.getCustomName();
         Text lv4 = lv2.getDisplayName();
         lv.printRow(lv2.getX(), lv2.getY(), lv2.getZ(), lv2.getUuid(), Registries.ENTITY_TYPE.getId(lv2.getType()), lv2.isAlive(), lv4.getString(), lv3 != null ? lv3.getString() : null);
      }

   }

   private void dumpBlockEntities(Writer writer) throws IOException {
      CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("type").startBody(writer);
      Iterator var3 = this.blockEntityTickers.iterator();

      while(var3.hasNext()) {
         BlockEntityTickInvoker lv2 = (BlockEntityTickInvoker)var3.next();
         BlockPos lv3 = lv2.getPos();
         lv.printRow(lv3.getX(), lv3.getY(), lv3.getZ(), lv2.getName());
      }

   }

   @VisibleForTesting
   public void clearUpdatesInArea(BlockBox box) {
      this.syncedBlockEventQueue.removeIf((event) -> {
         return box.contains(event.pos());
      });
   }

   public void updateNeighbors(BlockPos pos, Block block) {
      if (!this.isDebugWorld()) {
         this.updateNeighborsAlways(pos, block);
      }

   }

   public float getBrightness(Direction direction, boolean shaded) {
      return 1.0F;
   }

   public Iterable iterateEntities() {
      return this.getEntityLookup().iterate();
   }

   public String toString() {
      return "ServerLevel[" + this.worldProperties.getLevelName() + "]";
   }

   public boolean isFlat() {
      return this.server.getSaveProperties().isFlatWorld();
   }

   public long getSeed() {
      return this.server.getSaveProperties().getGeneratorOptions().getSeed();
   }

   @Nullable
   public EnderDragonFight getEnderDragonFight() {
      return this.enderDragonFight;
   }

   public ServerWorld toServerWorld() {
      return this;
   }

   @VisibleForTesting
   public String getDebugString() {
      return String.format(Locale.ROOT, "players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s", this.players.size(), this.entityManager.getDebugString(), getTopFive(this.entityManager.getLookup().iterate(), (entity) -> {
         return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
      }), this.blockEntityTickers.size(), getTopFive(this.blockEntityTickers, BlockEntityTickInvoker::getName), this.getBlockTickScheduler().getTickCount(), this.getFluidTickScheduler().getTickCount(), this.asString());
   }

   private static String getTopFive(Iterable items, Function classifier) {
      try {
         Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
         Iterator var3 = items.iterator();

         while(var3.hasNext()) {
            Object object = var3.next();
            String string = (String)classifier.apply(object);
            object2IntOpenHashMap.addTo(string, 1);
         }

         return (String)object2IntOpenHashMap.object2IntEntrySet().stream().sorted(Comparator.comparing(Object2IntMap.Entry::getIntValue).reversed()).limit(5L).map((entry) -> {
            String var10000 = (String)entry.getKey();
            return var10000 + ":" + entry.getIntValue();
         }).collect(Collectors.joining(","));
      } catch (Exception var6) {
         return "";
      }
   }

   public static void createEndSpawnPlatform(ServerWorld world) {
      BlockPos lv = END_SPAWN_POS;
      int i = lv.getX();
      int j = lv.getY() - 2;
      int k = lv.getZ();
      BlockPos.iterate(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach((pos) -> {
         world.setBlockState(pos, Blocks.AIR.getDefaultState());
      });
      BlockPos.iterate(i - 2, j, k - 2, i + 2, j, k + 2).forEach((pos) -> {
         world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
      });
   }

   protected EntityLookup getEntityLookup() {
      return this.entityManager.getLookup();
   }

   public void loadEntities(Stream entities) {
      this.entityManager.loadEntities(entities);
   }

   public void addEntities(Stream entities) {
      this.entityManager.addEntities(entities);
   }

   public void disableTickSchedulers(WorldChunk chunk) {
      chunk.disableTickSchedulers(this.getLevelProperties().getTime());
   }

   public void cacheStructures(Chunk chunk) {
      this.server.execute(() -> {
         this.structureLocator.cache(chunk.getPos(), chunk.getStructureStarts());
      });
   }

   public void close() throws IOException {
      super.close();
      this.entityManager.close();
   }

   public String asString() {
      String var10000 = this.chunkManager.getDebugString();
      return "Chunks[S] W: " + var10000 + " E: " + this.entityManager.getDebugString();
   }

   public boolean isChunkLoaded(long chunkPos) {
      return this.entityManager.isLoaded(chunkPos);
   }

   private boolean isTickingFutureReady(long chunkPos) {
      return this.isChunkLoaded(chunkPos) && this.chunkManager.isTickingFutureReady(chunkPos);
   }

   public boolean shouldTickEntity(BlockPos pos) {
      return this.entityManager.shouldTick(pos) && this.chunkManager.threadedAnvilChunkStorage.getTicketManager().shouldTickEntities(ChunkPos.toLong(pos));
   }

   public boolean shouldTick(BlockPos pos) {
      return this.entityManager.shouldTick(pos);
   }

   public boolean shouldTick(ChunkPos pos) {
      return this.entityManager.shouldTick(pos);
   }

   public FeatureSet getEnabledFeatures() {
      return this.server.getSaveProperties().getEnabledFeatures();
   }

   // $FF: synthetic method
   public Scoreboard getScoreboard() {
      return this.getScoreboard();
   }

   // $FF: synthetic method
   public ChunkManager getChunkManager() {
      return this.getChunkManager();
   }

   // $FF: synthetic method
   public QueryableTickScheduler getFluidTickScheduler() {
      return this.getFluidTickScheduler();
   }

   // $FF: synthetic method
   public QueryableTickScheduler getBlockTickScheduler() {
      return this.getBlockTickScheduler();
   }

   private final class ServerEntityHandler implements EntityHandler {
      ServerEntityHandler() {
      }

      public void create(Entity arg) {
      }

      public void destroy(Entity arg) {
         ServerWorld.this.getScoreboard().resetEntityScore(arg);
      }

      public void startTicking(Entity arg) {
         ServerWorld.this.entityList.add(arg);
      }

      public void stopTicking(Entity arg) {
         ServerWorld.this.entityList.remove(arg);
      }

      public void startTracking(Entity arg) {
         ServerWorld.this.getChunkManager().loadEntity(arg);
         if (arg instanceof ServerPlayerEntity lv) {
            ServerWorld.this.players.add(lv);
            ServerWorld.this.updateSleepingPlayers();
         }

         if (arg instanceof MobEntity lv2) {
            if (ServerWorld.this.duringListenerUpdate) {
               String string = "onTrackingStart called during navigation iteration";
               Util.error("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
            }

            ServerWorld.this.loadedMobs.add(lv2);
         }

         if (arg instanceof EnderDragonEntity lv3) {
            EnderDragonPart[] var9 = lv3.getBodyParts();
            int var4 = var9.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               EnderDragonPart lv4 = var9[var5];
               ServerWorld.this.dragonParts.put(lv4.getId(), lv4);
            }
         }

         arg.updateEventHandler(EntityGameEventHandler::onEntitySetPosCallback);
      }

      public void stopTracking(Entity arg) {
         ServerWorld.this.getChunkManager().unloadEntity(arg);
         if (arg instanceof ServerPlayerEntity lv) {
            ServerWorld.this.players.remove(lv);
            ServerWorld.this.updateSleepingPlayers();
         }

         if (arg instanceof MobEntity lv2) {
            if (ServerWorld.this.duringListenerUpdate) {
               String string = "onTrackingStart called during navigation iteration";
               Util.error("onTrackingStart called during navigation iteration", new IllegalStateException("onTrackingStart called during navigation iteration"));
            }

            ServerWorld.this.loadedMobs.remove(lv2);
         }

         if (arg instanceof EnderDragonEntity lv3) {
            EnderDragonPart[] var9 = lv3.getBodyParts();
            int var4 = var9.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               EnderDragonPart lv4 = var9[var5];
               ServerWorld.this.dragonParts.remove(lv4.getId());
            }
         }

         arg.updateEventHandler(EntityGameEventHandler::onEntityRemoval);
      }

      public void updateLoadStatus(Entity arg) {
         arg.updateEventHandler(EntityGameEventHandler::onEntitySetPos);
      }

      // $FF: synthetic method
      public void updateLoadStatus(Object entity) {
         this.updateLoadStatus((Entity)entity);
      }

      // $FF: synthetic method
      public void stopTracking(Object entity) {
         this.stopTracking((Entity)entity);
      }

      // $FF: synthetic method
      public void startTracking(Object entity) {
         this.startTracking((Entity)entity);
      }

      // $FF: synthetic method
      public void startTicking(Object entity) {
         this.startTicking((Entity)entity);
      }

      // $FF: synthetic method
      public void destroy(Object entity) {
         this.destroy((Entity)entity);
      }

      // $FF: synthetic method
      public void create(Object entity) {
         this.create((Entity)entity);
      }
   }
}
