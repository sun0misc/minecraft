package net.minecraft.client.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.EntityList;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientWorld extends World {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final double PARTICLE_Y_OFFSET = 0.05;
   private static final int field_34805 = 10;
   private static final int field_34806 = 1000;
   final EntityList entityList = new EntityList();
   private final ClientEntityManager entityManager = new ClientEntityManager(Entity.class, new ClientEntityHandler());
   private final ClientPlayNetworkHandler networkHandler;
   private final WorldRenderer worldRenderer;
   private final Properties clientWorldProperties;
   private final DimensionEffects dimensionEffects;
   private final MinecraftClient client = MinecraftClient.getInstance();
   final List players = Lists.newArrayList();
   private Scoreboard scoreboard = new Scoreboard();
   private final Map mapStates = Maps.newHashMap();
   private static final long field_32640 = 16777215L;
   private int lightningTicksLeft;
   private final Object2ObjectArrayMap colorCache = (Object2ObjectArrayMap)Util.make(new Object2ObjectArrayMap(3), (map) -> {
      map.put(BiomeColors.GRASS_COLOR, new BiomeColorCache((pos) -> {
         return this.calculateColor(pos, BiomeColors.GRASS_COLOR);
      }));
      map.put(BiomeColors.FOLIAGE_COLOR, new BiomeColorCache((pos) -> {
         return this.calculateColor(pos, BiomeColors.FOLIAGE_COLOR);
      }));
      map.put(BiomeColors.WATER_COLOR, new BiomeColorCache((pos) -> {
         return this.calculateColor(pos, BiomeColors.WATER_COLOR);
      }));
   });
   private final ClientChunkManager chunkManager;
   private final Deque chunkUpdaters = Queues.newArrayDeque();
   private int simulationDistance;
   private final PendingUpdateManager pendingUpdateManager = new PendingUpdateManager();
   private static final Set BLOCK_MARKER_ITEMS;

   public void handlePlayerActionResponse(int sequence) {
      this.pendingUpdateManager.processPendingUpdates(sequence, this);
   }

   public void handleBlockUpdate(BlockPos pos, BlockState state, int flags) {
      if (!this.pendingUpdateManager.hasPendingUpdate(pos, state)) {
         super.setBlockState(pos, state, flags, 512);
      }

   }

   public void processPendingUpdate(BlockPos pos, BlockState state, Vec3d playerPos) {
      BlockState lv = this.getBlockState(pos);
      if (lv != state) {
         this.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
         PlayerEntity lv2 = this.client.player;
         if (this == lv2.world && lv2.collidesWithStateAtPos(pos, state)) {
            lv2.updatePosition(playerPos.x, playerPos.y, playerPos.z);
         }
      }

   }

   PendingUpdateManager getPendingUpdateManager() {
      return this.pendingUpdateManager;
   }

   public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
      if (this.pendingUpdateManager.hasPendingSequence()) {
         BlockState lv = this.getBlockState(pos);
         boolean bl = super.setBlockState(pos, state, flags, maxUpdateDepth);
         if (bl) {
            this.pendingUpdateManager.addPendingUpdate(pos, lv, this.client.player);
         }

         return bl;
      } else {
         return super.setBlockState(pos, state, flags, maxUpdateDepth);
      }
   }

   public ClientWorld(ClientPlayNetworkHandler networkHandler, Properties properties, RegistryKey registryRef, RegistryEntry dimensionTypeEntry, int loadDistance, int simulationDistance, Supplier profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed) {
      super(properties, registryRef, networkHandler.getRegistryManager(), dimensionTypeEntry, profiler, true, debugWorld, seed, 1000000);
      this.networkHandler = networkHandler;
      this.chunkManager = new ClientChunkManager(this, loadDistance);
      this.clientWorldProperties = properties;
      this.worldRenderer = worldRenderer;
      this.dimensionEffects = DimensionEffects.byDimensionType((DimensionType)dimensionTypeEntry.value());
      this.setSpawnPos(new BlockPos(8, 64, 8), 0.0F);
      this.simulationDistance = simulationDistance;
      this.calculateAmbientDarkness();
      this.initWeatherGradients();
   }

   public void enqueueChunkUpdate(Runnable updater) {
      this.chunkUpdaters.add(updater);
   }

   public void runQueuedChunkUpdates() {
      int i = this.chunkUpdaters.size();
      int j = i < 1000 ? Math.max(10, i / 10) : i;

      for(int k = 0; k < j; ++k) {
         Runnable runnable = (Runnable)this.chunkUpdaters.poll();
         if (runnable == null) {
            break;
         }

         runnable.run();
      }

   }

   public boolean hasNoChunkUpdaters() {
      return this.chunkUpdaters.isEmpty();
   }

   public DimensionEffects getDimensionEffects() {
      return this.dimensionEffects;
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      this.getWorldBorder().tick();
      this.tickTime();
      this.getProfiler().push("blocks");
      this.chunkManager.tick(shouldKeepTicking, true);
      this.getProfiler().pop();
   }

   private void tickTime() {
      this.setTime(this.properties.getTime() + 1L);
      if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
         this.setTimeOfDay(this.properties.getTimeOfDay() + 1L);
      }

   }

   public void setTime(long time) {
      this.clientWorldProperties.setTime(time);
   }

   public void setTimeOfDay(long timeOfDay) {
      if (timeOfDay < 0L) {
         timeOfDay = -timeOfDay;
         ((GameRules.BooleanRule)this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(false, (MinecraftServer)null);
      } else {
         ((GameRules.BooleanRule)this.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE)).set(true, (MinecraftServer)null);
      }

      this.clientWorldProperties.setTimeOfDay(timeOfDay);
   }

   public Iterable getEntities() {
      return this.getEntityLookup().iterate();
   }

   public void tickEntities() {
      Profiler lv = this.getProfiler();
      lv.push("entities");
      this.entityList.forEach((entity) -> {
         if (!entity.isRemoved() && !entity.hasVehicle()) {
            this.tickEntity(this::tickEntity, entity);
         }
      });
      lv.pop();
      this.tickBlockEntities();
   }

   public boolean shouldUpdatePostDeath(Entity entity) {
      return entity.getChunkPos().getChebyshevDistance(this.client.player.getChunkPos()) <= this.simulationDistance;
   }

   public void tickEntity(Entity entity) {
      entity.resetPosition();
      ++entity.age;
      this.getProfiler().push(() -> {
         return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
      });
      entity.tick();
      this.getProfiler().pop();
      Iterator var2 = entity.getPassengerList().iterator();

      while(var2.hasNext()) {
         Entity lv = (Entity)var2.next();
         this.tickPassenger(entity, lv);
      }

   }

   private void tickPassenger(Entity entity, Entity passenger) {
      if (!passenger.isRemoved() && passenger.getVehicle() == entity) {
         if (passenger instanceof PlayerEntity || this.entityList.has(passenger)) {
            passenger.resetPosition();
            ++passenger.age;
            passenger.tickRiding();
            Iterator var3 = passenger.getPassengerList().iterator();

            while(var3.hasNext()) {
               Entity lv = (Entity)var3.next();
               this.tickPassenger(passenger, lv);
            }

         }
      } else {
         passenger.stopRiding();
      }
   }

   public void unloadBlockEntities(WorldChunk chunk) {
      chunk.clear();
      this.chunkManager.getLightingProvider().setColumnEnabled(chunk.getPos(), false);
      this.entityManager.stopTicking(chunk.getPos());
   }

   public void resetChunkColor(ChunkPos chunkPos) {
      this.colorCache.forEach((resolver, cache) -> {
         cache.reset(chunkPos.x, chunkPos.z);
      });
      this.entityManager.startTicking(chunkPos);
   }

   public void reloadColor() {
      this.colorCache.forEach((resolver, cache) -> {
         cache.reset();
      });
   }

   public boolean isChunkLoaded(int chunkX, int chunkZ) {
      return true;
   }

   public int getRegularEntityCount() {
      return this.entityManager.getEntityCount();
   }

   public void addPlayer(int id, AbstractClientPlayerEntity player) {
      this.addEntityPrivate(id, player);
   }

   public void addEntity(int id, Entity entity) {
      this.addEntityPrivate(id, entity);
   }

   private void addEntityPrivate(int id, Entity entity) {
      this.removeEntity(id, Entity.RemovalReason.DISCARDED);
      this.entityManager.addEntity(entity);
   }

   public void removeEntity(int entityId, Entity.RemovalReason removalReason) {
      Entity lv = (Entity)this.getEntityLookup().get(entityId);
      if (lv != null) {
         lv.setRemoved(removalReason);
         lv.onRemoved();
      }

   }

   @Nullable
   public Entity getEntityById(int id) {
      return (Entity)this.getEntityLookup().get(id);
   }

   public void disconnect() {
      this.networkHandler.getConnection().disconnect(Text.translatable("multiplayer.status.quitting"));
   }

   public void doRandomBlockDisplayTicks(int centerX, int centerY, int centerZ) {
      int l = true;
      Random lv = Random.create();
      Block lv2 = this.getBlockParticle();
      BlockPos.Mutable lv3 = new BlockPos.Mutable();

      for(int m = 0; m < 667; ++m) {
         this.randomBlockDisplayTick(centerX, centerY, centerZ, 16, lv, lv2, lv3);
         this.randomBlockDisplayTick(centerX, centerY, centerZ, 32, lv, lv2, lv3);
      }

   }

   @Nullable
   private Block getBlockParticle() {
      if (this.client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
         ItemStack lv = this.client.player.getMainHandStack();
         Item lv2 = lv.getItem();
         if (BLOCK_MARKER_ITEMS.contains(lv2) && lv2 instanceof BlockItem) {
            BlockItem lv3 = (BlockItem)lv2;
            return lv3.getBlock();
         }
      }

      return null;
   }

   public void randomBlockDisplayTick(int centerX, int centerY, int centerZ, int radius, Random random, @Nullable Block block, BlockPos.Mutable pos) {
      int m = centerX + this.random.nextInt(radius) - this.random.nextInt(radius);
      int n = centerY + this.random.nextInt(radius) - this.random.nextInt(radius);
      int o = centerZ + this.random.nextInt(radius) - this.random.nextInt(radius);
      pos.set(m, n, o);
      BlockState lv = this.getBlockState(pos);
      lv.getBlock().randomDisplayTick(lv, this, pos, random);
      FluidState lv2 = this.getFluidState(pos);
      if (!lv2.isEmpty()) {
         lv2.randomDisplayTick(this, pos, random);
         ParticleEffect lv3 = lv2.getParticle();
         if (lv3 != null && this.random.nextInt(10) == 0) {
            boolean bl = lv.isSideSolidFullSquare(this, pos, Direction.DOWN);
            BlockPos lv4 = pos.down();
            this.addParticle(lv4, this.getBlockState(lv4), lv3, bl);
         }
      }

      if (block == lv.getBlock()) {
         this.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, lv), (double)m + 0.5, (double)n + 0.5, (double)o + 0.5, 0.0, 0.0, 0.0);
      }

      if (!lv.isFullCube(this, pos)) {
         ((Biome)this.getBiome(pos).value()).getParticleConfig().ifPresent((config) -> {
            if (config.shouldAddParticle(this.random)) {
               this.addParticle(config.getParticle(), (double)pos.getX() + this.random.nextDouble(), (double)pos.getY() + this.random.nextDouble(), (double)pos.getZ() + this.random.nextDouble(), 0.0, 0.0, 0.0);
            }

         });
      }

   }

   private void addParticle(BlockPos pos, BlockState state, ParticleEffect parameters, boolean solidBelow) {
      if (state.getFluidState().isEmpty()) {
         VoxelShape lv = state.getCollisionShape(this, pos);
         double d = lv.getMax(Direction.Axis.Y);
         if (d < 1.0) {
            if (solidBelow) {
               this.addParticle((double)pos.getX(), (double)(pos.getX() + 1), (double)pos.getZ(), (double)(pos.getZ() + 1), (double)(pos.getY() + 1) - 0.05, parameters);
            }
         } else if (!state.isIn(BlockTags.IMPERMEABLE)) {
            double e = lv.getMin(Direction.Axis.Y);
            if (e > 0.0) {
               this.addParticle(pos, parameters, lv, (double)pos.getY() + e - 0.05);
            } else {
               BlockPos lv2 = pos.down();
               BlockState lv3 = this.getBlockState(lv2);
               VoxelShape lv4 = lv3.getCollisionShape(this, lv2);
               double f = lv4.getMax(Direction.Axis.Y);
               if (f < 1.0 && lv3.getFluidState().isEmpty()) {
                  this.addParticle(pos, parameters, lv, (double)pos.getY() - 0.05);
               }
            }
         }

      }
   }

   private void addParticle(BlockPos pos, ParticleEffect parameters, VoxelShape shape, double y) {
      this.addParticle((double)pos.getX() + shape.getMin(Direction.Axis.X), (double)pos.getX() + shape.getMax(Direction.Axis.X), (double)pos.getZ() + shape.getMin(Direction.Axis.Z), (double)pos.getZ() + shape.getMax(Direction.Axis.Z), y, parameters);
   }

   private void addParticle(double minX, double maxX, double minZ, double maxZ, double y, ParticleEffect parameters) {
      this.addParticle(parameters, MathHelper.lerp(this.random.nextDouble(), minX, maxX), y, MathHelper.lerp(this.random.nextDouble(), minZ, maxZ), 0.0, 0.0, 0.0);
   }

   public CrashReportSection addDetailsToCrashReport(CrashReport report) {
      CrashReportSection lv = super.addDetailsToCrashReport(report);
      lv.add("Server brand", () -> {
         return this.client.player.getServerBrand();
      });
      lv.add("Server type", () -> {
         return this.client.getServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
      });
      return lv;
   }

   public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed) {
      if (except == this.client.player) {
         this.playSound(x, y, z, (SoundEvent)sound.value(), category, volume, pitch, false, seed);
      }

   }

   public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry sound, SoundCategory category, float volume, float pitch, long seed) {
      if (except == this.client.player) {
         this.client.getSoundManager().play(new EntityTrackingSoundInstance((SoundEvent)sound.value(), category, volume, pitch, entity, seed));
      }

   }

   public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
      this.playSound(x, y, z, sound, category, volume, pitch, useDistance, this.random.nextLong());
   }

   private void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed) {
      double i = this.client.gameRenderer.getCamera().getPos().squaredDistanceTo(x, y, z);
      PositionedSoundInstance lv = new PositionedSoundInstance(event, category, volume, pitch, Random.create(seed), x, y, z);
      if (useDistance && i > 100.0) {
         double j = Math.sqrt(i) / 40.0;
         this.client.getSoundManager().play(lv, (int)(j * 20.0));
      } else {
         this.client.getSoundManager().play(lv);
      }

   }

   public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, @Nullable NbtCompound nbt) {
      this.client.particleManager.addParticle(new FireworksSparkParticle.FireworkParticle(this, x, y, z, velocityX, velocityY, velocityZ, this.client.particleManager, nbt));
   }

   public void sendPacket(Packet packet) {
      this.networkHandler.sendPacket(packet);
   }

   public RecipeManager getRecipeManager() {
      return this.networkHandler.getRecipeManager();
   }

   public void setScoreboard(Scoreboard scoreboard) {
      this.scoreboard = scoreboard;
   }

   public QueryableTickScheduler getBlockTickScheduler() {
      return EmptyTickSchedulers.getClientTickScheduler();
   }

   public QueryableTickScheduler getFluidTickScheduler() {
      return EmptyTickSchedulers.getClientTickScheduler();
   }

   public ClientChunkManager getChunkManager() {
      return this.chunkManager;
   }

   @Nullable
   public MapState getMapState(String id) {
      return (MapState)this.mapStates.get(id);
   }

   public void putClientsideMapState(String id, MapState state) {
      this.mapStates.put(id, state);
   }

   public void putMapState(String id, MapState state) {
   }

   public int getNextMapId() {
      return 0;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
      this.worldRenderer.updateBlock(this, pos, oldState, newState, flags);
   }

   public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
      this.worldRenderer.scheduleBlockRerenderIfNeeded(pos, old, updated);
   }

   public void scheduleBlockRenders(int x, int y, int z) {
      this.worldRenderer.scheduleBlockRenders(x, y, z);
   }

   public void markChunkRenderability(int chunkX, int chunkZ) {
      WorldChunk lv = this.chunkManager.getWorldChunk(chunkX, chunkZ, false);
      if (lv != null) {
         lv.setShouldRenderOnUpdate(true);
      }

   }

   public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
      this.worldRenderer.setBlockBreakingInfo(entityId, pos, progress);
   }

   public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
      this.worldRenderer.processGlobalEvent(eventId, pos, data);
   }

   public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
      try {
         this.worldRenderer.processWorldEvent(eventId, pos, data);
      } catch (Throwable var8) {
         CrashReport lv = CrashReport.create(var8, "Playing level event");
         CrashReportSection lv2 = lv.addElement("Level event being played");
         lv2.add("Block coordinates", (Object)CrashReportSection.createPositionString(this, pos));
         lv2.add("Event source", (Object)player);
         lv2.add("Event type", (Object)eventId);
         lv2.add("Event data", (Object)data);
         throw new CrashException(lv);
      }
   }

   public void addParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn(), x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addImportantParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, false, true, x, y, z, velocityX, velocityY, velocityZ);
   }

   public void addImportantParticle(ParticleEffect parameters, boolean alwaysSpawn, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
      this.worldRenderer.addParticle(parameters, parameters.getType().shouldAlwaysSpawn() || alwaysSpawn, true, x, y, z, velocityX, velocityY, velocityZ);
   }

   public List getPlayers() {
      return this.players;
   }

   public RegistryEntry getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
      return this.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS);
   }

   public float getSkyBrightness(float tickDelta) {
      float g = this.getSkyAngle(tickDelta);
      float h = 1.0F - (MathHelper.cos(g * 6.2831855F) * 2.0F + 0.2F);
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      h = 1.0F - h;
      h *= 1.0F - this.getRainGradient(tickDelta) * 5.0F / 16.0F;
      h *= 1.0F - this.getThunderGradient(tickDelta) * 5.0F / 16.0F;
      return h * 0.8F + 0.2F;
   }

   public Vec3d getSkyColor(Vec3d cameraPos, float tickDelta) {
      float g = this.getSkyAngle(tickDelta);
      Vec3d lv = cameraPos.subtract(2.0, 2.0, 2.0).multiply(0.25);
      BiomeAccess lv2 = this.getBiomeAccess();
      Vec3d lv3 = CubicSampler.sampleColor(lv, (x, y, z) -> {
         return Vec3d.unpackRgb(((Biome)lv2.getBiomeForNoiseGen(x, y, z).value()).getSkyColor());
      });
      float h = MathHelper.cos(g * 6.2831855F) * 2.0F + 0.5F;
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      float i = (float)lv3.x * h;
      float j = (float)lv3.y * h;
      float k = (float)lv3.z * h;
      float l = this.getRainGradient(tickDelta);
      float m;
      float n;
      if (l > 0.0F) {
         m = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.6F;
         n = 1.0F - l * 0.75F;
         i = i * n + m * (1.0F - n);
         j = j * n + m * (1.0F - n);
         k = k * n + m * (1.0F - n);
      }

      m = this.getThunderGradient(tickDelta);
      if (m > 0.0F) {
         n = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.2F;
         float o = 1.0F - m * 0.75F;
         i = i * o + n * (1.0F - o);
         j = j * o + n * (1.0F - o);
         k = k * o + n * (1.0F - o);
      }

      if (!(Boolean)this.client.options.getHideLightningFlashes().getValue() && this.lightningTicksLeft > 0) {
         n = (float)this.lightningTicksLeft - tickDelta;
         if (n > 1.0F) {
            n = 1.0F;
         }

         n *= 0.45F;
         i = i * (1.0F - n) + 0.8F * n;
         j = j * (1.0F - n) + 0.8F * n;
         k = k * (1.0F - n) + 1.0F * n;
      }

      return new Vec3d((double)i, (double)j, (double)k);
   }

   public Vec3d getCloudsColor(float tickDelta) {
      float g = this.getSkyAngle(tickDelta);
      float h = MathHelper.cos(g * 6.2831855F) * 2.0F + 0.5F;
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      float i = 1.0F;
      float j = 1.0F;
      float k = 1.0F;
      float l = this.getRainGradient(tickDelta);
      float m;
      float n;
      if (l > 0.0F) {
         m = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.6F;
         n = 1.0F - l * 0.95F;
         i = i * n + m * (1.0F - n);
         j = j * n + m * (1.0F - n);
         k = k * n + m * (1.0F - n);
      }

      i *= h * 0.9F + 0.1F;
      j *= h * 0.9F + 0.1F;
      k *= h * 0.85F + 0.15F;
      m = this.getThunderGradient(tickDelta);
      if (m > 0.0F) {
         n = (i * 0.3F + j * 0.59F + k * 0.11F) * 0.2F;
         float o = 1.0F - m * 0.95F;
         i = i * o + n * (1.0F - o);
         j = j * o + n * (1.0F - o);
         k = k * o + n * (1.0F - o);
      }

      return new Vec3d((double)i, (double)j, (double)k);
   }

   public float method_23787(float f) {
      float g = this.getSkyAngle(f);
      float h = 1.0F - (MathHelper.cos(g * 6.2831855F) * 2.0F + 0.25F);
      h = MathHelper.clamp(h, 0.0F, 1.0F);
      return h * h * 0.5F;
   }

   public int getLightningTicksLeft() {
      return this.lightningTicksLeft;
   }

   public void setLightningTicksLeft(int lightningTicksLeft) {
      this.lightningTicksLeft = lightningTicksLeft;
   }

   public float getBrightness(Direction direction, boolean shaded) {
      boolean bl2 = this.getDimensionEffects().isDarkened();
      if (!shaded) {
         return bl2 ? 0.9F : 1.0F;
      } else {
         switch (direction) {
            case DOWN:
               return bl2 ? 0.9F : 0.5F;
            case UP:
               return bl2 ? 0.9F : 1.0F;
            case NORTH:
            case SOUTH:
               return 0.8F;
            case WEST:
            case EAST:
               return 0.6F;
            default:
               return 1.0F;
         }
      }
   }

   public int getColor(BlockPos pos, ColorResolver colorResolver) {
      BiomeColorCache lv = (BiomeColorCache)this.colorCache.get(colorResolver);
      return lv.getBiomeColor(pos);
   }

   public int calculateColor(BlockPos pos, ColorResolver colorResolver) {
      int i = (Integer)MinecraftClient.getInstance().options.getBiomeBlendRadius().getValue();
      if (i == 0) {
         return colorResolver.getColor((Biome)this.getBiome(pos).value(), (double)pos.getX(), (double)pos.getZ());
      } else {
         int j = (i * 2 + 1) * (i * 2 + 1);
         int k = 0;
         int l = 0;
         int m = 0;
         CuboidBlockIterator lv = new CuboidBlockIterator(pos.getX() - i, pos.getY(), pos.getZ() - i, pos.getX() + i, pos.getY(), pos.getZ() + i);

         int n;
         for(BlockPos.Mutable lv2 = new BlockPos.Mutable(); lv.step(); m += n & 255) {
            lv2.set(lv.getX(), lv.getY(), lv.getZ());
            n = colorResolver.getColor((Biome)this.getBiome(lv2).value(), (double)lv2.getX(), (double)lv2.getZ());
            k += (n & 16711680) >> 16;
            l += (n & '\uff00') >> 8;
         }

         return (k / j & 255) << 16 | (l / j & 255) << 8 | m / j & 255;
      }
   }

   public void setSpawnPos(BlockPos pos, float angle) {
      this.properties.setSpawnPos(pos, angle);
   }

   public String toString() {
      return "ClientLevel";
   }

   public Properties getLevelProperties() {
      return this.clientWorldProperties;
   }

   public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {
   }

   protected Map getMapStates() {
      return ImmutableMap.copyOf(this.mapStates);
   }

   protected void putMapStates(Map mapStates) {
      this.mapStates.putAll(mapStates);
   }

   protected EntityLookup getEntityLookup() {
      return this.entityManager.getLookup();
   }

   public String asString() {
      String var10000 = this.chunkManager.getDebugString();
      return "Chunks[C] W: " + var10000 + " E: " + this.entityManager.getDebugString();
   }

   public void addBlockBreakParticles(BlockPos pos, BlockState state) {
      this.client.particleManager.addBlockBreakParticles(pos, state);
   }

   public void setSimulationDistance(int simulationDistance) {
      this.simulationDistance = simulationDistance;
   }

   public int getSimulationDistance() {
      return this.simulationDistance;
   }

   public FeatureSet getEnabledFeatures() {
      return this.networkHandler.getEnabledFeatures();
   }

   // $FF: synthetic method
   public WorldProperties getLevelProperties() {
      return this.getLevelProperties();
   }

   // $FF: synthetic method
   public ChunkManager getChunkManager() {
      return this.getChunkManager();
   }

   static {
      BLOCK_MARKER_ITEMS = Set.of(Items.BARRIER, Items.LIGHT);
   }

   @Environment(EnvType.CLIENT)
   private final class ClientEntityHandler implements EntityHandler {
      ClientEntityHandler() {
      }

      public void create(Entity arg) {
      }

      public void destroy(Entity arg) {
      }

      public void startTicking(Entity arg) {
         ClientWorld.this.entityList.add(arg);
      }

      public void stopTicking(Entity arg) {
         ClientWorld.this.entityList.remove(arg);
      }

      public void startTracking(Entity arg) {
         if (arg instanceof AbstractClientPlayerEntity) {
            ClientWorld.this.players.add((AbstractClientPlayerEntity)arg);
         }

      }

      public void stopTracking(Entity arg) {
         arg.detach();
         ClientWorld.this.players.remove(arg);
      }

      public void updateLoadStatus(Entity arg) {
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

   @Environment(EnvType.CLIENT)
   public static class Properties implements MutableWorldProperties {
      private final boolean hardcore;
      private final GameRules gameRules;
      private final boolean flatWorld;
      private int spawnX;
      private int spawnY;
      private int spawnZ;
      private float spawnAngle;
      private long time;
      private long timeOfDay;
      private boolean raining;
      private Difficulty difficulty;
      private boolean difficultyLocked;

      public Properties(Difficulty difficulty, boolean hardcore, boolean flatWorld) {
         this.difficulty = difficulty;
         this.hardcore = hardcore;
         this.flatWorld = flatWorld;
         this.gameRules = new GameRules();
      }

      public int getSpawnX() {
         return this.spawnX;
      }

      public int getSpawnY() {
         return this.spawnY;
      }

      public int getSpawnZ() {
         return this.spawnZ;
      }

      public float getSpawnAngle() {
         return this.spawnAngle;
      }

      public long getTime() {
         return this.time;
      }

      public long getTimeOfDay() {
         return this.timeOfDay;
      }

      public void setSpawnX(int spawnX) {
         this.spawnX = spawnX;
      }

      public void setSpawnY(int spawnY) {
         this.spawnY = spawnY;
      }

      public void setSpawnZ(int spawnZ) {
         this.spawnZ = spawnZ;
      }

      public void setSpawnAngle(float spawnAngle) {
         this.spawnAngle = spawnAngle;
      }

      public void setTime(long time) {
         this.time = time;
      }

      public void setTimeOfDay(long timeOfDay) {
         this.timeOfDay = timeOfDay;
      }

      public void setSpawnPos(BlockPos pos, float angle) {
         this.spawnX = pos.getX();
         this.spawnY = pos.getY();
         this.spawnZ = pos.getZ();
         this.spawnAngle = angle;
      }

      public boolean isThundering() {
         return false;
      }

      public boolean isRaining() {
         return this.raining;
      }

      public void setRaining(boolean raining) {
         this.raining = raining;
      }

      public boolean isHardcore() {
         return this.hardcore;
      }

      public GameRules getGameRules() {
         return this.gameRules;
      }

      public Difficulty getDifficulty() {
         return this.difficulty;
      }

      public boolean isDifficultyLocked() {
         return this.difficultyLocked;
      }

      public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
         MutableWorldProperties.super.populateCrashReport(reportSection, world);
      }

      public void setDifficulty(Difficulty difficulty) {
         this.difficulty = difficulty;
      }

      public void setDifficultyLocked(boolean difficultyLocked) {
         this.difficultyLocked = difficultyLocked;
      }

      public double getSkyDarknessHeight(HeightLimitView world) {
         return this.flatWorld ? (double)world.getBottomY() : 63.0;
      }

      public float getHorizonShadingRatio() {
         return this.flatWorld ? 1.0F : 0.03125F;
      }
   }
}
