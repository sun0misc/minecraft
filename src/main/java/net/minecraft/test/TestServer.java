package net.minecraft.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.datafixer.Schemas;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ApiServices;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TestServer extends MinecraftServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int RESULT_STRING_LOG_INTERVAL = 20;
   private static final ApiServices NONE_API_SERVICES;
   private final List batches;
   private final BlockPos pos;
   private static final GameRules GAME_RULES;
   private static final GeneratorOptions TEST_LEVEL;
   @Nullable
   private TestSet testSet;

   public static TestServer create(Thread thread, LevelStorage.Session session, ResourcePackManager resourcePackManager, Collection batches, BlockPos pos) {
      if (batches.isEmpty()) {
         throw new IllegalArgumentException("No test batches were given!");
      } else {
         resourcePackManager.scanPacks();
         DataConfiguration lv = new DataConfiguration(new DataPackSettings(new ArrayList(resourcePackManager.getNames()), List.of()), FeatureFlags.FEATURE_MANAGER.getFeatureSet());
         LevelInfo lv2 = new LevelInfo("Test Level", GameMode.CREATIVE, false, Difficulty.NORMAL, true, GAME_RULES, lv);
         SaveLoading.DataPacks lv3 = new SaveLoading.DataPacks(resourcePackManager, lv, false, true);
         SaveLoading.ServerConfig lv4 = new SaveLoading.ServerConfig(lv3, CommandManager.RegistrationEnvironment.DEDICATED, 4);

         try {
            LOGGER.debug("Starting resource loading");
            Stopwatch stopwatch = Stopwatch.createStarted();
            SaveLoader lv5 = (SaveLoader)Util.waitAndApply((executor) -> {
               return SaveLoading.load(lv4, (arg2) -> {
                  Registry lv = (new SimpleRegistry(RegistryKeys.DIMENSION, Lifecycle.stable())).freeze();
                  DimensionOptionsRegistryHolder.DimensionsConfig lv2x = ((WorldPreset)arg2.worldGenRegistryManager().get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.FLAT).value()).createDimensionsRegistryHolder().toConfig(lv);
                  return new SaveLoading.LoadContext(new LevelProperties(lv2, TEST_LEVEL, lv2x.specialWorldProperty(), lv2x.getLifecycle()), lv2x.toDynamicRegistryManager());
               }, SaveLoader::new, Util.getMainWorkerExecutor(), executor);
            }).get();
            stopwatch.stop();
            LOGGER.debug("Finished resource loading after {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new TestServer(thread, session, resourcePackManager, lv5, batches, pos);
         } catch (Exception var11) {
            LOGGER.warn("Failed to load vanilla datapack, bit oops", var11);
            System.exit(-1);
            throw new IllegalStateException();
         }
      }
   }

   private TestServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Collection batches, BlockPos pos) {
      super(serverThread, session, dataPackManager, saveLoader, Proxy.NO_PROXY, Schemas.getFixer(), NONE_API_SERVICES, WorldGenerationProgressLogger::new);
      this.batches = Lists.newArrayList(batches);
      this.pos = pos;
   }

   public boolean setupServer() {
      this.setPlayerManager(new PlayerManager(this, this.getCombinedDynamicRegistries(), this.saveHandler, 1) {
      });
      this.loadWorld();
      ServerWorld lv = this.getOverworld();
      lv.setSpawnPos(this.pos, 0.0F);
      int i = 20000000;
      lv.setWeather(20000000, 20000000, false, false);
      LOGGER.info("Started game test server");
      return true;
   }

   public void tick(BooleanSupplier shouldKeepTicking) {
      super.tick(shouldKeepTicking);
      ServerWorld lv = this.getOverworld();
      if (!this.isTesting()) {
         this.runTestBatches(lv);
      }

      if (lv.getTime() % 20L == 0L) {
         LOGGER.info(this.testSet.getResultString());
      }

      if (this.testSet.isDone()) {
         this.stop(false);
         LOGGER.info(this.testSet.getResultString());
         TestFailureLogger.stop();
         LOGGER.info("========= {} GAME TESTS COMPLETE ======================", this.testSet.getTestCount());
         if (this.testSet.failed()) {
            LOGGER.info("{} required tests failed :(", this.testSet.getFailedRequiredTestCount());
            this.testSet.getRequiredTests().forEach((test) -> {
               LOGGER.info("   - {}", test.getTemplatePath());
            });
         } else {
            LOGGER.info("All {} required tests passed :)", this.testSet.getTestCount());
         }

         if (this.testSet.hasFailedOptionalTests()) {
            LOGGER.info("{} optional tests failed", this.testSet.getFailedOptionalTestCount());
            this.testSet.getOptionalTests().forEach((test) -> {
               LOGGER.info("   - {}", test.getTemplatePath());
            });
         }

         LOGGER.info("====================================================");
      }

   }

   public void runTasksTillTickEnd() {
      this.runTasks();
   }

   public SystemDetails addExtraSystemDetails(SystemDetails details) {
      details.addSection("Type", "Game test server");
      return details;
   }

   public void exit() {
      super.exit();
      LOGGER.info("Game test server shutting down");
      System.exit(this.testSet.getFailedRequiredTestCount());
   }

   public void setCrashReport(CrashReport report) {
      super.setCrashReport(report);
      LOGGER.error("Game test server crashed\n{}", report.asString());
      System.exit(1);
   }

   private void runTestBatches(ServerWorld world) {
      Collection collection = TestUtil.runTestBatches(this.batches, new BlockPos(0, -60, 0), BlockRotation.NONE, world, TestManager.INSTANCE, 8);
      this.testSet = new TestSet(collection);
      LOGGER.info("{} tests are now running!", this.testSet.getTestCount());
   }

   private boolean isTesting() {
      return this.testSet != null;
   }

   public boolean isHardcore() {
      return false;
   }

   public int getOpPermissionLevel() {
      return 0;
   }

   public int getFunctionPermissionLevel() {
      return 4;
   }

   public boolean shouldBroadcastRconToOps() {
      return false;
   }

   public boolean isDedicated() {
      return false;
   }

   public int getRateLimit() {
      return 0;
   }

   public boolean isUsingNativeTransport() {
      return false;
   }

   public boolean areCommandBlocksEnabled() {
      return true;
   }

   public boolean isRemote() {
      return false;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return false;
   }

   public boolean isHost(GameProfile profile) {
      return false;
   }

   static {
      NONE_API_SERVICES = new ApiServices((MinecraftSessionService)null, SignatureVerifier.NOOP, (GameProfileRepository)null, (UserCache)null);
      GAME_RULES = (GameRules)Util.make(new GameRules(), (gameRules) -> {
         ((GameRules.BooleanRule)gameRules.get(GameRules.DO_MOB_SPAWNING)).set(false, (MinecraftServer)null);
         ((GameRules.BooleanRule)gameRules.get(GameRules.DO_WEATHER_CYCLE)).set(false, (MinecraftServer)null);
      });
      TEST_LEVEL = new GeneratorOptions(0L, false, false);
   }
}
