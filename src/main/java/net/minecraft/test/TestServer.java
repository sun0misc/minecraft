/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.class_9813;
import net.minecraft.datafixer.Schemas;
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
import net.minecraft.test.Batches;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestFailureLogger;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestStructurePlacer;
import net.minecraft.util.ApiServices;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import net.minecraft.util.profiler.log.DebugSampleLog;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TestServer
extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int RESULT_STRING_LOG_INTERVAL = 20;
    private static final int TEST_POS_XZ_RANGE = 14999992;
    private static final ApiServices NONE_API_SERVICES = new ApiServices(null, ServicesKeySet.EMPTY, null, null);
    private final MultiValueDebugSampleLogImpl debugSampleLog = new MultiValueDebugSampleLogImpl(4);
    private List<GameTestBatch> batches = new ArrayList<GameTestBatch>();
    private final List<TestFunction> testFunctions;
    private final BlockPos pos;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private static final GameRules GAME_RULES = Util.make(new GameRules(), gameRules -> {
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, null);
    });
    private static final GeneratorOptions TEST_LEVEL = new GeneratorOptions(0L, false, false);
    @Nullable
    private TestSet testSet;

    public static TestServer create(Thread thread, LevelStorage.Session session, ResourcePackManager resourcePackManager, Collection<TestFunction> batches, BlockPos pos) {
        if (batches.isEmpty()) {
            throw new IllegalArgumentException("No test functions were given!");
        }
        resourcePackManager.scanPacks();
        DataConfiguration lv = new DataConfiguration(new DataPackSettings(new ArrayList<String>(resourcePackManager.getIds()), List.of()), FeatureFlags.FEATURE_MANAGER.getFeatureSet());
        LevelInfo lv2 = new LevelInfo("Test Level", GameMode.CREATIVE, false, Difficulty.NORMAL, true, GAME_RULES, lv);
        SaveLoading.DataPacks lv3 = new SaveLoading.DataPacks(resourcePackManager, lv, false, true);
        SaveLoading.ServerConfig lv4 = new SaveLoading.ServerConfig(lv3, CommandManager.RegistrationEnvironment.DEDICATED, 4);
        try {
            LOGGER.debug("Starting resource loading");
            Stopwatch stopwatch = Stopwatch.createStarted();
            SaveLoader lv5 = (SaveLoader)Util.waitAndApply(executor -> SaveLoading.load(lv4, context -> {
                Registry<DimensionOptions> lv = new SimpleRegistry<DimensionOptions>(RegistryKeys.DIMENSION, Lifecycle.stable()).freeze();
                DimensionOptionsRegistryHolder.DimensionsConfig lv2 = context.worldGenRegistryManager().get(RegistryKeys.WORLD_PRESET).entryOf(WorldPresets.FLAT).value().createDimensionsRegistryHolder().toConfig(lv);
                return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(lv2, TEST_LEVEL, lv2.specialWorldProperty(), lv2.getLifecycle()), lv2.toDynamicRegistryManager());
            }, SaveLoader::new, Util.getMainWorkerExecutor(), executor)).get();
            stopwatch.stop();
            LOGGER.debug("Finished resource loading after {} ms", (Object)stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new TestServer(thread, session, resourcePackManager, lv5, batches, pos);
        } catch (Exception exception) {
            LOGGER.warn("Failed to load vanilla datapack, bit oops", exception);
            System.exit(-1);
            throw new IllegalStateException();
        }
    }

    private TestServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Collection<TestFunction> testFunctions, BlockPos pos) {
        super(serverThread, session, dataPackManager, saveLoader, Proxy.NO_PROXY, Schemas.getFixer(), NONE_API_SERVICES, WorldGenerationProgressLogger::create);
        this.testFunctions = Lists.newArrayList(testFunctions);
        this.pos = pos;
    }

    @Override
    public boolean setupServer() {
        this.setPlayerManager(new PlayerManager(this, this, this.getCombinedDynamicRegistries(), this.saveHandler, 1){});
        this.loadWorld();
        ServerWorld lv = this.getOverworld();
        this.batches = Lists.newArrayList(Batches.createBatches(this.testFunctions, lv));
        lv.setSpawnPos(this.pos, 0.0f);
        int i = 20000000;
        lv.setWeather(20000000, 20000000, false, false);
        LOGGER.info("Started game test server");
        return true;
    }

    @Override
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
            LOGGER.info("========= {} GAME TESTS COMPLETE IN {} ======================", (Object)this.testSet.getTestCount(), (Object)this.stopwatch.stop());
            if (this.testSet.failed()) {
                LOGGER.info("{} required tests failed :(", (Object)this.testSet.getFailedRequiredTestCount());
                this.testSet.getRequiredTests().forEach(test -> LOGGER.info("   - {}", (Object)test.getTemplatePath()));
            } else {
                LOGGER.info("All {} required tests passed :)", (Object)this.testSet.getTestCount());
            }
            if (this.testSet.hasFailedOptionalTests()) {
                LOGGER.info("{} optional tests failed", (Object)this.testSet.getFailedOptionalTestCount());
                this.testSet.getOptionalTests().forEach(test -> LOGGER.info("   - {}", (Object)test.getTemplatePath()));
            }
            LOGGER.info("====================================================");
        }
    }

    @Override
    public DebugSampleLog getDebugSampleLog() {
        return this.debugSampleLog;
    }

    @Override
    public boolean shouldPushTickTimeLog() {
        return false;
    }

    @Override
    public void runTasksTillTickEnd() {
        this.runTasks();
    }

    @Override
    public SystemDetails addExtraSystemDetails(SystemDetails details) {
        details.addSection("Type", "Game test server");
        return details;
    }

    @Override
    public void exit() {
        super.exit();
        LOGGER.info("Game test server shutting down");
        System.exit(this.testSet.getFailedRequiredTestCount());
    }

    @Override
    public void setCrashReport(CrashReport report) {
        super.setCrashReport(report);
        LOGGER.error("Game test server crashed\n{}", (Object)report.method_60920(class_9813.MINECRAFT_CRASH_REPORT));
        System.exit(1);
    }

    private void runTestBatches(ServerWorld world) {
        BlockPos lv = new BlockPos(world.random.nextBetween(-14999992, 14999992), -59, world.random.nextBetween(-14999992, 14999992));
        TestRunContext lv2 = TestRunContext.Builder.of(this.batches, world).initialSpawner(new TestStructurePlacer(lv, 8)).build();
        List<GameTestState> collection = lv2.getStates();
        this.testSet = new TestSet(collection);
        LOGGER.info("{} tests are now running at position {}!", (Object)this.testSet.getTestCount(), (Object)lv.toShortString());
        this.stopwatch.reset();
        this.stopwatch.start();
        lv2.start();
    }

    private boolean isTesting() {
        return this.testSet != null;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public int getOpPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionPermissionLevel() {
        return 4;
    }

    @Override
    public boolean shouldBroadcastRconToOps() {
        return false;
    }

    @Override
    public boolean isDedicated() {
        return false;
    }

    @Override
    public int getRateLimit() {
        return 0;
    }

    @Override
    public boolean isUsingNativeTransport() {
        return false;
    }

    @Override
    public boolean areCommandBlocksEnabled() {
        return true;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public boolean isHost(GameProfile profile) {
        return false;
    }
}

