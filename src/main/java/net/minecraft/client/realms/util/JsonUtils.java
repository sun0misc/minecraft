package net.minecraft.client.realms.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class JsonUtils {
   public static Object get(String key, JsonObject node, Function deserializer) {
      JsonElement jsonElement = node.get(key);
      if (jsonElement != null && !jsonElement.isJsonNull()) {
         if (!jsonElement.isJsonObject()) {
            throw new IllegalStateException("Required property " + key + " was not a JsonObject as espected");
         } else {
            return deserializer.apply(jsonElement.getAsJsonObject());
         }
      } else {
         throw new IllegalStateException("Missing required property: " + key);
      }
   }

   public static String getString(String key, JsonObject node) {
      String string2 = getStringOr(key, node, (String)null);
      if (string2 == null) {
         throw new IllegalStateException("Missing required property: " + key);
      } else {
         return string2;
      }
   }

   @Nullable
   public static String getStringOr(String key, JsonObject node, @Nullable String defaultValue) {
      JsonElement jsonElement = node.get(key);
      if (jsonElement != null) {
         return jsonElement.isJsonNull() ? defaultValue : jsonElement.getAsString();
      } else {
         return defaultValue;
      }
   }

   @Nullable
   public static UUID getUuidOr(String key, JsonObject node, @Nullable UUID defaultValue) {
      String string2 = getStringOr(key, node, (String)null);
      return string2 == null ? defaultValue : UUID.fromString(string2);
   }

   public static int getIntOr(String key, JsonObject node, int defaultValue) {
      JsonElement jsonElement = node.get(key);
      if (jsonElement != null) {
         return jsonElement.isJsonNull() ? defaultValue : jsonElement.getAsInt();
      } else {
         return defaultValue;
      }
   }

   public static long getLongOr(String key, JsonObject node, long defaultValue) {
      JsonElement jsonElement = node.get(key);
      if (jsonElement != null) {
         return jsonElement.isJsonNull() ? defaultValue : jsonElement.getAsLong();
      } else {
         return defaultValue;
      }
   }

   public static boolean getBooleanOr(String key, JsonObject node, boolean defaultValue) {
      JsonElement jsonElement = node.get(key);
      if (jsonElement != null) {
         return jsonElement.isJsonNull() ? defaultValue : jsonElement.getAsBoolean();
      } else {
         return defaultValue;
      }
   }

   public static Date getDateOr(String key, JsonObject node) {
      JsonElement jsonElement = node.get(key);
      return jsonElement != null ? new Date(Long.parseLong(jsonElement.getAsString())) : new Date();
   }
}
