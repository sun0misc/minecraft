package net.minecraft.predicate.entity;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class EntityEffectPredicate {
   public static final EntityEffectPredicate EMPTY = new EntityEffectPredicate(Collections.emptyMap());
   private final Map effects;

   public EntityEffectPredicate(Map effects) {
      this.effects = effects;
   }

   public static EntityEffectPredicate create() {
      return new EntityEffectPredicate(Maps.newLinkedHashMap());
   }

   public EntityEffectPredicate withEffect(StatusEffect statusEffect) {
      this.effects.put(statusEffect, new EffectData());
      return this;
   }

   public EntityEffectPredicate withEffect(StatusEffect statusEffect, EffectData data) {
      this.effects.put(statusEffect, data);
      return this;
   }

   public boolean test(Entity entity) {
      if (this == EMPTY) {
         return true;
      } else {
         return entity instanceof LivingEntity ? this.test(((LivingEntity)entity).getActiveStatusEffects()) : false;
      }
   }

   public boolean test(LivingEntity livingEntity) {
      return this == EMPTY ? true : this.test(livingEntity.getActiveStatusEffects());
   }

   public boolean test(Map effects) {
      if (this == EMPTY) {
         return true;
      } else {
         Iterator var2 = this.effects.entrySet().iterator();

         Map.Entry entry;
         StatusEffectInstance lv;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            entry = (Map.Entry)var2.next();
            lv = (StatusEffectInstance)effects.get(entry.getKey());
         } while(((EffectData)entry.getValue()).test(lv));

         return false;
      }
   }

   public static EntityEffectPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "effects");
         Map map = Maps.newLinkedHashMap();
         Iterator var3 = jsonObject.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            Identifier lv = new Identifier((String)entry.getKey());
            StatusEffect lv2 = (StatusEffect)Registries.STATUS_EFFECT.getOrEmpty(lv).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown effect '" + lv + "'");
            });
            EffectData lv3 = EntityEffectPredicate.EffectData.fromJson(JsonHelper.asObject((JsonElement)entry.getValue(), (String)entry.getKey()));
            map.put(lv2, lv3);
         }

         return new EntityEffectPredicate(map);
      } else {
         return EMPTY;
      }
   }

   public JsonElement toJson() {
      if (this == EMPTY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         Iterator var2 = this.effects.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry entry = (Map.Entry)var2.next();
            jsonObject.add(Registries.STATUS_EFFECT.getId((StatusEffect)entry.getKey()).toString(), ((EffectData)entry.getValue()).toJson());
         }

         return jsonObject;
      }
   }

   public static class EffectData {
      private final NumberRange.IntRange amplifier;
      private final NumberRange.IntRange duration;
      @Nullable
      private final Boolean ambient;
      @Nullable
      private final Boolean visible;

      public EffectData(NumberRange.IntRange amplifier, NumberRange.IntRange duration, @Nullable Boolean ambient, @Nullable Boolean visible) {
         this.amplifier = amplifier;
         this.duration = duration;
         this.ambient = ambient;
         this.visible = visible;
      }

      public EffectData() {
         this(NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, (Boolean)null, (Boolean)null);
      }

      public boolean test(@Nullable StatusEffectInstance statusEffectInstance) {
         if (statusEffectInstance == null) {
            return false;
         } else if (!this.amplifier.test(statusEffectInstance.getAmplifier())) {
            return false;
         } else if (!this.duration.test(statusEffectInstance.getDuration())) {
            return false;
         } else if (this.ambient != null && this.ambient != statusEffectInstance.isAmbient()) {
            return false;
         } else {
            return this.visible == null || this.visible == statusEffectInstance.shouldShowParticles();
         }
      }

      public JsonElement toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("amplifier", this.amplifier.toJson());
         jsonObject.add("duration", this.duration.toJson());
         jsonObject.addProperty("ambient", this.ambient);
         jsonObject.addProperty("visible", this.visible);
         return jsonObject;
      }

      public static EffectData fromJson(JsonObject json) {
         NumberRange.IntRange lv = NumberRange.IntRange.fromJson(json.get("amplifier"));
         NumberRange.IntRange lv2 = NumberRange.IntRange.fromJson(json.get("duration"));
         Boolean boolean_ = json.has("ambient") ? JsonHelper.getBoolean(json, "ambient") : null;
         Boolean boolean2 = json.has("visible") ? JsonHelper.getBoolean(json, "visible") : null;
         return new EffectData(lv, lv2, boolean_, boolean2);
      }
   }
}
