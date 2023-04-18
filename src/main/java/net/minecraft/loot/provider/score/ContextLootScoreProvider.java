package net.minecraft.loot.provider.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;
import org.jetbrains.annotations.Nullable;

public class ContextLootScoreProvider implements LootScoreProvider {
   final LootContext.EntityTarget target;

   ContextLootScoreProvider(LootContext.EntityTarget target) {
      this.target = target;
   }

   public static LootScoreProvider create(LootContext.EntityTarget target) {
      return new ContextLootScoreProvider(target);
   }

   public LootScoreProviderType getType() {
      return LootScoreProviderTypes.CONTEXT;
   }

   @Nullable
   public String getName(LootContext context) {
      Entity lv = (Entity)context.get(this.target.getParameter());
      return lv != null ? lv.getEntityName() : null;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(this.target.getParameter());
   }

   public static class CustomSerializer implements JsonSerializing.ElementSerializer {
      public JsonElement toJson(ContextLootScoreProvider arg, JsonSerializationContext jsonSerializationContext) {
         return jsonSerializationContext.serialize(arg.target);
      }

      public ContextLootScoreProvider fromJson(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
         LootContext.EntityTarget lv = (LootContext.EntityTarget)jsonDeserializationContext.deserialize(jsonElement, LootContext.EntityTarget.class);
         return new ContextLootScoreProvider(lv);
      }

      // $FF: synthetic method
      public Object fromJson(JsonElement json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, ContextLootScoreProvider arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("target", arg.target.name());
      }

      public ContextLootScoreProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         LootContext.EntityTarget lv = (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "target", jsonDeserializationContext, LootContext.EntityTarget.class);
         return new ContextLootScoreProvider(lv);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
