package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class ChangedDimensionCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("changed_dimension");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      RegistryKey lv = jsonObject.has("from") ? RegistryKey.of(RegistryKeys.WORLD, new Identifier(JsonHelper.getString(jsonObject, "from"))) : null;
      RegistryKey lv2 = jsonObject.has("to") ? RegistryKey.of(RegistryKeys.WORLD, new Identifier(JsonHelper.getString(jsonObject, "to"))) : null;
      return new Conditions(arg, lv, lv2);
   }

   public void trigger(ServerPlayerEntity player, RegistryKey from, RegistryKey to) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(from, to);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      @Nullable
      private final RegistryKey from;
      @Nullable
      private final RegistryKey to;

      public Conditions(EntityPredicate.Extended player, @Nullable RegistryKey from, @Nullable RegistryKey to) {
         super(ChangedDimensionCriterion.ID, player);
         this.from = from;
         this.to = to;
      }

      public static Conditions create() {
         return new Conditions(EntityPredicate.Extended.EMPTY, (RegistryKey)null, (RegistryKey)null);
      }

      public static Conditions create(RegistryKey from, RegistryKey to) {
         return new Conditions(EntityPredicate.Extended.EMPTY, from, to);
      }

      public static Conditions to(RegistryKey to) {
         return new Conditions(EntityPredicate.Extended.EMPTY, (RegistryKey)null, to);
      }

      public static Conditions from(RegistryKey from) {
         return new Conditions(EntityPredicate.Extended.EMPTY, from, (RegistryKey)null);
      }

      public boolean matches(RegistryKey from, RegistryKey to) {
         if (this.from != null && this.from != from) {
            return false;
         } else {
            return this.to == null || this.to == to;
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         if (this.from != null) {
            jsonObject.addProperty("from", this.from.getValue().toString());
         }

         if (this.to != null) {
            jsonObject.addProperty("to", this.to.getValue().toString());
         }

         return jsonObject;
      }
   }
}
