package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;

public interface ResourceMetadataReader {
   String getKey();

   Object fromJson(JsonObject json);
}
