package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class EntityScoresLootCondition implements LootCondition {
   final Map scores;
   final LootContext.EntityTarget target;

   EntityScoresLootCondition(Map scores, LootContext.EntityTarget target) {
      this.scores = ImmutableMap.copyOf(scores);
      this.target = target;
   }

   public LootConditionType getType() {
      return LootConditionTypes.ENTITY_SCORES;
   }

   public Set getRequiredParameters() {
      return (Set)Stream.concat(Stream.of(this.target.getParameter()), this.scores.values().stream().flatMap((arg) -> {
         return arg.getRequiredParameters().stream();
      })).collect(ImmutableSet.toImmutableSet());
   }

   public boolean test(LootContext arg) {
      Entity lv = (Entity)arg.get(this.target.getParameter());
      if (lv == null) {
         return false;
      } else {
         Scoreboard lv2 = lv.world.getScoreboard();
         Iterator var4 = this.scores.entrySet().iterator();

         Map.Entry entry;
         do {
            if (!var4.hasNext()) {
               return true;
            }

            entry = (Map.Entry)var4.next();
         } while(this.entityScoreIsInRange(arg, lv, lv2, (String)entry.getKey(), (BoundedIntUnaryOperator)entry.getValue()));

         return false;
      }
   }

   protected boolean entityScoreIsInRange(LootContext context, Entity entity, Scoreboard scoreboard, String objectiveName, BoundedIntUnaryOperator range) {
      ScoreboardObjective lv = scoreboard.getNullableObjective(objectiveName);
      if (lv == null) {
         return false;
      } else {
         String string2 = entity.getEntityName();
         return !scoreboard.playerHasObjective(string2, lv) ? false : range.test(context, scoreboard.getPlayerScore(string2, lv).getScore());
      }
   }

   public static Builder create(LootContext.EntityTarget target) {
      return new Builder(target);
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Builder implements LootCondition.Builder {
      private final Map scores = Maps.newHashMap();
      private final LootContext.EntityTarget target;

      public Builder(LootContext.EntityTarget target) {
         this.target = target;
      }

      public Builder score(String name, BoundedIntUnaryOperator value) {
         this.scores.put(name, value);
         return this;
      }

      public LootCondition build() {
         return new EntityScoresLootCondition(this.scores, this.target);
      }
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, EntityScoresLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject2 = new JsonObject();
         Iterator var5 = arg.scores.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            jsonObject2.add((String)entry.getKey(), jsonSerializationContext.serialize(entry.getValue()));
         }

         jsonObject.add("scores", jsonObject2);
         jsonObject.add("entity", jsonSerializationContext.serialize(arg.target));
      }

      public EntityScoresLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         Set set = JsonHelper.getObject(jsonObject, "scores").entrySet();
         Map map = Maps.newLinkedHashMap();
         Iterator var5 = set.iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            map.put((String)entry.getKey(), (BoundedIntUnaryOperator)JsonHelper.deserialize((JsonElement)entry.getValue(), "score", jsonDeserializationContext, BoundedIntUnaryOperator.class));
         }

         return new EntityScoresLootCondition(map, (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
