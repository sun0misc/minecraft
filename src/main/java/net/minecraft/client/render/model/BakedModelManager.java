package net.minecraft.client.render.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BakedModelManager implements ResourceReloader, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map LAYERS_TO_LOADERS;
   private Map models;
   private final SpriteAtlasManager atlasManager;
   private final BlockModels blockModelCache;
   private final BlockColors colorMap;
   private int mipmapLevels;
   private BakedModel missingModel;
   private Object2IntMap stateLookup;

   public BakedModelManager(TextureManager textureManager, BlockColors colorMap, int mipmap) {
      this.colorMap = colorMap;
      this.mipmapLevels = mipmap;
      this.blockModelCache = new BlockModels(this);
      this.atlasManager = new SpriteAtlasManager(LAYERS_TO_LOADERS, textureManager);
   }

   public BakedModel getModel(ModelIdentifier id) {
      return (BakedModel)this.models.getOrDefault(id, this.missingModel);
   }

   public BakedModel getMissingModel() {
      return this.missingModel;
   }

   public BlockModels getBlockModels() {
      return this.blockModelCache;
   }

   public final CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      prepareProfiler.startTick();
      CompletableFuture completableFuture = reloadModels(manager, prepareExecutor);
      CompletableFuture completableFuture2 = reloadBlockStates(manager, prepareExecutor);
      CompletableFuture completableFuture3 = completableFuture.thenCombineAsync(completableFuture2, (jsonUnbakedModels, blockStates) -> {
         return new ModelLoader(this.colorMap, prepareProfiler, jsonUnbakedModels, blockStates);
      }, prepareExecutor);
      Map map = this.atlasManager.reload(manager, this.mipmapLevels, prepareExecutor);
      CompletableFuture var10000 = CompletableFuture.allOf((CompletableFuture[])Stream.concat(map.values().stream(), Stream.of(completableFuture3)).toArray((i) -> {
         return new CompletableFuture[i];
      })).thenApplyAsync((void_1) -> {
         return this.bake(prepareProfiler, (Map)map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> {
            return (SpriteAtlasManager.AtlasPreparation)((CompletableFuture)entry.getValue()).join();
         })), (ModelLoader)completableFuture3.join());
      }, prepareExecutor).thenCompose((result) -> {
         return result.readyForUpload.thenApply((void_) -> {
            return result;
         });
      });
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((result) -> {
         this.upload(result, applyProfiler);
      }, applyExecutor);
   }

   private static CompletableFuture reloadModels(ResourceManager resourceManager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         return ModelLoader.MODELS_FINDER.findResources(resourceManager);
      }, executor).thenCompose((models) -> {
         List list = new ArrayList(models.size());
         Iterator var3 = models.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            list.add(CompletableFuture.supplyAsync(() -> {
               try {
                  Reader reader = ((Resource)entry.getValue()).getReader();

                  Pair var2;
                  try {
                     var2 = Pair.of((Identifier)entry.getKey(), JsonUnbakedModel.deserialize((Reader)reader));
                  } catch (Throwable var5) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var4) {
                           var5.addSuppressed(var4);
                        }
                     }

                     throw var5;
                  }

                  if (reader != null) {
                     reader.close();
                  }

                  return var2;
               } catch (Exception var6) {
                  LOGGER.error("Failed to load model {}", entry.getKey(), var6);
                  return null;
               }
            }, executor));
         }

         return Util.combineSafe(list).thenApply((modelsx) -> {
            return (Map)modelsx.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));
         });
      });
   }

   private static CompletableFuture reloadBlockStates(ResourceManager resourceManager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         return ModelLoader.BLOCK_STATES_FINDER.findAllResources(resourceManager);
      }, executor).thenCompose((blockStates) -> {
         List list = new ArrayList(blockStates.size());
         Iterator var3 = blockStates.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            list.add(CompletableFuture.supplyAsync(() -> {
               List list = (List)entry.getValue();
               List list2 = new ArrayList(list.size());
               Iterator var3 = list.iterator();

               while(var3.hasNext()) {
                  Resource lv = (Resource)var3.next();

                  try {
                     Reader reader = lv.getReader();

                     try {
                        JsonObject jsonObject = JsonHelper.deserialize((Reader)reader);
                        list2.add(new ModelLoader.SourceTrackedData(lv.getResourcePackName(), jsonObject));
                     } catch (Throwable var9) {
                        if (reader != null) {
                           try {
                              reader.close();
                           } catch (Throwable var8) {
                              var9.addSuppressed(var8);
                           }
                        }

                        throw var9;
                     }

                     if (reader != null) {
                        reader.close();
                     }
                  } catch (Exception var10) {
                     LOGGER.error("Failed to load blockstate {} from pack {}", new Object[]{entry.getKey(), lv.getResourcePackName(), var10});
                  }
               }

               return Pair.of((Identifier)entry.getKey(), list2);
            }, executor));
         }

         return Util.combineSafe(list).thenApply((blockStatesx) -> {
            return (Map)blockStatesx.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));
         });
      });
   }

   private BakingResult bake(Profiler profiler, Map preparations, ModelLoader modelLoader) {
      profiler.push("load");
      profiler.swap("baking");
      Multimap multimap = HashMultimap.create();
      modelLoader.bake((id, spriteId) -> {
         SpriteAtlasManager.AtlasPreparation lv = (SpriteAtlasManager.AtlasPreparation)preparations.get(spriteId.getAtlasId());
         Sprite lv2 = lv.getSprite(spriteId.getTextureId());
         if (lv2 != null) {
            return lv2;
         } else {
            multimap.put(id, spriteId);
            return lv.getMissingSprite();
         }
      });
      multimap.asMap().forEach((modelId, spriteIds) -> {
         LOGGER.warn("Missing textures in model {}:\n{}", modelId, spriteIds.stream().sorted(SpriteIdentifier.COMPARATOR).map((arg) -> {
            Identifier var10000 = arg.getAtlasId();
            return "    " + var10000 + ":" + arg.getTextureId();
         }).collect(Collectors.joining("\n")));
      });
      profiler.swap("dispatch");
      Map map2 = modelLoader.getBakedModelMap();
      BakedModel lv = (BakedModel)map2.get(ModelLoader.MISSING_ID);
      Map map3 = new IdentityHashMap();
      Iterator var8 = Registries.BLOCK.iterator();

      while(var8.hasNext()) {
         Block lv2 = (Block)var8.next();
         lv2.getStateManager().getStates().forEach((state) -> {
            Identifier lvx = state.getBlock().getRegistryEntry().registryKey().getValue();
            BakedModel lv2 = (BakedModel)map2.getOrDefault(BlockModels.getModelId(lvx, state), lv);
            map3.put(state, lv2);
         });
      }

      CompletableFuture completableFuture = CompletableFuture.allOf((CompletableFuture[])preparations.values().stream().map(SpriteAtlasManager.AtlasPreparation::whenComplete).toArray((i) -> {
         return new CompletableFuture[i];
      }));
      profiler.pop();
      profiler.endTick();
      return new BakingResult(modelLoader, lv, map3, preparations, completableFuture);
   }

   private void upload(BakingResult bakingResult, Profiler profiler) {
      profiler.startTick();
      profiler.push("upload");
      bakingResult.atlasPreparations.values().forEach(SpriteAtlasManager.AtlasPreparation::upload);
      ModelLoader lv = bakingResult.modelLoader;
      this.models = lv.getBakedModelMap();
      this.stateLookup = lv.getStateLookup();
      this.missingModel = bakingResult.missingModel;
      profiler.swap("cache");
      this.blockModelCache.setModels(bakingResult.modelCache);
      profiler.pop();
      profiler.endTick();
   }

   public boolean shouldRerender(BlockState from, BlockState to) {
      if (from == to) {
         return false;
      } else {
         int i = this.stateLookup.getInt(from);
         if (i != -1) {
            int j = this.stateLookup.getInt(to);
            if (i == j) {
               FluidState lv = from.getFluidState();
               FluidState lv2 = to.getFluidState();
               return lv != lv2;
            }
         }

         return true;
      }
   }

   public SpriteAtlasTexture getAtlas(Identifier id) {
      return this.atlasManager.getAtlas(id);
   }

   public void close() {
      this.atlasManager.close();
   }

   public void setMipmapLevels(int mipmapLevels) {
      this.mipmapLevels = mipmapLevels;
   }

   static {
      LAYERS_TO_LOADERS = Map.of(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, new Identifier("banner_patterns"), TexturedRenderLayers.BEDS_ATLAS_TEXTURE, new Identifier("beds"), TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier("chests"), TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, new Identifier("shield_patterns"), TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, new Identifier("signs"), TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("shulker_boxes"), TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE, new Identifier("armor_trims"), TexturedRenderLayers.DECORATED_POT_ATLAS_TEXTURE, new Identifier("decorated_pot"), SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("blocks"));
   }

   @Environment(EnvType.CLIENT)
   private static record BakingResult(ModelLoader modelLoader, BakedModel missingModel, Map modelCache, Map atlasPreparations, CompletableFuture readyForUpload) {
      final ModelLoader modelLoader;
      final BakedModel missingModel;
      final Map modelCache;
      final Map atlasPreparations;
      final CompletableFuture readyForUpload;

      BakingResult(ModelLoader arg, BakedModel arg2, Map map, Map map2, CompletableFuture completableFuture) {
         this.modelLoader = arg;
         this.missingModel = arg2;
         this.modelCache = map;
         this.atlasPreparations = map2;
         this.readyForUpload = completableFuture;
      }

      public ModelLoader modelLoader() {
         return this.modelLoader;
      }

      public BakedModel missingModel() {
         return this.missingModel;
      }

      public Map modelCache() {
         return this.modelCache;
      }

      public Map atlasPreparations() {
         return this.atlasPreparations;
      }

      public CompletableFuture readyForUpload() {
         return this.readyForUpload;
      }
   }
}
