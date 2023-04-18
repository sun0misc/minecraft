package net.minecraft.loot.provider.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.score.ContextLootScoreProvider;
import net.minecraft.loot.provider.score.LootScoreProvider;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class ScoreLootNumberProvider implements LootNumberProvider {
   final LootScoreProvider target;
   final String score;
   final float scale;

   ScoreLootNumberProvider(LootScoreProvider target, String score, float scale) {
      this.target = target;
      this.score = score;
      this.scale = scale;
   }

   public LootNumberProviderType getType() {
      return LootNumberProviderTypes.SCORE;
   }

   public Set getRequiredParameters() {
      return this.target.getRequiredParameters();
   }

   public static ScoreLootNumberProvider create(LootContext.EntityTarget target, String score) {
      return create(target, score, 1.0F);
   }

   public static ScoreLootNumberProvider create(LootContext.EntityTarget target, String score, float scale) {
      return new ScoreLootNumberProvider(ContextLootScoreProvider.create(target), score, scale);
   }

   public float nextFloat(LootContext context) {
      String string = this.target.getName(context);
      if (string == null) {
         return 0.0F;
      } else {
         Scoreboard lv = context.getWorld().getScoreboard();
         ScoreboardObjective lv2 = lv.getNullableObjective(this.score);
         if (lv2 == null) {
            return 0.0F;
         } else {
            return !lv.playerHasObjective(string, lv2) ? 0.0F : (float)lv.getPlayerScore(string, lv2).getScore() * this.scale;
         }
      }
   }

   public static class Serializer implements JsonSerializer {
      public ScoreLootNumberProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         String string = JsonHelper.getString(jsonObject, "score");
         float f = JsonHelper.getFloat(jsonObject, "scale", 1.0F);
         LootScoreProvider lv = (LootScoreProvider)JsonHelper.deserialize(jsonObject, "target", jsonDeserializationContext, LootScoreProvider.class);
         return new ScoreLootNumberProvider(lv, string, f);
      }

      public void toJson(JsonObject jsonObject, ScoreLootNumberProvider arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("score", arg.score);
         jsonObject.add("target", jsonSerializationContext.serialize(arg.target));
         jsonObject.addProperty("scale", arg.scale);
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
