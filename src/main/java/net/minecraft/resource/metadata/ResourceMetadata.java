package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.resource.InputSupplier;
import net.minecraft.util.JsonHelper;

public interface ResourceMetadata {
   ResourceMetadata NONE = new ResourceMetadata() {
      public Optional decode(ResourceMetadataReader reader) {
         return Optional.empty();
      }
   };
   InputSupplier NONE_SUPPLIER = () -> {
      return NONE;
   };

   static ResourceMetadata create(InputStream stream) throws IOException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

      ResourceMetadata var3;
      try {
         final JsonObject jsonObject = JsonHelper.deserialize((Reader)bufferedReader);
         var3 = new ResourceMetadata() {
            public Optional decode(ResourceMetadataReader reader) {
               String string = reader.getKey();
               return jsonObject.has(string) ? Optional.of(reader.fromJson(JsonHelper.getObject(jsonObject, string))) : Optional.empty();
            }
         };
      } catch (Throwable var5) {
         try {
            bufferedReader.close();
         } catch (Throwable var4) {
            var5.addSuppressed(var4);
         }

         throw var5;
      }

      bufferedReader.close();
      return var3;
   }

   Optional decode(ResourceMetadataReader reader);
}
