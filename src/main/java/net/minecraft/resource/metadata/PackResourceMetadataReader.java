package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

public class PackResourceMetadataReader implements ResourceMetadataSerializer {
   public PackResourceMetadata fromJson(JsonObject jsonObject) {
      Text lv = Text.Serializer.fromJson(jsonObject.get("description"));
      if (lv == null) {
         throw new JsonParseException("Invalid/missing description!");
      } else {
         int i = JsonHelper.getInt(jsonObject, "pack_format");
         return new PackResourceMetadata(lv, i);
      }
   }

   public JsonObject toJson(PackResourceMetadata arg) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("description", Text.Serializer.toJsonTree(arg.getDescription()));
      jsonObject.addProperty("pack_format", arg.getPackFormat());
      return jsonObject;
   }

   public String getKey() {
      return "pack";
   }

   // $FF: synthetic method
   public Object fromJson(JsonObject json) {
      return this.fromJson(json);
   }
}
