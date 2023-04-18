package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory {
   @Nullable
   public TypeAdapter create(Gson gson, TypeToken typeToken) {
      Class class_ = typeToken.getRawType();
      if (!class_.isEnum()) {
         return null;
      } else {
         final Map map = Maps.newHashMap();
         Object[] var5 = class_.getEnumConstants();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Object object = var5[var7];
            map.put(this.getKey(object), object);
         }

         return new TypeAdapter() {
            public void write(JsonWriter writer, Object o) throws IOException {
               if (o == null) {
                  writer.nullValue();
               } else {
                  writer.value(LowercaseEnumTypeAdapterFactory.this.getKey(o));
               }

            }

            @Nullable
            public Object read(JsonReader reader) throws IOException {
               if (reader.peek() == JsonToken.NULL) {
                  reader.nextNull();
                  return null;
               } else {
                  return map.get(reader.nextString());
               }
            }
         };
      }
   }

   String getKey(Object o) {
      return o instanceof Enum ? ((Enum)o).name().toLowerCase(Locale.ROOT) : o.toString().toLowerCase(Locale.ROOT);
   }
}
