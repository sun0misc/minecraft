package net.minecraft.client.font;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public enum FontType {
   BITMAP("bitmap", BitmapFont.Loader::fromJson),
   TTF("ttf", TrueTypeFontLoader::fromJson),
   SPACE("space", SpaceFont::fromJson),
   LEGACY_UNICODE("legacy_unicode", UnicodeTextureFont.Loader::fromJson);

   private static final Map REGISTRY = (Map)Util.make(Maps.newHashMap(), (map) -> {
      FontType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         FontType lv = var1[var3];
         map.put(lv.id, lv);
      }

   });
   private final String id;
   private final Function loaderFactory;

   private FontType(String id, Function factory) {
      this.id = id;
      this.loaderFactory = factory;
   }

   public static FontType byId(String id) {
      FontType lv = (FontType)REGISTRY.get(id);
      if (lv == null) {
         throw new IllegalArgumentException("Invalid type: " + id);
      } else {
         return lv;
      }
   }

   public FontLoader createLoader(JsonObject json) {
      return (FontLoader)this.loaderFactory.apply(json);
   }

   // $FF: synthetic method
   private static FontType[] method_36876() {
      return new FontType[]{BITMAP, TTF, SPACE, LEGACY_UNICODE};
   }
}
