package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

@Environment(EnvType.CLIENT)
public class ParticleTextureData {
   private final List textureList;

   private ParticleTextureData(List textureList) {
      this.textureList = textureList;
   }

   public List getTextureList() {
      return this.textureList;
   }

   public static ParticleTextureData load(JsonObject json) {
      JsonArray jsonArray = JsonHelper.getArray(json, "textures", (JsonArray)null);
      if (jsonArray == null) {
         return new ParticleTextureData(List.of());
      } else {
         List list = (List)Streams.stream(jsonArray).map((texture) -> {
            return JsonHelper.asString(texture, "texture");
         }).map(Identifier::new).collect(ImmutableList.toImmutableList());
         return new ParticleTextureData(list);
      }
   }
}
