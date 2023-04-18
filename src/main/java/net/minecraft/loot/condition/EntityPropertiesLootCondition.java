package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.Vec3d;

public class EntityPropertiesLootCondition implements LootCondition {
   final EntityPredicate predicate;
   final LootContext.EntityTarget entity;

   EntityPropertiesLootCondition(EntityPredicate predicate, LootContext.EntityTarget entity) {
      this.predicate = predicate;
      this.entity = entity;
   }

   public LootConditionType getType() {
      return LootConditionTypes.ENTITY_PROPERTIES;
   }

   public Set getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.ORIGIN, this.entity.getParameter());
   }

   public boolean test(LootContext arg) {
      Entity lv = (Entity)arg.get(this.entity.getParameter());
      Vec3d lv2 = (Vec3d)arg.get(LootContextParameters.ORIGIN);
      return this.predicate.test(arg.getWorld(), lv2, lv);
   }

   public static LootCondition.Builder create(LootContext.EntityTarget entity) {
      return builder(entity, EntityPredicate.Builder.create());
   }

   public static LootCondition.Builder builder(LootContext.EntityTarget entity, EntityPredicate.Builder predicateBuilder) {
      return () -> {
         return new EntityPropertiesLootCondition(predicateBuilder.build(), entity);
      };
   }

   public static LootCondition.Builder builder(LootContext.EntityTarget entity, EntityPredicate predicate) {
      return () -> {
         return new EntityPropertiesLootCondition(predicate, entity);
      };
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((LootContext)context);
   }

   public static class Serializer implements JsonSerializer {
      public void toJson(JsonObject jsonObject, EntityPropertiesLootCondition arg, JsonSerializationContext jsonSerializationContext) {
         jsonObject.add("predicate", arg.predicate.toJson());
         jsonObject.add("entity", jsonSerializationContext.serialize(arg.entity));
      }

      public EntityPropertiesLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         EntityPredicate lv = EntityPredicate.fromJson(jsonObject.get("predicate"));
         return new EntityPropertiesLootCondition(lv, (LootContext.EntityTarget)JsonHelper.deserialize(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class));
      }

      // $FF: synthetic method
      public Object fromJson(JsonObject json, JsonDeserializationContext context) {
         return this.fromJson(json, context);
      }
   }
}
