/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
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
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
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
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.MultipartUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.WeightedUnbakedModel;
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

@Environment(value=EnvType.CLIENT)
public class ModelLoader {
    public static final SpriteIdentifier FIRE_0 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.method_60656("block/fire_0"));
    public static final SpriteIdentifier FIRE_1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.method_60656("block/fire_1"));
    public static final SpriteIdentifier LAVA_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.method_60656("block/lava_flow"));
    public static final SpriteIdentifier WATER_FLOW = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.method_60656("block/water_flow"));
    public static final SpriteIdentifier WATER_OVERLAY = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.method_60656("block/water_overlay"));
    public static final SpriteIdentifier BANNER_BASE = new SpriteIdentifier(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, Identifier.method_60656("entity/banner_base"));
    public static final SpriteIdentifier SHIELD_BASE = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.method_60656("entity/shield_base"));
    public static final SpriteIdentifier SHIELD_BASE_NO_PATTERN = new SpriteIdentifier(TexturedRenderLayers.SHIELD_PATTERNS_ATLAS_TEXTURE, Identifier.method_60656("entity/shield_base_nopattern"));
    public static final int field_32983 = 10;
    public static final List<Identifier> BLOCK_DESTRUCTION_STAGES = IntStream.range(0, 10).mapToObj(stage -> Identifier.method_60656("block/destroy_stage_" + stage)).collect(Collectors.toList());
    public static final List<Identifier> BLOCK_DESTRUCTION_STAGE_TEXTURES = BLOCK_DESTRUCTION_STAGES.stream().map(id -> id.withPath(string -> "textures/" + string + ".png")).collect(Collectors.toList());
    public static final List<RenderLayer> BLOCK_DESTRUCTION_RENDER_LAYERS = BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(RenderLayer::getBlockBreaking).collect(Collectors.toList());
    static final int field_32984 = -1;
    private static final int field_32985 = 0;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String BUILTIN = "builtin/";
    private static final String BUILTIN_GENERATED = "builtin/generated";
    private static final String BUILTIN_ENTITY = "builtin/entity";
    private static final String MISSING = "missing";
    public static final ModelIdentifier MISSING_ID = ModelIdentifier.ofVanilla("builtin/missing", "missing");
    public static final ResourceFinder BLOCK_STATES_FINDER = ResourceFinder.json("blockstates");
    public static final ResourceFinder MODELS_FINDER = ResourceFinder.json("models");
    @VisibleForTesting
    public static final String MISSING_DEFINITION = ("{    'textures': {       'particle': '" + MissingSprite.getMissingSpriteId().getPath() + "',       'missingno': '" + MissingSprite.getMissingSpriteId().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '\"');
    private static final Map<String, String> BUILTIN_MODEL_DEFINITIONS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_DEFINITION));
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter KEY_VALUE_SPLITTER = Splitter.on('=').limit(2);
    public static final JsonUnbakedModel GENERATION_MARKER = Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"front\"}"), model -> {
        model.id = "generation marker";
    });
    public static final JsonUnbakedModel BLOCK_ENTITY_MARKER = Util.make(JsonUnbakedModel.deserialize("{\"gui_light\": \"side\"}"), model -> {
        model.id = "block entity marker";
    });
    private static final StateManager<Block, BlockState> ITEM_FRAME_STATE_FACTORY = new StateManager.Builder(Blocks.AIR).add(BooleanProperty.of("map")).build(Block::getDefaultState, BlockState::new);
    static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final Map<Identifier, StateManager<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(Identifier.method_60656("item_frame"), ITEM_FRAME_STATE_FACTORY, Identifier.method_60656("glow_item_frame"), ITEM_FRAME_STATE_FACTORY);
    private final BlockColors blockColors;
    private final Map<Identifier, JsonUnbakedModel> jsonUnbakedModels;
    private final Map<Identifier, List<SourceTrackedData>> blockStates;
    private final Set<Identifier> modelsToLoad = Sets.newHashSet();
    private final ModelVariantMap.DeserializationContext variantMapDeserializationContext = new ModelVariantMap.DeserializationContext();
    private final Map<Identifier, UnbakedModel> unbakedModels = Maps.newHashMap();
    final Map<BakedModelCacheKey, BakedModel> bakedModelCache = Maps.newHashMap();
    private final Map<Identifier, UnbakedModel> modelsToBake = Maps.newHashMap();
    private final Map<Identifier, BakedModel> bakedModels = Maps.newHashMap();
    private int nextStateId = 1;
    private final Object2IntMap<BlockState> stateLookup = Util.make(new Object2IntOpenHashMap(), map -> map.defaultReturnValue(-1));

    public ModelLoader(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<SourceTrackedData>> blockStates) {
        this.blockColors = blockColors;
        this.jsonUnbakedModels = jsonUnbakedModels;
        this.blockStates = blockStates;
        profiler.push("missing_model");
        try {
            this.unbakedModels.put(MISSING_ID, this.loadModelFromJson(MISSING_ID));
            this.addModel(MISSING_ID);
        } catch (IOException iOException) {
            LOGGER.error("Error loading missing model, should never happen :(", iOException);
            throw new RuntimeException(iOException);
        }
        profiler.swap("static_definitions");
        STATIC_DEFINITIONS.forEach((id, stateManager) -> stateManager.getStates().forEach(state -> this.addModel(BlockModels.getModelId(id, state))));
        profiler.swap("blocks");
        for (Block lv : Registries.BLOCK) {
            lv.getStateManager().getStates().forEach(state -> this.addModel(BlockModels.getModelId(state)));
        }
        profiler.swap("items");
        for (Identifier lv2 : Registries.ITEM.getIds()) {
            this.addModel(new ModelIdentifier(lv2, "inventory"));
        }
        profiler.swap("special");
        this.addModel(ItemRenderer.TRIDENT_IN_HAND);
        this.addModel(ItemRenderer.SPYGLASS_IN_HAND);
        this.modelsToBake.values().forEach(model -> model.setParents(this::getOrLoadModel));
        profiler.pop();
    }

    public void bake(BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader) {
        this.modelsToBake.keySet().forEach(modelId -> {
            BakedModel lv = null;
            try {
                lv = new BakerImpl(spriteLoader, (Identifier)modelId).bake((Identifier)modelId, ModelRotation.X0_Y0);
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", modelId, (Object)exception);
            }
            if (lv != null) {
                this.bakedModels.put((Identifier)modelId, lv);
            }
        });
    }

    private static Predicate<BlockState> stateKeyToPredicate(StateManager<Block, BlockState> stateFactory, String key) {
        HashMap<Property<?>, ?> map = Maps.newHashMap();
        for (String string2 : COMMA_SPLITTER.split(key)) {
            Iterator<String> iterator = KEY_VALUE_SPLITTER.split(string2).iterator();
            if (!iterator.hasNext()) continue;
            String string3 = iterator.next();
            Property<?> lv = stateFactory.getProperty(string3);
            if (lv != null && iterator.hasNext()) {
                String string4 = iterator.next();
                Object comparable = ModelLoader.getPropertyValue(lv, string4);
                if (comparable != null) {
                    map.put(lv, comparable);
                    continue;
                }
                throw new RuntimeException("Unknown value: '" + string4 + "' for blockstate property: '" + string3 + "' " + String.valueOf(lv.getValues()));
            }
            if (string3.isEmpty()) continue;
            throw new RuntimeException("Unknown blockstate property: '" + string3 + "'");
        }
        Block lv2 = stateFactory.getOwner();
        return state -> {
            if (state == null || !state.isOf(lv2)) {
                return false;
            }
            for (Map.Entry entry : map.entrySet()) {
                if (Objects.equals(state.get((Property)entry.getKey()), entry.getValue())) continue;
                return false;
            }
            return true;
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getPropertyValue(Property<T> property, String string) {
        return (T)((Comparable)property.parse(string).orElse(null));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public UnbakedModel getOrLoadModel(Identifier id) {
        if (this.unbakedModels.containsKey(id)) {
            return this.unbakedModels.get(id);
        }
        if (this.modelsToLoad.contains(id)) {
            throw new IllegalStateException("Circular reference while loading " + String.valueOf(id));
        }
        this.modelsToLoad.add(id);
        UnbakedModel lv = this.unbakedModels.get(MISSING_ID);
        while (!this.modelsToLoad.isEmpty()) {
            Identifier lv2 = this.modelsToLoad.iterator().next();
            try {
                if (this.unbakedModels.containsKey(lv2)) continue;
                this.loadModel(lv2);
            } catch (ModelLoaderException lv3) {
                LOGGER.warn(lv3.getMessage());
                this.unbakedModels.put(lv2, lv);
            } catch (Exception exception) {
                LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", lv2, id, exception);
                this.unbakedModels.put(lv2, lv);
            } finally {
                this.modelsToLoad.remove(lv2);
            }
        }
        return this.unbakedModels.getOrDefault(id, lv);
    }

    private void loadModel(Identifier id2) throws Exception {
        if (!(id2 instanceof ModelIdentifier)) {
            this.putModel(id2, this.loadModelFromJson(id2));
            return;
        }
        ModelIdentifier lv = (ModelIdentifier)id2;
        if (Objects.equals(lv.getVariant(), "inventory")) {
            Identifier lv2 = id2.withPrefixedPath("item/");
            JsonUnbakedModel lv3 = this.loadModelFromJson(lv2);
            this.putModel(lv, lv3);
            this.unbakedModels.put(lv2, lv3);
        } else {
            Identifier lv2 = Identifier.method_60655(id2.getNamespace(), id2.getPath());
            StateManager lv4 = Optional.ofNullable(STATIC_DEFINITIONS.get(lv2)).orElseGet(() -> Registries.BLOCK.get(lv2).getStateManager());
            this.variantMapDeserializationContext.setStateFactory(lv4);
            ImmutableList<Property<?>> list = ImmutableList.copyOf(this.blockColors.getProperties((Block)lv4.getOwner()));
            ImmutableList immutableList = lv4.getStates();
            HashMap<ModelIdentifier, BlockState> map = Maps.newHashMap();
            immutableList.forEach(state -> map.put(BlockModels.getModelId(lv2, state), (BlockState)state));
            HashMap map2 = Maps.newHashMap();
            Identifier lv5 = BLOCK_STATES_FINDER.toResourcePath(id2);
            UnbakedModel lv6 = this.unbakedModels.get(MISSING_ID);
            ModelDefinition lv7 = new ModelDefinition(ImmutableList.of(lv6), ImmutableList.of());
            Pair<UnbakedModel, Supplier<ModelDefinition>> pair = Pair.of(lv6, () -> lv7);
            try {
                List<Pair> list2 = this.blockStates.getOrDefault(lv5, List.of()).stream().map(blockState -> {
                    try {
                        return Pair.of(blockState.source, ModelVariantMap.fromJson(this.variantMapDeserializationContext, blockState.data));
                    } catch (Exception exception) {
                        throw new ModelLoaderException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", lv5, blockState.source, exception.getMessage()));
                    }
                }).toList();
                for (Pair pair2 : list2) {
                    MultipartUnbakedModel lv9;
                    ModelVariantMap lv8 = (ModelVariantMap)pair2.getSecond();
                    IdentityHashMap map3 = Maps.newIdentityHashMap();
                    if (lv8.hasMultipartModel()) {
                        lv9 = lv8.getMultipartModel();
                        immutableList.forEach(state -> map3.put(state, Pair.of(lv9, () -> ModelDefinition.create(state, lv9, list))));
                    } else {
                        lv9 = null;
                    }
                    lv8.getVariantMap().forEach((key, model) -> {
                        try {
                            immutableList.stream().filter(ModelLoader.stateKeyToPredicate(lv4, key)).forEach(state -> {
                                Pair<WeightedUnbakedModel, Supplier<ModelDefinition>> pair2 = map3.put(state, Pair.of(model, () -> ModelDefinition.create(state, model, list)));
                                if (pair2 != null && pair2.getFirst() != lv9) {
                                    map3.put(state, pair);
                                    throw new RuntimeException("Overlapping definition with: " + (String)lv8.getVariantMap().entrySet().stream().filter(entry -> entry.getValue() == pair2.getFirst()).findFirst().get().getKey());
                                }
                            });
                        } catch (Exception exception) {
                            LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", lv5, pair2.getFirst(), key, exception.getMessage());
                        }
                    });
                    map2.putAll(map3);
                }
            } catch (ModelLoaderException lv10) {
                throw lv10;
            } catch (Exception exception) {
                throw new ModelLoaderException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s': %s", lv5, exception));
            } finally {
                HashMap<ModelDefinition, Set> map5 = Maps.newHashMap();
                map.forEach((id, state) -> {
                    Pair pair2 = (Pair)map2.get(state);
                    if (pair2 == null) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", (Object)lv5, id);
                        pair2 = pair;
                    }
                    this.putModel((Identifier)id, (UnbakedModel)pair2.getFirst());
                    try {
                        ModelDefinition lv = (ModelDefinition)((Supplier)pair2.getSecond()).get();
                        map5.computeIfAbsent(lv, definition -> Sets.newIdentityHashSet()).add(state);
                    } catch (Exception exception) {
                        LOGGER.warn("Exception evaluating model definition: '{}'", id, (Object)exception);
                    }
                });
                map5.forEach((definition, states) -> {
                    Iterator iterator = states.iterator();
                    while (iterator.hasNext()) {
                        BlockState lv = (BlockState)iterator.next();
                        if (lv.getRenderType() == BlockRenderType.MODEL) continue;
                        iterator.remove();
                        this.stateLookup.put(lv, 0);
                    }
                    if (states.size() > 1) {
                        this.addStates((Iterable<BlockState>)states);
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

    private void addStates(Iterable<BlockState> states) {
        int i = this.nextStateId++;
        states.forEach(state -> this.stateLookup.put((BlockState)state, i));
    }

    private JsonUnbakedModel loadModelFromJson(Identifier id) throws IOException {
        String string = id.getPath();
        if (BUILTIN_GENERATED.equals(string)) {
            return GENERATION_MARKER;
        }
        if (BUILTIN_ENTITY.equals(string)) {
            return BLOCK_ENTITY_MARKER;
        }
        if (string.startsWith(BUILTIN)) {
            String string2 = string.substring(BUILTIN.length());
            String string3 = BUILTIN_MODEL_DEFINITIONS.get(string2);
            if (string3 == null) {
                throw new FileNotFoundException(id.toString());
            }
            StringReader reader = new StringReader(string3);
            JsonUnbakedModel lv = JsonUnbakedModel.deserialize(reader);
            lv.id = id.toString();
            return lv;
        }
        Identifier lv2 = MODELS_FINDER.toResourcePath(id);
        JsonUnbakedModel lv3 = this.jsonUnbakedModels.get(lv2);
        if (lv3 == null) {
            throw new FileNotFoundException(lv2.toString());
        }
        lv3.id = id.toString();
        return lv3;
    }

    public Map<Identifier, BakedModel> getBakedModelMap() {
        return this.bakedModels;
    }

    public Object2IntMap<BlockState> getStateLookup() {
        return this.stateLookup;
    }

    @Environment(value=EnvType.CLIENT)
    static class ModelLoaderException
    extends RuntimeException {
        public ModelLoaderException(String message) {
            super(message);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ModelDefinition {
        private final List<UnbakedModel> components;
        private final List<Object> values;

        public ModelDefinition(List<UnbakedModel> components, List<Object> values) {
            this.components = components;
            this.values = values;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ModelDefinition) {
                ModelDefinition lv = (ModelDefinition)o;
                return Objects.equals(this.components, lv.components) && Objects.equals(this.values, lv.values);
            }
            return false;
        }

        public int hashCode() {
            return 31 * this.components.hashCode() + this.values.hashCode();
        }

        public static ModelDefinition create(BlockState state, MultipartUnbakedModel rawModel, Collection<Property<?>> properties) {
            StateManager<Block, BlockState> lv = state.getBlock().getStateManager();
            List list = rawModel.getComponents().stream().filter(component -> component.getPredicate(lv).test(state)).map(MultipartModelComponent::getModel).collect(ImmutableList.toImmutableList());
            List<Object> list2 = ModelDefinition.getStateValues(state, properties);
            return new ModelDefinition(list, list2);
        }

        public static ModelDefinition create(BlockState state, UnbakedModel rawModel, Collection<Property<?>> properties) {
            List<Object> list = ModelDefinition.getStateValues(state, properties);
            return new ModelDefinition(ImmutableList.of(rawModel), list);
        }

        private static List<Object> getStateValues(BlockState state, Collection<Property<?>> properties) {
            return properties.stream().map(state::get).collect(ImmutableList.toImmutableList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record SourceTrackedData(String source, JsonElement data) {
    }

    @Environment(value=EnvType.CLIENT)
    class BakerImpl
    implements Baker {
        private final Function<SpriteIdentifier, Sprite> textureGetter = spriteId -> (Sprite)spriteLoader.apply(modelId, (SpriteIdentifier)spriteId);

        BakerImpl(BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader, Identifier modelId) {
        }

        @Override
        public UnbakedModel getOrLoadModel(Identifier id) {
            return ModelLoader.this.getOrLoadModel(id);
        }

        @Override
        public BakedModel bake(Identifier id, ModelBakeSettings settings) {
            JsonUnbakedModel lv4;
            BakedModelCacheKey lv = new BakedModelCacheKey(id, settings.getRotation(), settings.isUvLocked());
            BakedModel lv2 = ModelLoader.this.bakedModelCache.get(lv);
            if (lv2 != null) {
                return lv2;
            }
            UnbakedModel lv3 = this.getOrLoadModel(id);
            if (lv3 instanceof JsonUnbakedModel && (lv4 = (JsonUnbakedModel)lv3).getRootModel() == GENERATION_MARKER) {
                return ITEM_MODEL_GENERATOR.create(this.textureGetter, lv4).bake(this, lv4, this.textureGetter, settings, id, false);
            }
            BakedModel lv5 = lv3.bake(this, this.textureGetter, settings, id);
            ModelLoader.this.bakedModelCache.put(lv, lv5);
            return lv5;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record BakedModelCacheKey(Identifier id, AffineTransformation transformation, boolean isUvLocked) {
    }
}

