package net.minecraft.client.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AtlasSourceManager {
   private static final BiMap SOURCE_TYPE_BY_ID = HashBiMap.create();
   public static final AtlasSourceType SINGLE;
   public static final AtlasSourceType DIRECTORY;
   public static final AtlasSourceType FILTER;
   public static final AtlasSourceType UNSTITCH;
   public static final AtlasSourceType PALETTED_PERMUTATIONS;
   public static Codec CODEC;
   public static Codec TYPE_CODEC;
   public static Codec LIST_CODEC;

   private static AtlasSourceType register(String id, Codec codec) {
      AtlasSourceType lv = new AtlasSourceType(codec);
      Identifier lv2 = new Identifier(id);
      AtlasSourceType lv3 = (AtlasSourceType)SOURCE_TYPE_BY_ID.putIfAbsent(lv2, lv);
      if (lv3 != null) {
         throw new IllegalStateException("Duplicate registration " + lv2);
      } else {
         return lv;
      }
   }

   static {
      SINGLE = register("single", SingleAtlasSource.CODEC);
      DIRECTORY = register("directory", DirectoryAtlasSource.CODEC);
      FILTER = register("filter", FilterAtlasSource.CODEC);
      UNSTITCH = register("unstitch", UnstitchAtlasSource.CODEC);
      PALETTED_PERMUTATIONS = register("paletted_permutations", PalettedPermutationsAtlasSource.CODEC);
      CODEC = Identifier.CODEC.flatXmap((id) -> {
         AtlasSourceType lv = (AtlasSourceType)SOURCE_TYPE_BY_ID.get(id);
         return lv != null ? DataResult.success(lv) : DataResult.error(() -> {
            return "Unknown type " + id;
         });
      }, (type) -> {
         Identifier lv = (Identifier)SOURCE_TYPE_BY_ID.inverse().get(type);
         return type != null ? DataResult.success(lv) : DataResult.error(() -> {
            return "Unknown type " + lv;
         });
      });
      TYPE_CODEC = CODEC.dispatch(AtlasSource::getType, AtlasSourceType::codec);
      LIST_CODEC = TYPE_CODEC.listOf().fieldOf("sources").codec();
   }
}
