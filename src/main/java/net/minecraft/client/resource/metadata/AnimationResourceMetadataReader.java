package net.minecraft.client.resource.metadata;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AnimationResourceMetadataReader implements ResourceMetadataReader {
   public AnimationResourceMetadata fromJson(JsonObject jsonObject) {
      ImmutableList.Builder builder = ImmutableList.builder();
      int i = JsonHelper.getInt(jsonObject, "frametime", 1);
      if (i != 1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid default frame time");
      }

      int j;
      if (jsonObject.has("frames")) {
         try {
            JsonArray jsonArray = JsonHelper.getArray(jsonObject, "frames");

            for(j = 0; j < jsonArray.size(); ++j) {
               JsonElement jsonElement = jsonArray.get(j);
               AnimationFrameResourceMetadata lv = this.readFrameMetadata(j, jsonElement);
               if (lv != null) {
                  builder.add(lv);
               }
            }
         } catch (ClassCastException var8) {
            throw new JsonParseException("Invalid animation->frames: expected array, was " + jsonObject.get("frames"), var8);
         }
      }

      int k = JsonHelper.getInt(jsonObject, "width", -1);
      j = JsonHelper.getInt(jsonObject, "height", -1);
      if (k != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)k, "Invalid width");
      }

      if (j != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)j, "Invalid height");
      }

      boolean bl = JsonHelper.getBoolean(jsonObject, "interpolate", false);
      return new AnimationResourceMetadata(builder.build(), k, j, i, bl);
   }

   @Nullable
   private AnimationFrameResourceMetadata readFrameMetadata(int frame, JsonElement json) {
      if (json.isJsonPrimitive()) {
         return new AnimationFrameResourceMetadata(JsonHelper.asInt(json, "frames[" + frame + "]"));
      } else if (json.isJsonObject()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "frames[" + frame + "]");
         int j = JsonHelper.getInt(jsonObject, "time", -1);
         if (jsonObject.has("time")) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)j, "Invalid frame time");
         }

         int k = JsonHelper.getInt(jsonObject, "index");
         Validate.inclusiveBetween(0L, 2147483647L, (long)k, "Invalid frame index");
         return new AnimationFrameResourceMetadata(k, j);
      } else {
         return null;
      }
   }

   public String getKey() {
      return "animation";
   }

   // $FF: synthetic method
   public Object fromJson(JsonObject json) {
      return this.fromJson(json);
   }
}
