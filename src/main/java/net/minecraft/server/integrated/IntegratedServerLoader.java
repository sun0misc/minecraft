package net.minecraft.server.integrated;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.DatapackFailureScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class IntegratedServerLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftClient client;
   private final LevelStorage storage;

   public IntegratedServerLoader(MinecraftClient client, LevelStorage storage) {
      this.client = client;
      this.storage = storage;
   }

   public void start(Screen parent, String levelName) {
      this.start(parent, levelName, false, true);
   }

   public void createAndStart(String levelName, LevelInfo levelInfo, GeneratorOptions dynamicRegistryManager, Function dimensionsRegistrySupplier) {
      LevelStorage.Session lv = this.createSession(levelName);
      if (lv != null) {
         ResourcePackManager lv2 = VanillaDataPackProvider.createManager(lv);
         DataConfiguration lv3 = levelInfo.getDataConfiguration();

         try {
            SaveLoading.DataPacks lv4 = new SaveLoading.DataPacks(lv2, lv3, false, false);
            SaveLoader lv5 = (SaveLoader)this.load(lv4, (context) -> {
               DimensionOptionsRegistryHolder.DimensionsConfig lv = ((DimensionOptionsRegistryHolder)dimensionsRegistrySupplier.apply(context.worldGenRegistryManager())).toConfig(context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION));
               return new SaveLoading.LoadContext(new LevelProperties(levelInfo, dynamicRegistryManager, lv.specialWorldProperty(), lv.getLifecycle()), lv.toDynamicRegistryManager());
            }, SaveLoader::new);
            this.client.startIntegratedServer(levelName, lv, lv2, lv5, true);
         } catch (Exception var10) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", var10);
            close(lv, levelName);
         }

      }
   }

   @Nullable
   private LevelStorage.Session createSession(String levelName) {
      try {
         return this.storage.createSession(levelName);
      } catch (IOException var3) {
         LOGGER.warn("Failed to read level {} data", levelName, var3);
         SystemToast.addWorldAccessFailureToast(this.client, levelName);
         this.client.setScreen((Screen)null);
         return null;
      }
   }

   public void start(LevelStorage.Session session, DataPackContents dataPackContents, CombinedDynamicRegistries dynamicRegistryManager, SaveProperties saveProperties) {
      ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
      LifecycledResourceManager lv2 = (LifecycledResourceManager)(new SaveLoading.DataPacks(lv, saveProperties.getDataConfiguration(), false, false)).load().getSecond();
      this.client.startIntegratedServer(session.getDirectoryName(), session, lv, new SaveLoader(lv2, dataPackContents, dynamicRegistryManager, saveProperties), true);
   }

   private SaveLoader createSaveLoader(LevelStorage.Session session, boolean safeMode, ResourcePackManager dataPackManager) throws Exception {
      SaveLoading.DataPacks lv = this.createDataPackConfig(session, safeMode, dataPackManager);
      return (SaveLoader)this.load(lv, (context) -> {
         DynamicOps dynamicOps = RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)context.worldGenRegistryManager());
         Registry lv = context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION);
         Pair pair = session.readLevelProperties(dynamicOps, context.dataConfiguration(), lv, context.worldGenRegistryManager().getRegistryLifecycle());
         if (pair == null) {
            throw new IllegalStateException("Failed to load world");
         } else {
            return new SaveLoading.LoadContext((SaveProperties)pair.getFirst(), ((DimensionOptionsRegistryHolder.DimensionsConfig)pair.getSecond()).toDynamicRegistryManager());
         }
      }, SaveLoader::new);
   }

   public Pair loadForRecreation(LevelStorage.Session session) throws Exception {
      ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
      SaveLoading.DataPacks lv2 = this.createDataPackConfig(session, false, lv);
      return (Pair)this.load(lv2, (context) -> {
         DynamicOps dynamicOps = RegistryOps.of(NbtOps.INSTANCE, (RegistryWrapper.WrapperLookup)context.worldGenRegistryManager());
         Registry lv = (new SimpleRegistry(RegistryKeys.DIMENSION, Lifecycle.stable())).freeze();
         Pair pair = session.readLevelProperties(dynamicOps, context.dataConfiguration(), lv, context.worldGenRegistryManager().getRegistryLifecycle());
         if (pair == null) {
            throw new IllegalStateException("Failed to load world");
         } else {
            @Environment(EnvType.CLIENT)
            record CurrentSettings(LevelInfo levelInfo, GeneratorOptions options, Registry existingDimensionRegistry) {
               final LevelInfo levelInfo;
               final GeneratorOptions options;
               final Registry existingDimensionRegistry;

               CurrentSettings(LevelInfo arg, GeneratorOptions arg2, Registry arg3) {
                  this.levelInfo = arg;
                  this.options = arg2;
                  this.existingDimensionRegistry = arg3;
               }

               public LevelInfo levelInfo() {
                  return this.levelInfo;
               }

               public GeneratorOptions options() {
                  return this.options;
               }

               public Registry existingDimensionRegistry() {
                  return this.existingDimensionRegistry;
               }
            }

            return new SaveLoading.LoadContext(new CurrentSettings(((SaveProperties)pair.getFirst()).getLevelInfo(), ((SaveProperties)pair.getFirst()).getGeneratorOptions(), ((DimensionOptionsRegistryHolder.DimensionsConfig)pair.getSecond()).dimensions()), context.dimensionsRegistryManager());
         }
      }, (resourceManager, dataPackContents, combinedRegistryManager, currentSettings) -> {
         resourceManager.close();
         return Pair.of(currentSettings.levelInfo, new GeneratorOptionsHolder(currentSettings.options, new DimensionOptionsRegistryHolder(currentSettings.existingDimensionRegistry), combinedRegistryManager, dataPackContents, currentSettings.levelInfo.getDataConfiguration()));
      });
   }

   private SaveLoading.DataPacks createDataPackConfig(LevelStorage.Session session, boolean safeMode, ResourcePackManager dataPackManager) {
      DataConfiguration lv = session.getDataPackSettings();
      if (lv == null) {
         throw new IllegalStateException("Failed to load data pack config");
      } else {
         return new SaveLoading.DataPacks(dataPackManager, lv, safeMode, false);
      }
   }

   public SaveLoader createSaveLoader(LevelStorage.Session session, boolean safeMode) throws Exception {
      ResourcePackManager lv = VanillaDataPackProvider.createManager(session);
      return this.createSaveLoader(session, safeMode, lv);
   }

   private Object load(SaveLoading.DataPacks dataPacks, SaveLoading.LoadContextSupplier loadContextSupplier, SaveLoading.SaveApplierFactory saveApplierFactory) throws Exception {
      SaveLoading.ServerConfig lv = new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.INTEGRATED, 2);
      CompletableFuture completableFuture = SaveLoading.load(lv, loadContextSupplier, saveApplierFactory, Util.getMainWorkerExecutor(), this.client);
      MinecraftClient var10000 = this.client;
      Objects.requireNonNull(completableFuture);
      var10000.runTasks(completableFuture::isDone);
      return completableFuture.get();
   }

   private void start(Screen parent, String levelName, boolean safeMode, boolean canShowBackupPrompt) {
      LevelStorage.Session lv = this.createSession(levelName);
      if (lv != null) {
         ResourcePackManager lv2 = VanillaDataPackProvider.createManager(lv);

         SaveLoader lv3;
         try {
            lv3 = this.createSaveLoader(lv, safeMode, lv2);
         } catch (Exception var11) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", var11);
            if (!safeMode) {
               this.client.setScreen(new DatapackFailureScreen(() -> {
                  this.start(parent, levelName, true, canShowBackupPrompt);
               }));
            } else {
               this.client.setScreen(new NoticeScreen(() -> {
                  this.client.setScreen((Screen)null);
               }, Text.translatable("datapackFailure.safeMode.failed.title"), Text.translatable("datapackFailure.safeMode.failed.description"), ScreenTexts.TO_TITLE, true));
            }

            close(lv, levelName);
            return;
         }

         SaveProperties lv4 = lv3.saveProperties();
         boolean bl3 = lv4.getGeneratorOptions().isLegacyCustomizedType();
         boolean bl4 = lv4.getLifecycle() != Lifecycle.stable();
         if (!canShowBackupPrompt || !bl3 && !bl4) {
            this.client.getServerResourcePackProvider().loadServerPack(lv).thenApply((void_) -> {
               return true;
            }).exceptionallyComposeAsync((throwable) -> {
               LOGGER.warn("Failed to load pack: ", throwable);
               return this.showPackLoadFailureScreen();
            }, this.client).thenAcceptAsync((proceed) -> {
               if (proceed) {
                  this.client.startIntegratedServer(levelName, lv, lv2, lv3, false);
               } else {
                  lv3.close();
                  close(lv, levelName);
                  this.client.getServerResourcePackProvider().clear().thenRunAsync(() -> {
                     this.client.setScreen(parent);
                  }, this.client);
               }

            }, this.client).exceptionally((throwable) -> {
               this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Load world"));
               return null;
            });
         } else {
            this.showBackupPromptScreen(parent, levelName, bl3, () -> {
               this.start(parent, levelName, safeMode, false);
            });
            lv3.close();
            close(lv, levelName);
         }
      }
   }

   private CompletableFuture showPackLoadFailureScreen() {
      CompletableFuture completableFuture = new CompletableFuture();
      MinecraftClient var10000 = this.client;
      Objects.requireNonNull(completableFuture);
      var10000.setScreen(new ConfirmScreen(completableFuture::complete, Text.translatable("multiplayer.texturePrompt.failure.line1"), Text.translatable("multiplayer.texturePrompt.failure.line2"), ScreenTexts.PROCEED, ScreenTexts.CANCEL));
      return completableFuture;
   }

   private static void close(LevelStorage.Session session, String levelName) {
      try {
         session.close();
      } catch (IOException var3) {
         LOGGER.warn("Failed to unlock access to level {}", levelName, var3);
      }

   }

   private void showBackupPromptScreen(Screen parent, String levelName, boolean customized, Runnable callback) {
      MutableText lv;
      MutableText lv2;
      if (customized) {
         lv = Text.translatable("selectWorld.backupQuestion.customized");
         lv2 = Text.translatable("selectWorld.backupWarning.customized");
      } else {
         lv = Text.translatable("selectWorld.backupQuestion.experimental");
         lv2 = Text.translatable("selectWorld.backupWarning.experimental");
      }

      this.client.setScreen(new BackupPromptScreen(parent, (backup, eraseCache) -> {
         if (backup) {
            EditWorldScreen.onBackupConfirm(this.storage, levelName);
         }

         callback.run();
      }, lv, lv2, false));
   }

   public static void tryLoad(MinecraftClient client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings) {
      BooleanConsumer booleanConsumer = (confirmed) -> {
         if (confirmed) {
            loader.run();
         } else {
            client.setScreen(parent);
         }

      };
      if (!bypassWarnings && lifecycle != Lifecycle.stable()) {
         if (lifecycle == Lifecycle.experimental()) {
            client.setScreen(new ConfirmScreen(booleanConsumer, Text.translatable("selectWorld.warning.experimental.title"), Text.translatable("selectWorld.warning.experimental.question")));
         } else {
            client.setScreen(new ConfirmScreen(booleanConsumer, Text.translatable("selectWorld.warning.deprecated.title"), Text.translatable("selectWorld.warning.deprecated.question")));
         }
      } else {
         loader.run();
      }

   }
}
