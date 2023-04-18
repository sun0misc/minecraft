package net.minecraft.registry.tag;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;

public class TagManagerLoader implements ResourceReloader {
   private static final Map DIRECTORIES;
   private final DynamicRegistryManager registryManager;
   private List registryTags = List.of();

   public TagManagerLoader(DynamicRegistryManager registryManager) {
      this.registryManager = registryManager;
   }

   public List getRegistryTags() {
      return this.registryTags;
   }

   public static String getPath(RegistryKey registry) {
      String string = (String)DIRECTORIES.get(registry);
      return string != null ? string : "tags/" + registry.getValue().getPath();
   }

   public CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      List list = this.registryManager.streamAllRegistries().map((registry) -> {
         return this.buildRequiredGroup(manager, prepareExecutor, registry);
      }).toList();
      CompletableFuture var10000 = CompletableFuture.allOf((CompletableFuture[])list.toArray((i) -> {
         return new CompletableFuture[i];
      }));
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((void_) -> {
         this.registryTags = (List)list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList());
      }, applyExecutor);
   }

   private CompletableFuture buildRequiredGroup(ResourceManager resourceManager, Executor prepareExecutor, DynamicRegistryManager.Entry requirement) {
      RegistryKey lv = requirement.key();
      Registry lv2 = requirement.value();
      TagGroupLoader lv3 = new TagGroupLoader((id) -> {
         return lv2.getEntry(RegistryKey.of(lv, id));
      }, getPath(lv));
      return CompletableFuture.supplyAsync(() -> {
         return new RegistryTags(lv, lv3.load(resourceManager));
      }, prepareExecutor);
   }

   static {
      DIRECTORIES = Map.of(RegistryKeys.BLOCK, "tags/blocks", RegistryKeys.ENTITY_TYPE, "tags/entity_types", RegistryKeys.FLUID, "tags/fluids", RegistryKeys.GAME_EVENT, "tags/game_events", RegistryKeys.ITEM, "tags/items");
   }

   public static record RegistryTags(RegistryKey key, Map tags) {
      public RegistryTags(RegistryKey arg, Map map) {
         this.key = arg;
         this.tags = map;
      }

      public RegistryKey key() {
         return this.key;
      }

      public Map tags() {
         return this.tags;
      }
   }
}
