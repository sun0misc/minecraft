package net.minecraft.predicate.entity;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.NumberRange;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class PlayerPredicate implements TypeSpecificPredicate {
   public static final int field_33928 = 100;
   private final NumberRange.IntRange experienceLevel;
   @Nullable
   private final GameMode gameMode;
   private final Map stats;
   private final Object2BooleanMap recipes;
   private final Map advancements;
   private final EntityPredicate lookingAt;

   private static AdvancementPredicate criterionFromJson(JsonElement json) {
      if (json.isJsonPrimitive()) {
         boolean bl = json.getAsBoolean();
         return new CompletedAdvancementPredicate(bl);
      } else {
         Object2BooleanMap object2BooleanMap = new Object2BooleanOpenHashMap();
         JsonObject jsonObject = JsonHelper.asObject(json, "criterion data");
         jsonObject.entrySet().forEach((entry) -> {
            boolean bl = JsonHelper.asBoolean((JsonElement)entry.getValue(), "criterion test");
            object2BooleanMap.put((String)entry.getKey(), bl);
         });
         return new AdvancementCriteriaPredicate(object2BooleanMap);
      }
   }

   PlayerPredicate(NumberRange.IntRange experienceLevel, @Nullable GameMode gameMode, Map stats, Object2BooleanMap recipes, Map advancements, EntityPredicate lookingAt) {
      this.experienceLevel = experienceLevel;
      this.gameMode = gameMode;
      this.stats = stats;
      this.recipes = recipes;
      this.advancements = advancements;
      this.lookingAt = lookingAt;
   }

   public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
      if (!(entity instanceof ServerPlayerEntity lv)) {
         return false;
      } else if (!this.experienceLevel.test(lv.experienceLevel)) {
         return false;
      } else if (this.gameMode != null && this.gameMode != lv.interactionManager.getGameMode()) {
         return false;
      } else {
         StatHandler lv2 = lv.getStatHandler();
         Iterator var6 = this.stats.entrySet().iterator();

         while(var6.hasNext()) {
            Map.Entry entry = (Map.Entry)var6.next();
            int i = lv2.getStat((Stat)entry.getKey());
            if (!((NumberRange.IntRange)entry.getValue()).test(i)) {
               return false;
            }
         }

         RecipeBook lv3 = lv.getRecipeBook();
         ObjectIterator var13 = this.recipes.object2BooleanEntrySet().iterator();

         while(var13.hasNext()) {
            Object2BooleanMap.Entry entry2 = (Object2BooleanMap.Entry)var13.next();
            if (lv3.contains((Identifier)entry2.getKey()) != entry2.getBooleanValue()) {
               return false;
            }
         }

         if (!this.advancements.isEmpty()) {
            PlayerAdvancementTracker lv4 = lv.getAdvancementTracker();
            ServerAdvancementLoader lv5 = lv.getServer().getAdvancementLoader();
            Iterator var9 = this.advancements.entrySet().iterator();

            while(var9.hasNext()) {
               Map.Entry entry3 = (Map.Entry)var9.next();
               Advancement lv6 = lv5.get((Identifier)entry3.getKey());
               if (lv6 == null || !((AdvancementPredicate)entry3.getValue()).test(lv4.getProgress(lv6))) {
                  return false;
               }
            }
         }

         if (this.lookingAt != EntityPredicate.ANY) {
            Vec3d lv7 = lv.getEyePos();
            Vec3d lv8 = lv.getRotationVec(1.0F);
            Vec3d lv9 = lv7.add(lv8.x * 100.0, lv8.y * 100.0, lv8.z * 100.0);
            EntityHitResult lv10 = ProjectileUtil.getEntityCollision(lv.world, lv, lv7, lv9, (new Box(lv7, lv9)).expand(1.0), (hitEntity) -> {
               return !hitEntity.isSpectator();
            }, 0.0F);
            if (lv10 != null && lv10.getType() == HitResult.Type.ENTITY) {
               Entity lv11 = lv10.getEntity();
               if (this.lookingAt.test(lv, lv11) && lv.canSee(lv11)) {
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   public static PlayerPredicate fromJson(JsonObject json) {
      NumberRange.IntRange lv = NumberRange.IntRange.fromJson(json.get("level"));
      String string = JsonHelper.getString(json, "gamemode", "");
      GameMode lv2 = GameMode.byName(string, (GameMode)null);
      Map map = Maps.newHashMap();
      JsonArray jsonArray = JsonHelper.getArray(json, "stats", (JsonArray)null);
      if (jsonArray != null) {
         Iterator var6 = jsonArray.iterator();

         while(var6.hasNext()) {
            JsonElement jsonElement = (JsonElement)var6.next();
            JsonObject jsonObject2 = JsonHelper.asObject(jsonElement, "stats entry");
            Identifier lv3 = new Identifier(JsonHelper.getString(jsonObject2, "type"));
            StatType lv4 = (StatType)Registries.STAT_TYPE.get(lv3);
            if (lv4 == null) {
               throw new JsonParseException("Invalid stat type: " + lv3);
            }

            Identifier lv5 = new Identifier(JsonHelper.getString(jsonObject2, "stat"));
            Stat lv6 = getStat(lv4, lv5);
            NumberRange.IntRange lv7 = NumberRange.IntRange.fromJson(jsonObject2.get("value"));
            map.put(lv6, lv7);
         }
      }

      Object2BooleanMap object2BooleanMap = new Object2BooleanOpenHashMap();
      JsonObject jsonObject3 = JsonHelper.getObject(json, "recipes", new JsonObject());
      Iterator var16 = jsonObject3.entrySet().iterator();

      while(var16.hasNext()) {
         Map.Entry entry = (Map.Entry)var16.next();
         Identifier lv8 = new Identifier((String)entry.getKey());
         boolean bl = JsonHelper.asBoolean((JsonElement)entry.getValue(), "recipe present");
         object2BooleanMap.put(lv8, bl);
      }

      Map map2 = Maps.newHashMap();
      JsonObject jsonObject4 = JsonHelper.getObject(json, "advancements", new JsonObject());
      Iterator var21 = jsonObject4.entrySet().iterator();

      while(var21.hasNext()) {
         Map.Entry entry2 = (Map.Entry)var21.next();
         Identifier lv9 = new Identifier((String)entry2.getKey());
         AdvancementPredicate lv10 = criterionFromJson((JsonElement)entry2.getValue());
         map2.put(lv9, lv10);
      }

      EntityPredicate lv11 = EntityPredicate.fromJson(json.get("looking_at"));
      return new PlayerPredicate(lv, lv2, map, object2BooleanMap, map2, lv11);
   }

   private static Stat getStat(StatType type, Identifier id) {
      Registry lv = type.getRegistry();
      Object object = lv.get(id);
      if (object == null) {
         throw new JsonParseException("Unknown object " + id + " for stat type " + Registries.STAT_TYPE.getId(type));
      } else {
         return type.getOrCreateStat(object);
      }
   }

   private static Identifier getStatId(Stat stat) {
      return stat.getType().getRegistry().getId(stat.getValue());
   }

   public JsonObject typeSpecificToJson() {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("level", this.experienceLevel.toJson());
      if (this.gameMode != null) {
         jsonObject.addProperty("gamemode", this.gameMode.getName());
      }

      if (!this.stats.isEmpty()) {
         JsonArray jsonArray = new JsonArray();
         this.stats.forEach((stat, arg2) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", Registries.STAT_TYPE.getId(stat.getType()).toString());
            jsonObject.addProperty("stat", getStatId(stat).toString());
            jsonObject.add("value", arg2.toJson());
            jsonArray.add(jsonObject);
         });
         jsonObject.add("stats", jsonArray);
      }

      JsonObject jsonObject2;
      if (!this.recipes.isEmpty()) {
         jsonObject2 = new JsonObject();
         this.recipes.forEach((id, boolean_) -> {
            jsonObject2.addProperty(id.toString(), boolean_);
         });
         jsonObject.add("recipes", jsonObject2);
      }

      if (!this.advancements.isEmpty()) {
         jsonObject2 = new JsonObject();
         this.advancements.forEach((id, arg2) -> {
            jsonObject2.add(id.toString(), arg2.toJson());
         });
         jsonObject.add("advancements", jsonObject2);
      }

      jsonObject.add("looking_at", this.lookingAt.toJson());
      return jsonObject;
   }

   public TypeSpecificPredicate.Deserializer getDeserializer() {
      return TypeSpecificPredicate.Deserializers.PLAYER;
   }

   private static class CompletedAdvancementPredicate implements AdvancementPredicate {
      private final boolean done;

      public CompletedAdvancementPredicate(boolean done) {
         this.done = done;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(this.done);
      }

      public boolean test(AdvancementProgress arg) {
         return arg.isDone() == this.done;
      }

      // $FF: synthetic method
      public boolean test(Object progress) {
         return this.test((AdvancementProgress)progress);
      }
   }

   private static class AdvancementCriteriaPredicate implements AdvancementPredicate {
      private final Object2BooleanMap criteria;

      public AdvancementCriteriaPredicate(Object2BooleanMap criteria) {
         this.criteria = criteria;
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         Object2BooleanMap var10000 = this.criteria;
         Objects.requireNonNull(jsonObject);
         var10000.forEach(jsonObject::addProperty);
         return jsonObject;
      }

      public boolean test(AdvancementProgress arg) {
         ObjectIterator var2 = this.criteria.object2BooleanEntrySet().iterator();

         Object2BooleanMap.Entry entry;
         CriterionProgress lv;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            entry = (Object2BooleanMap.Entry)var2.next();
            lv = arg.getCriterionProgress((String)entry.getKey());
         } while(lv != null && lv.isObtained() == entry.getBooleanValue());

         return false;
      }

      // $FF: synthetic method
      public boolean test(Object progress) {
         return this.test((AdvancementProgress)progress);
      }
   }

   private interface AdvancementPredicate extends Predicate {
      JsonElement toJson();
   }

   public static class Builder {
      private NumberRange.IntRange experienceLevel;
      @Nullable
      private GameMode gameMode;
      private final Map stats;
      private final Object2BooleanMap recipes;
      private final Map advancements;
      private EntityPredicate lookingAt;

      public Builder() {
         this.experienceLevel = NumberRange.IntRange.ANY;
         this.stats = Maps.newHashMap();
         this.recipes = new Object2BooleanOpenHashMap();
         this.advancements = Maps.newHashMap();
         this.lookingAt = EntityPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder experienceLevel(NumberRange.IntRange experienceLevel) {
         this.experienceLevel = experienceLevel;
         return this;
      }

      public Builder stat(Stat stat, NumberRange.IntRange value) {
         this.stats.put(stat, value);
         return this;
      }

      public Builder recipe(Identifier id, boolean unlocked) {
         this.recipes.put(id, unlocked);
         return this;
      }

      public Builder gameMode(GameMode gameMode) {
         this.gameMode = gameMode;
         return this;
      }

      public Builder lookingAt(EntityPredicate lookingAt) {
         this.lookingAt = lookingAt;
         return this;
      }

      public Builder advancement(Identifier id, boolean done) {
         this.advancements.put(id, new CompletedAdvancementPredicate(done));
         return this;
      }

      public Builder advancement(Identifier id, Map criteria) {
         this.advancements.put(id, new AdvancementCriteriaPredicate(new Object2BooleanOpenHashMap(criteria)));
         return this;
      }

      public PlayerPredicate build() {
         return new PlayerPredicate(this.experienceLevel, this.gameMode, this.stats, this.recipes, this.advancements, this.lookingAt);
      }
   }
}
