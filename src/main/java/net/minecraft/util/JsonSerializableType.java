package net.minecraft.util;

public class JsonSerializableType {
   private final JsonSerializer jsonSerializer;

   public JsonSerializableType(JsonSerializer jsonSerializer) {
      this.jsonSerializer = jsonSerializer;
   }

   public JsonSerializer getJsonSerializer() {
      return this.jsonSerializer;
   }
}
