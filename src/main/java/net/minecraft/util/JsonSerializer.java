package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public interface JsonSerializer {
   void toJson(JsonObject json, Object object, JsonSerializationContext context);

   Object fromJson(JsonObject json, JsonDeserializationContext context);
}
