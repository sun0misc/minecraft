package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.UserApiService.UserFlag;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.GlTimer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.gui.WorldGenerationProgressTracker;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.OutOfMemoryScreen;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsPeriodicCheckers;
import net.minecraft.client.realms.util.Realms32BitWarningChecker;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.ReporterEnvironment;
import net.minecraft.client.resource.DefaultClientResourcePackProvider;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.PeriodicNotificationManager;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.resource.ServerResourcePackProvider;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.search.IdentifierSearchProvider;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.client.search.TextSearchProvider;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.PaintingManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.util.Bans;
import net.minecraft.client.util.ClientSamplerSource;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.telemetry.TelemetryManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.QueueingWorldGenerationProgressListener;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.sound.MusicSound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.KeybindTranslations;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.MetricsData;
import net.minecraft.util.ModStatus;
import net.minecraft.util.Nullables;
import net.minecraft.util.PathUtil;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.TickDurationMonitor;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import net.minecraft.util.ZipCompressor;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.DebugRecorder;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.DummyRecorder;
import net.minecraft.util.profiler.EmptyProfileResult;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerTiming;
import net.minecraft.util.profiler.RecordDumper;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.util.profiler.TickTimeTracker;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class MinecraftClient extends ReentrantThreadExecutor implements WindowEventHandler {
   static MinecraftClient instance;
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final boolean IS_SYSTEM_MAC;
   private static final int field_32145 = 10;
   public static final Identifier DEFAULT_FONT_ID;
   public static final Identifier UNICODE_FONT_ID;
   public static final Identifier ALT_TEXT_RENDERER_ID;
   private static final Identifier REGIONAL_COMPLIANCIES_ID;
   private static final CompletableFuture COMPLETED_UNIT_FUTURE;
   private static final Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
   public static final String GL_ERROR_DIALOGUE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
   private final Path resourcePackDir;
   private final PropertyMap sessionPropertyMap;
   private final TextureManager textureManager;
   private final DataFixer dataFixer;
   private final WindowProvider windowProvider;
   private final Window window;
   private final RenderTickCounter renderTickCounter = new RenderTickCounter(20.0F, 0L);
   private final BufferBuilderStorage bufferBuilders;
   public final WorldRenderer worldRenderer;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;
   public final ParticleManager particleManager;
   private final SearchManager searchManager = new SearchManager();
   private final Session session;
   public final TextRenderer textRenderer;
   public final TextRenderer advanceValidatingTextRenderer;
   public final GameRenderer gameRenderer;
   public final DebugRenderer debugRenderer;
   private final AtomicReference worldGenProgressTracker = new AtomicReference();
   public final InGameHud inGameHud;
   public final GameOptions options;
   private final HotbarStorage creativeHotbarStorage;
   public final Mouse mouse;
   public final Keyboard keyboard;
   private GuiNavigationType navigationType;
   public final File runDirectory;
   private final String gameVersion;
   private final String versionType;
   private final Proxy networkProxy;
   private final LevelStorage levelStorage;
   public final MetricsData metricsData;
   private final boolean is64Bit;
   private final boolean isDemo;
   private final boolean multiplayerEnabled;
   private final boolean onlineChatEnabled;
   private final ReloadableResourceManagerImpl resourceManager;
   private final DefaultResourcePack defaultResourcePack;
   private final ServerResourcePackProvider serverResourcePackProvider;
   private final ResourcePackManager resourcePackManager;
   private final LanguageManager languageManager;
   private final BlockColors blockColors;
   private final ItemColors itemColors;
   private final Framebuffer framebuffer;
   private final SoundManager soundManager;
   private final MusicTracker musicTracker;
   private final FontManager fontManager;
   private final SplashTextResourceSupplier splashTextLoader;
   private final VideoWarningManager videoWarningManager;
   private final PeriodicNotificationManager regionalComplianciesManager;
   private final YggdrasilAuthenticationService authenticationService;
   private final MinecraftSessionService sessionService;
   private final SignatureVerifier servicesSignatureVerifier;
   private final UserApiService userApiService;
   private final PlayerSkinProvider skinProvider;
   private final BakedModelManager bakedModelManager;
   private final BlockRenderManager blockRenderManager;
   private final PaintingManager paintingManager;
   private final StatusEffectSpriteManager statusEffectSpriteManager;
   private final ToastManager toastManager;
   private final TutorialManager tutorialManager;
   private final SocialInteractionsManager socialInteractionsManager;
   private final EntityModelLoader entityModelLoader;
   private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
   private final TelemetryManager telemetryManager;
   private final ProfileKeys profileKeys;
   private final RealmsPeriodicCheckers realmsPeriodicCheckers;
   private final QuickPlayLogger quickPlayLogger;
   @Nullable
   public ClientPlayerInteractionManager interactionManager;
   @Nullable
   public ClientWorld world;
   @Nullable
   public ClientPlayerEntity player;
   @Nullable
   private IntegratedServer server;
   @Nullable
   private ClientConnection integratedServerConnection;
   private boolean integratedServerRunning;
   @Nullable
   public Entity cameraEntity;
   @Nullable
   public Entity targetedEntity;
   @Nullable
   public HitResult crosshairTarget;
   private int itemUseCooldown;
   protected int attackCooldown;
   private volatile boolean paused;
   private float pausedTickDelta;
   private long lastMetricsSampleTime;
   private long nextDebugInfoUpdateTime;
   private int fpsCounter;
   public boolean skipGameRender;
   @Nullable
   public Screen currentScreen;
   @Nullable
   private Overlay overlay;
   private boolean connectedToRealms;
   private Thread thread;
   private volatile boolean running;
   @Nullable
   private Supplier crashReportSupplier;
   private static int currentFps;
   public String fpsDebugString;
   private long renderTime;
   public boolean wireFrame;
   public boolean debugChunkInfo;
   public boolean debugChunkOcclusion;
   public boolean chunkCullingEnabled;
   private boolean windowFocused;
   private final Queue renderTaskQueue;
   @Nullable
   private CompletableFuture resourceReloadFuture;
   @Nullable
   private TutorialToast socialInteractionsToast;
   private Profiler profiler;
   private int trackingTick;
   private final TickTimeTracker tickTimeTracker;
   @Nullable
   private ProfileResult tickProfilerResult;
   private Recorder recorder;
   private final ResourceReloadLogger resourceReloadLogger;
   private long metricsSampleDuration;
   private double gpuUtilizationPercentage;
   @Nullable
   private GlTimer.Query currentGlTimerQuery;
   private final Realms32BitWarningChecker realms32BitWarningChecker;
   private final NarratorManager narratorManager;
   private final MessageHandler messageHandler;
   private AbuseReportContext abuseReportContext;
   private String openProfilerSection;

   public MinecraftClient(RunArgs args) {
      super("Client");
      this.navigationType = GuiNavigationType.NONE;
      this.metricsData = new MetricsData();
      this.regionalComplianciesManager = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES_ID, MinecraftClient::isCountrySetTo);
      this.lastMetricsSampleTime = Util.getMeasuringTimeNano();
      this.fpsDebugString = "";
      this.chunkCullingEnabled = true;
      this.renderTaskQueue = Queues.newConcurrentLinkedQueue();
      this.profiler = DummyProfiler.INSTANCE;
      this.tickTimeTracker = new TickTimeTracker(Util.nanoTimeSupplier, () -> {
         return this.trackingTick;
      });
      this.recorder = DummyRecorder.INSTANCE;
      this.resourceReloadLogger = new ResourceReloadLogger();
      this.openProfilerSection = "root";
      instance = this;
      this.runDirectory = args.directories.runDir;
      File file = args.directories.assetDir;
      this.resourcePackDir = args.directories.resourcePackDir.toPath();
      this.gameVersion = args.game.version;
      this.versionType = args.game.versionType;
      this.sessionPropertyMap = args.network.profileProperties;
      DefaultClientResourcePackProvider lv = new DefaultClientResourcePackProvider(args.directories.getAssetDir());
      this.serverResourcePackProvider = new ServerResourcePackProvider(new File(this.runDirectory, "server-resource-packs"));
      ResourcePackProvider lv2 = new FileResourcePackProvider(this.resourcePackDir, ResourceType.CLIENT_RESOURCES, ResourcePackSource.NONE);
      this.resourcePackManager = new ResourcePackManager(new ResourcePackProvider[]{lv, this.serverResourcePackProvider, lv2});
      this.defaultResourcePack = lv.getResourcePack();
      this.networkProxy = args.network.netProxy;
      this.authenticationService = new YggdrasilAuthenticationService(this.networkProxy);
      this.sessionService = this.authenticationService.createMinecraftSessionService();
      this.userApiService = this.createUserApiService(this.authenticationService, args);
      this.servicesSignatureVerifier = SignatureVerifier.create(this.authenticationService.getServicesKey());
      this.session = args.network.session;
      LOGGER.info("Setting user: {}", this.session.getUsername());
      LOGGER.debug("(Session ID is {})", this.session.getSessionId());
      this.isDemo = args.game.demo;
      this.multiplayerEnabled = !args.game.multiplayerDisabled;
      this.onlineChatEnabled = !args.game.onlineChatDisabled;
      this.is64Bit = checkIs64Bit();
      this.server = null;
      KeybindTranslations.setFactory(KeyBinding::getLocalizedName);
      this.dataFixer = Schemas.getFixer();
      this.toastManager = new ToastManager(this);
      this.thread = Thread.currentThread();
      this.options = new GameOptions(this, this.runDirectory);
      RenderSystem.setShaderGlintAlpha((Double)this.options.getGlintStrength().getValue());
      this.running = true;
      this.tutorialManager = new TutorialManager(this, this.options);
      this.creativeHotbarStorage = new HotbarStorage(this.runDirectory, this.dataFixer);
      LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
      WindowSettings lv3;
      if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
         lv3 = new WindowSettings(this.options.overrideWidth, this.options.overrideHeight, args.windowSettings.fullscreenWidth, args.windowSettings.fullscreenHeight, args.windowSettings.fullscreen);
      } else {
         lv3 = args.windowSettings;
      }

      Util.nanoTimeSupplier = RenderSystem.initBackendSystem();
      this.windowProvider = new WindowProvider(this);
      this.window = this.windowProvider.createWindow(lv3, this.options.fullscreenResolution, this.getWindowTitle());
      this.onWindowFocusChanged(true);

      try {
         if (IS_SYSTEM_MAC) {
            MacWindowUtil.setApplicationIconImage(this.getDefaultResourceSupplier("icons", "minecraft.icns"));
         } else {
            this.window.setIcon(this.getDefaultResourceSupplier("icons", "icon_16x16.png"), this.getDefaultResourceSupplier("icons", "icon_32x32.png"));
         }
      } catch (IOException var10) {
         LOGGER.error("Couldn't set icon", var10);
      }

      this.window.setFramerateLimit((Integer)this.options.getMaxFps().getValue());
      this.mouse = new Mouse(this);
      this.mouse.setup(this.window.getHandle());
      this.keyboard = new Keyboard(this);
      this.keyboard.setup(this.window.getHandle());
      RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
      this.framebuffer = new WindowFramebuffer(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
      this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.framebuffer.clear(IS_SYSTEM_MAC);
      this.resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES);
      this.resourcePackManager.scanPacks();
      this.options.addResourcePackProfilesToManager(this.resourcePackManager);
      this.languageManager = new LanguageManager(this.options.language);
      this.resourceManager.registerReloader(this.languageManager);
      this.textureManager = new TextureManager(this.resourceManager);
      this.resourceManager.registerReloader(this.textureManager);
      this.skinProvider = new PlayerSkinProvider(this.textureManager, new File(file, "skins"), this.sessionService);
      this.levelStorage = new LevelStorage(this.runDirectory.toPath().resolve("saves"), this.runDirectory.toPath().resolve("backups"), this.dataFixer);
      this.soundManager = new SoundManager(this.options);
      this.resourceManager.registerReloader(this.soundManager);
      this.splashTextLoader = new SplashTextResourceSupplier(this.session);
      this.resourceManager.registerReloader(this.splashTextLoader);
      this.musicTracker = new MusicTracker(this);
      this.fontManager = new FontManager(this.textureManager);
      this.textRenderer = this.fontManager.createTextRenderer();
      this.advanceValidatingTextRenderer = this.fontManager.createAdvanceValidatingTextRenderer();
      this.resourceManager.registerReloader(this.fontManager.getResourceReloadListener());
      this.initFont(this.forcesUnicodeFont());
      this.resourceManager.registerReloader(new GrassColormapResourceSupplier());
      this.resourceManager.registerReloader(new FoliageColormapResourceSupplier());
      this.window.setPhase("Startup");
      RenderSystem.setupDefaultState(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
      this.window.setPhase("Post startup");
      this.blockColors = BlockColors.create();
      this.itemColors = ItemColors.create(this.blockColors);
      this.bakedModelManager = new BakedModelManager(this.textureManager, this.blockColors, (Integer)this.options.getMipmapLevels().getValue());
      this.resourceManager.registerReloader(this.bakedModelManager);
      this.entityModelLoader = new EntityModelLoader();
      this.resourceManager.registerReloader(this.entityModelLoader);
      this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.textRenderer, this.entityModelLoader, this::getBlockRenderManager, this::getItemRenderer, this::getEntityRenderDispatcher);
      this.resourceManager.registerReloader(this.blockEntityRenderDispatcher);
      BuiltinModelItemRenderer lv4 = new BuiltinModelItemRenderer(this.blockEntityRenderDispatcher, this.entityModelLoader);
      this.resourceManager.registerReloader(lv4);
      this.itemRenderer = new ItemRenderer(this, this.textureManager, this.bakedModelManager, this.itemColors, lv4);
      this.resourceManager.registerReloader(this.itemRenderer);
      this.bufferBuilders = new BufferBuilderStorage();
      this.socialInteractionsManager = new SocialInteractionsManager(this, this.userApiService);
      this.blockRenderManager = new BlockRenderManager(this.bakedModelManager.getBlockModels(), lv4, this.blockColors);
      this.resourceManager.registerReloader(this.blockRenderManager);
      this.entityRenderDispatcher = new EntityRenderDispatcher(this, this.textureManager, this.itemRenderer, this.blockRenderManager, this.textRenderer, this.options, this.entityModelLoader);
      this.resourceManager.registerReloader(this.entityRenderDispatcher);
      this.gameRenderer = new GameRenderer(this, this.entityRenderDispatcher.getHeldItemRenderer(), this.resourceManager, this.bufferBuilders);
      this.resourceManager.registerReloader(this.gameRenderer.createProgramReloader());
      this.worldRenderer = new WorldRenderer(this, this.entityRenderDispatcher, this.blockEntityRenderDispatcher, this.bufferBuilders);
      this.resourceManager.registerReloader(this.worldRenderer);
      this.initializeSearchProviders();
      this.resourceManager.registerReloader(this.searchManager);
      this.particleManager = new ParticleManager(this.world, this.textureManager);
      this.resourceManager.registerReloader(this.particleManager);
      this.paintingManager = new PaintingManager(this.textureManager);
      this.resourceManager.registerReloader(this.paintingManager);
      this.statusEffectSpriteManager = new StatusEffectSpriteManager(this.textureManager);
      this.resourceManager.registerReloader(this.statusEffectSpriteManager);
      this.videoWarningManager = new VideoWarningManager();
      this.resourceManager.registerReloader(this.videoWarningManager);
      this.resourceManager.registerReloader(this.regionalComplianciesManager);
      this.inGameHud = new InGameHud(this, this.itemRenderer);
      this.debugRenderer = new DebugRenderer(this);
      RealmsClient lv5 = RealmsClient.createRealmsClient(this);
      this.realmsPeriodicCheckers = new RealmsPeriodicCheckers(lv5);
      RenderSystem.setErrorCallback(this::handleGlErrorByDisableVsync);
      if (this.framebuffer.textureWidth == this.window.getFramebufferWidth() && this.framebuffer.textureHeight == this.window.getFramebufferHeight()) {
         if ((Boolean)this.options.getFullscreen().getValue() && !this.window.isFullscreen()) {
            this.window.toggleFullscreen();
            this.options.getFullscreen().setValue(this.window.isFullscreen());
         }
      } else {
         int var10002 = this.window.getFramebufferWidth();
         StringBuilder stringBuilder = new StringBuilder("Recovering from unsupported resolution (" + var10002 + "x" + this.window.getFramebufferHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).");
         if (GlDebug.isDebugMessageEnabled()) {
            stringBuilder.append("\n\nReported GL debug messages:\n").append(String.join("\n", GlDebug.collectDebugMessages()));
         }

         this.window.setWindowedSize(this.framebuffer.textureWidth, this.framebuffer.textureHeight);
         TinyFileDialogs.tinyfd_messageBox("Minecraft", stringBuilder.toString(), "ok", "error", false);
      }

      this.window.setVsync((Boolean)this.options.getEnableVsync().getValue());
      this.window.setRawMouseMotion((Boolean)this.options.getRawMouseInput().getValue());
      this.window.logOnGlError();
      this.onResolutionChanged();
      this.gameRenderer.preloadPrograms(this.defaultResourcePack.getFactory());
      this.telemetryManager = new TelemetryManager(this, this.userApiService, this.session);
      this.profileKeys = ProfileKeys.create(this.userApiService, this.session, this.runDirectory.toPath());
      this.realms32BitWarningChecker = new Realms32BitWarningChecker(this);
      this.narratorManager = new NarratorManager(this);
      this.messageHandler = new MessageHandler(this);
      this.messageHandler.setChatDelay((Double)this.options.getChatDelay().getValue());
      this.abuseReportContext = AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), this.userApiService);
      SplashOverlay.init(this);
      List list = this.resourcePackManager.createResourcePacks();
      this.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.INITIAL, list);
      ResourceReload lv6 = this.resourceManager.reload(Util.getMainWorkerExecutor(), this, COMPLETED_UNIT_FUTURE, list);
      this.setOverlay(new SplashOverlay(this, lv6, (throwable) -> {
         Util.ifPresentOrElse(throwable, this::handleResourceReloadException, () -> {
            if (SharedConstants.isDevelopment) {
               this.checkGameData();
            }

            this.resourceReloadLogger.finish();
         });
      }, false));
      this.quickPlayLogger = QuickPlayLogger.create(args.quickPlay.path());
      if (this.isMultiplayerBanned()) {
         this.setScreen(Bans.createBanScreen((confirmed) -> {
            if (confirmed) {
               Util.getOperatingSystem().open("https://aka.ms/mcjavamoderation");
            }

            this.onInitFinished(lv5, lv6, args.quickPlay);
         }, this.getMultiplayerBanDetails()));
      } else {
         this.onInitFinished(lv5, lv6, args.quickPlay);
      }

   }

   private void onInitFinished(RealmsClient realms, ResourceReload reload, RunArgs.QuickPlay quickPlay) {
      if (quickPlay.isEnabled()) {
         QuickPlay.startQuickPlay(this, quickPlay, reload, realms);
      } else if (this.options.onboardAccessibility) {
         this.setScreen(new AccessibilityOnboardingScreen(this.options));
      } else {
         this.setScreen(new TitleScreen(true));
      }

   }

   private InputSupplier getDefaultResourceSupplier(String... segments) throws IOException {
      InputSupplier lv = this.defaultResourcePack.openRoot(segments);
      if (lv == null) {
         throw new FileNotFoundException(String.join("/", segments));
      } else {
         return lv;
      }
   }

   private static boolean isCountrySetTo(Object country) {
      try {
         return Locale.getDefault().getISO3Country().equals(country);
      } catch (MissingResourceException var2) {
         return false;
      }
   }

   public void updateWindowTitle() {
      this.window.setTitle(this.getWindowTitle());
   }

   private String getWindowTitle() {
      StringBuilder stringBuilder = new StringBuilder("Minecraft");
      if (getModStatus().isModded()) {
         stringBuilder.append("*");
      }

      stringBuilder.append(" ");
      stringBuilder.append(SharedConstants.getGameVersion().getName());
      ClientPlayNetworkHandler lv = this.getNetworkHandler();
      if (lv != null && lv.getConnection().isOpen()) {
         stringBuilder.append(" - ");
         if (this.server != null && !this.server.isRemote()) {
            stringBuilder.append(I18n.translate("title.singleplayer"));
         } else if (this.isConnectedToRealms()) {
            stringBuilder.append(I18n.translate("title.multiplayer.realms"));
         } else if (this.server == null && (this.getCurrentServerEntry() == null || !this.getCurrentServerEntry().isLocal())) {
            stringBuilder.append(I18n.translate("title.multiplayer.other"));
         } else {
            stringBuilder.append(I18n.translate("title.multiplayer.lan"));
         }
      }

      return stringBuilder.toString();
   }

   private UserApiService createUserApiService(YggdrasilAuthenticationService authService, RunArgs runArgs) {
      try {
         return authService.createUserApiService(runArgs.network.session.getAccessToken());
      } catch (AuthenticationException var4) {
         LOGGER.error("Failed to verify authentication", var4);
         return UserApiService.OFFLINE;
      }
   }

   public static ModStatus getModStatus() {
      return ModStatus.check("vanilla", ClientBrandRetriever::getClientModName, "Client", MinecraftClient.class);
   }

   private void handleResourceReloadException(Throwable throwable) {
      if (this.resourcePackManager.getEnabledNames().size() > 1) {
         this.onResourceReloadFailure(throwable, (Text)null);
      } else {
         Util.throwUnchecked(throwable);
      }

   }

   public void onResourceReloadFailure(Throwable exception, @Nullable Text resourceName) {
      LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", exception);
      this.resourceReloadLogger.recover(exception);
      this.resourcePackManager.setEnabledProfiles(Collections.emptyList());
      this.options.resourcePacks.clear();
      this.options.incompatibleResourcePacks.clear();
      this.options.write();
      this.reloadResources(true).thenRun(() -> {
         this.showResourceReloadFailureToast(resourceName);
      });
   }

   private void onForcedResourceReloadFailure() {
      this.setOverlay((Overlay)null);
      if (this.world != null) {
         this.world.disconnect();
         this.disconnect();
      }

      this.setScreen(new TitleScreen());
      this.showResourceReloadFailureToast((Text)null);
   }

   private void showResourceReloadFailureToast(@Nullable Text description) {
      ToastManager lv = this.getToastManager();
      SystemToast.show(lv, SystemToast.Type.PACK_LOAD_FAILURE, Text.translatable("resourcePack.load_fail"), description);
   }

   public void run() {
      this.thread = Thread.currentThread();
      if (Runtime.getRuntime().availableProcessors() > 4) {
         this.thread.setPriority(10);
      }

      try {
         boolean bl = false;

         while(this.running) {
            if (this.crashReportSupplier != null) {
               printCrashReport((CrashReport)this.crashReportSupplier.get());
               return;
            }

            try {
               TickDurationMonitor lv = TickDurationMonitor.create("Renderer");
               boolean bl2 = this.shouldMonitorTickDuration();
               this.profiler = this.startMonitor(bl2, lv);
               this.profiler.startTick();
               this.recorder.startTick();
               this.render(!bl);
               this.recorder.endTick();
               this.profiler.endTick();
               this.endMonitor(bl2, lv);
            } catch (OutOfMemoryError var4) {
               if (bl) {
                  throw var4;
               }

               this.cleanUpAfterCrash();
               this.setScreen(new OutOfMemoryScreen());
               System.gc();
               LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", var4);
               bl = true;
            }
         }
      } catch (CrashException var5) {
         this.addDetailsToCrashReport(var5.getReport());
         this.cleanUpAfterCrash();
         LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", var5);
         printCrashReport(var5.getReport());
      } catch (Throwable var6) {
         CrashReport lv3 = this.addDetailsToCrashReport(new CrashReport("Unexpected error", var6));
         LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", var6);
         this.cleanUpAfterCrash();
         printCrashReport(lv3);
      }

   }

   void initFont(boolean forcesUnicode) {
      this.fontManager.setIdOverrides(forcesUnicode ? ImmutableMap.of(DEFAULT_FONT_ID, UNICODE_FONT_ID) : ImmutableMap.of());
   }

   private void initializeSearchProviders() {
      this.searchManager.put(SearchManager.ITEM_TOOLTIP, (stacks) -> {
         return new TextSearchProvider((stack) -> {
            return stack.getTooltip((PlayerEntity)null, TooltipContext.Default.BASIC.withCreative()).stream().map((tooltip) -> {
               return Formatting.strip(tooltip.getString()).trim();
            }).filter((string) -> {
               return !string.isEmpty();
            });
         }, (stack) -> {
            return Stream.of(Registries.ITEM.getId(stack.getItem()));
         }, stacks);
      });
      this.searchManager.put(SearchManager.ITEM_TAG, (stacks) -> {
         return new IdentifierSearchProvider((stack) -> {
            return stack.streamTags().map(TagKey::id);
         }, stacks);
      });
      this.searchManager.put(SearchManager.RECIPE_OUTPUT, (resultCollections) -> {
         return new TextSearchProvider((resultCollection) -> {
            return resultCollection.getAllRecipes().stream().flatMap((recipe) -> {
               return recipe.getOutput(resultCollection.getRegistryManager()).getTooltip((PlayerEntity)null, TooltipContext.Default.BASIC).stream();
            }).map((text) -> {
               return Formatting.strip(text.getString()).trim();
            }).filter((text) -> {
               return !text.isEmpty();
            });
         }, (resultCollection) -> {
            return resultCollection.getAllRecipes().stream().map((recipe) -> {
               return Registries.ITEM.getId(recipe.getOutput(resultCollection.getRegistryManager()).getItem());
            });
         }, resultCollections);
      });
      ItemGroups.getSearchGroup().setSearchProviderReloader((stacks) -> {
         this.reloadSearchProvider(SearchManager.ITEM_TOOLTIP, stacks);
         this.reloadSearchProvider(SearchManager.ITEM_TAG, stacks);
      });
   }

   private void handleGlErrorByDisableVsync(int error, long description) {
      this.options.getEnableVsync().setValue(false);
      this.options.write();
   }

   private static boolean checkIs64Bit() {
      String[] strings = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
      String[] var1 = strings;
      int var2 = strings.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String string = var1[var3];
         String string2 = System.getProperty(string);
         if (string2 != null && string2.contains("64")) {
            return true;
         }
      }

      return false;
   }

   public Framebuffer getFramebuffer() {
      return this.framebuffer;
   }

   public String getGameVersion() {
      return this.gameVersion;
   }

   public String getVersionType() {
      return this.versionType;
   }

   public void setCrashReportSupplierAndAddDetails(CrashReport crashReport) {
      this.crashReportSupplier = () -> {
         return this.addDetailsToCrashReport(crashReport);
      };
   }

   public void setCrashReportSupplier(CrashReport crashReport) {
      this.crashReportSupplier = () -> {
         return crashReport;
      };
   }

   public static void printCrashReport(CrashReport report) {
      File file = new File(getInstance().runDirectory, "crash-reports");
      File file2 = new File(file, "crash-" + Util.getFormattedCurrentTime() + "-client.txt");
      Bootstrap.println(report.asString());
      if (report.getFile() != null) {
         Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + report.getFile());
         System.exit(-1);
      } else if (report.writeToFile(file2)) {
         Bootstrap.println("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
         System.exit(-1);
      } else {
         Bootstrap.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
         System.exit(-2);
      }

   }

   public boolean forcesUnicodeFont() {
      return (Boolean)this.options.getForceUnicodeFont().getValue();
   }

   public CompletableFuture reloadResources() {
      return this.reloadResources(false);
   }

   private CompletableFuture reloadResources(boolean force) {
      if (this.resourceReloadFuture != null) {
         return this.resourceReloadFuture;
      } else {
         CompletableFuture completableFuture = new CompletableFuture();
         if (!force && this.overlay instanceof SplashOverlay) {
            this.resourceReloadFuture = completableFuture;
            return completableFuture;
         } else {
            this.resourcePackManager.scanPacks();
            List list = this.resourcePackManager.createResourcePacks();
            if (!force) {
               this.resourceReloadLogger.reload(ResourceReloadLogger.ReloadReason.MANUAL, list);
            }

            this.setOverlay(new SplashOverlay(this, this.resourceManager.reload(Util.getMainWorkerExecutor(), this, COMPLETED_UNIT_FUTURE, list), (error) -> {
               Util.ifPresentOrElse(error, (throwable) -> {
                  if (force) {
                     this.onForcedResourceReloadFailure();
                  } else {
                     this.handleResourceReloadException(throwable);
                  }

               }, () -> {
                  this.worldRenderer.reload();
                  this.resourceReloadLogger.finish();
                  completableFuture.complete((Object)null);
               });
            }, true));
            return completableFuture;
         }
      }
   }

   private void checkGameData() {
      boolean bl = false;
      BlockModels lv = this.getBlockRenderManager().getModels();
      BakedModel lv2 = lv.getModelManager().getMissingModel();
      Iterator var4 = Registries.BLOCK.iterator();

      while(var4.hasNext()) {
         Block lv3 = (Block)var4.next();
         UnmodifiableIterator var6 = lv3.getStateManager().getStates().iterator();

         while(var6.hasNext()) {
            BlockState lv4 = (BlockState)var6.next();
            if (lv4.getRenderType() == BlockRenderType.MODEL) {
               BakedModel lv5 = lv.getModel(lv4);
               if (lv5 == lv2) {
                  LOGGER.debug("Missing model for: {}", lv4);
                  bl = true;
               }
            }
         }
      }

      Sprite lv6 = lv2.getParticleSprite();
      Iterator var11 = Registries.BLOCK.iterator();

      while(var11.hasNext()) {
         Block lv7 = (Block)var11.next();
         UnmodifiableIterator var14 = lv7.getStateManager().getStates().iterator();

         while(var14.hasNext()) {
            BlockState lv8 = (BlockState)var14.next();
            Sprite lv9 = lv.getModelParticleSprite(lv8);
            if (!lv8.isAir() && lv9 == lv6) {
               LOGGER.debug("Missing particle icon for: {}", lv8);
               bl = true;
            }
         }
      }

      var11 = Registries.ITEM.iterator();

      while(var11.hasNext()) {
         Item lv10 = (Item)var11.next();
         ItemStack lv11 = lv10.getDefaultStack();
         String string = lv11.getTranslationKey();
         String string2 = Text.translatable(string).getString();
         if (string2.toLowerCase(Locale.ROOT).equals(lv10.getTranslationKey())) {
            LOGGER.debug("Missing translation for: {} {} {}", new Object[]{lv11, string, lv10});
         }
      }

      bl |= HandledScreens.isMissingScreens();
      bl |= EntityRenderers.isMissingRendererFactories();
      if (bl) {
         throw new IllegalStateException("Your game data is foobar, fix the errors above!");
      }
   }

   public LevelStorage getLevelStorage() {
      return this.levelStorage;
   }

   private void openChatScreen(String text) {
      ChatRestriction lv = this.getChatRestriction();
      if (!lv.allowsChat(this.isInSingleplayer())) {
         if (this.inGameHud.shouldShowChatDisabledScreen()) {
            this.inGameHud.setCanShowChatDisabledScreen(false);
            this.setScreen(new ConfirmLinkScreen((confirmed) -> {
               if (confirmed) {
                  Util.getOperatingSystem().open("https://aka.ms/JavaAccountSettings");
               }

               this.setScreen((Screen)null);
            }, MinecraftClient.ChatRestriction.MORE_INFO_TEXT, "https://aka.ms/JavaAccountSettings", true));
         } else {
            Text lv2 = lv.getDescription();
            this.inGameHud.setOverlayMessage(lv2, false);
            this.narratorManager.narrate(lv2);
            this.inGameHud.setCanShowChatDisabledScreen(lv == MinecraftClient.ChatRestriction.DISABLED_BY_PROFILE);
         }
      } else {
         this.setScreen(new ChatScreen(text));
      }

   }

   public void setScreen(@Nullable Screen screen) {
      if (SharedConstants.isDevelopment && Thread.currentThread() != this.thread) {
         LOGGER.error("setScreen called from non-game thread");
      }

      if (this.currentScreen != null) {
         this.currentScreen.removed();
      }

      if (screen == null && this.world == null) {
         screen = new TitleScreen();
      } else if (screen == null && this.player.isDead()) {
         if (this.player.showsDeathScreen()) {
            screen = new DeathScreen((Text)null, this.world.getLevelProperties().isHardcore());
         } else {
            this.player.requestRespawn();
         }
      }

      this.currentScreen = (Screen)screen;
      if (this.currentScreen != null) {
         this.currentScreen.onDisplayed();
      }

      BufferRenderer.reset();
      if (screen != null) {
         this.mouse.unlockCursor();
         KeyBinding.unpressAll();
         ((Screen)screen).init(this, this.window.getScaledWidth(), this.window.getScaledHeight());
         this.skipGameRender = false;
      } else {
         this.soundManager.resumeAll();
         this.mouse.lockCursor();
      }

      this.updateWindowTitle();
   }

   public void setOverlay(@Nullable Overlay overlay) {
      this.overlay = overlay;
   }

   public void stop() {
      try {
         LOGGER.info("Stopping!");

         try {
            this.narratorManager.destroy();
         } catch (Throwable var7) {
         }

         try {
            if (this.world != null) {
               this.world.disconnect();
            }

            this.disconnect();
         } catch (Throwable var6) {
         }

         if (this.currentScreen != null) {
            this.currentScreen.removed();
         }

         this.close();
      } finally {
         Util.nanoTimeSupplier = System::nanoTime;
         if (this.crashReportSupplier == null) {
            System.exit(0);
         }

      }

   }

   public void close() {
      if (this.currentGlTimerQuery != null) {
         this.currentGlTimerQuery.close();
      }

      try {
         this.telemetryManager.close();
         this.regionalComplianciesManager.close();
         this.bakedModelManager.close();
         this.fontManager.close();
         this.gameRenderer.close();
         this.worldRenderer.close();
         this.soundManager.close();
         this.particleManager.clearAtlas();
         this.statusEffectSpriteManager.close();
         this.paintingManager.close();
         this.textureManager.close();
         this.resourceManager.close();
         Util.shutdownExecutors();
      } catch (Throwable var5) {
         LOGGER.error("Shutdown failure!", var5);
         throw var5;
      } finally {
         this.windowProvider.close();
         this.window.close();
      }

   }

   private void render(boolean tick) {
      this.window.setPhase("Pre render");
      long l = Util.getMeasuringTimeNano();
      if (this.window.shouldClose()) {
         this.scheduleStop();
      }

      if (this.resourceReloadFuture != null && !(this.overlay instanceof SplashOverlay)) {
         CompletableFuture completableFuture = this.resourceReloadFuture;
         this.resourceReloadFuture = null;
         this.reloadResources().thenRun(() -> {
            completableFuture.complete((Object)null);
         });
      }

      Runnable runnable;
      while((runnable = (Runnable)this.renderTaskQueue.poll()) != null) {
         runnable.run();
      }

      if (tick) {
         int i = this.renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs());
         this.profiler.push("scheduledExecutables");
         this.runTasks();
         this.profiler.pop();
         this.profiler.push("tick");

         for(int j = 0; j < Math.min(10, i); ++j) {
            this.profiler.visit("clientTick");
            this.tick();
         }

         this.profiler.pop();
      }

      this.mouse.updateMouse();
      this.window.setPhase("Render");
      this.profiler.push("sound");
      this.soundManager.updateListenerPosition(this.gameRenderer.getCamera());
      this.profiler.pop();
      this.profiler.push("render");
      long m = Util.getMeasuringTimeNano();
      boolean bl2;
      if (!this.options.debugEnabled && !this.recorder.isActive()) {
         bl2 = false;
         this.gpuUtilizationPercentage = 0.0;
      } else {
         bl2 = this.currentGlTimerQuery == null || this.currentGlTimerQuery.isResultAvailable();
         if (bl2) {
            GlTimer.getInstance().ifPresent(GlTimer::beginProfile);
         }
      }

      RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, IS_SYSTEM_MAC);
      this.framebuffer.beginWrite(true);
      BackgroundRenderer.clearFog();
      this.profiler.push("display");
      RenderSystem.enableCull();
      this.profiler.pop();
      if (!this.skipGameRender) {
         this.profiler.swap("gameRenderer");
         this.gameRenderer.render(this.paused ? this.pausedTickDelta : this.renderTickCounter.tickDelta, l, tick);
         this.profiler.pop();
      }

      if (this.tickProfilerResult != null) {
         this.profiler.push("fpsPie");
         this.drawProfilerResults(new MatrixStack(), this.tickProfilerResult);
         this.profiler.pop();
      }

      this.profiler.push("blit");
      this.framebuffer.endWrite();
      this.framebuffer.draw(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
      this.renderTime = Util.getMeasuringTimeNano() - m;
      if (bl2) {
         GlTimer.getInstance().ifPresent((glTimer) -> {
            this.currentGlTimerQuery = glTimer.endProfile();
         });
      }

      this.profiler.swap("updateDisplay");
      this.window.swapBuffers();
      int k = this.getFramerateLimit();
      if (k < 260) {
         RenderSystem.limitDisplayFPS(k);
      }

      this.profiler.swap("yield");
      Thread.yield();
      this.profiler.pop();
      this.window.setPhase("Post render");
      ++this.fpsCounter;
      boolean bl3 = this.isIntegratedServerRunning() && (this.currentScreen != null && this.currentScreen.shouldPause() || this.overlay != null && this.overlay.pausesGame()) && !this.server.isRemote();
      if (this.paused != bl3) {
         if (this.paused) {
            this.pausedTickDelta = this.renderTickCounter.tickDelta;
         } else {
            this.renderTickCounter.tickDelta = this.pausedTickDelta;
         }

         this.paused = bl3;
      }

      long n = Util.getMeasuringTimeNano();
      long o = n - this.lastMetricsSampleTime;
      if (bl2) {
         this.metricsSampleDuration = o;
      }

      this.metricsData.pushSample(o);
      this.lastMetricsSampleTime = n;
      this.profiler.push("fpsUpdate");
      if (this.currentGlTimerQuery != null && this.currentGlTimerQuery.isResultAvailable()) {
         this.gpuUtilizationPercentage = (double)this.currentGlTimerQuery.queryResult() * 100.0 / (double)this.metricsSampleDuration;
      }

      while(Util.getMeasuringTimeMs() >= this.nextDebugInfoUpdateTime + 1000L) {
         String string;
         if (this.gpuUtilizationPercentage > 0.0) {
            String var10000 = this.gpuUtilizationPercentage > 100.0 ? Formatting.RED + "100%" : Math.round(this.gpuUtilizationPercentage) + "%";
            string = " GPU: " + var10000;
         } else {
            string = "";
         }

         currentFps = this.fpsCounter;
         this.fpsDebugString = String.format(Locale.ROOT, "%d fps T: %s%s%s%s B: %d%s", currentFps, k == 260 ? "inf" : k, (Boolean)this.options.getEnableVsync().getValue() ? " vsync" : "", this.options.getGraphicsMode().getValue(), this.options.getCloudRenderMode().getValue() == CloudRenderMode.OFF ? "" : (this.options.getCloudRenderMode().getValue() == CloudRenderMode.FAST ? " fast-clouds" : " fancy-clouds"), this.options.getBiomeBlendRadius().getValue(), string);
         this.nextDebugInfoUpdateTime += 1000L;
         this.fpsCounter = 0;
      }

      this.profiler.pop();
   }

   private boolean shouldMonitorTickDuration() {
      return this.options.debugEnabled && this.options.debugProfilerEnabled && !this.options.hudHidden;
   }

   private Profiler startMonitor(boolean active, @Nullable TickDurationMonitor monitor) {
      if (!active) {
         this.tickTimeTracker.disable();
         if (!this.recorder.isActive() && monitor == null) {
            return DummyProfiler.INSTANCE;
         }
      }

      Object lv;
      if (active) {
         if (!this.tickTimeTracker.isActive()) {
            this.trackingTick = 0;
            this.tickTimeTracker.enable();
         }

         ++this.trackingTick;
         lv = this.tickTimeTracker.getProfiler();
      } else {
         lv = DummyProfiler.INSTANCE;
      }

      if (this.recorder.isActive()) {
         lv = Profiler.union((Profiler)lv, this.recorder.getProfiler());
      }

      return TickDurationMonitor.tickProfiler((Profiler)lv, monitor);
   }

   private void endMonitor(boolean active, @Nullable TickDurationMonitor monitor) {
      if (monitor != null) {
         monitor.endTick();
      }

      if (active) {
         this.tickProfilerResult = this.tickTimeTracker.getResult();
      } else {
         this.tickProfilerResult = null;
      }

      this.profiler = this.tickTimeTracker.getProfiler();
   }

   public void onResolutionChanged() {
      int i = this.window.calculateScaleFactor((Integer)this.options.getGuiScale().getValue(), this.forcesUnicodeFont());
      this.window.setScaleFactor((double)i);
      if (this.currentScreen != null) {
         this.currentScreen.resize(this, this.window.getScaledWidth(), this.window.getScaledHeight());
      }

      Framebuffer lv = this.getFramebuffer();
      lv.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), IS_SYSTEM_MAC);
      this.gameRenderer.onResized(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
      this.mouse.onResolutionChanged();
   }

   public void onCursorEnterChanged() {
      this.mouse.setResolutionChanged();
   }

   public int getCurrentFps() {
      return currentFps;
   }

   public long getRenderTime() {
      return this.renderTime;
   }

   private int getFramerateLimit() {
      return this.world != null || this.currentScreen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
   }

   public void cleanUpAfterCrash() {
      try {
         CrashMemoryReserve.releaseMemory();
         this.worldRenderer.cleanUp();
      } catch (Throwable var3) {
      }

      try {
         System.gc();
         if (this.integratedServerRunning && this.server != null) {
            this.server.stop(true);
         }

         this.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
      } catch (Throwable var2) {
      }

      System.gc();
   }

   public boolean toggleDebugProfiler(Consumer chatMessageSender) {
      if (this.recorder.isActive()) {
         this.stopRecorder();
         return false;
      } else {
         Consumer consumer2 = (result) -> {
            if (result != EmptyProfileResult.INSTANCE) {
               int i = result.getTickSpan();
               double d = (double)result.getTimeSpan() / (double)TimeHelper.SECOND_IN_NANOS;
               this.execute(() -> {
                  chatMessageSender.accept(Text.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), i, String.format(Locale.ROOT, "%.2f", (double)i / d)));
               });
            }
         };
         Consumer consumer3 = (path) -> {
            Text lv = Text.literal(path.toString()).formatted(Formatting.UNDERLINE).styled((style) -> {
               return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toFile().getParent()));
            });
            this.execute(() -> {
               chatMessageSender.accept(Text.translatable("debug.profiling.stop", lv));
            });
         };
         SystemDetails lv = addSystemDetailsToCrashReport(new SystemDetails(), this, this.languageManager, this.gameVersion, this.options);
         Consumer consumer4 = (files) -> {
            Path path = this.saveProfilingResult(lv, files);
            consumer3.accept(path);
         };
         Consumer consumer5;
         if (this.server == null) {
            consumer5 = (path) -> {
               consumer4.accept(ImmutableList.of(path));
            };
         } else {
            this.server.addSystemDetails(lv);
            CompletableFuture completableFuture = new CompletableFuture();
            CompletableFuture completableFuture2 = new CompletableFuture();
            CompletableFuture.allOf(completableFuture, completableFuture2).thenRunAsync(() -> {
               consumer4.accept(ImmutableList.of((Path)completableFuture.join(), (Path)completableFuture2.join()));
            }, Util.getIoWorkerExecutor());
            IntegratedServer var10000 = this.server;
            Consumer var10001 = (result) -> {
            };
            Objects.requireNonNull(completableFuture2);
            var10000.setupRecorder(var10001, completableFuture2::complete);
            Objects.requireNonNull(completableFuture);
            consumer5 = completableFuture::complete;
         }

         this.recorder = DebugRecorder.of(new ClientSamplerSource(Util.nanoTimeSupplier, this.worldRenderer), Util.nanoTimeSupplier, Util.getIoWorkerExecutor(), new RecordDumper("client"), (result) -> {
            this.recorder = DummyRecorder.INSTANCE;
            consumer2.accept(result);
         }, consumer5);
         return true;
      }
   }

   private void stopRecorder() {
      this.recorder.stop();
      if (this.server != null) {
         this.server.stopRecorder();
      }

   }

   private void forceStopRecorder() {
      this.recorder.forceStop();
      if (this.server != null) {
         this.server.forceStopRecorder();
      }

   }

   private Path saveProfilingResult(SystemDetails details, List files) {
      String string;
      if (this.isInSingleplayer()) {
         string = this.getServer().getSaveProperties().getLevelName();
      } else {
         ServerInfo lv = this.getCurrentServerEntry();
         string = lv != null ? lv.name : "unknown";
      }

      Path path;
      try {
         String string2 = String.format(Locale.ROOT, "%s-%s-%s", Util.getFormattedCurrentTime(), string, SharedConstants.getGameVersion().getId());
         String string3 = PathUtil.getNextUniqueName(RecordDumper.DEBUG_PROFILING_DIRECTORY, string2, ".zip");
         path = RecordDumper.DEBUG_PROFILING_DIRECTORY.resolve(string3);
      } catch (IOException var23) {
         throw new UncheckedIOException(var23);
      }

      boolean var18 = false;

      try {
         var18 = true;
         ZipCompressor lv2 = new ZipCompressor(path);

         try {
            lv2.write(Paths.get("system.txt"), details.collect());
            lv2.write(Paths.get("client").resolve(this.options.getOptionsFile().getName()), this.options.collectProfiledOptions());
            Objects.requireNonNull(lv2);
            files.forEach(lv2::copyAll);
         } catch (Throwable var22) {
            try {
               lv2.close();
            } catch (Throwable var19) {
               var22.addSuppressed(var19);
            }

            throw var22;
         }

         lv2.close();
         var18 = false;
      } finally {
         if (var18) {
            Iterator var9 = files.iterator();

            while(var9.hasNext()) {
               Path path3 = (Path)var9.next();

               try {
                  FileUtils.forceDelete(path3.toFile());
               } catch (IOException var20) {
                  LOGGER.warn("Failed to delete temporary profiling result {}", path3, var20);
               }
            }

         }
      }

      Iterator var27 = files.iterator();

      while(var27.hasNext()) {
         Path path2 = (Path)var27.next();

         try {
            FileUtils.forceDelete(path2.toFile());
         } catch (IOException var21) {
            LOGGER.warn("Failed to delete temporary profiling result {}", path2, var21);
         }
      }

      return path;
   }

   public void handleProfilerKeyPress(int digit) {
      if (this.tickProfilerResult != null) {
         List list = this.tickProfilerResult.getTimings(this.openProfilerSection);
         if (!list.isEmpty()) {
            ProfilerTiming lv = (ProfilerTiming)list.remove(0);
            if (digit == 0) {
               if (!lv.name.isEmpty()) {
                  int j = this.openProfilerSection.lastIndexOf(30);
                  if (j >= 0) {
                     this.openProfilerSection = this.openProfilerSection.substring(0, j);
                  }
               }
            } else {
               --digit;
               if (digit < list.size() && !"unspecified".equals(((ProfilerTiming)list.get(digit)).name)) {
                  if (!this.openProfilerSection.isEmpty()) {
                     this.openProfilerSection = this.openProfilerSection + "\u001e";
                  }

                  String var10001 = this.openProfilerSection;
                  this.openProfilerSection = var10001 + ((ProfilerTiming)list.get(digit)).name;
               }
            }

         }
      }
   }

   private void drawProfilerResults(MatrixStack matrices, ProfileResult profileResult) {
      List list = profileResult.getTimings(this.openProfilerSection);
      ProfilerTiming lv = (ProfilerTiming)list.remove(0);
      RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, IS_SYSTEM_MAC);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)this.window.getFramebufferWidth(), (float)this.window.getFramebufferHeight(), 0.0F, 1000.0F, 3000.0F);
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
      MatrixStack lv2 = RenderSystem.getModelViewStack();
      lv2.push();
      lv2.loadIdentity();
      lv2.translate(0.0F, 0.0F, -2000.0F);
      RenderSystem.applyModelViewMatrix();
      RenderSystem.lineWidth(1.0F);
      Tessellator lv3 = Tessellator.getInstance();
      BufferBuilder lv4 = lv3.getBuffer();
      int i = true;
      int j = this.window.getFramebufferWidth() - 160 - 10;
      int k = this.window.getFramebufferHeight() - 320;
      RenderSystem.enableBlend();
      lv4.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      lv4.vertex((double)((float)j - 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).next();
      lv4.vertex((double)((float)j - 176.0F), (double)(k + 320), 0.0).color(200, 0, 0, 0).next();
      lv4.vertex((double)((float)j + 176.0F), (double)(k + 320), 0.0).color(200, 0, 0, 0).next();
      lv4.vertex((double)((float)j + 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).next();
      lv3.draw();
      RenderSystem.disableBlend();
      double d = 0.0;

      ProfilerTiming lv5;
      int m;
      for(Iterator var14 = list.iterator(); var14.hasNext(); d += lv5.parentSectionUsagePercentage) {
         lv5 = (ProfilerTiming)var14.next();
         int l = MathHelper.floor(lv5.parentSectionUsagePercentage / 4.0) + 1;
         lv4.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
         m = lv5.getColor();
         int n = m >> 16 & 255;
         int o = m >> 8 & 255;
         int p = m & 255;
         lv4.vertex((double)j, (double)k, 0.0).color(n, o, p, 255).next();

         int q;
         float f;
         float g;
         float h;
         for(q = l; q >= 0; --q) {
            f = (float)((d + lv5.parentSectionUsagePercentage * (double)q / (double)l) * 6.2831854820251465 / 100.0);
            g = MathHelper.sin(f) * 160.0F;
            h = MathHelper.cos(f) * 160.0F * 0.5F;
            lv4.vertex((double)((float)j + g), (double)((float)k - h), 0.0).color(n, o, p, 255).next();
         }

         lv3.draw();
         lv4.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

         for(q = l; q >= 0; --q) {
            f = (float)((d + lv5.parentSectionUsagePercentage * (double)q / (double)l) * 6.2831854820251465 / 100.0);
            g = MathHelper.sin(f) * 160.0F;
            h = MathHelper.cos(f) * 160.0F * 0.5F;
            if (!(h > 0.0F)) {
               lv4.vertex((double)((float)j + g), (double)((float)k - h), 0.0).color(n >> 1, o >> 1, p >> 1, 255).next();
               lv4.vertex((double)((float)j + g), (double)((float)k - h + 10.0F), 0.0).color(n >> 1, o >> 1, p >> 1, 255).next();
            }
         }

         lv3.draw();
      }

      DecimalFormat decimalFormat = new DecimalFormat("##0.00");
      decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
      String string = ProfileResult.getHumanReadableName(lv.name);
      String string2 = "";
      if (!"unspecified".equals(string)) {
         string2 = string2 + "[0] ";
      }

      if (string.isEmpty()) {
         string2 = string2 + "ROOT ";
      } else {
         string2 = string2 + string + " ";
      }

      m = 16777215;
      this.textRenderer.drawWithShadow(matrices, string2, (float)(j - 160), (float)(k - 80 - 16), 16777215);
      String var10000 = decimalFormat.format(lv.totalUsagePercentage);
      string2 = var10000 + "%";
      this.textRenderer.drawWithShadow(matrices, string2, (float)(j + 160 - this.textRenderer.getWidth(string2)), (float)(k - 80 - 16), 16777215);

      for(int r = 0; r < list.size(); ++r) {
         ProfilerTiming lv6 = (ProfilerTiming)list.get(r);
         StringBuilder stringBuilder = new StringBuilder();
         if ("unspecified".equals(lv6.name)) {
            stringBuilder.append("[?] ");
         } else {
            stringBuilder.append("[").append(r + 1).append("] ");
         }

         String string3 = stringBuilder.append(lv6.name).toString();
         this.textRenderer.drawWithShadow(matrices, string3, (float)(j - 160), (float)(k + 80 + r * 8 + 20), lv6.getColor());
         var10000 = decimalFormat.format(lv6.parentSectionUsagePercentage);
         string3 = var10000 + "%";
         this.textRenderer.drawWithShadow(matrices, string3, (float)(j + 160 - 50 - this.textRenderer.getWidth(string3)), (float)(k + 80 + r * 8 + 20), lv6.getColor());
         var10000 = decimalFormat.format(lv6.totalUsagePercentage);
         string3 = var10000 + "%";
         this.textRenderer.drawWithShadow(matrices, string3, (float)(j + 160 - this.textRenderer.getWidth(string3)), (float)(k + 80 + r * 8 + 20), lv6.getColor());
      }

      lv2.pop();
      RenderSystem.applyModelViewMatrix();
   }

   public void scheduleStop() {
      this.running = false;
   }

   public boolean isRunning() {
      return this.running;
   }

   public void openPauseMenu(boolean pause) {
      if (this.currentScreen == null) {
         boolean bl2 = this.isIntegratedServerRunning() && !this.server.isRemote();
         if (bl2) {
            this.setScreen(new GameMenuScreen(!pause));
            this.soundManager.pauseAll();
         } else {
            this.setScreen(new GameMenuScreen(true));
         }

      }
   }

   private void handleBlockBreaking(boolean breaking) {
      if (!breaking) {
         this.attackCooldown = 0;
      }

      if (this.attackCooldown <= 0 && !this.player.isUsingItem()) {
         if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult lv = (BlockHitResult)this.crosshairTarget;
            BlockPos lv2 = lv.getBlockPos();
            if (!this.world.getBlockState(lv2).isAir()) {
               Direction lv3 = lv.getSide();
               if (this.interactionManager.updateBlockBreakingProgress(lv2, lv3)) {
                  this.particleManager.addBlockBreakingParticles(lv2, lv3);
                  this.player.swingHand(Hand.MAIN_HAND);
               }
            }

         } else {
            this.interactionManager.cancelBlockBreaking();
         }
      }
   }

   private boolean doAttack() {
      if (this.attackCooldown > 0) {
         return false;
      } else if (this.crosshairTarget == null) {
         LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
         if (this.interactionManager.hasLimitedAttackSpeed()) {
            this.attackCooldown = 10;
         }

         return false;
      } else if (this.player.isRiding()) {
         return false;
      } else {
         ItemStack lv = this.player.getStackInHand(Hand.MAIN_HAND);
         if (!lv.isItemEnabled(this.world.getEnabledFeatures())) {
            return false;
         } else {
            boolean bl = false;
            switch (this.crosshairTarget.getType()) {
               case ENTITY:
                  this.interactionManager.attackEntity(this.player, ((EntityHitResult)this.crosshairTarget).getEntity());
                  break;
               case BLOCK:
                  BlockHitResult lv2 = (BlockHitResult)this.crosshairTarget;
                  BlockPos lv3 = lv2.getBlockPos();
                  if (!this.world.getBlockState(lv3).isAir()) {
                     this.interactionManager.attackBlock(lv3, lv2.getSide());
                     if (this.world.getBlockState(lv3).isAir()) {
                        bl = true;
                     }
                     break;
                  }
               case MISS:
                  if (this.interactionManager.hasLimitedAttackSpeed()) {
                     this.attackCooldown = 10;
                  }

                  this.player.resetLastAttackedTicks();
            }

            this.player.swingHand(Hand.MAIN_HAND);
            return bl;
         }
      }
   }

   private void doItemUse() {
      if (!this.interactionManager.isBreakingBlock()) {
         this.itemUseCooldown = 4;
         if (!this.player.isRiding()) {
            if (this.crosshairTarget == null) {
               LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
            }

            Hand[] var1 = Hand.values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
               Hand lv = var1[var3];
               ItemStack lv2 = this.player.getStackInHand(lv);
               if (!lv2.isItemEnabled(this.world.getEnabledFeatures())) {
                  return;
               }

               if (this.crosshairTarget != null) {
                  switch (this.crosshairTarget.getType()) {
                     case ENTITY:
                        EntityHitResult lv3 = (EntityHitResult)this.crosshairTarget;
                        Entity lv4 = lv3.getEntity();
                        if (!this.world.getWorldBorder().contains(lv4.getBlockPos())) {
                           return;
                        }

                        ActionResult lv5 = this.interactionManager.interactEntityAtLocation(this.player, lv4, lv3, lv);
                        if (!lv5.isAccepted()) {
                           lv5 = this.interactionManager.interactEntity(this.player, lv4, lv);
                        }

                        if (lv5.isAccepted()) {
                           if (lv5.shouldSwingHand()) {
                              this.player.swingHand(lv);
                           }

                           return;
                        }
                        break;
                     case BLOCK:
                        BlockHitResult lv6 = (BlockHitResult)this.crosshairTarget;
                        int i = lv2.getCount();
                        ActionResult lv7 = this.interactionManager.interactBlock(this.player, lv, lv6);
                        if (lv7.isAccepted()) {
                           if (lv7.shouldSwingHand()) {
                              this.player.swingHand(lv);
                              if (!lv2.isEmpty() && (lv2.getCount() != i || this.interactionManager.hasCreativeInventory())) {
                                 this.gameRenderer.firstPersonRenderer.resetEquipProgress(lv);
                              }
                           }

                           return;
                        }

                        if (lv7 == ActionResult.FAIL) {
                           return;
                        }
                  }
               }

               if (!lv2.isEmpty()) {
                  ActionResult lv8 = this.interactionManager.interactItem(this.player, lv);
                  if (lv8.isAccepted()) {
                     if (lv8.shouldSwingHand()) {
                        this.player.swingHand(lv);
                     }

                     this.gameRenderer.firstPersonRenderer.resetEquipProgress(lv);
                     return;
                  }
               }
            }

         }
      }
   }

   public MusicTracker getMusicTracker() {
      return this.musicTracker;
   }

   public void tick() {
      if (this.itemUseCooldown > 0) {
         --this.itemUseCooldown;
      }

      this.profiler.push("gui");
      this.messageHandler.processDelayedMessages();
      this.inGameHud.tick(this.paused);
      this.profiler.pop();
      this.gameRenderer.updateTargetedEntity(1.0F);
      this.tutorialManager.tick(this.world, this.crosshairTarget);
      this.profiler.push("gameMode");
      if (!this.paused && this.world != null) {
         this.interactionManager.tick();
      }

      this.profiler.swap("textures");
      if (this.world != null) {
         this.textureManager.tick();
      }

      if (this.currentScreen == null && this.player != null) {
         if (this.player.isDead() && !(this.currentScreen instanceof DeathScreen)) {
            this.setScreen((Screen)null);
         } else if (this.player.isSleeping() && this.world != null) {
            this.setScreen(new SleepingChatScreen());
         }
      } else {
         Screen var2 = this.currentScreen;
         if (var2 instanceof SleepingChatScreen) {
            SleepingChatScreen lv = (SleepingChatScreen)var2;
            if (!this.player.isSleeping()) {
               lv.closeChatIfEmpty();
            }
         }
      }

      if (this.currentScreen != null) {
         this.attackCooldown = 10000;
      }

      if (this.currentScreen != null) {
         Screen.wrapScreenError(() -> {
            this.currentScreen.tick();
         }, "Ticking screen", this.currentScreen.getClass().getCanonicalName());
      }

      if (!this.options.debugEnabled) {
         this.inGameHud.resetDebugHudChunk();
      }

      if (this.overlay == null && (this.currentScreen == null || this.currentScreen.passEvents)) {
         this.profiler.swap("Keybindings");
         this.handleInputEvents();
         if (this.attackCooldown > 0) {
            --this.attackCooldown;
         }
      }

      if (this.world != null) {
         this.profiler.swap("gameRenderer");
         if (!this.paused) {
            this.gameRenderer.tick();
         }

         this.profiler.swap("levelRenderer");
         if (!this.paused) {
            this.worldRenderer.tick();
         }

         this.profiler.swap("level");
         if (!this.paused) {
            if (this.world.getLightningTicksLeft() > 0) {
               this.world.setLightningTicksLeft(this.world.getLightningTicksLeft() - 1);
            }

            this.world.tickEntities();
         }
      } else if (this.gameRenderer.getPostProcessor() != null) {
         this.gameRenderer.disablePostProcessor();
      }

      if (!this.paused) {
         this.musicTracker.tick();
      }

      this.soundManager.tick(this.paused);
      if (this.world != null) {
         if (!this.paused) {
            if (!this.options.joinedFirstServer && this.isConnectedToServer()) {
               Text lv2 = Text.translatable("tutorial.socialInteractions.title");
               Text lv3 = Text.translatable("tutorial.socialInteractions.description", TutorialManager.keyToText("socialInteractions"));
               this.socialInteractionsToast = new TutorialToast(TutorialToast.Type.SOCIAL_INTERACTIONS, lv2, lv3, true);
               this.tutorialManager.add(this.socialInteractionsToast, 160);
               this.options.joinedFirstServer = true;
               this.options.write();
            }

            this.tutorialManager.tick();

            try {
               this.world.tick(() -> {
                  return true;
               });
            } catch (Throwable var4) {
               CrashReport lv4 = CrashReport.create(var4, "Exception in world tick");
               if (this.world == null) {
                  CrashReportSection lv5 = lv4.addElement("Affected level");
                  lv5.add("Problem", (Object)"Level is null!");
               } else {
                  this.world.addDetailsToCrashReport(lv4);
               }

               throw new CrashException(lv4);
            }
         }

         this.profiler.swap("animateTick");
         if (!this.paused && this.world != null) {
            this.world.doRandomBlockDisplayTicks(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
         }

         this.profiler.swap("particles");
         if (!this.paused) {
            this.particleManager.tick();
         }
      } else if (this.integratedServerConnection != null) {
         this.profiler.swap("pendingConnection");
         this.integratedServerConnection.tick();
      }

      this.profiler.swap("keyboard");
      this.keyboard.pollDebugCrash();
      this.profiler.pop();
   }

   private boolean isConnectedToServer() {
      return !this.integratedServerRunning || this.server != null && this.server.isRemote();
   }

   private void handleInputEvents() {
      for(; this.options.togglePerspectiveKey.wasPressed(); this.worldRenderer.scheduleTerrainUpdate()) {
         Perspective lv = this.options.getPerspective();
         this.options.setPerspective(this.options.getPerspective().next());
         if (lv.isFirstPerson() != this.options.getPerspective().isFirstPerson()) {
            this.gameRenderer.onCameraEntitySet(this.options.getPerspective().isFirstPerson() ? this.getCameraEntity() : null);
         }
      }

      while(this.options.smoothCameraKey.wasPressed()) {
         this.options.smoothCameraEnabled = !this.options.smoothCameraEnabled;
      }

      for(int i = 0; i < 9; ++i) {
         boolean bl = this.options.saveToolbarActivatorKey.isPressed();
         boolean bl2 = this.options.loadToolbarActivatorKey.isPressed();
         if (this.options.hotbarKeys[i].wasPressed()) {
            if (this.player.isSpectator()) {
               this.inGameHud.getSpectatorHud().selectSlot(i);
            } else if (!this.player.isCreative() || this.currentScreen != null || !bl2 && !bl) {
               this.player.getInventory().selectedSlot = i;
            } else {
               CreativeInventoryScreen.onHotbarKeyPress(this, i, bl2, bl);
            }
         }
      }

      while(this.options.socialInteractionsKey.wasPressed()) {
         if (!this.isConnectedToServer()) {
            this.player.sendMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
            this.narratorManager.narrate(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
         } else {
            if (this.socialInteractionsToast != null) {
               this.tutorialManager.remove(this.socialInteractionsToast);
               this.socialInteractionsToast = null;
            }

            this.setScreen(new SocialInteractionsScreen());
         }
      }

      while(this.options.inventoryKey.wasPressed()) {
         if (this.interactionManager.hasRidingInventory()) {
            this.player.openRidingInventory();
         } else {
            this.tutorialManager.onInventoryOpened();
            this.setScreen(new InventoryScreen(this.player));
         }
      }

      while(this.options.advancementsKey.wasPressed()) {
         this.setScreen(new AdvancementsScreen(this.player.networkHandler.getAdvancementHandler()));
      }

      while(this.options.swapHandsKey.wasPressed()) {
         if (!this.player.isSpectator()) {
            this.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
         }
      }

      while(this.options.dropKey.wasPressed()) {
         if (!this.player.isSpectator() && this.player.dropSelectedItem(Screen.hasControlDown())) {
            this.player.swingHand(Hand.MAIN_HAND);
         }
      }

      while(this.options.chatKey.wasPressed()) {
         this.openChatScreen("");
      }

      if (this.currentScreen == null && this.overlay == null && this.options.commandKey.wasPressed()) {
         this.openChatScreen("/");
      }

      boolean bl3 = false;
      if (this.player.isUsingItem()) {
         if (!this.options.useKey.isPressed()) {
            this.interactionManager.stopUsingItem(this.player);
         }

         label117:
         while(true) {
            if (!this.options.attackKey.wasPressed()) {
               while(this.options.useKey.wasPressed()) {
               }

               while(true) {
                  if (this.options.pickItemKey.wasPressed()) {
                     continue;
                  }
                  break label117;
               }
            }
         }
      } else {
         while(this.options.attackKey.wasPressed()) {
            bl3 |= this.doAttack();
         }

         while(this.options.useKey.wasPressed()) {
            this.doItemUse();
         }

         while(this.options.pickItemKey.wasPressed()) {
            this.doItemPick();
         }
      }

      if (this.options.useKey.isPressed() && this.itemUseCooldown == 0 && !this.player.isUsingItem()) {
         this.doItemUse();
      }

      this.handleBlockBreaking(this.currentScreen == null && !bl3 && this.options.attackKey.isPressed() && this.mouse.isCursorLocked());
   }

   public TelemetryManager getTelemetryManager() {
      return this.telemetryManager;
   }

   public double getGpuUtilizationPercentage() {
      return this.gpuUtilizationPercentage;
   }

   public ProfileKeys getProfileKeys() {
      return this.profileKeys;
   }

   public IntegratedServerLoader createIntegratedServerLoader() {
      return new IntegratedServerLoader(this, this.levelStorage);
   }

   public void startIntegratedServer(String levelName, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld) {
      this.disconnect();
      this.worldGenProgressTracker.set((Object)null);
      Instant instant = Instant.now();

      try {
         session.backupLevelDataFile(saveLoader.combinedDynamicRegistries().getCombinedRegistryManager(), saveLoader.saveProperties());
         ApiServices lv = ApiServices.create(this.authenticationService, this.runDirectory);
         lv.userCache().setExecutor(this);
         SkullBlockEntity.setServices(lv, this);
         UserCache.setUseRemote(false);
         this.server = (IntegratedServer)MinecraftServer.startServer((thread) -> {
            return new IntegratedServer(thread, this, session, dataPackManager, saveLoader, lv, (spawnChunkRadius) -> {
               WorldGenerationProgressTracker lv = new WorldGenerationProgressTracker(spawnChunkRadius + 0);
               this.worldGenProgressTracker.set(lv);
               Queue var10001 = this.renderTaskQueue;
               Objects.requireNonNull(var10001);
               return QueueingWorldGenerationProgressListener.create(lv, var10001::add);
            });
         });
         this.integratedServerRunning = true;
         this.ensureAbuseReportContext(ReporterEnvironment.ofIntegratedServer());
         this.quickPlayLogger.setWorld(QuickPlayLogger.WorldType.SINGLEPLAYER, levelName, saveLoader.saveProperties().getLevelName());
      } catch (Throwable var12) {
         CrashReport lv2 = CrashReport.create(var12, "Starting integrated server");
         CrashReportSection lv3 = lv2.addElement("Starting integrated server");
         lv3.add("Level ID", (Object)levelName);
         lv3.add("Level Name", () -> {
            return saveLoader.saveProperties().getLevelName();
         });
         throw new CrashException(lv2);
      }

      while(this.worldGenProgressTracker.get() == null) {
         Thread.yield();
      }

      LevelLoadingScreen lv4 = new LevelLoadingScreen((WorldGenerationProgressTracker)this.worldGenProgressTracker.get());
      this.setScreen(lv4);
      this.profiler.push("waitForServer");

      while(!this.server.isLoading()) {
         lv4.tick();
         this.render(false);

         try {
            Thread.sleep(16L);
         } catch (InterruptedException var11) {
         }

         if (this.crashReportSupplier != null) {
            printCrashReport((CrashReport)this.crashReportSupplier.get());
            return;
         }
      }

      this.profiler.pop();
      Duration duration = Duration.between(instant, Instant.now());
      SocketAddress socketAddress = this.server.getNetworkIo().bindLocal();
      ClientConnection lv5 = ClientConnection.connectLocal(socketAddress);
      lv5.setPacketListener(new ClientLoginNetworkHandler(lv5, this, (ServerInfo)null, (Screen)null, newWorld, duration, (status) -> {
      }));
      lv5.send(new HandshakeC2SPacket(socketAddress.toString(), 0, NetworkState.LOGIN));
      lv5.send(new LoginHelloC2SPacket(this.getSession().getUsername(), Optional.ofNullable(this.getSession().getUuidOrNull())));
      this.integratedServerConnection = lv5;
   }

   public void joinWorld(ClientWorld world) {
      ProgressScreen lv = new ProgressScreen(true);
      lv.setTitle(Text.translatable("connect.joining"));
      this.reset(lv);
      this.world = world;
      this.setWorld(world);
      if (!this.integratedServerRunning) {
         ApiServices lv2 = ApiServices.create(this.authenticationService, this.runDirectory);
         lv2.userCache().setExecutor(this);
         SkullBlockEntity.setServices(lv2, this);
         UserCache.setUseRemote(false);
      }

   }

   public void disconnect() {
      this.disconnect(new ProgressScreen(true));
   }

   public void disconnect(Screen screen) {
      ClientPlayNetworkHandler lv = this.getNetworkHandler();
      if (lv != null) {
         this.cancelTasks();
         lv.clearWorld();
      }

      this.socialInteractionsManager.unloadBlockList();
      if (this.recorder.isActive()) {
         this.forceStopRecorder();
      }

      IntegratedServer lv2 = this.server;
      this.server = null;
      this.gameRenderer.reset();
      this.interactionManager = null;
      this.narratorManager.clear();
      this.reset(screen);
      if (this.world != null) {
         if (lv2 != null) {
            this.profiler.push("waitForServer");

            while(!lv2.isStopping()) {
               this.render(false);
            }

            this.profiler.pop();
         }

         this.serverResourcePackProvider.clear();
         this.inGameHud.clear();
         this.integratedServerRunning = false;
      }

      this.world = null;
      this.setWorld((ClientWorld)null);
      this.player = null;
      SkullBlockEntity.clearServices();
   }

   private void reset(Screen screen) {
      this.profiler.push("forcedTick");
      this.soundManager.stopAll();
      this.cameraEntity = null;
      this.integratedServerConnection = null;
      this.setScreen(screen);
      this.render(false);
      this.profiler.pop();
   }

   public void setScreenAndRender(Screen screen) {
      this.profiler.push("forcedTick");
      this.setScreen(screen);
      this.render(false);
      this.profiler.pop();
   }

   private void setWorld(@Nullable ClientWorld world) {
      this.worldRenderer.setWorld(world);
      this.particleManager.setWorld(world);
      this.blockEntityRenderDispatcher.setWorld(world);
      this.updateWindowTitle();
   }

   public boolean isOptionalTelemetryEnabled() {
      return this.isOptionalTelemetryEnabledByApi() && (Boolean)this.options.getTelemetryOptInExtra().getValue();
   }

   public boolean isOptionalTelemetryEnabledByApi() {
      return this.isTelemetryEnabledByApi() && this.userApiService.properties().flag(UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
   }

   public boolean isTelemetryEnabledByApi() {
      return this.userApiService.properties().flag(UserFlag.TELEMETRY_ENABLED);
   }

   public boolean isMultiplayerEnabled() {
      return this.multiplayerEnabled && this.userApiService.properties().flag(UserFlag.SERVERS_ALLOWED) && this.getMultiplayerBanDetails() == null;
   }

   public boolean isRealmsEnabled() {
      return this.userApiService.properties().flag(UserFlag.REALMS_ALLOWED) && this.getMultiplayerBanDetails() == null;
   }

   public boolean isMultiplayerBanned() {
      return this.getMultiplayerBanDetails() != null;
   }

   @Nullable
   public BanDetails getMultiplayerBanDetails() {
      return (BanDetails)this.userApiService.properties().bannedScopes().get("MULTIPLAYER");
   }

   public boolean shouldBlockMessages(UUID sender) {
      if (this.getChatRestriction().allowsChat(false)) {
         return this.socialInteractionsManager.isPlayerMuted(sender);
      } else {
         return (this.player == null || !sender.equals(this.player.getUuid())) && !sender.equals(Util.NIL_UUID);
      }
   }

   public ChatRestriction getChatRestriction() {
      if (this.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN) {
         return MinecraftClient.ChatRestriction.DISABLED_BY_OPTIONS;
      } else if (!this.onlineChatEnabled) {
         return MinecraftClient.ChatRestriction.DISABLED_BY_LAUNCHER;
      } else {
         return !this.userApiService.properties().flag(UserFlag.CHAT_ALLOWED) ? MinecraftClient.ChatRestriction.DISABLED_BY_PROFILE : MinecraftClient.ChatRestriction.ENABLED;
      }
   }

   public final boolean isDemo() {
      return this.isDemo;
   }

   @Nullable
   public ClientPlayNetworkHandler getNetworkHandler() {
      return this.player == null ? null : this.player.networkHandler;
   }

   public static boolean isHudEnabled() {
      return !instance.options.hudHidden;
   }

   public static boolean isFancyGraphicsOrBetter() {
      return ((GraphicsMode)instance.options.getGraphicsMode().getValue()).getId() >= GraphicsMode.FANCY.getId();
   }

   public static boolean isFabulousGraphicsOrBetter() {
      return !instance.gameRenderer.isRenderingPanorama() && ((GraphicsMode)instance.options.getGraphicsMode().getValue()).getId() >= GraphicsMode.FABULOUS.getId();
   }

   public static boolean isAmbientOcclusionEnabled() {
      return (Boolean)instance.options.getAo().getValue();
   }

   private void doItemPick() {
      if (this.crosshairTarget != null && this.crosshairTarget.getType() != HitResult.Type.MISS) {
         boolean bl = this.player.getAbilities().creativeMode;
         BlockEntity lv = null;
         HitResult.Type lv2 = this.crosshairTarget.getType();
         ItemStack lv6;
         if (lv2 == HitResult.Type.BLOCK) {
            BlockPos lv3 = ((BlockHitResult)this.crosshairTarget).getBlockPos();
            BlockState lv4 = this.world.getBlockState(lv3);
            if (lv4.isAir()) {
               return;
            }

            Block lv5 = lv4.getBlock();
            lv6 = lv5.getPickStack(this.world, lv3, lv4);
            if (lv6.isEmpty()) {
               return;
            }

            if (bl && Screen.hasControlDown() && lv4.hasBlockEntity()) {
               lv = this.world.getBlockEntity(lv3);
            }
         } else {
            if (lv2 != HitResult.Type.ENTITY || !bl) {
               return;
            }

            Entity lv7 = ((EntityHitResult)this.crosshairTarget).getEntity();
            lv6 = lv7.getPickBlockStack();
            if (lv6 == null) {
               return;
            }
         }

         if (lv6.isEmpty()) {
            String string = "";
            if (lv2 == HitResult.Type.BLOCK) {
               string = Registries.BLOCK.getId(this.world.getBlockState(((BlockHitResult)this.crosshairTarget).getBlockPos()).getBlock()).toString();
            } else if (lv2 == HitResult.Type.ENTITY) {
               string = Registries.ENTITY_TYPE.getId(((EntityHitResult)this.crosshairTarget).getEntity().getType()).toString();
            }

            LOGGER.warn("Picking on: [{}] {} gave null item", lv2, string);
         } else {
            PlayerInventory lv8 = this.player.getInventory();
            if (lv != null) {
               this.addBlockEntityNbt(lv6, lv);
            }

            int i = lv8.getSlotWithStack(lv6);
            if (bl) {
               lv8.addPickBlock(lv6);
               this.interactionManager.clickCreativeStack(this.player.getStackInHand(Hand.MAIN_HAND), 36 + lv8.selectedSlot);
            } else if (i != -1) {
               if (PlayerInventory.isValidHotbarIndex(i)) {
                  lv8.selectedSlot = i;
               } else {
                  this.interactionManager.pickFromInventory(i);
               }
            }

         }
      }
   }

   private void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
      NbtCompound lv = blockEntity.createNbtWithIdentifyingData();
      BlockItem.setBlockEntityNbt(stack, blockEntity.getType(), lv);
      NbtCompound lv2;
      if (stack.getItem() instanceof SkullItem && lv.contains("SkullOwner")) {
         lv2 = lv.getCompound("SkullOwner");
         NbtCompound lv3 = stack.getOrCreateNbt();
         lv3.put("SkullOwner", lv2);
         NbtCompound lv4 = lv3.getCompound("BlockEntityTag");
         lv4.remove("SkullOwner");
         lv4.remove("x");
         lv4.remove("y");
         lv4.remove("z");
      } else {
         lv2 = new NbtCompound();
         NbtList lv5 = new NbtList();
         lv5.add(NbtString.of("\"(+NBT)\""));
         lv2.put("Lore", lv5);
         stack.setSubNbt("display", lv2);
      }
   }

   public CrashReport addDetailsToCrashReport(CrashReport report) {
      SystemDetails lv = report.getSystemDetailsSection();
      addSystemDetailsToCrashReport(lv, this, this.languageManager, this.gameVersion, this.options);
      if (this.world != null) {
         this.world.addDetailsToCrashReport(report);
      }

      if (this.server != null) {
         this.server.addSystemDetails(lv);
      }

      this.resourceReloadLogger.addReloadSection(report);
      return report;
   }

   public static void addSystemDetailsToCrashReport(@Nullable MinecraftClient client, @Nullable LanguageManager languageManager, String version, @Nullable GameOptions options, CrashReport report) {
      SystemDetails lv = report.getSystemDetailsSection();
      addSystemDetailsToCrashReport(lv, client, languageManager, version, options);
   }

   private static SystemDetails addSystemDetailsToCrashReport(SystemDetails systemDetails, @Nullable MinecraftClient client, @Nullable LanguageManager languageManager, String version, GameOptions options) {
      systemDetails.addSection("Launched Version", () -> {
         return version;
      });
      systemDetails.addSection("Backend library", RenderSystem::getBackendDescription);
      systemDetails.addSection("Backend API", RenderSystem::getApiDescription);
      systemDetails.addSection("Window size", () -> {
         return client != null ? client.window.getFramebufferWidth() + "x" + client.window.getFramebufferHeight() : "<not initialized>";
      });
      systemDetails.addSection("GL Caps", RenderSystem::getCapsString);
      systemDetails.addSection("GL debug messages", () -> {
         return GlDebug.isDebugMessageEnabled() ? String.join("\n", GlDebug.collectDebugMessages()) : "<disabled>";
      });
      systemDetails.addSection("Using VBOs", () -> {
         return "Yes";
      });
      systemDetails.addSection("Is Modded", () -> {
         return getModStatus().getMessage();
      });
      systemDetails.addSection("Type", "Client (map_client.txt)");
      if (options != null) {
         if (instance != null) {
            String string2 = instance.getVideoWarningManager().getWarningsAsString();
            if (string2 != null) {
               systemDetails.addSection("GPU Warnings", string2);
            }
         }

         systemDetails.addSection("Graphics mode", ((GraphicsMode)options.getGraphicsMode().getValue()).toString());
         systemDetails.addSection("Resource Packs", () -> {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator var2 = options.resourcePacks.iterator();

            while(var2.hasNext()) {
               String string = (String)var2.next();
               if (stringBuilder.length() > 0) {
                  stringBuilder.append(", ");
               }

               stringBuilder.append(string);
               if (options.incompatibleResourcePacks.contains(string)) {
                  stringBuilder.append(" (incompatible)");
               }
            }

            return stringBuilder.toString();
         });
      }

      if (languageManager != null) {
         systemDetails.addSection("Current Language", () -> {
            return languageManager.getLanguage();
         });
      }

      systemDetails.addSection("CPU", GlDebugInfo::getCpuInfo);
      return systemDetails;
   }

   public static MinecraftClient getInstance() {
      return instance;
   }

   public CompletableFuture reloadResourcesConcurrently() {
      return this.submit(this::reloadResources).thenCompose((future) -> {
         return future;
      });
   }

   public void ensureAbuseReportContext(ReporterEnvironment environment) {
      if (!this.abuseReportContext.environmentEquals(environment)) {
         this.abuseReportContext = AbuseReportContext.create(environment, this.userApiService);
      }

   }

   @Nullable
   public ServerInfo getCurrentServerEntry() {
      return (ServerInfo)Nullables.map(this.getNetworkHandler(), ClientPlayNetworkHandler::getServerInfo);
   }

   public boolean isInSingleplayer() {
      return this.integratedServerRunning;
   }

   public boolean isIntegratedServerRunning() {
      return this.integratedServerRunning && this.server != null;
   }

   @Nullable
   public IntegratedServer getServer() {
      return this.server;
   }

   public boolean isConnectedToLocalServer() {
      IntegratedServer lv = this.getServer();
      return lv != null && !lv.isRemote();
   }

   public Session getSession() {
      return this.session;
   }

   public PropertyMap getSessionProperties() {
      if (this.sessionPropertyMap.isEmpty()) {
         GameProfile gameProfile = this.getSessionService().fillProfileProperties(this.session.getProfile(), false);
         this.sessionPropertyMap.putAll(gameProfile.getProperties());
      }

      return this.sessionPropertyMap;
   }

   public Proxy getNetworkProxy() {
      return this.networkProxy;
   }

   public TextureManager getTextureManager() {
      return this.textureManager;
   }

   public ResourceManager getResourceManager() {
      return this.resourceManager;
   }

   public ResourcePackManager getResourcePackManager() {
      return this.resourcePackManager;
   }

   public DefaultResourcePack getDefaultResourcePack() {
      return this.defaultResourcePack;
   }

   public ServerResourcePackProvider getServerResourcePackProvider() {
      return this.serverResourcePackProvider;
   }

   public Path getResourcePackDir() {
      return this.resourcePackDir;
   }

   public LanguageManager getLanguageManager() {
      return this.languageManager;
   }

   public Function getSpriteAtlas(Identifier id) {
      SpriteAtlasTexture var10000 = this.bakedModelManager.getAtlas(id);
      Objects.requireNonNull(var10000);
      return var10000::getSprite;
   }

   public boolean is64Bit() {
      return this.is64Bit;
   }

   public boolean isPaused() {
      return this.paused;
   }

   public VideoWarningManager getVideoWarningManager() {
      return this.videoWarningManager;
   }

   public SoundManager getSoundManager() {
      return this.soundManager;
   }

   public MusicSound getMusicType() {
      MusicSound lv = (MusicSound)Nullables.map(this.currentScreen, Screen::getMusic);
      if (lv != null) {
         return lv;
      } else if (this.player != null) {
         if (this.player.world.getRegistryKey() == World.END) {
            return this.inGameHud.getBossBarHud().shouldPlayDragonMusic() ? MusicType.DRAGON : MusicType.END;
         } else {
            RegistryEntry lv2 = this.player.world.getBiome(this.player.getBlockPos());
            if (this.musicTracker.isPlayingType(MusicType.UNDERWATER) || this.player.isSubmergedInWater() && lv2.isIn(BiomeTags.PLAYS_UNDERWATER_MUSIC)) {
               return MusicType.UNDERWATER;
            } else {
               return this.player.world.getRegistryKey() != World.NETHER && this.player.getAbilities().creativeMode && this.player.getAbilities().allowFlying ? MusicType.CREATIVE : (MusicSound)((Biome)lv2.value()).getMusic().orElse(MusicType.GAME);
            }
         }
      } else {
         return MusicType.MENU;
      }
   }

   public MinecraftSessionService getSessionService() {
      return this.sessionService;
   }

   public PlayerSkinProvider getSkinProvider() {
      return this.skinProvider;
   }

   @Nullable
   public Entity getCameraEntity() {
      return this.cameraEntity;
   }

   public void setCameraEntity(Entity entity) {
      this.cameraEntity = entity;
      this.gameRenderer.onCameraEntitySet(entity);
   }

   public boolean hasOutline(Entity entity) {
      return entity.isGlowing() || this.player != null && this.player.isSpectator() && this.options.spectatorOutlinesKey.isPressed() && entity.getType() == EntityType.PLAYER;
   }

   protected Thread getThread() {
      return this.thread;
   }

   protected Runnable createTask(Runnable runnable) {
      return runnable;
   }

   protected boolean canExecute(Runnable task) {
      return true;
   }

   public BlockRenderManager getBlockRenderManager() {
      return this.blockRenderManager;
   }

   public EntityRenderDispatcher getEntityRenderDispatcher() {
      return this.entityRenderDispatcher;
   }

   public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
      return this.blockEntityRenderDispatcher;
   }

   public ItemRenderer getItemRenderer() {
      return this.itemRenderer;
   }

   public SearchProvider getSearchProvider(SearchManager.Key key) {
      return this.searchManager.get(key);
   }

   public void reloadSearchProvider(SearchManager.Key key, List values) {
      this.searchManager.reload(key, values);
   }

   public MetricsData getMetricsData() {
      return this.metricsData;
   }

   public boolean isConnectedToRealms() {
      return this.connectedToRealms;
   }

   public void setConnectedToRealms(boolean connectedToRealms) {
      this.connectedToRealms = connectedToRealms;
   }

   public DataFixer getDataFixer() {
      return this.dataFixer;
   }

   public float getTickDelta() {
      return this.renderTickCounter.tickDelta;
   }

   public float getLastFrameDuration() {
      return this.renderTickCounter.lastFrameDuration;
   }

   public BlockColors getBlockColors() {
      return this.blockColors;
   }

   public boolean hasReducedDebugInfo() {
      return this.player != null && this.player.hasReducedDebugInfo() || (Boolean)this.options.getReducedDebugInfo().getValue();
   }

   public ToastManager getToastManager() {
      return this.toastManager;
   }

   public TutorialManager getTutorialManager() {
      return this.tutorialManager;
   }

   public boolean isWindowFocused() {
      return this.windowFocused;
   }

   public HotbarStorage getCreativeHotbarStorage() {
      return this.creativeHotbarStorage;
   }

   public BakedModelManager getBakedModelManager() {
      return this.bakedModelManager;
   }

   public PaintingManager getPaintingManager() {
      return this.paintingManager;
   }

   public StatusEffectSpriteManager getStatusEffectSpriteManager() {
      return this.statusEffectSpriteManager;
   }

   public void onWindowFocusChanged(boolean focused) {
      this.windowFocused = focused;
   }

   public Text takePanorama(File directory, int width, int height) {
      int k = this.window.getFramebufferWidth();
      int l = this.window.getFramebufferHeight();
      Framebuffer lv = new SimpleFramebuffer(width, height, true, IS_SYSTEM_MAC);
      float f = this.player.getPitch();
      float g = this.player.getYaw();
      float h = this.player.prevPitch;
      float m = this.player.prevYaw;
      this.gameRenderer.setBlockOutlineEnabled(false);

      MutableText var12;
      try {
         this.gameRenderer.setRenderingPanorama(true);
         this.worldRenderer.reloadTransparencyPostProcessor();
         this.window.setFramebufferWidth(width);
         this.window.setFramebufferHeight(height);

         for(int n = 0; n < 6; ++n) {
            switch (n) {
               case 0:
                  this.player.setYaw(g);
                  this.player.setPitch(0.0F);
                  break;
               case 1:
                  this.player.setYaw((g + 90.0F) % 360.0F);
                  this.player.setPitch(0.0F);
                  break;
               case 2:
                  this.player.setYaw((g + 180.0F) % 360.0F);
                  this.player.setPitch(0.0F);
                  break;
               case 3:
                  this.player.setYaw((g - 90.0F) % 360.0F);
                  this.player.setPitch(0.0F);
                  break;
               case 4:
                  this.player.setYaw(g);
                  this.player.setPitch(-90.0F);
                  break;
               case 5:
               default:
                  this.player.setYaw(g);
                  this.player.setPitch(90.0F);
            }

            this.player.prevYaw = this.player.getYaw();
            this.player.prevPitch = this.player.getPitch();
            lv.beginWrite(true);
            this.gameRenderer.renderWorld(1.0F, 0L, new MatrixStack());

            try {
               Thread.sleep(10L);
            } catch (InterruptedException var17) {
            }

            ScreenshotRecorder.saveScreenshot(directory, "panorama_" + n + ".png", lv, (message) -> {
            });
         }

         Text lv2 = Text.literal(directory.getName()).formatted(Formatting.UNDERLINE).styled((style) -> {
            return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, directory.getAbsolutePath()));
         });
         var12 = Text.translatable("screenshot.success", lv2);
         return var12;
      } catch (Exception var18) {
         LOGGER.error("Couldn't save image", var18);
         var12 = Text.translatable("screenshot.failure", var18.getMessage());
      } finally {
         this.player.setPitch(f);
         this.player.setYaw(g);
         this.player.prevPitch = h;
         this.player.prevYaw = m;
         this.gameRenderer.setBlockOutlineEnabled(true);
         this.window.setFramebufferWidth(k);
         this.window.setFramebufferHeight(l);
         lv.delete();
         this.gameRenderer.setRenderingPanorama(false);
         this.worldRenderer.reloadTransparencyPostProcessor();
         this.getFramebuffer().beginWrite(true);
      }

      return var12;
   }

   private Text takeHugeScreenshot(File gameDirectory, int unitWidth, int unitHeight, int width, int height) {
      try {
         ByteBuffer byteBuffer = GlDebugInfo.allocateMemory(unitWidth * unitHeight * 3);
         ScreenshotRecorder lv = new ScreenshotRecorder(gameDirectory, width, height, unitHeight);
         float f = (float)width / (float)unitWidth;
         float g = (float)height / (float)unitHeight;
         float h = f > g ? f : g;

         for(int m = (height - 1) / unitHeight * unitHeight; m >= 0; m -= unitHeight) {
            for(int n = 0; n < width; n += unitWidth) {
               RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
               float o = (float)(width - unitWidth) / 2.0F * 2.0F - (float)(n * 2);
               float p = (float)(height - unitHeight) / 2.0F * 2.0F - (float)(m * 2);
               o /= (float)unitWidth;
               p /= (float)unitHeight;
               this.gameRenderer.renderWithZoom(h, o, p);
               byteBuffer.clear();
               RenderSystem.pixelStore(3333, 1);
               RenderSystem.pixelStore(3317, 1);
               RenderSystem.readPixels(0, 0, unitWidth, unitHeight, 32992, 5121, byteBuffer);
               lv.getIntoBuffer(byteBuffer, n, m, unitWidth, unitHeight);
            }

            lv.writeToStream();
         }

         File file2 = lv.finish();
         GlDebugInfo.freeMemory(byteBuffer);
         Text lv2 = Text.literal(file2.getName()).formatted(Formatting.UNDERLINE).styled((style) -> {
            return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath()));
         });
         return Text.translatable("screenshot.success", lv2);
      } catch (Exception var15) {
         LOGGER.warn("Couldn't save screenshot", var15);
         return Text.translatable("screenshot.failure", var15.getMessage());
      }
   }

   public Profiler getProfiler() {
      return this.profiler;
   }

   @Nullable
   public WorldGenerationProgressTracker getWorldGenerationProgressTracker() {
      return (WorldGenerationProgressTracker)this.worldGenProgressTracker.get();
   }

   public SplashTextResourceSupplier getSplashTextLoader() {
      return this.splashTextLoader;
   }

   @Nullable
   public Overlay getOverlay() {
      return this.overlay;
   }

   public SocialInteractionsManager getSocialInteractionsManager() {
      return this.socialInteractionsManager;
   }

   public boolean shouldRenderAsync() {
      return false;
   }

   public Window getWindow() {
      return this.window;
   }

   public BufferBuilderStorage getBufferBuilders() {
      return this.bufferBuilders;
   }

   public void setMipmapLevels(int mipmapLevels) {
      this.bakedModelManager.setMipmapLevels(mipmapLevels);
   }

   public EntityModelLoader getEntityModelLoader() {
      return this.entityModelLoader;
   }

   public boolean shouldFilterText() {
      return this.userApiService.properties().flag(UserFlag.PROFANITY_FILTER_ENABLED);
   }

   public void loadBlockList() {
      this.socialInteractionsManager.loadBlockList();
      this.getProfileKeys().fetchKeyPair();
   }

   public Realms32BitWarningChecker getRealms32BitWarningChecker() {
      return this.realms32BitWarningChecker;
   }

   public SignatureVerifier getServicesSignatureVerifier() {
      return this.servicesSignatureVerifier;
   }

   public GuiNavigationType getNavigationType() {
      return this.navigationType;
   }

   public void setNavigationType(GuiNavigationType navigationType) {
      this.navigationType = navigationType;
   }

   public NarratorManager getNarratorManager() {
      return this.narratorManager;
   }

   public MessageHandler getMessageHandler() {
      return this.messageHandler;
   }

   public AbuseReportContext getAbuseReportContext() {
      return this.abuseReportContext;
   }

   public RealmsPeriodicCheckers getRealmsPeriodicCheckers() {
      return this.realmsPeriodicCheckers;
   }

   public QuickPlayLogger getQuickPlayLogger() {
      return this.quickPlayLogger;
   }

   static {
      IS_SYSTEM_MAC = Util.getOperatingSystem() == Util.OperatingSystem.OSX;
      DEFAULT_FONT_ID = new Identifier("default");
      UNICODE_FONT_ID = new Identifier("uniform");
      ALT_TEXT_RENDERER_ID = new Identifier("alt");
      REGIONAL_COMPLIANCIES_ID = new Identifier("regional_compliancies.json");
      COMPLETED_UNIT_FUTURE = CompletableFuture.completedFuture(Unit.INSTANCE);
      SOCIAL_INTERACTIONS_NOT_AVAILABLE = Text.translatable("multiplayer.socialInteractions.not_available");
   }

   @Environment(EnvType.CLIENT)
   public static enum ChatRestriction {
      ENABLED(ScreenTexts.EMPTY) {
         public boolean allowsChat(boolean singlePlayer) {
            return true;
         }
      },
      DISABLED_BY_OPTIONS(Text.translatable("chat.disabled.options").formatted(Formatting.RED)) {
         public boolean allowsChat(boolean singlePlayer) {
            return false;
         }
      },
      DISABLED_BY_LAUNCHER(Text.translatable("chat.disabled.launcher").formatted(Formatting.RED)) {
         public boolean allowsChat(boolean singlePlayer) {
            return singlePlayer;
         }
      },
      DISABLED_BY_PROFILE(Text.translatable("chat.disabled.profile", Text.keybind(MinecraftClient.instance.options.chatKey.getTranslationKey())).formatted(Formatting.RED)) {
         public boolean allowsChat(boolean singlePlayer) {
            return singlePlayer;
         }
      };

      static final Text MORE_INFO_TEXT = Text.translatable("chat.disabled.profile.moreInfo");
      private final Text description;

      ChatRestriction(Text description) {
         this.description = description;
      }

      public Text getDescription() {
         return this.description;
      }

      public abstract boolean allowsChat(boolean singlePlayer);

      // $FF: synthetic method
      private static ChatRestriction[] method_36862() {
         return new ChatRestriction[]{ENABLED, DISABLED_BY_OPTIONS, DISABLED_BY_LAUNCHER, DISABLED_BY_PROFILE};
      }
   }
}
