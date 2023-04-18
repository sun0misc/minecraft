package net.minecraft.loot.provider.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;

public final class ConstantLootNumberProvider implements LootNumberProvider {
   final float value;

   ConstantLootNumberProvider(float value) {
      this.value = value;
   }

   public LootNumberProviderType getType() {
      return LootNumberProviderTypes.CONSTANT;
   }

   public float nextFloat(LootContext context) {
      return this.value;
   }

   public static ConstantLootNumberProvider create(float value) {
      return new ConstantLootNumberProvider(value);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         return Float.compare(((ConstantLootNumberProvider)o).value, this.value) == 0;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.value != 0.0F ? Float.floatToIntBits(this.value) : 0;
   }

   public static class CustomSerializer implements JsonSerializing.ElementSerializer {
      public JsonElement toJson(ConstantLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
         return new JsonPrimitive(arg.value);
      }

      public ConstantLootNumberProvider fromJson(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
         return new ConstantLootNumberProvider(JsonHelper.asFloat(jsonElement, "value"));
      }

      // $FF: synthetic method
      public Object fromJson(JsonElement json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, ConstantLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("value", arg.value);
      }

      public ConstantLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         float f = JsonHelper.getFloat(jsonObject, "value");
         return new ConstantLootNumberProvider(f);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
