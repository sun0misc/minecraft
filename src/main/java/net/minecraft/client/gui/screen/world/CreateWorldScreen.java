package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.ExperimentalWarningScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
   private static final int field_42165 = 1;
   private static final int field_42166 = 210;
   private static final int field_42167 = 36;
   private static final int field_42168 = 1;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TEMP_DIR_PREFIX = "mcworld-";
   static final Text GAME_MODE_TEXT = Text.translatable("selectWorld.gameMode");
   static final Text ENTER_NAME_TEXT = Text.translatable("selectWorld.enterName");
   static final Text EXPERIMENTS_TEXT = Text.translatable("selectWorld.experiments");
   static final Text ALLOW_COMMANDS_INFO_TEXT = Text.translatable("selectWorld.allowCommands.info");
   private static final Text PREPARING_TEXT = Text.translatable("createWorld.preparing");
   private static final int field_42170 = 10;
   private static final int field_42171 = 8;
   public static final Identifier HEADER_SEPARATOR_TEXTURE = new Identifier("textures/gui/header_separator.png");
   public static final Identifier FOOTER_SEPARATOR_TEXTURE = new Identifier("textures/gui/footer_separator.png");
   final WorldCreator worldCreator;
   private final TabManager tabManager = new TabManager(this::addDrawableChild, (child) -> {
      this.remove(child);
   });
   private boolean recreated;
   @Nullable
   private final Screen parent;
   @Nullable
   private Path dataPackTempDir;
   @Nullable
   private ResourcePackManager packManager;
   @Nullable
   private GridWidget grid;
   @Nullable
   private TabNavigationWidget tabNavigation;

   public static void create(MinecraftClient client, @Nullable Screen parent) {
      showMessage(client, PREPARING_TEXT);
      ResourcePackManager lv = new ResourcePackManager(new ResourcePackProvider[]{new VanillaDataPackProvider()});
      SaveLoading.ServerConfig lv2 = createServerConfig(lv, DataConfiguration.SAFE_MODE);
      CompletableFuture completableFuture = SaveLoading.load(lv2, (context) -> {
         return new SaveLoading.LoadContext(new WorldCreationSettings(new WorldGenSettings(GeneratorOptions.createRandom(), WorldPresets.createDemoOptions(context.worldGenRegistryManager())), context.dataConfiguration()), context.dimensionsRegistryManager());
      }, (resourceManager, dataPackContents, combinedDynamicRegistries, generatorOptions) -> {
         resourceManager.close();
         return new GeneratorOptionsHolder(generatorOptions.worldGenSettings(), combinedDynamicRegistries, dataPackContents, generatorOptions.dataConfiguration());
      }, Util.getMainWorkerExecutor(), client);
      Objects.requireNonNull(completableFuture);
      client.runTasks(completableFuture::isDone);
      client.setScreen(new CreateWorldScreen(client, parent, (GeneratorOptionsHolder)completableFuture.join(), Optional.of(WorldPresets.DEFAULT), OptionalLong.empty()));
   }

   public static CreateWorldScreen create(MinecraftClient client, @Nullable Screen parent, LevelInfo levelInfo, GeneratorOptionsHolder generatorOptionsHolder, @Nullable Path dataPackTempDir) {
      CreateWorldScreen lv = new CreateWorldScreen(client, parent, generatorOptionsHolder, WorldPresets.getWorldPreset(generatorOptionsHolder.selectedDimensions().dimensions()), OptionalLong.of(generatorOptionsHolder.generatorOptions().getSeed()));
      lv.recreated = true;
      lv.worldCreator.setWorldName(levelInfo.getLevelName());
      lv.worldCreator.setCheatsEnabled(levelInfo.areCommandsAllowed());
      lv.worldCreator.setDifficulty(levelInfo.getDifficulty());
      lv.worldCreator.getGameRules().setAllValues(levelInfo.getGameRules(), (MinecraftServer)null);
      if (levelInfo.isHardcore()) {
         lv.worldCreator.setGameMode(WorldCreator.Mode.HARDCORE);
      } else if (levelInfo.getGameMode().isSurvivalLike()) {
         lv.worldCreator.setGameMode(WorldCreator.Mode.SURVIVAL);
      } else if (levelInfo.getGameMode().isCreative()) {
         lv.worldCreator.setGameMode(WorldCreator.Mode.CREATIVE);
      }

      lv.dataPackTempDir = dataPackTempDir;
      return lv;
   }

   private CreateWorldScreen(MinecraftClient client, @Nullable Screen parent, GeneratorOptionsHolder generatorOptionsHolder, Optional defaultWorldType, OptionalLong seed) {
      super(Text.translatable("selectWorld.create"));
      this.parent = parent;
      this.worldCreator = new WorldCreator(client.getLevelStorage().getSavesDirectory(), generatorOptionsHolder, defaultWorldType, seed);
   }

   public WorldCreator getWorldCreator() {
      return this.worldCreator;
   }

   public void tick() {
      this.tabManager.tick();
   }

   protected void init() {
      this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width).tabs(new GameTab(), new WorldTab(), new MoreTab()).build();
      this.addDrawableChild(this.tabNavigation);
      this.grid = (new GridWidget()).setColumnSpacing(10);
      GridWidget.Adder lv = this.grid.createAdder(2);
      lv.add(ButtonWidget.builder(Text.translatable("selectWorld.create"), (button) -> {
         this.createLevel();
      }).build());
      lv.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
         this.onCloseScreen();
      }).build());
      this.grid.forEachChild((child) -> {
         child.setNavigationOrder(1);
         this.addDrawableChild(child);
      });
      this.tabNavigation.selectTab(0, false);
      this.worldCreator.update();
      this.initTabNavigation();
   }

   public void initTabNavigation() {
      if (this.tabNavigation != null && this.grid != null) {
         this.tabNavigation.setWidth(this.width);
         this.tabNavigation.init();
         this.grid.refreshPositions();
         SimplePositioningWidget.setPos(this.grid, 0, this.height - 36, this.width, 36);
         int i = this.tabNavigation.getNavigationFocus().getBottom();
         ScreenRect lv = new ScreenRect(0, i, this.width, this.grid.getY() - i);
         this.tabManager.setTabArea(lv);
      }
   }

   private static void showMessage(MinecraftClient client, Text text) {
      client.setScreenAndRender(new MessageScreen(text));
   }

   private void createLevel() {
      GeneratorOptionsHolder lv = this.worldCreator.getGeneratorOptionsHolder();
      DimensionOptionsRegistryHolder.DimensionsConfig lv2 = lv.selectedDimensions().toConfig(lv.dimensionOptionsRegistry());
      CombinedDynamicRegistries lv3 = lv.combinedDynamicRegistries().with(ServerDynamicRegistryType.DIMENSIONS, (DynamicRegistryManager.Immutable[])(lv2.toDynamicRegistryManager()));
      Lifecycle lifecycle = FeatureFlags.isNotVanilla(lv.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
      Lifecycle lifecycle2 = lv3.getCombinedRegistryManager().getRegistryLifecycle();
      Lifecycle lifecycle3 = lifecycle2.add(lifecycle);
      boolean bl = !this.recreated && lifecycle2 == Lifecycle.stable();
      IntegratedServerLoader.tryLoad(this.client, this, lifecycle3, () -> {
         this.startServer(lv2.specialWorldProperty(), lv3, lifecycle3);
      }, bl);
   }

   private void startServer(LevelProperties.SpecialProperty specialProperty, CombinedDynamicRegistries combinedDynamicRegistries, Lifecycle lifecycle) {
      showMessage(this.client, PREPARING_TEXT);
      Optional optional = this.createSession();
      if (!optional.isEmpty()) {
         this.clearDataPackTempDir();
         boolean bl = specialProperty == LevelProperties.SpecialProperty.DEBUG;
         GeneratorOptionsHolder lv = this.worldCreator.getGeneratorOptionsHolder();
         LevelInfo lv2 = this.createLevelInfo(bl);
         SaveProperties lv3 = new LevelProperties(lv2, lv.generatorOptions(), specialProperty, lifecycle);
         this.client.createIntegratedServerLoader().start((LevelStorage.Session)optional.get(), lv.dataPackContents(), combinedDynamicRegistries, lv3);
      }
   }

   private LevelInfo createLevelInfo(boolean debugWorld) {
      String string = this.worldCreator.getWorldName().trim();
      if (debugWorld) {
         GameRules lv = new GameRules();
         ((GameRules.BooleanRule)lv.get(GameRules.DO_DAYLIGHT_CYCLE)).set(false, (MinecraftServer)null);
         return new LevelInfo(string, GameMode.SPECTATOR, false, Difficulty.PEACEFUL, true, lv, DataConfiguration.SAFE_MODE);
      } else {
         return new LevelInfo(string, this.worldCreator.getGameMode().defaultGameMode, this.worldCreator.isHardcore(), this.worldCreator.getDifficulty(), this.worldCreator.areCheatsEnabled(), this.worldCreator.getGameRules(), this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.tabNavigation.trySwitchTabsWithKey(keyCode)) {
         return true;
      } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
         return false;
      } else {
         this.createLevel();
         return true;
      }
   }

   public void close() {
      this.onCloseScreen();
   }

   public void onCloseScreen() {
      this.client.setScreen(this.parent);
      this.clearDataPackTempDir();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      RenderSystem.setShaderTexture(0, FOOTER_SEPARATOR_TEXTURE);
      drawTexture(matrices, 0, MathHelper.roundUpToMultiple(this.height - 36 - 2, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void renderBackgroundTexture(MatrixStack matrices) {
      RenderSystem.setShaderTexture(0, LIGHT_DIRT_BACKGROUND_TEXTURE);
      int i = true;
      drawTexture(matrices, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
   }

   protected Element addSelectableChild(Element child) {
      return super.addSelectableChild(child);
   }

   protected Element addDrawableChild(Element drawableElement) {
      return super.addDrawableChild(drawableElement);
   }

   @Nullable
   private Path getDataPackTempDir() {
      if (this.dataPackTempDir == null) {
         try {
            this.dataPackTempDir = Files.createTempDirectory("mcworld-");
         } catch (IOException var2) {
            LOGGER.warn("Failed to create temporary dir", var2);
            SystemToast.addPackCopyFailure(this.client, this.worldCreator.getWorldDirectoryName());
            this.onCloseScreen();
         }
      }

      return this.dataPackTempDir;
   }

   void openExperimentsScreen(DataConfiguration dataConfiguration) {
      Pair pair = this.getScannedPack(dataConfiguration);
      if (pair != null) {
         this.client.setScreen(new ExperimentsScreen(this, (ResourcePackManager)pair.getSecond(), (resourcePackManager) -> {
            this.applyDataPacks(resourcePackManager, false, this::openExperimentsScreen);
         }));
      }

   }

   void openPackScreen(DataConfiguration dataConfiguration) {
      Pair pair = this.getScannedPack(dataConfiguration);
      if (pair != null) {
         this.client.setScreen(new PackScreen((ResourcePackManager)pair.getSecond(), (resourcePackManager) -> {
            this.applyDataPacks(resourcePackManager, true, this::openPackScreen);
         }, (Path)pair.getFirst(), Text.translatable("dataPack.title")));
      }

   }

   private void applyDataPacks(ResourcePackManager dataPackManager, boolean fromPackScreen, Consumer configurationSetter) {
      List list = ImmutableList.copyOf(dataPackManager.getEnabledNames());
      List list2 = (List)dataPackManager.getNames().stream().filter((name) -> {
         return !list.contains(name);
      }).collect(ImmutableList.toImmutableList());
      DataConfiguration lv = new DataConfiguration(new DataPackSettings(list, list2), this.worldCreator.getGeneratorOptionsHolder().dataConfiguration().enabledFeatures());
      if (this.worldCreator.updateDataConfiguration(lv)) {
         this.client.setScreen(this);
      } else {
         FeatureSet lv2 = dataPackManager.getRequestedFeatures();
         if (FeatureFlags.isNotVanilla(lv2) && fromPackScreen) {
            this.client.setScreen(new ExperimentalWarningScreen(dataPackManager.getEnabledProfiles(), (confirmed) -> {
               if (confirmed) {
                  this.validateDataPacks(dataPackManager, lv, configurationSetter);
               } else {
                  configurationSetter.accept(this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
               }

            }));
         } else {
            this.validateDataPacks(dataPackManager, lv, configurationSetter);
         }

      }
   }

   private void validateDataPacks(ResourcePackManager dataPackManager, DataConfiguration dataConfiguration, Consumer configurationSetter) {
      this.client.setScreenAndRender(new MessageScreen(Text.translatable("dataPack.validation.working")));
      SaveLoading.ServerConfig lv = createServerConfig(dataPackManager, dataConfiguration);
      CompletableFuture var10000 = SaveLoading.load(lv, (context) -> {
         if (context.worldGenRegistryManager().get(RegistryKeys.WORLD_PRESET).size() == 0) {
            throw new IllegalStateException("Needs at least one world preset to continue");
         } else if (context.worldGenRegistryManager().get(RegistryKeys.BIOME).size() == 0) {
            throw new IllegalStateException("Needs at least one biome continue");
         } else {
            GeneratorOptionsHolder lv = this.worldCreator.getGeneratorOptionsHolder();
            DynamicOps dynamicOps = RegistryOps.of(JsonOps.INSTANCE, (RegistryWrapper.WrapperLookup)lv.getCombinedRegistryManager());
            DataResult dataResult = WorldGenSettings.encode(dynamicOps, lv.generatorOptions(), (DimensionOptionsRegistryHolder)lv.selectedDimensions()).setLifecycle(Lifecycle.stable());
            DynamicOps dynamicOps2 = RegistryOps.of(JsonOps.INSTANCE, (RegistryWrapper.WrapperLookup)context.worldGenRegistryManager());
            DataResult var10000 = dataResult.flatMap((json) -> {
               return WorldGenSettings.CODEC.parse(dynamicOps2, json);
            });
            Logger var10003 = LOGGER;
            Objects.requireNonNull(var10003);
            WorldGenSettings lv2 = (WorldGenSettings)var10000.getOrThrow(false, Util.addPrefix("Error parsing worldgen settings after loading data packs: ", var10003::error));
            return new SaveLoading.LoadContext(new WorldCreationSettings(lv2, context.dataConfiguration()), context.dimensionsRegistryManager());
         }
      }, (resourceManager, dataPackContents, combinedDynamicRegistries, context) -> {
         resourceManager.close();
         return new GeneratorOptionsHolder(context.worldGenSettings(), combinedDynamicRegistries, dataPackContents, context.dataConfiguration());
      }, Util.getMainWorkerExecutor(), this.client);
      WorldCreator var10001 = this.worldCreator;
      Objects.requireNonNull(var10001);
      var10000.thenAcceptAsync(var10001::setGeneratorOptionsHolder, this.client).handle((void_, throwable) -> {
         if (throwable != null) {
            LOGGER.warn("Failed to validate datapack", throwable);
            this.client.setScreen(new ConfirmScreen((confirmed) -> {
               if (confirmed) {
                  configurationSetter.accept(this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
               } else {
                  configurationSetter.accept(DataConfiguration.SAFE_MODE);
               }

            }, Text.translatable("dataPack.validation.failed"), ScreenTexts.EMPTY, Text.translatable("dataPack.validation.back"), Text.translatable("dataPack.validation.reset")));
         } else {
            this.client.setScreen(this);
         }

         return null;
      });
   }

   private static SaveLoading.ServerConfig createServerConfig(ResourcePackManager dataPackManager, DataConfiguration dataConfiguration) {
      SaveLoading.DataPacks lv = new SaveLoading.DataPacks(dataPackManager, dataConfiguration, false, true);
      return new SaveLoading.ServerConfig(lv, CommandManager.RegistrationEnvironment.INTEGRATED, 2);
   }

   private void clearDataPackTempDir() {
      if (this.dataPackTempDir != null) {
         try {
            Stream stream = Files.walk(this.dataPackTempDir);

            try {
               stream.sorted(Comparator.reverseOrder()).forEach((path) -> {
                  try {
                     Files.delete(path);
                  } catch (IOException var2) {
                     LOGGER.warn("Failed to remove temporary file {}", path, var2);
                  }

               });
            } catch (Throwable var5) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException var6) {
            LOGGER.warn("Failed to list temporary dir {}", this.dataPackTempDir);
         }

         this.dataPackTempDir = null;
      }

   }

   private static void copyDataPack(Path srcFolder, Path destFolder, Path dataPackFile) {
      try {
         Util.relativeCopy(srcFolder, destFolder, dataPackFile);
      } catch (IOException var4) {
         LOGGER.warn("Failed to copy datapack file from {} to {}", dataPackFile, destFolder);
         throw new UncheckedIOException(var4);
      }
   }

   private Optional createSession() {
      String string = this.worldCreator.getWorldDirectoryName();

      try {
         LevelStorage.Session lv = this.client.getLevelStorage().createSession(string);
         if (this.dataPackTempDir == null) {
            return Optional.of(lv);
         }

         try {
            Stream stream = Files.walk(this.dataPackTempDir);

            Optional var5;
            try {
               Path path = lv.getDirectory(WorldSavePath.DATAPACKS);
               PathUtil.createDirectories(path);
               stream.filter((pathx) -> {
                  return !pathx.equals(this.dataPackTempDir);
               }).forEach((pathx) -> {
                  copyDataPack(this.dataPackTempDir, path, pathx);
               });
               var5 = Optional.of(lv);
            } catch (Throwable var7) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stream != null) {
               stream.close();
            }

            return var5;
         } catch (UncheckedIOException | IOException var8) {
            LOGGER.warn("Failed to copy datapacks to world {}", string, var8);
            lv.close();
         }
      } catch (UncheckedIOException | IOException var9) {
         LOGGER.warn("Failed to create access for {}", string, var9);
      }

      SystemToast.addPackCopyFailure(this.client, string);
      this.onCloseScreen();
      return Optional.empty();
   }

   @Nullable
   public static Path copyDataPack(Path srcFolder, MinecraftClient client) {
      MutableObject mutableObject = new MutableObject();

      try {
         Stream stream = Files.walk(srcFolder);

         try {
            stream.filter((dataPackFile) -> {
               return !dataPackFile.equals(srcFolder);
            }).forEach((dataPackFile) -> {
               Path path3 = (Path)mutableObject.getValue();
               if (path3 == null) {
                  try {
                     path3 = Files.createTempDirectory("mcworld-");
                  } catch (IOException var5) {
                     LOGGER.warn("Failed to create temporary dir");
                     throw new UncheckedIOException(var5);
                  }

                  mutableObject.setValue(path3);
               }

               copyDataPack(srcFolder, path3, dataPackFile);
            });
         } catch (Throwable var7) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (UncheckedIOException | IOException var8) {
         LOGGER.warn("Failed to copy datapacks from world {}", srcFolder, var8);
         SystemToast.addPackCopyFailure(client, srcFolder.toString());
         return null;
      }

      return (Path)mutableObject.getValue();
   }

   @Nullable
   private Pair getScannedPack(DataConfiguration dataConfiguration) {
      Path path = this.getDataPackTempDir();
      if (path != null) {
         if (this.packManager == null) {
            this.packManager = VanillaDataPackProvider.createManager(path);
            this.packManager.scanPacks();
         }

         this.packManager.setEnabledProfiles(dataConfiguration.dataPacks().getEnabled());
         return Pair.of(path, this.packManager);
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   class GameTab extends GridScreenTab {
      private static final Text GAME_TAB_TITLE_TEXT = Text.translatable("createWorld.tab.game.title");
      private static final Text ALLOW_COMMANDS_TEXT = Text.translatable("selectWorld.allowCommands");
      private final TextFieldWidget worldNameField;

      GameTab() {
         super(GAME_TAB_TITLE_TEXT);
         GridWidget.Adder lv = this.grid.setRowSpacing(8).createAdder(1);
         Positioner lv2 = lv.copyPositioner();
         GridWidget.Adder lv3 = (new GridWidget()).setRowSpacing(4).createAdder(1);
         lv3.add(new TextWidget(CreateWorldScreen.ENTER_NAME_TEXT, CreateWorldScreen.this.client.textRenderer), lv3.copyPositioner().marginLeft(1));
         this.worldNameField = (TextFieldWidget)lv3.add(new TextFieldWidget(CreateWorldScreen.this.textRenderer, 0, 0, 208, 20, Text.translatable("selectWorld.enterName")), lv3.copyPositioner().margin(1));
         this.worldNameField.setText(CreateWorldScreen.this.worldCreator.getWorldName());
         TextFieldWidget var10000 = this.worldNameField;
         WorldCreator var10001 = CreateWorldScreen.this.worldCreator;
         Objects.requireNonNull(var10001);
         var10000.setChangedListener(var10001::setWorldName);
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            this.worldNameField.setTooltip(Tooltip.of(Text.translatable("selectWorld.targetFolder", Text.literal(creator.getWorldDirectoryName()).formatted(Formatting.ITALIC))));
         });
         CreateWorldScreen.this.setInitialFocus(this.worldNameField);
         lv.add(lv3.getGridWidget(), lv.copyPositioner().alignHorizontalCenter());
         CyclingButtonWidget lv4 = (CyclingButtonWidget)lv.add(CyclingButtonWidget.builder((value) -> {
            return value.name;
         }).values((Object[])(WorldCreator.Mode.SURVIVAL, WorldCreator.Mode.HARDCORE, WorldCreator.Mode.CREATIVE)).build(0, 0, 210, 20, CreateWorldScreen.GAME_MODE_TEXT, (button, value) -> {
            CreateWorldScreen.this.worldCreator.setGameMode(value);
         }), lv2);
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            lv4.setValue(creator.getGameMode());
            lv4.active = !creator.isDebug();
            lv4.setTooltip(Tooltip.of(creator.getGameMode().getInfo()));
         });
         CyclingButtonWidget lv5 = (CyclingButtonWidget)lv.add(CyclingButtonWidget.builder(Difficulty::getTranslatableName).values((Object[])Difficulty.values()).build(0, 0, 210, 20, Text.translatable("options.difficulty"), (button, value) -> {
            CreateWorldScreen.this.worldCreator.setDifficulty(value);
         }), lv2);
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            lv5.setValue(CreateWorldScreen.this.worldCreator.getDifficulty());
            lv5.active = !CreateWorldScreen.this.worldCreator.isHardcore();
            lv5.setTooltip(Tooltip.of(CreateWorldScreen.this.worldCreator.getDifficulty().getInfo()));
         });
         CyclingButtonWidget lv6 = (CyclingButtonWidget)lv.add(CyclingButtonWidget.onOffBuilder().tooltip((value) -> {
            return Tooltip.of(CreateWorldScreen.ALLOW_COMMANDS_INFO_TEXT);
         }).build(0, 0, 210, 20, ALLOW_COMMANDS_TEXT, (button, value) -> {
            CreateWorldScreen.this.worldCreator.setCheatsEnabled(value);
         }));
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            lv6.setValue(CreateWorldScreen.this.worldCreator.areCheatsEnabled());
            lv6.active = !CreateWorldScreen.this.worldCreator.isDebug() && !CreateWorldScreen.this.worldCreator.isHardcore();
         });
         lv.add(ButtonWidget.builder(CreateWorldScreen.EXPERIMENTS_TEXT, (button) -> {
            CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
         }).width(210).build());
      }

      public void tick() {
         this.worldNameField.tick();
      }
   }

   @Environment(EnvType.CLIENT)
   private class WorldTab extends GridScreenTab {
      private static final Text WORLD_TAB_TITLE_TEXT = Text.translatable("createWorld.tab.world.title");
      private static final Text AMPLIFIED_GENERATOR_INFO_TEXT = Text.translatable("generator.minecraft.amplified.info");
      private static final Text MAP_FEATURES_TEXT = Text.translatable("selectWorld.mapFeatures");
      private static final Text MAP_FEATURES_INFO_TEXT = Text.translatable("selectWorld.mapFeatures.info");
      private static final Text BONUS_ITEMS_TEXT = Text.translatable("selectWorld.bonusItems");
      private static final Text ENTER_SEED_TEXT = Text.translatable("selectWorld.enterSeed");
      static final Text SEED_INFO_TEXT;
      private static final int field_42190 = 310;
      private final TextFieldWidget seedField;
      private final ButtonWidget customizeButton;

      WorldTab() {
         super(WORLD_TAB_TITLE_TEXT);
         GridWidget.Adder lv = this.grid.setColumnSpacing(10).setRowSpacing(8).createAdder(2);
         CyclingButtonWidget lv2 = (CyclingButtonWidget)lv.add(CyclingButtonWidget.builder(WorldCreator.WorldType::getName).values(this.getWorldTypes()).narration(WorldTab::getWorldTypeNarrationMessage).build(0, 0, 150, 20, Text.translatable("selectWorld.mapType"), (argx, arg2) -> {
            CreateWorldScreen.this.worldCreator.setWorldType(arg2);
         }));
         lv2.setValue(CreateWorldScreen.this.worldCreator.getWorldType());
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            WorldCreator.WorldType lv = creator.getWorldType();
            lv2.setValue(lv);
            if (lv.isAmplified()) {
               lv2.setTooltip(Tooltip.of(AMPLIFIED_GENERATOR_INFO_TEXT));
            } else {
               lv2.setTooltip((Tooltip)null);
            }

            lv2.active = CreateWorldScreen.this.worldCreator.getWorldType().preset() != null;
         });
         this.customizeButton = (ButtonWidget)lv.add(ButtonWidget.builder(Text.translatable("selectWorld.customizeType"), (button) -> {
            this.openCustomizeScreen();
         }).build());
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            this.customizeButton.active = !creator.isDebug() && creator.getLevelScreenProvider() != null;
         });
         GridWidget.Adder lv3 = (new GridWidget()).setRowSpacing(4).createAdder(1);
         lv3.add((new TextWidget(ENTER_SEED_TEXT, CreateWorldScreen.this.textRenderer)).alignLeft());
         this.seedField = (TextFieldWidget)lv3.add(new TextFieldWidget(CreateWorldScreen.this.textRenderer, 0, 0, 308, 20, Text.translatable("selectWorld.enterSeed")) {
            protected MutableText getNarrationMessage() {
               return super.getNarrationMessage().append(ScreenTexts.SENTENCE_SEPARATOR).append(CreateWorldScreen.WorldTab.SEED_INFO_TEXT);
            }
         }, lv.copyPositioner().margin(1));
         this.seedField.setPlaceholder(SEED_INFO_TEXT);
         this.seedField.setText(CreateWorldScreen.this.worldCreator.getSeed());
         this.seedField.setChangedListener((seed) -> {
            CreateWorldScreen.this.worldCreator.setSeed(this.seedField.getText());
         });
         lv.add(lv3.getGridWidget(), 2);
         WorldScreenOptionGrid.Builder lv4 = WorldScreenOptionGrid.builder(310).marginLeft(1);
         Text var10001 = MAP_FEATURES_TEXT;
         WorldCreator var10002 = CreateWorldScreen.this.worldCreator;
         Objects.requireNonNull(var10002);
         BooleanSupplier var7 = var10002::shouldGenerateStructures;
         WorldCreator var10003 = CreateWorldScreen.this.worldCreator;
         Objects.requireNonNull(var10003);
         lv4.add(var10001, var7, var10003::setGenerateStructures).toggleable(() -> {
            return !CreateWorldScreen.this.worldCreator.isDebug();
         }).tooltip(MAP_FEATURES_INFO_TEXT);
         var10001 = BONUS_ITEMS_TEXT;
         var10002 = CreateWorldScreen.this.worldCreator;
         Objects.requireNonNull(var10002);
         var7 = var10002::isBonusChestEnabled;
         var10003 = CreateWorldScreen.this.worldCreator;
         Objects.requireNonNull(var10003);
         lv4.add(var10001, var7, var10003::setBonusChestEnabled).toggleable(() -> {
            return !CreateWorldScreen.this.worldCreator.isHardcore() && !CreateWorldScreen.this.worldCreator.isDebug();
         });
         WorldScreenOptionGrid lv5 = lv4.build((widget) -> {
            lv.add(widget, 2);
         });
         CreateWorldScreen.this.worldCreator.addListener((creator) -> {
            lv5.refresh();
         });
      }

      private void openCustomizeScreen() {
         LevelScreenProvider lv = CreateWorldScreen.this.worldCreator.getLevelScreenProvider();
         if (lv != null) {
            CreateWorldScreen.this.client.setScreen(lv.createEditScreen(CreateWorldScreen.this, CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder()));
         }

      }

      private CyclingButtonWidget.Values getWorldTypes() {
         return new CyclingButtonWidget.Values() {
            public List getCurrent() {
               return CyclingButtonWidget.HAS_ALT_DOWN.getAsBoolean() ? CreateWorldScreen.this.worldCreator.getExtendedWorldTypes() : CreateWorldScreen.this.worldCreator.getNormalWorldTypes();
            }

            public List getDefaults() {
               return CreateWorldScreen.this.worldCreator.getNormalWorldTypes();
            }
         };
      }

      private static MutableText getWorldTypeNarrationMessage(CyclingButtonWidget worldTypeButton) {
         return ((WorldCreator.WorldType)worldTypeButton.getValue()).isAmplified() ? ScreenTexts.joinSentences(worldTypeButton.getGenericNarrationMessage(), AMPLIFIED_GENERATOR_INFO_TEXT) : worldTypeButton.getGenericNarrationMessage();
      }

      public void tick() {
         this.seedField.tick();
      }

      static {
         SEED_INFO_TEXT = Text.translatable("selectWorld.seedInfo").formatted(Formatting.DARK_GRAY);
      }
   }

   @Environment(EnvType.CLIENT)
   private class MoreTab extends GridScreenTab {
      private static final Text MORE_TAB_TITLE_TEXT = Text.translatable("createWorld.tab.more.title");
      private static final Text GAME_RULES_TEXT = Text.translatable("selectWorld.gameRules");
      private static final Text DATA_PACKS_TEXT = Text.translatable("selectWorld.dataPacks");

      MoreTab() {
         super(MORE_TAB_TITLE_TEXT);
         GridWidget.Adder lv = this.grid.setRowSpacing(8).createAdder(1);
         lv.add(ButtonWidget.builder(GAME_RULES_TEXT, (button) -> {
            this.openGameRulesScreen();
         }).width(210).build());
         lv.add(ButtonWidget.builder(CreateWorldScreen.EXPERIMENTS_TEXT, (button) -> {
            CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
         }).width(210).build());
         lv.add(ButtonWidget.builder(DATA_PACKS_TEXT, (button) -> {
            CreateWorldScreen.this.openPackScreen(CreateWorldScreen.this.worldCreator.getGeneratorOptionsHolder().dataConfiguration());
         }).width(210).build());
      }

      private void openGameRulesScreen() {
         CreateWorldScreen.this.client.setScreen(new EditGameRulesScreen(CreateWorldScreen.this.worldCreator.getGameRules().copy(), (gameRules) -> {
            CreateWorldScreen.this.client.setScreen(CreateWorldScreen.this);
            WorldCreator var10001 = CreateWorldScreen.this.worldCreator;
            Objects.requireNonNull(var10001);
            gameRules.ifPresent(var10001::setGameRules);
         }));
      }
   }

   @Environment(EnvType.CLIENT)
   private static record WorldCreationSettings(WorldGenSettings worldGenSettings, DataConfiguration dataConfiguration) {
      WorldCreationSettings(WorldGenSettings arg, DataConfiguration arg2) {
         this.worldGenSettings = arg;
         this.dataConfiguration = arg2;
      }

      public WorldGenSettings worldGenSettings() {
         return this.worldGenSettings;
      }

      public DataConfiguration dataConfiguration() {
         return this.dataConfiguration;
      }
   }
}
