package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class JsonUnbakedModel implements UnbakedModel {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final BakedQuadFactory QUAD_FACTORY = new BakedQuadFactory();
   @VisibleForTesting
   static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(JsonUnbakedModel.class, new Deserializer()).registerTypeAdapter(ModelElement.class, new ModelElement.Deserializer()).registerTypeAdapter(ModelElementFace.class, new ModelElementFace.Deserializer()).registerTypeAdapter(ModelElementTexture.class, new ModelElementTexture.Deserializer()).registerTypeAdapter(Transformation.class, new Transformation.Deserializer()).registerTypeAdapter(ModelTransformation.class, new ModelTransformation.Deserializer()).registerTypeAdapter(ModelOverride.class, new ModelOverride.Deserializer()).create();
   private static final char TEXTURE_REFERENCE_INITIAL = '#';
   public static final String PARTICLE_KEY = "particle";
   private static final boolean field_42912 = true;
   private final List elements;
   @Nullable
   private final GuiLight guiLight;
   @Nullable
   private final Boolean ambientOcclusion;
   private final ModelTransformation transformations;
   private final List overrides;
   public String id = "";
   @VisibleForTesting
   protected final Map textureMap;
   @Nullable
   protected JsonUnbakedModel parent;
   @Nullable
   protected Identifier parentId;

   public static JsonUnbakedModel deserialize(Reader input) {
      return (JsonUnbakedModel)JsonHelper.deserialize(GSON, input, JsonUnbakedModel.class);
   }

   public static JsonUnbakedModel deserialize(String json) {
      return deserialize((Reader)(new StringReader(json)));
   }

   public JsonUnbakedModel(@Nullable Identifier parentId, List elements, Map textureMap, @Nullable Boolean ambientOcclusion, @Nullable GuiLight guiLight, ModelTransformation transformations, List overrides) {
      this.elements = elements;
      this.ambientOcclusion = ambientOcclusion;
      this.guiLight = guiLight;
      this.textureMap = textureMap;
      this.parentId = parentId;
      this.transformations = transformations;
      this.overrides = overrides;
   }

   public List getElements() {
      return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
   }

   public boolean useAmbientOcclusion() {
      if (this.ambientOcclusion != null) {
         return this.ambientOcclusion;
      } else {
         return this.parent != null ? this.parent.useAmbientOcclusion() : true;
      }
   }

   public GuiLight getGuiLight() {
      if (this.guiLight != null) {
         return this.guiLight;
      } else {
         return this.parent != null ? this.parent.getGuiLight() : JsonUnbakedModel.GuiLight.BLOCK;
      }
   }

   public boolean needsResolution() {
      return this.parentId == null || this.parent != null && this.parent.needsResolution();
   }

   public List getOverrides() {
      return this.overrides;
   }

   private ModelOverrideList compileOverrides(Baker baker, JsonUnbakedModel parent) {
      return this.overrides.isEmpty() ? ModelOverrideList.EMPTY : new ModelOverrideList(baker, parent, this.overrides);
   }

   public Collection getModelDependencies() {
      Set set = Sets.newHashSet();
      Iterator var2 = this.overrides.iterator();

      while(var2.hasNext()) {
         ModelOverride lv = (ModelOverride)var2.next();
         set.add(lv.getModelId());
      }

      if (this.parentId != null) {
         set.add(this.parentId);
      }

      return set;
   }

   public void setParents(Function modelLoader) {
      Set set = Sets.newLinkedHashSet();

      for(JsonUnbakedModel lv = this; lv.parentId != null && lv.parent == null; lv = lv.parent) {
         set.add(lv);
         UnbakedModel lv2 = (UnbakedModel)modelLoader.apply(lv.parentId);
         if (lv2 == null) {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.parentId, lv);
         }

         if (set.contains(lv2)) {
            LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", new Object[]{lv, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentId});
            lv2 = null;
         }

         if (lv2 == null) {
            lv.parentId = ModelLoader.MISSING_ID;
            lv2 = (UnbakedModel)modelLoader.apply(lv.parentId);
         }

         if (!(lv2 instanceof JsonUnbakedModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
         }

         lv.parent = (JsonUnbakedModel)lv2;
      }

      this.overrides.forEach((override) -> {
         UnbakedModel lv = (UnbakedModel)modelLoader.apply(override.getModelId());
         if (!Objects.equals(lv, this)) {
            lv.setParents(modelLoader);
         }
      });
   }

   public BakedModel bake(Baker baker, Function textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
      return this.bake(baker, this, textureGetter, rotationContainer, modelId, true);
   }

   public BakedModel bake(Baker baker, JsonUnbakedModel parent, Function textureGetter, ModelBakeSettings settings, Identifier id, boolean hasDepth) {
      Sprite lv = (Sprite)textureGetter.apply(this.resolveSprite("particle"));
      if (this.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
         return new BuiltinBakedModel(this.getTransformations(), this.compileOverrides(baker, parent), lv, this.getGuiLight().isSide());
      } else {
         BasicBakedModel.Builder lv2 = (new BasicBakedModel.Builder(this, this.compileOverrides(baker, parent), hasDepth)).setParticle(lv);
         Iterator var9 = this.getElements().iterator();

         while(var9.hasNext()) {
            ModelElement lv3 = (ModelElement)var9.next();
            Iterator var11 = lv3.faces.keySet().iterator();

            while(var11.hasNext()) {
               Direction lv4 = (Direction)var11.next();
               ModelElementFace lv5 = (ModelElementFace)lv3.faces.get(lv4);
               Sprite lv6 = (Sprite)textureGetter.apply(this.resolveSprite(lv5.textureId));
               if (lv5.cullFace == null) {
                  lv2.addQuad(createQuad(lv3, lv5, lv6, lv4, settings, id));
               } else {
                  lv2.addQuad(Direction.transform(settings.getRotation().getMatrix(), lv5.cullFace), createQuad(lv3, lv5, lv6, lv4, settings, id));
               }
            }
         }

         return lv2.build();
      }
   }

   private static BakedQuad createQuad(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side, ModelBakeSettings settings, Identifier id) {
      return QUAD_FACTORY.bake(element.from, element.to, elementFace, sprite, side, settings, element.rotation, element.shade, id);
   }

   public boolean textureExists(String name) {
      return !MissingSprite.getMissingSpriteId().equals(this.resolveSprite(name).getTextureId());
   }

   public SpriteIdentifier resolveSprite(String spriteName) {
      if (isTextureReference(spriteName)) {
         spriteName = spriteName.substring(1);
      }

      List list = Lists.newArrayList();

      while(true) {
         Either either = this.resolveTexture(spriteName);
         Optional optional = either.left();
         if (optional.isPresent()) {
            return (SpriteIdentifier)optional.get();
         }

         spriteName = (String)either.right().get();
         if (list.contains(spriteName)) {
            LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", new Object[]{Joiner.on("->").join(list), spriteName, this.id});
            return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId());
         }

         list.add(spriteName);
      }
   }

   private Either resolveTexture(String name) {
      for(JsonUnbakedModel lv = this; lv != null; lv = lv.parent) {
         Either either = (Either)lv.textureMap.get(name);
         if (either != null) {
            return either;
         }
      }

      return Either.left(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MissingSprite.getMissingSpriteId()));
   }

   static boolean isTextureReference(String reference) {
      return reference.charAt(0) == '#';
   }

   public JsonUnbakedModel getRootModel() {
      return this.parent == null ? this : this.parent.getRootModel();
   }

   public ModelTransformation getTransformations() {
      Transformation lv = this.getTransformation(ModelTransformationMode.THIRD_PERSON_LEFT_HAND);
      Transformation lv2 = this.getTransformation(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
      Transformation lv3 = this.getTransformation(ModelTransformationMode.FIRST_PERSON_LEFT_HAND);
      Transformation lv4 = this.getTransformation(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND);
      Transformation lv5 = this.getTransformation(ModelTransformationMode.HEAD);
      Transformation lv6 = this.getTransformation(ModelTransformationMode.GUI);
      Transformation lv7 = this.getTransformation(ModelTransformationMode.GROUND);
      Transformation lv8 = this.getTransformation(ModelTransformationMode.FIXED);
      return new ModelTransformation(lv, lv2, lv3, lv4, lv5, lv6, lv7, lv8);
   }

   private Transformation getTransformation(ModelTransformationMode renderMode) {
      return this.parent != null && !this.transformations.isTransformationDefined(renderMode) ? this.parent.getTransformation(renderMode) : this.transformations.getTransformation(renderMode);
   }

   public String toString() {
      return this.id;
   }

   @Environment(EnvType.CLIENT)
   public static enum GuiLight {
      ITEM("front"),
      BLOCK("side");

      private final String name;

      private GuiLight(String name) {
         this.name = name;
      }

      public static GuiLight byName(String value) {
         GuiLight[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            GuiLight lv = var1[var3];
            if (lv.name.equals(value)) {
               return lv;
            }
         }

         throw new IllegalArgumentException("Invalid gui light: " + value);
      }

      public boolean isSide() {
         return this == BLOCK;
      }

      // $FF: synthetic method
      private static GuiLight[] method_36920() {
         return new GuiLight[]{ITEM, BLOCK};
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public JsonUnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         List list = this.elementsFromJson(jsonDeserializationContext, jsonObject);
         String string = this.parentFromJson(jsonObject);
         Map map = this.texturesFromJson(jsonObject);
         Boolean boolean_ = this.ambientOcclusionFromJson(jsonObject);
         ModelTransformation lv = ModelTransformation.NONE;
         if (jsonObject.has("display")) {
            JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "display");
            lv = (ModelTransformation)jsonDeserializationContext.deserialize(jsonObject2, ModelTransformation.class);
         }

         List list2 = this.overridesFromJson(jsonDeserializationContext, jsonObject);
         GuiLight lv2 = null;
         if (jsonObject.has("gui_light")) {
            lv2 = JsonUnbakedModel.GuiLight.byName(JsonHelper.getString(jsonObject, "gui_light"));
         }

         Identifier lv3 = string.isEmpty() ? null : new Identifier(string);
         return new JsonUnbakedModel(lv3, list, map, boolean_, lv2, lv, list2);
      }

      protected List overridesFromJson(JsonDeserializationContext context, JsonObject object) {
         List list = Lists.newArrayList();
         if (object.has("overrides")) {
            JsonArray jsonArray = JsonHelper.getArray(object, "overrides");
            Iterator var5 = jsonArray.iterator();

            while(var5.hasNext()) {
               JsonElement jsonElement = (JsonElement)var5.next();
               list.add((ModelOverride)context.deserialize(jsonElement, ModelOverride.class));
            }
         }

         return list;
      }

      private Map texturesFromJson(JsonObject object) {
         Identifier lv = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
         Map map = Maps.newHashMap();
         if (object.has("textures")) {
            JsonObject jsonObject2 = JsonHelper.getObject(object, "textures");
            Iterator var5 = jsonObject2.entrySet().iterator();

            while(var5.hasNext()) {
               Map.Entry entry = (Map.Entry)var5.next();
               map.put((String)entry.getKey(), resolveReference(lv, ((JsonElement)entry.getValue()).getAsString()));
            }
         }

         return map;
      }

      private static Either resolveReference(Identifier id, String name) {
         if (JsonUnbakedModel.isTextureReference(name)) {
            return Either.right(name.substring(1));
         } else {
            Identifier lv = Identifier.tryParse(name);
            if (lv == null) {
               throw new JsonParseException(name + " is not valid resource location");
            } else {
               return Either.left(new SpriteIdentifier(id, lv));
            }
         }
      }

      private String parentFromJson(JsonObject json) {
         return JsonHelper.getString(json, "parent", "");
      }

      @Nullable
      protected Boolean ambientOcclusionFromJson(JsonObject json) {
         return json.has("ambientocclusion") ? JsonHelper.getBoolean(json, "ambientocclusion") : null;
      }

      protected List elementsFromJson(JsonDeserializationContext context, JsonObject json) {
         List list = Lists.newArrayList();
         if (json.has("elements")) {
            Iterator var4 = JsonHelper.getArray(json, "elements").iterator();

            while(var4.hasNext()) {
               JsonElement jsonElement = (JsonElement)var4.next();
               list.add((ModelElement)context.deserialize(jsonElement, ModelElement.class));
            }
         }

         return list;
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement element, Type unused, JsonDeserializationContext ctx) throws JsonParseException {
         return this.deserialize(element, unused, ctx);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class UncheckedModelException extends RuntimeException {
      public UncheckedModelException(String message) {
         super(message);
      }
   }
}
