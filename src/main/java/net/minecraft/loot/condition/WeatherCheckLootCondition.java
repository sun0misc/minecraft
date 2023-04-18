package net.minecraft.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

public class WeatherCheckLootCondition implements LootCondition {
   @Nullable
   final Boolean raining;
   @Nullable
   final Boolean thundering;

   WeatherCheckLootCondition(@Nullable Boolean raining, @Nullable Boolean thundering) {
      this.raining = raining;
      this.thundering = thundering;
   }

   public LootConditionType getType() {
      return LootConditionTypes.WEATHER_CHECK;
   }

   public boolean test(LootContext arg) {
      ServerWorld lv = arg.getWorld();
      if (this.raining != null && this.raining != lv.isRaining()) {
         return false;
      } else {
         return this.thundering == null || this.thundering == lv.isThundering();
      }
   }

   public static Builder create() {
      return new Builder();
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Builder implements LootCondition.Builder {
      @Nullable
      private Boolean raining;
      @Nullable
      private Boolean thundering;

      public Builder raining(@Nullable Boolean raining) {
         this.raining = raining;
         return this;
      }

      public Builder thundering(@Nullable Boolean thundering) {
         this.thundering = thundering;
         return this;
      }

      public WeatherCheckLootCondition build() {
         return new WeatherCheckLootCondition(this.raining, this.thundering);
      }

      // $FF: synthetic method
      public LootCondition build() {
         return this.build();
      }
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, WeatherCheckLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("raining", arg.raining);
         jsonObject.addProperty("thundering", arg.thundering);
      }

      public WeatherCheckLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         Boolean boolean_ = jsonObject.has("raining") ? JsonHelper.getBoolean(jsonObject, "raining") : null;
         Boolean boolean2 = jsonObject.has("thundering") ? JsonHelper.getBoolean(jsonObject, "thundering") : null;
         return new WeatherCheckLootCondition(boolean_, boolean2);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
