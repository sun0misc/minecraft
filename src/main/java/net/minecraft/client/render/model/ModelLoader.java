package net.minecraft.client.render.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelLoader {
   public static final SpriteIdentifier FIRE_0;
   public static final SpriteIdentifier FIRE_1;
   public static final SpriteIdentifier LAVA_FLOW;
   public static final SpriteIdentifier WATER_FLOW;
   public static final SpriteIdentifier WATER_OVERLAY;
   public static final SpriteIdentifier BANNER_BASE;
   public static final SpriteIdentifier SHIELD_BASE;
   public static final SpriteIdentifier SHIELD_BASE_NO_PATTERN;
   public static final int field_32983 = 10;
   public static final List BLOCK_DESTRUCTION_STAGES;
   public static final List BLOCK_DESTRUCTION_STAGE_TEXTURES;
   public static final List BLOCK_DESTRUCTION_RENDER_LAYERS;
   static final int field_32984 = -1;
   private static final int field_32985 = 0;
   private static final Logger LOGGER;
   private static final String BUILTIN = "builtin/";
   private static final String BUILTIN_GENERATED = "builtin/generated";
   private static final String BUILTIN_ENTITY = "builtin/entity";
   private static final String MISSING = "missing";
   public static final ModelIdentifier MISSING_ID;
   public static final ResourceFinder BLOCK_STATES_FINDER;
   public static final ResourceFinder MODELS_FINDER;
   @VisibleForTesting
   public static final String MISSING_DEFINITION;
   private static final Map BUILTIN_MODEL_DEFINITIONS;
   private static final Splitter COMMA_SPLITTER;
   private static final Splitter KEY_VALUE_SPLITTER;
   public static final JsonUnbakedModel GENERATION_MARKER;
   public static final JsonUnbakedModel BLOCK_ENTITY_MARKER;
   private static final StateManager ITEM_FRAME_STATE_FACTORY;
   static final ItemModelGenerator ITEM_MODEL_GENERATOR;
   private static final Map STATIC_DEFINITIONS;
   private final BlockColors blockColors;
   private final Map jsonUnbakedModels;
   private final Map blockStates;
   private final Set modelsToLoad = Sets.newHashSet();
   private final ModelVariantMap.DeserializationContext variantMapDeserializationContext = new ModelVariantMap.DeserializationContext();
   private final Map unbakedModels = Maps.newHashMap();
   final Map bakedModelCache = Maps.newHashMap();
   private final Map modelsToBake = Maps.newHashMap();
   private final Map bakedModels = Maps.newHashMap();
   private int nextStateId = 1;
   private final Object2IntMap stateLookup = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), (map) -> {
      map.defaultReturnValue(-1);
   });

   public ModelLoader(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates) {
      this.blockColors = blockColors;
      this.jsonUnbakedModels = jsonUnbakedModels;
      this.blockStates = blockStates;
      profiler.push("missing_model");

      try {
         this.unbakedModels.put(MISSING_ID, this.loadModelFromJson(MISSING_ID));
         this.addModel(MISSING_ID);
      } catch (IOException var7) {
         LOGGER.error("Error loading missing model, should never happen :(", var7);
         throw new RuntimeException(var7);
      }

      profiler.swap("static_definitions");
      STATIC_DEFINITIONS.forEach((id, stateManager) -> {
         stateManager.getStates().forEach((state) -> {
            this.addModel(BlockModels.getModelId(id, state));
         });
      });
      profiler.swap("blocks");
      Iterator var5 = Registries.BLOCK.iterator();

      while(var5.hasNext()) {
         Block lv = (Block)var5.next();
         lv.getStateManager().getStates().forEach((state) -> {
            this.addModel(BlockModels.getModelId(state));
         });
      }

      profiler.swap("items");
      var5 = Registries.ITEM.getIds().iterator();

      while(var5.hasNext()) {
         Identifier lv2 = (Identifier)var5.next();
         this.addModel(new ModelIdentifier(lv2, "inventory"));
      }

      profiler.swap("special");
      this.addModel(ItemRenderer.TRIDENT_IN_HAND);
      this.addModel(ItemRenderer.SPYGLASS_IN_HAND);
      this.modelsToBake.values().forEach((model) -> {
         model.setParents(this::getOrLoadModel);
      });
      profiler.pop();
   }

   public void bake(BiFunction spriteLoader) {
      this.modelsToBake.keySet().forEach((modelId) -> {
         BakedModel lv = null;

         try {
            lv = (new BakerImpl(spriteLoader, modelId)).bake(modelId, ModelRotation.X0_Y0);
         } catch (Exception var5) {
            LOGGER.warn("Unable to bake model: '{}': {}", modelId, var5);
         }

         if (lv != null) {
            this.bakedModels.put(modelId, lv);
         }

      });
   }

   private static Predicate stateKeyToPredicate(StateManager stateFactory, String key) {
      Map map = Maps.newHashMap();
      Iterator var3 = COMMA_SPLITTER.split(key).iterator();

      while(true) {
         while(true) {
            Iterator iterator;
            do {
               if (!var3.hasNext()) {
                  Block lv2 = (Block)stateFactory.getOwner();
                  return (state) -> {
                     if (state != null && state.isOf(lv2)) {
                        Iterator var3 = map.entrySet().iterator();

                        Map.Entry entry;
                        do {
                           if (!var3.hasNext()) {
                              return true;
                           }

                           entry = (Map.Entry)var3.next();
                        } while(Objects.equals(state.get((Property)entry.getKey()), entry.getValue()));

                        return false;
                     } else {
                        return false;
                     }
                  };
               }

               String string2 = (String)var3.next();
               iterator = KEY_VALUE_SPLITTER.split(string2).iterator();
            } while(!iterator.hasNext());

            String string3 = (String)iterator.next();
            Property lv = stateFactory.getProperty(string3);
            if (lv != null && iterator.hasNext()) {
               String string4 = (String)iterator.next();
               Comparable comparable = getPropertyValue(lv, string4);
               if (comparable == null) {
                  throw new RuntimeException("Unknown value: '" + string4 + "' for blockstate property: '" + string3 + "' " + lv.getValues());
               }

               map.put(lv, comparable);
            } else if (!string3.isEmpty()) {
               throw new RuntimeException("Unknown blockstate property: '" + string3 + "'");
            }
         }
      }
   }

   @Nullable
   static Comparable getPropertyValue(Property property, String string) {
      return (Comparable)property.parse(string).orElse((Object)null);
   }

   public UnbakedModel getOrLoadModel(Identifier id) {
      if (this.unbakedModels.containsKey(id)) {
         return (UnbakedModel)this.unbakedModels.get(id);
      } else if (this.modelsToLoad.contains(id)) {
         throw new IllegalStateException("Circular reference while loading " + id);
      } else {
         this.modelsToLoad.add(id);
         UnbakedModel lv = (UnbakedModel)this.unbakedModels.get(MISSING_ID);

         while(!this.modelsToLoad.isEmpty()) {
            Identifier lv2 = (Identifier)this.modelsToLoad.iterator().next();

            try {
               if (!this.unbakedModels.containsKey(lv2)) {
                  this.loadModel(lv2);
               }
            } catch (ModelLoaderException var9) {
               LOGGER.warn(var9.getMessage());
               this.unbakedModels.put(lv2, lv);
            } catch (Exception var10) {
               LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", new Object[]{lv2, id, var10});
               this.unbakedModels.put(lv2, lv);
            } finally {
               this.modelsToLoad.remove(lv2);
            }
         }

         return (UnbakedModel)this.unbakedModels.getOrDefault(id, lv);
      }
   }

   private void loadModel(Identifier id) throws Exception {
      if (!(id instanceof ModelIdentifier lv)) {
         this.putModel(id, this.loadModelFromJson(id));
      } else {
         Identifier lv2;
         if (Objects.equals(lv.getVariant(), "inventory")) {
            lv2 = id.withPrefixedPath("item/");
            JsonUnbakedModel lv3 = this.loadModelFromJson(lv2);
            this.putModel(lv, lv3);
            this.unbakedModels.put(lv2, lv3);
         } else {
            lv2 = new Identifier(id.getNamespace(), id.getPath());
            StateManager lv4 = (StateManager)Optional.ofNullable((StateManager)STATIC_DEFINITIONS.get(lv2)).orElseGet(() -> {
               return ((Block)Registries.BLOCK.get(lv2)).getStateManager();
            });
            this.variantMapDeserializationContext.setStateFactory(lv4);
            List list = ImmutableList.copyOf(this.blockColors.getProperties((Block)lv4.getOwner()));
            ImmutableList immutableList = lv4.getStates();
            Map map = Maps.newHashMap();
            immutableList.forEach((state) -> {
               map.put(BlockModels.getModelId(lv2, state), state);
            });
            Map map2 = Maps.newHashMap();
            Identifier lv5 = BLOCK_STATES_FINDER.toResourcePath(id);
            UnbakedModel lv6 = (UnbakedModel)this.unbakedModels.get(MISSING_ID);
            ModelDefinition lv7 = new ModelDefinition(ImmutableList.of(lv6), ImmutableList.of());
            Pair pair = Pair.of(lv6, () -> {
               return lv7;
            });
            boolean var24 = false;

            try {
               var24 = true;
               List list2 = ((List)this.blockStates.getOrDefault(lv5, List.of())).stream().map((blockState) -> {
                  try {
                     return Pair.of(blockState.source, ModelVariantMap.fromJson(this.variantMapDeserializationContext, blockState.data));
                  } catch (Exception var4) {
                     throw new ModelLoaderException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", lv5, blockState.source, var4.getMessage()));
                  }
               }).toList();
               Iterator var14 = list2.iterator();

               while(true) {
                  if (!var14.hasNext()) {
                     var24 = false;
                     break;
                  }

                  Pair pair2 = (Pair)var14.next();
                  ModelVariantMap lv8 = (ModelVariantMap)pair2.getSecond();
                  Map map3 = Maps.newIdentityHashMap();
                  MultipartUnbakedModel lv9;
                  if (lv8.hasMultipartModel()) {
                     lv9 = lv8.getMultipartModel();
                     immutableList.forEach((state) -> {
                        map3.put(state, Pair.of(lv9, () -> {
                           return ModelLoader.ModelDefinition.create(state, (MultipartUnbakedModel)lv9, list);
                        }));
                     });
                  } else {
                     lv9 = null;
                  }

                  lv8.getVariantMap().forEach((key, model) -> {
                     try {
                        immutableList.stream().filter(stateKeyToPredicate(lv4, key)).forEach((state) -> {
                           Pair pair2 = (Pair)map3.put(state, Pair.of(model, () -> {
                              return ModelLoader.ModelDefinition.create(state, (UnbakedModel)model, list);
                           }));
                           if (pair2 != null && pair2.getFirst() != lv9) {
                              map3.put(state, pair);
                              Optional var10002 = lv8.getVariantMap().entrySet().stream().filter((entry) -> {
                                 return entry.getValue() == pair2.getFirst();
                              }).findFirst();
                              throw new RuntimeException("Overlapping definition with: " + (String)((Map.Entry)var10002.get()).getKey());
                           }
                        });
                     } catch (Exception var12) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", new Object[]{lv5, pair2.getFirst(), key, var12.getMessage()});
                     }

                  });
                  map2.putAll(map3);
               }
            } catch (ModelLoaderException var25) {
               throw var25;
            } catch (Exception var26) {
               throw new ModelLoaderException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s': %s", lv5, var26));
            } finally {
               if (var24) {
                  HashMap map5 = Maps.newHashMap();
                  map.forEach((idx, state) -> {
                     Pair pair2 = (Pair)map2.get(state);
                     if (pair2 == null) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", lv5, idx);
                        pair2 = pair;
                     }

                     this.putModel(idx, (UnbakedModel)pair2.getFirst());

                     try {
                        ModelDefinition lv = (ModelDefinition)((Supplier)pair2.getSecond()).get();
                        ((Set)map4.computeIfAbsent(lv, (definition) -> {
                           return Sets.newIdentityHashSet();
                        })).add(state);
                     } catch (Exception var9) {
                        LOGGER.warn("Exception evaluating model definition: '{}'", idx, var9);
                     }

                  });
                  map5.forEach((definition, states) -> {
                     Iterator iterator = states.iterator();

                     while(iterator.hasNext()) {
                        BlockState lv = (BlockState)iterator.next();
                        if (lv.getRenderType() != BlockRenderType.MODEL) {
                           iterator.remove();
                           this.stateLookup.put(lv, 0);
                        }
                     }

                     if (states.size() > 1) {
                        this.addStates(states);
                     }

                  });
               }
            }

            Map map4 = Maps.newHashMap();
            map.forEach((idx, state) -> {
               Pair pair2 = (Pair)map2.get(state);
               if (pair2 == null) {
                  LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", lv5, idx);
                  pair2 = pair;
               }

               this.putModel(idx, (UnbakedModel)pair2.getFirst());

               try {
                  ModelDefinition lv = (ModelDefinition)((Supplier)pair2.getSecond()).get();
                  ((Set)map4.computeIfAbsent(lv, (definition) -> {
                     return Sets.newIdentityHashSet();
                  })).add(state);
               } catch (Exception var9) {
                  LOGGER.warn("Exception evaluating model definition: '{}'", idx, var9);
               }

            });
            map4.forEach((definition, states) -> {
               Iterator iterator = states.iterator();

               while(iterator.hasNext()) {
                  BlockState lv = (BlockState)iterator.next();
                  if (lv.getRenderType() != BlockRenderType.MODEL) {
                     iterator.remove();
                     this.stateLookup.put(lv, 0);
                  }
               }

               if (states.size() > 1) {
                  this.addStates(states);
               }

            });
         }

      }
   }

   private void putModel(Identifier id, UnbakedModel unbakedModel) {
      this.unbakedModels.put(id, unbakedModel);
      this.modelsToLoad.addAll(unbakedModel.getModelDependencies());
   }

   private void addModel(ModelIdentifier modelId) {
      UnbakedModel lv = this.getOrLoadModel(modelId);
      this.unbakedModels.put(modelId, lv);
      this.modelsToBake.put(modelId, lv);
   }

   private void addStates(Iterable states) {
      int i = this.nextStateId++;
      states.forEach((state) -> {
         this.stateLookup.put(state, i);
      });
   }

   private JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException {
      String string = id.getPath();
      if ("builtin/generated".equals(string)) {
         return GENERATION_MARKER;
      } else if ("builtin/entity".equals(string)) {
         return BLOCK_ENTITY_MARKER;
      } else if (string.startsWith("builtin/")) {
         String string2 = string.substring("builtin/".length());
         String string3 = (String)BUILTIN_MODEL_DEFINITIONS.get(string2);
         if (string3 == null) {
            throw new FileNotFoundException(id.toString());
         } else {
            Reader reader = new StringReader(string3);
            JsonUnbakedModel lv = JsonUnbakedModel.deserialize((Reader)reader);
            lv.id = id.toString();
            return lv;
         }
      } else {
         Identifier lv2 = MODELS_FINDER.toResourcePath(id);
         JsonUnbakedModel lv3 = (JsonUnbakedModel)this.jsonUnbakedModels.get(lv2);
         if (lv3 == null) {
            throw new FileNotFoundException(lv2.toString());
         } else {
            lv3.id = id.toString();
            return lv3;
         }
      }
   }

   public Map getBakedModelMap() {
      return this.bakedModels;
   }

   public Object2IntMap getStateLookup() {
      return this.stateLookup;
   }

   static {
      FIRE_0 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/fire_0"));
      FIRE_1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/fire_1"));
      LAVA_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/lava_flow"));
      WATER_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/water_flow"));
      WATER_OVERLAY = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("block/water_overlay"));
      BANNER_BASE = new SpriteIdentifier(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, new Identifier("entity/banner_base"));
      SHIELD_BASE = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, new Identifier("entity/shield_base"));
      SHIELD_BASE_NO_PATTERN = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, new Identifier("entity/shield_base_nopattern"));
      BLOCK_DESTRUCTION_STAGES = (List)IntStream.range(0, 10).mapToObj((stage) -> {
         return new Identifier("block/destroy_stage_" + stage);
      }).collect(Collectors.toList());
      BLOCK_DESTRUCTION_STAGE_TEXTURES = (List)BLOCK_DESTRUCTION_STAGES.stream().map((id) -> {
         return new Identifier("textures/" + id.getPath() + ".png");
      }).collect(Collectors.toList());
      BLOCK_DESTRUCTION_RENDER_LAYERS = (List)BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(RenderLayer::getBlockBreaking).collect(Collectors.toList());
      LOGGER = LogUtils.getLogger();
      MISSING_ID = ModelIdentifier.ofVanilla("builtin/missing", "missing");
      BLOCK_STATES_FINDER = ResourceFinder.json("blockstates");
      MODELS_FINDER = ResourceFinder.json("models");
      MISSING_DEFINITION = ("{    'textures': {       'particle': '" + MissingSprite.getMissingSpriteId().getPath() + "',       'missingno': '" + MissingSprite.getMissingSpriteId().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '"');
      BUILTIN_MODEL_DEFINITIONS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_DEFINITION));
      COMMA_SPLITTER = Splitter.on(',');
      KEY_VALUE_SPLITTER = Splitter.on('=').limit(2);
      GENERATION_MARKER = (JsonUnbakedModel)Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"front\"}"), (model) -> {
         model.id = "generation marker";
      });
      BLOCK_ENTITY_MARKER = (JsonUnbakedModel)Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"side\"}"), (model) -> {
         model.id = "block entity marker";
      });
      ITEM_FRAME_STATE_FACTORY = (new StateManager.Builder(Blocks.AIR)).add(BooleanProperty.of("map")).build(Block::getDefaultState, BlockState::new);
      ITEM_MODEL_GENERATOR = new ItemModelGenerator();
      STATIC_DEFINITIONS = ImmutableMap.of(new Identifier("item_frame"), ITEM_FRAME_STATE_FACTORY, new Identifier("glow_item_frame"), ITEM_FRAME_STATE_FACTORY);
   }

   @Environment(EnvType.CLIENT)
   static class ModelLoaderException extends RuntimeException {
      public ModelLoaderException(String message) {
         super(message);
      }
   }

   @Environment(EnvType.CLIENT)
   static class ModelDefinition {
      private final List components;
      private final List values;

      public ModelDefinition(List components, List values) {
         this.components = components;
         this.values = values;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof ModelDefinition)) {
            return false;
         } else {
            ModelDefinition lv = (ModelDefinition)o;
            return Objects.equals(this.components, lv.components) && Objects.equals(this.values, lv.values);
         }
      }

      public int hashCode() {
         return 31 * this.components.hashCode() + this.values.hashCode();
      }

      public static ModelDefinition create(BlockState state, MultipartUnbakedModel rawModel, Collection properties) {
         StateManager lv = state.getBlock().getStateManager();
         List list = (List)rawModel.getComponents().stream().filter((component) -> {
            return component.getPredicate(lv).test(state);
         }).map(MultipartModelComponent::getModel).collect(ImmutableList.toImmutableList());
         List list2 = getStateValues(state, properties);
         return new ModelDefinition(list, list2);
      }

      public static ModelDefinition create(BlockState state, UnbakedModel rawModel, Collection properties) {
         List list = getStateValues(state, properties);
         return new ModelDefinition(ImmutableList.of(rawModel), list);
      }

      private static List getStateValues(BlockState state, Collection properties) {
         Stream var10000 = properties.stream();
         Objects.requireNonNull(state);
         return (List)var10000.map(state::get).collect(ImmutableList.toImmutableList());
      }
   }

   @Environment(EnvType.CLIENT)
   public static record SourceTrackedData(String source, JsonElement data) {
      final String source;
      final JsonElement data;

      public SourceTrackedData(String string, JsonElement jsonElement) {
         this.source = string;
         this.data = jsonElement;
      }

      public String source() {
         return this.source;
      }

      public JsonElement data() {
         return this.data;
      }
   }

   @Environment(EnvType.CLIENT)
   private class BakerImpl implements Baker {
      private final Function textureGetter;

      BakerImpl(BiFunction spriteLoader, Identifier modelId) {
         this.textureGetter = (spriteId) -> {
            return (Sprite)spriteLoader.apply(modelId, spriteId);
         };
      }

      public UnbakedModel getOrLoadModel(Identifier id) {
         return ModelLoader.this.getOrLoadModel(id);
      }

      public BakedModel bake(Identifier id, ModelBakeSettings settings) {
         BakedModelCacheKey lv = new BakedModelCacheKey(id, settings.getRotation(), settings.isUvLocked());
         BakedModel lv2 = (BakedModel)ModelLoader.this.bakedModelCache.get(lv);
         if (lv2 != null) {
            return lv2;
         } else {
            UnbakedModel lv3 = this.getOrLoadModel(id);
            if (lv3 instanceof JsonUnbakedModel) {
               JsonUnbakedModel lv4 = (JsonUnbakedModel)lv3;
               if (lv4.getRootModel() == ModelLoader.GENERATION_MARKER) {
                  return ModelLoader.ITEM_MODEL_GENERATOR.create(this.textureGetter, lv4).bake(this, lv4, this.textureGetter, settings, id, false);
               }
            }

            BakedModel lv5 = lv3.bake(this, this.textureGetter, settings, id);
            ModelLoader.this.bakedModelCache.put(lv, lv5);
            return lv5;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static record BakedModelCacheKey(Identifier id, AffineTransformation transformation, boolean isUvLocked) {
      BakedModelCacheKey(Identifier arg, AffineTransformation arg2, boolean bl) {
         this.id = arg;
         this.transformation = arg2;
         this.isUvLocked = bl;
      }

      public Identifier id() {
         return this.id;
      }

      public AffineTransformation transformation() {
         return this.transformation;
      }

      public boolean isUvLocked() {
         return this.isUvLocked;
      }
   }
}
