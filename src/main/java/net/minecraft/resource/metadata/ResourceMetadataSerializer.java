package net.minecraft.resource.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public interface ResourceMetadataSerializer extends ResourceMetadataReader {
   JsonObject toJson(Object metadata);

   static ResourceMetadataSerializer fromCodec(final String key, final Codec codec) {
      return new ResourceMetadataSerializer() {
         public String getKey() {
            return key;
         }

         public Object fromJson(JsonObject json) {
            return codec.parse(JsonOps.INSTANCE, json).getOrThrow(false, (error) -> {
            });
         }

         public JsonObject toJson(Object metadata) {
            return ((JsonElement)codec.encodeStart(JsonOps.INSTANCE, metadata).getOrThrow(false, (error) -> {
            })).getAsJsonObject();
         }
      };
   }
}
