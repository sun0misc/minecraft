package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.loot.LootManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.slf4j.Logger;

public class DataPackContents {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final CompletableFuture COMPLETED_UNIT;
   private final CommandRegistryAccess.EntryListCreationPolicySettable commandRegistryAccess;
   private final CommandManager commandManager;
   private final RecipeManager recipeManager = new RecipeManager();
   private final TagManagerLoader registryTagManager;
   private final LootManager lootManager = new LootManager();
   private final ServerAdvancementLoader serverAdvancementLoader;
   private final FunctionLoader functionLoader;

   public DataPackContents(DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel) {
      this.serverAdvancementLoader = new ServerAdvancementLoader(this.lootManager);
      this.registryTagManager = new TagManagerLoader(dynamicRegistryManager);
      this.commandRegistryAccess = CommandRegistryAccess.of((DynamicRegistryManager)dynamicRegistryManager, enabledFeatures);
      this.commandManager = new CommandManager(environment, this.commandRegistryAccess);
      this.commandRegistryAccess.setEntryListCreationPolicy(CommandRegistryAccess.EntryListCreationPolicy.CREATE_NEW);
      this.functionLoader = new FunctionLoader(functionPermissionLevel, this.commandManager.getDispatcher());
   }

   public FunctionLoader getFunctionLoader() {
      return this.functionLoader;
   }

   public LootManager getLootManager() {
      return this.lootManager;
   }

   public RecipeManager getRecipeManager() {
      return this.recipeManager;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   public ServerAdvancementLoader getServerAdvancementLoader() {
      return this.serverAdvancementLoader;
   }

   public List getContents() {
      return List.of(this.registryTagManager, this.lootManager, this.recipeManager, this.functionLoader, this.serverAdvancementLoader);
   }

   public static CompletableFuture reload(ResourceManager manager, DynamicRegistryManager.Immutable dynamicRegistryManager, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor) {
      DataPackContents lv = new DataPackContents(dynamicRegistryManager, enabledFeatures, environment, functionPermissionLevel);
      return SimpleResourceReload.start(manager, lv.getContents(), prepareExecutor, applyExecutor, COMPLETED_UNIT, LOGGER.isDebugEnabled()).whenComplete().whenComplete((void_, throwable) -> {
         lv.commandRegistryAccess.setEntryListCreationPolicy(CommandRegistryAccess.EntryListCreationPolicy.FAIL);
      }).thenApply((void_) -> {
         return lv;
      });
   }

   public void refresh(DynamicRegistryManager dynamicRegistryManager) {
      this.registryTagManager.getRegistryTags().forEach((tags) -> {
         repopulateTags(dynamicRegistryManager, tags);
      });
      Blocks.refreshShapeCache();
   }

   private static void repopulateTags(DynamicRegistryManager dynamicRegistryManager, TagManagerLoader.RegistryTags tags) {
      RegistryKey lv = tags.key();
      Map map = (Map)tags.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap((entry) -> {
         return TagKey.of(lv, (Identifier)entry.getKey());
      }, (entry) -> {
         return List.copyOf((Collection)entry.getValue());
      }));
      dynamicRegistryManager.get(lv).populateTags(map);
   }

   static {
      COMPLETED_UNIT = CompletableFuture.completedFuture(Unit.INSTANCE);
   }
}
