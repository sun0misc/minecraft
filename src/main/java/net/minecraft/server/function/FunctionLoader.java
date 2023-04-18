package net.minecraft.server.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

public class FunctionLoader implements ResourceReloader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceFinder FINDER = new ResourceFinder("functions", ".mcfunction");
   private volatile Map functions = ImmutableMap.of();
   private final TagGroupLoader tagLoader = new TagGroupLoader(this::get, "tags/functions");
   private volatile Map tags = Map.of();
   private final int level;
   private final CommandDispatcher commandDispatcher;

   public Optional get(Identifier id) {
      return Optional.ofNullable((CommandFunction)this.functions.get(id));
   }

   public Map getFunctions() {
      return this.functions;
   }

   public Collection getTagOrEmpty(Identifier id) {
      return (Collection)this.tags.getOrDefault(id, List.of());
   }

   public Iterable getTags() {
      return this.tags.keySet();
   }

   public FunctionLoader(int level, CommandDispatcher commandDispatcher) {
      this.level = level;
      this.commandDispatcher = commandDispatcher;
   }

   public CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      CompletableFuture completableFuture = CompletableFuture.supplyAsync(() -> {
         return this.tagLoader.loadTags(manager);
      }, prepareExecutor);
      CompletableFuture completableFuture2 = CompletableFuture.supplyAsync(() -> {
         return FINDER.findResources(manager);
      }, prepareExecutor).thenCompose((functions) -> {
         Map map2 = Maps.newHashMap();
         ServerCommandSource lv = new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ZERO, Vec2f.ZERO, (ServerWorld)null, this.level, "", ScreenTexts.EMPTY, (MinecraftServer)null, (Entity)null);
         Iterator var5 = functions.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            Identifier lv2 = (Identifier)entry.getKey();
            Identifier lv3 = FINDER.toResourceId(lv2);
            map2.put(lv3, CompletableFuture.supplyAsync(() -> {
               List list = readLines((Resource)entry.getValue());
               return CommandFunction.create(lv3, this.commandDispatcher, lv, list);
            }, prepareExecutor));
         }

         CompletableFuture[] completableFutures = (CompletableFuture[])map2.values().toArray(new CompletableFuture[0]);
         return CompletableFuture.allOf(completableFutures).handle((unused, ex) -> {
            return map2;
         });
      });
      CompletableFuture var10000 = completableFuture.thenCombine(completableFuture2, Pair::of);
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((intermediate) -> {
         Map map = (Map)intermediate.getSecond();
         ImmutableMap.Builder builder = ImmutableMap.builder();
         map.forEach((id, functionFuture) -> {
            functionFuture.handle((function, ex) -> {
               if (ex != null) {
                  LOGGER.error("Failed to load function {}", id, ex);
               } else {
                  builder.put(id, function);
               }

               return null;
            }).join();
         });
         this.functions = builder.build();
         this.tags = this.tagLoader.buildGroup((Map)intermediate.getFirst());
      }, applyExecutor);
   }

   private static List readLines(Resource resource) {
      try {
         BufferedReader bufferedReader = resource.getReader();

         List var2;
         try {
            var2 = bufferedReader.lines().toList();
         } catch (Throwable var5) {
            if (bufferedReader != null) {
               try {
                  bufferedReader.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (bufferedReader != null) {
            bufferedReader.close();
         }

         return var2;
      } catch (IOException var6) {
         throw new CompletionException(var6);
      }
   }
}
