package net.minecraft.util.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public interface LogReader extends Closeable {
   static LogReader create(final Codec codec, Reader reader) {
      final JsonReader jsonReader = new JsonReader(reader);
      jsonReader.setLenient(true);
      return new LogReader() {
         @Nullable
         public Object read() throws IOException {
            try {
               if (!jsonReader.hasNext()) {
                  return null;
               } else {
                  JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                  return Util.getResult(codec.parse(JsonOps.INSTANCE, jsonElement), IOException::new);
               }
            } catch (JsonParseException var2) {
               throw new IOException(var2);
            } catch (EOFException var3) {
               return null;
            }
         }

         public void close() throws IOException {
            jsonReader.close();
         }
      };
   }

   @Nullable
   Object read() throws IOException;
}
