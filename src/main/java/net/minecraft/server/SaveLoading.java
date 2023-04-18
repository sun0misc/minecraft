package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;

public class SaveLoading {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static CompletableFuture load(ServerConfig serverConfig, LoadContextSupplier loadContextSupplier, SaveApplierFactory saveApplierFactory, Executor prepareExecutor, Executor applyExecutor) {
      try {
         Pair pair = serverConfig.dataPacks.load();
         LifecycledResourceManager lv = (LifecycledResourceManager)pair.getSecond();
         CombinedDynamicRegistries lv2 = ServerDynamicRegistryType.createCombinedDynamicRegistries();
         CombinedDynamicRegistries lv3 = withRegistriesLoaded(lv, lv2, ServerDynamicRegistryType.WORLDGEN, RegistryLoader.DYNAMIC_REGISTRIES);
         DynamicRegistryManager.Immutable lv4 = lv3.getPrecedingRegistryManagers(ServerDynamicRegistryType.DIMENSIONS);
         DynamicRegistryManager.Immutable lv5 = RegistryLoader.load(lv, lv4, RegistryLoader.DIMENSION_REGISTRIES);
         DataConfiguration lv6 = (DataConfiguration)pair.getFirst();
         LoadContext lv7 = loadContextSupplier.get(new LoadContextSupplierContext(lv, lv6, lv4, lv5));
         CombinedDynamicRegistries lv8 = lv3.with(ServerDynamicRegistryType.DIMENSIONS, (DynamicRegistryManager.Immutable[])(lv7.dimensionsRegistryManager));
         DynamicRegistryManager.Immutable lv9 = lv8.getPrecedingRegistryManagers(ServerDynamicRegistryType.RELOADABLE);
         return DataPackContents.reload(lv, lv9, lv6.enabledFeatures(), serverConfig.commandEnvironment(), serverConfig.functionPermissionLevel(), prepareExecutor, applyExecutor).whenComplete((dataPackContents, throwable) -> {
            if (throwable != null) {
               lv.close();
            }

         }).thenApplyAsync((dataPackContents) -> {
            dataPackContents.refresh(lv9);
            return saveApplierFactory.create(lv, dataPackContents, lv8, lv7.extraData);
         }, applyExecutor);
      } catch (Exception var15) {
         return CompletableFuture.failedFuture(var15);
      }
   }

   private static DynamicRegistryManager.Immutable loadDynamicRegistryManager(ResourceManager resourceManager, CombinedDynamicRegistries combinedDynamicRegistries, ServerDynamicRegistryType type, List entries) {
      DynamicRegistryManager.Immutable lv = combinedDynamicRegistries.getPrecedingRegistryManagers(type);
      return RegistryLoader.load(resourceManager, lv, entries);
   }

   private static CombinedDynamicRegistries withRegistriesLoaded(ResourceManager resourceManager, CombinedDynamicRegistries combinedDynamicRegistries, ServerDynamicRegistryType type, List entries) {
      DynamicRegistryManager.Immutable lv = loadDynamicRegistryManager(resourceManager, combinedDynamicRegistries, type, entries);
      return combinedDynamicRegistries.with(type, (DynamicRegistryManager.Immutable[])(lv));
   }

   public static record ServerConfig(DataPacks dataPacks, CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel) {
      final DataPacks dataPacks;

      public ServerConfig(DataPacks arg, CommandManager.RegistrationEnvironment arg2, int i) {
         this.dataPacks = arg;
         this.commandEnvironment = arg2;
         this.functionPermissionLevel = i;
      }

      public DataPacks dataPacks() {
         return this.dataPacks;
      }

      public CommandManager.RegistrationEnvironment commandEnvironment() {
         return this.commandEnvironment;
      }

      public int functionPermissionLevel() {
         return this.functionPermissionLevel;
      }
   }

   public static record DataPacks(ResourcePackManager manager, DataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
      public DataPacks(ResourcePackManager arg, DataConfiguration arg2, boolean bl, boolean bl2) {
         this.manager = arg;
         this.initialDataConfig = arg2;
         this.safeMode = bl;
         this.initMode = bl2;
      }

      public Pair load() {
         FeatureSet lv = this.initMode ? FeatureFlags.FEATURE_MANAGER.getFeatureSet() : this.initialDataConfig.enabledFeatures();
         DataConfiguration lv2 = MinecraftServer.loadDataPacks(this.manager, this.initialDataConfig.dataPacks(), this.safeMode, lv);
         if (!this.initMode) {
            lv2 = lv2.withFeaturesAdded(this.initialDataConfig.enabledFeatures());
         }

         List list = this.manager.createResourcePacks();
         LifecycledResourceManager lv3 = new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, list);
         return Pair.of(lv2, lv3);
      }

      public ResourcePackManager manager() {
         return this.manager;
      }

      public DataConfiguration initialDataConfig() {
         return this.initialDataConfig;
      }

      public boolean safeMode() {
         return this.safeMode;
      }

      public boolean initMode() {
         return this.initMode;
      }
   }

   public static record LoadContextSupplierContext(ResourceManager resourceManager, DataConfiguration dataConfiguration, DynamicRegistryManager.Immutable worldGenRegistryManager, DynamicRegistryManager.Immutable dimensionsRegistryManager) {
      public LoadContextSupplierContext(ResourceManager arg, DataConfiguration arg2, DynamicRegistryManager.Immutable arg3, DynamicRegistryManager.Immutable arg4) {
         this.resourceManager = arg;
         this.dataConfiguration = arg2;
         this.worldGenRegistryManager = arg3;
         this.dimensionsRegistryManager = arg4;
      }

      public ResourceManager resourceManager() {
         return this.resourceManager;
      }

      public DataConfiguration dataConfiguration() {
         return this.dataConfiguration;
      }

      public DynamicRegistryManager.Immutable worldGenRegistryManager() {
         return this.worldGenRegistryManager;
      }

      public DynamicRegistryManager.Immutable dimensionsRegistryManager() {
         return this.dimensionsRegistryManager;
      }
   }

   @FunctionalInterface
   public interface LoadContextSupplier {
      LoadContext get(LoadContextSupplierContext context);
   }

   public static record LoadContext(Object extraData, DynamicRegistryManager.Immutable dimensionsRegistryManager) {
      final Object extraData;
      final DynamicRegistryManager.Immutable dimensionsRegistryManager;

      public LoadContext(Object object, DynamicRegistryManager.Immutable arg) {
         this.extraData = object;
         this.dimensionsRegistryManager = arg;
      }

      public Object extraData() {
         return this.extraData;
      }

      public DynamicRegistryManager.Immutable dimensionsRegistryManager() {
         return this.dimensionsRegistryManager;
      }
   }

   @FunctionalInterface
   public interface SaveApplierFactory {
      Object create(LifecycledResourceManager resourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries combinedDynamicRegistries, Object loadContext);
   }
}
