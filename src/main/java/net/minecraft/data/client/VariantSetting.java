package net.minecraft.data.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;

public class VariantSetting {
   final String key;
   final Function writer;

   public VariantSetting(String key, Function writer) {
      this.key = key;
      this.writer = writer;
   }

   public Value evaluate(Object value) {
      return new Value(value);
   }

   public String toString() {
      return this.key;
   }

   public class Value {
      private final Object value;

      public Value(Object value) {
         this.value = value;
      }

      public VariantSetting getParent() {
         return VariantSetting.this;
      }

      public void writeTo(JsonObject json) {
         json.add(VariantSetting.this.key, (JsonElement)VariantSetting.this.writer.apply(this.value));
      }

      public String toString() {
         return VariantSetting.this.key + "=" + this.value;
      }
   }
}
