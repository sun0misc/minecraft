package net.minecraft.world.level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LevelProperties implements ServerWorldProperties, SaveProperties {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final String PLAYER_KEY = "Player";
   protected static final String WORLD_GEN_SETTINGS_KEY = "WorldGenSettings";
   private LevelInfo levelInfo;
   private final GeneratorOptions generatorOptions;
   private final SpecialProperty specialProperty;
   private final Lifecycle lifecycle;
   private int spawnX;
   private int spawnY;
   private int spawnZ;
   private float spawnAngle;
   private long time;
   private long timeOfDay;
   @Nullable
   private final DataFixer dataFixer;
   private final int dataVersion;
   private boolean playerDataLoaded;
   @Nullable
   private NbtCompound playerData;
   private final int version;
   private int clearWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private boolean initialized;
   private boolean difficultyLocked;
   private WorldBorder.Properties worldBorder;
   private NbtCompound dragonFight;
   @Nullable
   private NbtCompound customBossEvents;
   private int wanderingTraderSpawnDelay;
   private int wanderingTraderSpawnChance;
   @Nullable
   private UUID wanderingTraderId;
   private final Set serverBrands;
   private boolean modded;
   private final Set removedFeatures;
   private final Timer scheduledEvents;

   private LevelProperties(@Nullable DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, boolean modded, int spawnX, int spawnY, int spawnZ, float spawnAngle, long time, long timeOfDay, int version, int clearWeatherTime, int rainTime, boolean raining, int thunderTime, boolean thundering, boolean initialized, boolean difficultyLocked, WorldBorder.Properties worldBorder, int wanderingTraderSpawnDelay, int wanderingTraderSpawnChance, @Nullable UUID wanderingTraderId, Set serverBrands, Set removedFeatures, Timer scheduledEvents, @Nullable NbtCompound customBossEvents, NbtCompound dragonFight, LevelInfo levelInfo, GeneratorOptions generatorOptions, SpecialProperty specialProperty, Lifecycle lifecycle) {
      this.dataFixer = dataFixer;
      this.modded = modded;
      this.spawnX = spawnX;
      this.spawnY = spawnY;
      this.spawnZ = spawnZ;
      this.spawnAngle = spawnAngle;
      this.time = time;
      this.timeOfDay = timeOfDay;
      this.version = version;
      this.clearWeatherTime = clearWeatherTime;
      this.rainTime = rainTime;
      this.raining = raining;
      this.thunderTime = thunderTime;
      this.thundering = thundering;
      this.initialized = initialized;
      this.difficultyLocked = difficultyLocked;
      this.worldBorder = worldBorder;
      this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
      this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
      this.wanderingTraderId = wanderingTraderId;
      this.serverBrands = serverBrands;
      this.removedFeatures = removedFeatures;
      this.playerData = playerData;
      this.dataVersion = dataVersion;
      this.scheduledEvents = scheduledEvents;
      this.customBossEvents = customBossEvents;
      this.dragonFight = dragonFight;
      this.levelInfo = levelInfo;
      this.generatorOptions = generatorOptions;
      this.specialProperty = specialProperty;
      this.lifecycle = lifecycle;
   }

   public LevelProperties(LevelInfo levelInfo, GeneratorOptions generatorOptions, SpecialProperty specialProperty, Lifecycle lifecycle) {
      this((DataFixer)null, SharedConstants.getGameVersion().getSaveVersion().getId(), (NbtCompound)null, false, 0, 0, 0, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_BORDER, 0, 0, (UUID)null, Sets.newLinkedHashSet(), new HashSet(), new Timer(TimerCallbackSerializer.INSTANCE), (NbtCompound)null, new NbtCompound(), levelInfo.withCopiedGameRules(), generatorOptions, specialProperty, lifecycle);
   }

   public static LevelProperties readProperties(Dynamic dynamic, DataFixer dataFixer, int dataVersion, @Nullable NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle) {
      long l = dynamic.get("Time").asLong(0L);
      NbtCompound lv = (NbtCompound)((Dynamic)dynamic.get("DragonFight").result().orElseGet(() -> {
         return dynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
      })).convert(NbtOps.INSTANCE).getValue();
      return new LevelProperties(dataFixer, dataVersion, playerData, dynamic.get("WasModded").asBoolean(false), dynamic.get("SpawnX").asInt(0), dynamic.get("SpawnY").asInt(0), dynamic.get("SpawnZ").asInt(0), dynamic.get("SpawnAngle").asFloat(0.0F), l, dynamic.get("DayTime").asLong(l), saveVersionInfo.getLevelFormatVersion(), dynamic.get("clearWeatherTime").asInt(0), dynamic.get("rainTime").asInt(0), dynamic.get("raining").asBoolean(false), dynamic.get("thunderTime").asInt(0), dynamic.get("thundering").asBoolean(false), dynamic.get("initialized").asBoolean(true), dynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Properties.fromDynamic(dynamic, WorldBorder.DEFAULT_BORDER), dynamic.get("WanderingTraderSpawnDelay").asInt(0), dynamic.get("WanderingTraderSpawnChance").asInt(0), (UUID)dynamic.get("WanderingTraderId").read(Uuids.INT_STREAM_CODEC).result().orElse((Object)null), (Set)dynamic.get("ServerBrands").asStream().flatMap((dynamicx) -> {
         return dynamicx.asString().result().stream();
      }).collect(Collectors.toCollection(Sets::newLinkedHashSet)), (Set)dynamic.get("removed_features").asStream().flatMap((dynamicx) -> {
         return dynamicx.asString().result().stream();
      }).collect(Collectors.toSet()), new Timer(TimerCallbackSerializer.INSTANCE, dynamic.get("ScheduledEvents").asStream()), (NbtCompound)dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), lv, levelInfo, generatorOptions, specialProperty, lifecycle);
   }

   public NbtCompound cloneWorldNbt(DynamicRegistryManager registryManager, @Nullable NbtCompound playerNbt) {
      this.loadPlayerData();
      if (playerNbt == null) {
         playerNbt = this.playerData;
      }

      NbtCompound lv = new NbtCompound();
      this.updateProperties(registryManager, lv, playerNbt);
      return lv;
   }

   private void updateProperties(DynamicRegistryManager registryManager, NbtCompound levelNbt, @Nullable NbtCompound playerNbt) {
      levelNbt.put("ServerBrands", createStringList(this.serverBrands));
      levelNbt.putBoolean("WasModded", this.modded);
      if (!this.removedFeatures.isEmpty()) {
         levelNbt.put("removed_features", createStringList(this.removedFeatures));
      }

      NbtCompound lv = new NbtCompound();
      lv.putString("Name", SharedConstants.getGameVersion().getName());
      lv.putInt("Id", SharedConstants.getGameVersion().getSaveVersion().getId());
      lv.putBoolean("Snapshot", !SharedConstants.getGameVersion().isStable());
      lv.putString("Series", SharedConstants.getGameVersion().getSaveVersion().getSeries());
      levelNbt.put("Version", lv);
      NbtHelper.putDataVersion(levelNbt);
      DynamicOps dynamicOps = RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)registryManager);
      DataResult var10000 = WorldGenSettings.encode(dynamicOps, this.generatorOptions, (DynamicRegistryManager)registryManager);
      Logger var10002 = LOGGER;
      Objects.requireNonNull(var10002);
      var10000.resultOrPartial(Util.addPrefix("WorldGenSettings: ", var10002::error)).ifPresent((arg2) -> {
         levelNbt.put("WorldGenSettings", arg2);
      });
      levelNbt.putInt("GameType", this.levelInfo.getGameMode().getId());
      levelNbt.putInt("SpawnX", this.spawnX);
      levelNbt.putInt("SpawnY", this.spawnY);
      levelNbt.putInt("SpawnZ", this.spawnZ);
      levelNbt.putFloat("SpawnAngle", this.spawnAngle);
      levelNbt.putLong("Time", this.time);
      levelNbt.putLong("DayTime", this.timeOfDay);
      levelNbt.putLong("LastPlayed", Util.getEpochTimeMs());
      levelNbt.putString("LevelName", this.levelInfo.getLevelName());
      levelNbt.putInt("version", 19133);
      levelNbt.putInt("clearWeatherTime", this.clearWeatherTime);
      levelNbt.putInt("rainTime", this.rainTime);
      levelNbt.putBoolean("raining", this.raining);
      levelNbt.putInt("thunderTime", this.thunderTime);
      levelNbt.putBoolean("thundering", this.thundering);
      levelNbt.putBoolean("hardcore", this.levelInfo.isHardcore());
      levelNbt.putBoolean("allowCommands", this.levelInfo.areCommandsAllowed());
      levelNbt.putBoolean("initialized", this.initialized);
      this.worldBorder.writeNbt(levelNbt);
      levelNbt.putByte("Difficulty", (byte)this.levelInfo.getDifficulty().getId());
      levelNbt.putBoolean("DifficultyLocked", this.difficultyLocked);
      levelNbt.put("GameRules", this.levelInfo.getGameRules().toNbt());
      levelNbt.put("DragonFight", this.dragonFight);
      if (playerNbt != null) {
         levelNbt.put("Player", playerNbt);
      }

      DataResult dataResult = DataConfiguration.CODEC.encodeStart(NbtOps.INSTANCE, this.levelInfo.getDataConfiguration());
      dataResult.get().ifLeft((dataConfiguration) -> {
         levelNbt.copyFrom((NbtCompound)dataConfiguration);
      }).ifRight((result) -> {
         LOGGER.warn("Failed to encode configuration {}", result.message());
      });
      if (this.customBossEvents != null) {
         levelNbt.put("CustomBossEvents", this.customBossEvents);
      }

      levelNbt.put("ScheduledEvents", this.scheduledEvents.toNbt());
      levelNbt.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
      levelNbt.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
      if (this.wanderingTraderId != null) {
         levelNbt.putUuid("WanderingTraderId", this.wanderingTraderId);
      }

   }

   private static NbtList createStringList(Set strings) {
      NbtList lv = new NbtList();
      Stream var10000 = strings.stream().map(NbtString::of);
      Objects.requireNonNull(lv);
      var10000.forEach(lv::add);
      return lv;
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

   private void loadPlayerData() {
      if (!this.playerDataLoaded && this.playerData != null) {
         if (this.dataVersion < SharedConstants.getGameVersion().getSaveVersion().getId()) {
            if (this.dataFixer == null) {
               throw (NullPointerException)Util.throwOrPause(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
            }

            this.playerData = DataFixTypes.PLAYER.update(this.dataFixer, this.playerData, this.dataVersion);
         }

         this.playerDataLoaded = true;
      }
   }

   public NbtCompound getPlayerData() {
      this.loadPlayerData();
      return this.playerData;
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

   public String getLevelName() {
      return this.levelInfo.getLevelName();
   }

   public int getVersion() {
      return this.version;
   }

   public int getClearWeatherTime() {
      return this.clearWeatherTime;
   }

   public void setClearWeatherTime(int clearWeatherTime) {
      this.clearWeatherTime = clearWeatherTime;
   }

   public boolean isThundering() {
      return this.thundering;
   }

   public void setThundering(boolean thundering) {
      this.thundering = thundering;
   }

   public int getThunderTime() {
      return this.thunderTime;
   }

   public void setThunderTime(int thunderTime) {
      this.thunderTime = thunderTime;
   }

   public boolean isRaining() {
      return this.raining;
   }

   public void setRaining(boolean raining) {
      this.raining = raining;
   }

   public int getRainTime() {
      return this.rainTime;
   }

   public void setRainTime(int rainTime) {
      this.rainTime = rainTime;
   }

   public GameMode getGameMode() {
      return this.levelInfo.getGameMode();
   }

   public void setGameMode(GameMode gameMode) {
      this.levelInfo = this.levelInfo.withGameMode(gameMode);
   }

   public boolean isHardcore() {
      return this.levelInfo.isHardcore();
   }

   public boolean areCommandsAllowed() {
      return this.levelInfo.areCommandsAllowed();
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setInitialized(boolean initialized) {
      this.initialized = initialized;
   }

   public GameRules getGameRules() {
      return this.levelInfo.getGameRules();
   }

   public WorldBorder.Properties getWorldBorder() {
      return this.worldBorder;
   }

   public void setWorldBorder(WorldBorder.Properties worldBorder) {
      this.worldBorder = worldBorder;
   }

   public Difficulty getDifficulty() {
      return this.levelInfo.getDifficulty();
   }

   public void setDifficulty(Difficulty difficulty) {
      this.levelInfo = this.levelInfo.withDifficulty(difficulty);
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   public void setDifficultyLocked(boolean difficultyLocked) {
      this.difficultyLocked = difficultyLocked;
   }

   public Timer getScheduledEvents() {
      return this.scheduledEvents;
   }

   public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
      ServerWorldProperties.super.populateCrashReport(reportSection, world);
      SaveProperties.super.populateCrashReport(reportSection);
   }

   public GeneratorOptions getGeneratorOptions() {
      return this.generatorOptions;
   }

   public boolean isFlatWorld() {
      return this.specialProperty == LevelProperties.SpecialProperty.FLAT;
   }

   public boolean isDebugWorld() {
      return this.specialProperty == LevelProperties.SpecialProperty.DEBUG;
   }

   public Lifecycle getLifecycle() {
      return this.lifecycle;
   }

   public NbtCompound getDragonFight() {
      return this.dragonFight;
   }

   public void setDragonFight(NbtCompound dragonFight) {
      this.dragonFight = dragonFight;
   }

   public DataConfiguration getDataConfiguration() {
      return this.levelInfo.getDataConfiguration();
   }

   public void updateLevelInfo(DataConfiguration dataConfiguration) {
      this.levelInfo = this.levelInfo.withDataConfiguration(dataConfiguration);
   }

   @Nullable
   public NbtCompound getCustomBossEvents() {
      return this.customBossEvents;
   }

   public void setCustomBossEvents(@Nullable NbtCompound customBossEvents) {
      this.customBossEvents = customBossEvents;
   }

   public int getWanderingTraderSpawnDelay() {
      return this.wanderingTraderSpawnDelay;
   }

   public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay) {
      this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
   }

   public int getWanderingTraderSpawnChance() {
      return this.wanderingTraderSpawnChance;
   }

   public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance) {
      this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
   }

   @Nullable
   public UUID getWanderingTraderId() {
      return this.wanderingTraderId;
   }

   public void setWanderingTraderId(UUID wanderingTraderId) {
      this.wanderingTraderId = wanderingTraderId;
   }

   public void addServerBrand(String brand, boolean modded) {
      this.serverBrands.add(brand);
      this.modded |= modded;
   }

   public boolean isModded() {
      return this.modded;
   }

   public Set getServerBrands() {
      return ImmutableSet.copyOf(this.serverBrands);
   }

   public Set getRemovedFeatures() {
      return Set.copyOf(this.removedFeatures);
   }

   public ServerWorldProperties getMainWorldProperties() {
      return this;
   }

   public LevelInfo getLevelInfo() {
      return this.levelInfo.withCopiedGameRules();
   }

   /** @deprecated */
   @Deprecated
   public static enum SpecialProperty {
      NONE,
      FLAT,
      DEBUG;

      // $FF: synthetic method
      private static SpecialProperty[] method_45559() {
         return new SpecialProperty[]{NONE, FLAT, DEBUG};
      }
   }
}
