package net.minecraft.predicate.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntityPredicate {
   public static final EntityPredicate ANY;
   private final EntityTypePredicate type;
   private final DistancePredicate distance;
   private final LocationPredicate location;
   private final LocationPredicate steppingOn;
   private final EntityEffectPredicate effects;
   private final NbtPredicate nbt;
   private final EntityFlagsPredicate flags;
   private final EntityEquipmentPredicate equipment;
   private final TypeSpecificPredicate typeSpecific;
   private final EntityPredicate vehicle;
   private final EntityPredicate passenger;
   private final EntityPredicate targetedEntity;
   @Nullable
   private final String team;

   private EntityPredicate(EntityTypePredicate type, DistancePredicate distance, LocationPredicate location, LocationPredicate steppingOn, EntityEffectPredicate effects, NbtPredicate nbt, EntityFlagsPredicate flags, EntityEquipmentPredicate equipment, TypeSpecificPredicate typeSpecific, @Nullable String team) {
      this.type = type;
      this.distance = distance;
      this.location = location;
      this.steppingOn = steppingOn;
      this.effects = effects;
      this.nbt = nbt;
      this.flags = flags;
      this.equipment = equipment;
      this.typeSpecific = typeSpecific;
      this.passenger = this;
      this.vehicle = this;
      this.targetedEntity = this;
      this.team = team;
   }

   EntityPredicate(EntityTypePredicate type, DistancePredicate distance, LocationPredicate location, LocationPredicate steppingOn, EntityEffectPredicate effects, NbtPredicate nbt, EntityFlagsPredicate flags, EntityEquipmentPredicate equipment, TypeSpecificPredicate typeSpecific, EntityPredicate vehicle, EntityPredicate passenger, EntityPredicate targetedEntity, @Nullable String team) {
      this.type = type;
      this.distance = distance;
      this.location = location;
      this.steppingOn = steppingOn;
      this.effects = effects;
      this.nbt = nbt;
      this.flags = flags;
      this.equipment = equipment;
      this.typeSpecific = typeSpecific;
      this.vehicle = vehicle;
      this.passenger = passenger;
      this.targetedEntity = targetedEntity;
      this.team = team;
   }

   public boolean test(ServerPlayerEntity player, @Nullable Entity entity) {
      return this.test(player.getWorld(), player.getPos(), entity);
   }

   public boolean test(ServerWorld world, @Nullable Vec3d pos, @Nullable Entity entity) {
      if (this == ANY) {
         return true;
      } else if (entity == null) {
         return false;
      } else if (!this.type.matches(entity.getType())) {
         return false;
      } else {
         if (pos == null) {
            if (this.distance != DistancePredicate.ANY) {
               return false;
            }
         } else if (!this.distance.test(pos.x, pos.y, pos.z, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
         }

         if (!this.location.test(world, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
         } else {
            if (this.steppingOn != LocationPredicate.ANY) {
               Vec3d lv = Vec3d.ofCenter(entity.getLandingPos());
               if (!this.steppingOn.test(world, lv.getX(), lv.getY(), lv.getZ())) {
                  return false;
               }
            }

            if (!this.effects.test(entity)) {
               return false;
            } else if (!this.nbt.test(entity)) {
               return false;
            } else if (!this.flags.test(entity)) {
               return false;
            } else if (!this.equipment.test(entity)) {
               return false;
            } else if (!this.typeSpecific.test(entity, world, pos)) {
               return false;
            } else if (!this.vehicle.test(world, pos, entity.getVehicle())) {
               return false;
            } else if (this.passenger != ANY && entity.getPassengerList().stream().noneMatch((entityx) -> {
               return this.passenger.test(world, pos, entityx);
            })) {
               return false;
            } else if (!this.targetedEntity.test(world, pos, entity instanceof MobEntity ? ((MobEntity)entity).getTarget() : null)) {
               return false;
            } else {
               if (this.team != null) {
                  AbstractTeam lv2 = entity.getScoreboardTeam();
                  if (lv2 == null || !this.team.equals(lv2.getName())) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public static EntityPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "entity");
         EntityTypePredicate lv = EntityTypePredicate.fromJson(jsonObject.get("type"));
         DistancePredicate lv2 = DistancePredicate.fromJson(jsonObject.get("distance"));
         LocationPredicate lv3 = LocationPredicate.fromJson(jsonObject.get("location"));
         LocationPredicate lv4 = LocationPredicate.fromJson(jsonObject.get("stepping_on"));
         EntityEffectPredicate lv5 = EntityEffectPredicate.fromJson(jsonObject.get("effects"));
         NbtPredicate lv6 = NbtPredicate.fromJson(jsonObject.get("nbt"));
         EntityFlagsPredicate lv7 = EntityFlagsPredicate.fromJson(jsonObject.get("flags"));
         EntityEquipmentPredicate lv8 = EntityEquipmentPredicate.fromJson(jsonObject.get("equipment"));
         TypeSpecificPredicate lv9 = TypeSpecificPredicate.fromJson(jsonObject.get("type_specific"));
         EntityPredicate lv10 = fromJson(jsonObject.get("vehicle"));
         EntityPredicate lv11 = fromJson(jsonObject.get("passenger"));
         EntityPredicate lv12 = fromJson(jsonObject.get("targeted_entity"));
         String string = JsonHelper.getString(jsonObject, "team", (String)null);
         return (new Builder()).type(lv).distance(lv2).location(lv3).steppingOn(lv4).effects(lv5).nbt(lv6).flags(lv7).equipment(lv8).typeSpecific(lv9).team(string).vehicle(lv10).passenger(lv11).targetedEntity(lv12).build();
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("type", this.type.toJson());
         jsonObject.add("distance", this.distance.toJson());
         jsonObject.add("location", this.location.toJson());
         jsonObject.add("stepping_on", this.steppingOn.toJson());
         jsonObject.add("effects", this.effects.toJson());
         jsonObject.add("nbt", this.nbt.toJson());
         jsonObject.add("flags", this.flags.toJson());
         jsonObject.add("equipment", this.equipment.toJson());
         jsonObject.add("type_specific", this.typeSpecific.toJson());
         jsonObject.add("vehicle", this.vehicle.toJson());
         jsonObject.add("passenger", this.passenger.toJson());
         jsonObject.add("targeted_entity", this.targetedEntity.toJson());
         jsonObject.addProperty("team", this.team);
         return jsonObject;
      }
   }

   public static LootContext createAdvancementEntityLootContext(ServerPlayerEntity player, Entity target) {
      return (new LootContext.Builder(player.getWorld())).parameter(LootContextParameters.THIS_ENTITY, target).parameter(LootContextParameters.ORIGIN, player.getPos()).random(player.getRandom()).build(LootContextTypes.ADVANCEMENT_ENTITY);
   }

   static {
      ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, EntityEffectPredicate.EMPTY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, TypeSpecificPredicate.ANY, (String)null);
   }

   public static class Builder {
      private EntityTypePredicate type;
      private DistancePredicate distance;
      private LocationPredicate location;
      private LocationPredicate steppingOn;
      private EntityEffectPredicate effects;
      private NbtPredicate nbt;
      private EntityFlagsPredicate flags;
      private EntityEquipmentPredicate equipment;
      private TypeSpecificPredicate typeSpecific;
      private EntityPredicate vehicle;
      private EntityPredicate passenger;
      private EntityPredicate targetedEntity;
      @Nullable
      private String team;

      public Builder() {
         this.type = EntityTypePredicate.ANY;
         this.distance = DistancePredicate.ANY;
         this.location = LocationPredicate.ANY;
         this.steppingOn = LocationPredicate.ANY;
         this.effects = EntityEffectPredicate.EMPTY;
         this.nbt = NbtPredicate.ANY;
         this.flags = EntityFlagsPredicate.ANY;
         this.equipment = EntityEquipmentPredicate.ANY;
         this.typeSpecific = TypeSpecificPredicate.ANY;
         this.vehicle = EntityPredicate.ANY;
         this.passenger = EntityPredicate.ANY;
         this.targetedEntity = EntityPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder type(EntityType type) {
         this.type = EntityTypePredicate.create(type);
         return this;
      }

      public Builder type(TagKey tag) {
         this.type = EntityTypePredicate.create(tag);
         return this;
      }

      public Builder type(EntityTypePredicate type) {
         this.type = type;
         return this;
      }

      public Builder distance(DistancePredicate distance) {
         this.distance = distance;
         return this;
      }

      public Builder location(LocationPredicate location) {
         this.location = location;
         return this;
      }

      public Builder steppingOn(LocationPredicate location) {
         this.steppingOn = location;
         return this;
      }

      public Builder effects(EntityEffectPredicate effects) {
         this.effects = effects;
         return this;
      }

      public Builder nbt(NbtPredicate nbt) {
         this.nbt = nbt;
         return this;
      }

      public Builder flags(EntityFlagsPredicate flags) {
         this.flags = flags;
         return this;
      }

      public Builder equipment(EntityEquipmentPredicate equipment) {
         this.equipment = equipment;
         return this;
      }

      public Builder typeSpecific(TypeSpecificPredicate typeSpecific) {
         this.typeSpecific = typeSpecific;
         return this;
      }

      public Builder vehicle(EntityPredicate vehicle) {
         this.vehicle = vehicle;
         return this;
      }

      public Builder passenger(EntityPredicate passenger) {
         this.passenger = passenger;
         return this;
      }

      public Builder targetedEntity(EntityPredicate targetedEntity) {
         this.targetedEntity = targetedEntity;
         return this;
      }

      public Builder team(@Nullable String team) {
         this.team = team;
         return this;
      }

      public EntityPredicate build() {
         return new EntityPredicate(this.type, this.distance, this.location, this.steppingOn, this.effects, this.nbt, this.flags, this.equipment, this.typeSpecific, this.vehicle, this.passenger, this.targetedEntity, this.team);
      }
   }

   public static class Extended {
      public static final Extended EMPTY = new Extended(new LootCondition[0]);
      private final LootCondition[] conditions;
      private final Predicate combinedCondition;

      private Extended(LootCondition[] conditions) {
         this.conditions = conditions;
         this.combinedCondition = LootConditionTypes.joinAnd(conditions);
      }

      public static Extended create(LootCondition... conditions) {
         return new Extended(conditions);
      }

      public static Extended getInJson(JsonObject root, String key, AdvancementEntityPredicateDeserializer predicateDeserializer) {
         JsonElement jsonElement = root.get(key);
         return fromJson(key, predicateDeserializer, jsonElement);
      }

      public static Extended[] requireInJson(JsonObject root, String key, AdvancementEntityPredicateDeserializer predicateDeserializer) {
         JsonElement jsonElement = root.get(key);
         if (jsonElement != null && !jsonElement.isJsonNull()) {
            JsonArray jsonArray = JsonHelper.asArray(jsonElement, key);
            Extended[] lvs = new Extended[jsonArray.size()];

            for(int i = 0; i < jsonArray.size(); ++i) {
               lvs[i] = fromJson(key + "[" + i + "]", predicateDeserializer, jsonArray.get(i));
            }

            return lvs;
         } else {
            return new Extended[0];
         }
      }

      private static Extended fromJson(String key, AdvancementEntityPredicateDeserializer predicateDeserializer, @Nullable JsonElement json) {
         if (json != null && json.isJsonArray()) {
            LootCondition[] lvs = predicateDeserializer.loadConditions(json.getAsJsonArray(), predicateDeserializer.getAdvancementId() + "/" + key, LootContextTypes.ADVANCEMENT_ENTITY);
            return new Extended(lvs);
         } else {
            EntityPredicate lv = EntityPredicate.fromJson(json);
            return ofLegacy(lv);
         }
      }

      public static Extended ofLegacy(EntityPredicate predicate) {
         if (predicate == EntityPredicate.ANY) {
            return EMPTY;
         } else {
            LootCondition lv = EntityPropertiesLootCondition.builder(LootContext.EntityTarget.THIS, predicate).build();
            return new Extended(new LootCondition[]{lv});
         }
      }

      public boolean test(LootContext context) {
         return this.combinedCondition.test(context);
      }

      public JsonElement toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : predicateSerializer.conditionsToJson(this.conditions));
      }

      public static JsonElement toPredicatesJsonArray(Extended[] predicates, AdvancementEntityPredicateSerializer predicateSerializer) {
         if (predicates.length == 0) {
            return JsonNull.INSTANCE;
         } else {
            JsonArray jsonArray = new JsonArray();
            Extended[] var3 = predicates;
            int var4 = predicates.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               Extended lv = var3[var5];
               jsonArray.add(lv.toJson(predicateSerializer));
            }

            return jsonArray;
         }
      }
   }
}
