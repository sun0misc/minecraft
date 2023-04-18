package net.minecraft.predicate.entity;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.TagPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class DamageSourcePredicate {
   public static final DamageSourcePredicate EMPTY = DamageSourcePredicate.Builder.create().build();
   private final List tagPredicates;
   private final EntityPredicate directEntity;
   private final EntityPredicate sourceEntity;

   public DamageSourcePredicate(List tagPredicates, EntityPredicate directEntity, EntityPredicate sourceEntity) {
      this.tagPredicates = tagPredicates;
      this.directEntity = directEntity;
      this.sourceEntity = sourceEntity;
   }

   public boolean test(ServerPlayerEntity player, DamageSource damageSource) {
      return this.test(player.getWorld(), player.getPos(), damageSource);
   }

   public boolean test(ServerWorld world, Vec3d pos, DamageSource damageSource) {
      if (this == EMPTY) {
         return true;
      } else {
         Iterator var4 = this.tagPredicates.iterator();

         TagPredicate lv;
         do {
            if (!var4.hasNext()) {
               if (!this.directEntity.test(world, pos, damageSource.getSource())) {
                  return false;
               }

               if (!this.sourceEntity.test(world, pos, damageSource.getAttacker())) {
                  return false;
               }

               return true;
            }

            lv = (TagPredicate)var4.next();
         } while(lv.test(damageSource.getTypeRegistryEntry()));

         return false;
      }
   }

   public static DamageSourcePredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "damage type");
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "tags", (JsonArray)null);
         Object list;
         if (jsonArray != null) {
            list = new ArrayList(jsonArray.size());
            Iterator var4 = jsonArray.iterator();

            while(var4.hasNext()) {
               JsonElement jsonElement2 = (JsonElement)var4.next();
               ((List)list).add(TagPredicate.fromJson(jsonElement2, RegistryKeys.DAMAGE_TYPE));
            }
         } else {
            list = List.of();
         }

         EntityPredicate lv = EntityPredicate.fromJson(jsonObject.get("direct_entity"));
         EntityPredicate lv2 = EntityPredicate.fromJson(jsonObject.get("source_entity"));
         return new DamageSourcePredicate((List)list, lv, lv2);
      } else {
         return EMPTY;
      }
   }

   public JsonElement toJson() {
      if (this == EMPTY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (!this.tagPredicates.isEmpty()) {
            JsonArray jsonArray = new JsonArray(this.tagPredicates.size());

            for(int i = 0; i < this.tagPredicates.size(); ++i) {
               jsonArray.add(((TagPredicate)this.tagPredicates.get(i)).toJson());
            }

            jsonObject.add("tags", jsonArray);
         }

         jsonObject.add("direct_entity", this.directEntity.toJson());
         jsonObject.add("source_entity", this.sourceEntity.toJson());
         return jsonObject;
      }
   }

   public static class Builder {
      private final ImmutableList.Builder tagPredicates = ImmutableList.builder();
      private EntityPredicate directEntity;
      private EntityPredicate sourceEntity;

      public Builder() {
         this.directEntity = EntityPredicate.ANY;
         this.sourceEntity = EntityPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder tag(TagPredicate tagPredicate) {
         this.tagPredicates.add(tagPredicate);
         return this;
      }

      public Builder directEntity(EntityPredicate entity) {
         this.directEntity = entity;
         return this;
      }

      public Builder directEntity(EntityPredicate.Builder entity) {
         this.directEntity = entity.build();
         return this;
      }

      public Builder sourceEntity(EntityPredicate entity) {
         this.sourceEntity = entity;
         return this;
      }

      public Builder sourceEntity(EntityPredicate.Builder entity) {
         this.sourceEntity = entity.build();
         return this;
      }

      public DamageSourcePredicate build() {
         return new DamageSourcePredicate(this.tagPredicates.build(), this.directEntity, this.sourceEntity);
      }
   }
}
