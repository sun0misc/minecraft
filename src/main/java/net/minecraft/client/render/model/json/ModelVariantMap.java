package net.minecraft.client.render.model.json;

import com.google.common.annotations.VisibleForTesting;
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
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.MultipartUnbakedModel;
import net.minecraft.state.StateManager;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelVariantMap {
   private final Map variantMap = Maps.newLinkedHashMap();
   private MultipartUnbakedModel multipartModel;

   public static ModelVariantMap fromJson(DeserializationContext context, Reader reader) {
      return (ModelVariantMap)JsonHelper.deserialize(context.gson, reader, ModelVariantMap.class);
   }

   public static ModelVariantMap fromJson(DeserializationContext context, JsonElement json) {
      return (ModelVariantMap)context.gson.fromJson(json, ModelVariantMap.class);
   }

   public ModelVariantMap(Map variantMap, MultipartUnbakedModel multipartModel) {
      this.multipartModel = multipartModel;
      this.variantMap.putAll(variantMap);
   }

   public ModelVariantMap(List variantMapList) {
      ModelVariantMap lv = null;

      ModelVariantMap lv2;
      for(Iterator var3 = variantMapList.iterator(); var3.hasNext(); this.variantMap.putAll(lv2.variantMap)) {
         lv2 = (ModelVariantMap)var3.next();
         if (lv2.hasMultipartModel()) {
            this.variantMap.clear();
            lv = lv2;
         }
      }

      if (lv != null) {
         this.multipartModel = lv.multipartModel;
      }

   }

   @VisibleForTesting
   public boolean containsVariant(String key) {
      return this.variantMap.get(key) != null;
   }

   @VisibleForTesting
   public WeightedUnbakedModel getVariant(String key) {
      WeightedUnbakedModel lv = (WeightedUnbakedModel)this.variantMap.get(key);
      if (lv == null) {
         throw new VariantAbsentException();
      } else {
         return lv;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         if (o instanceof ModelVariantMap) {
            ModelVariantMap lv = (ModelVariantMap)o;
            if (this.variantMap.equals(lv.variantMap)) {
               return this.hasMultipartModel() ? this.multipartModel.equals(lv.multipartModel) : !lv.hasMultipartModel();
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return 31 * this.variantMap.hashCode() + (this.hasMultipartModel() ? this.multipartModel.hashCode() : 0);
   }

   public Map getVariantMap() {
      return this.variantMap;
   }

   @VisibleForTesting
   public Set getAllModels() {
      Set set = Sets.newHashSet(this.variantMap.values());
      if (this.hasMultipartModel()) {
         set.addAll(this.multipartModel.getModels());
      }

      return set;
   }

   public boolean hasMultipartModel() {
      return this.multipartModel != null;
   }

   public MultipartUnbakedModel getMultipartModel() {
      return this.multipartModel;
   }

   @Environment(EnvType.CLIENT)
   public static final class DeserializationContext {
      protected final Gson gson = (new GsonBuilder()).registerTypeAdapter(ModelVariantMap.class, new Deserializer()).registerTypeAdapter(ModelVariant.class, new ModelVariant.Deserializer()).registerTypeAdapter(WeightedUnbakedModel.class, new WeightedUnbakedModel.Deserializer()).registerTypeAdapter(MultipartUnbakedModel.class, new MultipartUnbakedModel.Deserializer(this)).registerTypeAdapter(MultipartModelComponent.class, new MultipartModelComponent.Deserializer()).create();
      private StateManager stateFactory;

      public StateManager getStateFactory() {
         return this.stateFactory;
      }

      public void setStateFactory(StateManager stateFactory) {
         this.stateFactory = stateFactory;
      }
   }

   @Environment(EnvType.CLIENT)
   protected class VariantAbsentException extends RuntimeException {
   }

   @Environment(EnvType.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public ModelVariantMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         Map map = this.variantsFromJson(jsonDeserializationContext, jsonObject);
         MultipartUnbakedModel lv = this.multipartFromJson(jsonDeserializationContext, jsonObject);
         if (!map.isEmpty() || lv != null && !lv.getModels().isEmpty()) {
            return new ModelVariantMap(map, lv);
         } else {
            throw new JsonParseException("Neither 'variants' nor 'multipart' found");
         }
      }

      protected Map variantsFromJson(JsonDeserializationContext context, JsonObject object) {
         Map map = Maps.newHashMap();
         if (object.has("variants")) {
            JsonObject jsonObject2 = JsonHelper.getObject(object, "variants");
            Iterator var5 = jsonObject2.entrySet().iterator();

            while(var5.hasNext()) {
               Map.Entry entry = (Map.Entry)var5.next();
               map.put((String)entry.getKey(), (WeightedUnbakedModel)context.deserialize((JsonElement)entry.getValue(), WeightedUnbakedModel.class));
            }
         }

         return map;
      }

      @Nullable
      protected MultipartUnbakedModel multipartFromJson(JsonDeserializationContext context, JsonObject object) {
         if (!object.has("multipart")) {
            return null;
         } else {
            JsonArray jsonArray = JsonHelper.getArray(object, "multipart");
            return (MultipartUnbakedModel)context.deserialize(jsonArray, MultipartUnbakedModel.class);
         }
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(functionJson, unused, context);
      }
   }
}
