package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.function.Function;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class JsonSerializing {
   public static SerializerBuilder createSerializerBuilder(Registry registry, String rootFieldName, String idFieldName, Function typeGetter) {
      return new SerializerBuilder(registry, rootFieldName, idFieldName, typeGetter);
   }

   public static class SerializerBuilder {
      private final Registry registry;
      private final String rootFieldName;
      private final String idFieldName;
      private final Function typeGetter;
      @Nullable
      private com.mojang.datafixers.util.Pair elementSerializer;
      @Nullable
      private JsonSerializableType defaultType;

      SerializerBuilder(Registry registry, String rootFieldName, String idFieldName, Function typeIdentification) {
         this.registry = registry;
         this.rootFieldName = rootFieldName;
         this.idFieldName = idFieldName;
         this.typeGetter = typeIdentification;
      }

      public SerializerBuilder elementSerializer(JsonSerializableType type, ElementSerializer serializer) {
         this.elementSerializer = com.mojang.datafixers.util.Pair.of(type, serializer);
         return this;
      }

      public SerializerBuilder defaultType(JsonSerializableType defaultType) {
         this.defaultType = defaultType;
         return this;
      }

      public Object build() {
         return new GsonSerializer(this.registry, this.rootFieldName, this.idFieldName, this.typeGetter, this.defaultType, this.elementSerializer);
      }
   }

   public interface ElementSerializer {
      JsonElement toJson(Object object, JsonSerializationContext context);

      Object fromJson(JsonElement json, JsonDeserializationContext context);
   }

   private static class GsonSerializer implements JsonDeserializer, com.google.gson.JsonSerializer {
      private final Registry registry;
      private final String rootFieldName;
      private final String idFieldName;
      private final Function typeGetter;
      @Nullable
      private final JsonSerializableType defaultType;
      @Nullable
      private final com.mojang.datafixers.util.Pair elementSerializer;

      GsonSerializer(Registry registry, String rootFieldName, String idFieldName, Function typeGetter, @Nullable JsonSerializableType defaultType, @Nullable com.mojang.datafixers.util.Pair elementSerializer) {
         this.registry = registry;
         this.rootFieldName = rootFieldName;
         this.idFieldName = idFieldName;
         this.typeGetter = typeGetter;
         this.defaultType = defaultType;
         this.elementSerializer = elementSerializer;
      }

      public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         if (json.isJsonObject()) {
            JsonObject jsonObject = JsonHelper.asObject(json, this.rootFieldName);
            String string = JsonHelper.getString(jsonObject, this.idFieldName, "");
            JsonSerializableType lv;
            if (string.isEmpty()) {
               lv = this.defaultType;
            } else {
               Identifier lv2 = new Identifier(string);
               lv = (JsonSerializableType)this.registry.get(lv2);
            }

            if (lv == null) {
               throw new JsonSyntaxException("Unknown type '" + string + "'");
            } else {
               return lv.getJsonSerializer().fromJson(jsonObject, context);
            }
         } else if (this.elementSerializer == null) {
            throw new UnsupportedOperationException("Object " + json + " can't be deserialized");
         } else {
            return ((ElementSerializer)this.elementSerializer.getSecond()).fromJson(json, context);
         }
      }

      public JsonElement serialize(Object object, Type type, JsonSerializationContext context) {
         JsonSerializableType lv = (JsonSerializableType)this.typeGetter.apply(object);
         if (this.elementSerializer != null && this.elementSerializer.getFirst() == lv) {
            return ((ElementSerializer)this.elementSerializer.getSecond()).toJson(object, context);
         } else if (lv == null) {
            throw new JsonSyntaxException("Unknown type: " + object);
         } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(this.idFieldName, this.registry.getId(lv).toString());
            lv.getJsonSerializer().toJson(jsonObject, object, context);
            return jsonObject;
         }
      }
   }
}
