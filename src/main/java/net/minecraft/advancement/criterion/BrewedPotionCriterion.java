package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class BrewedPotionCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("brewed_potion");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      Potion lv = null;
      if (jsonObject.has("potion")) {
         Identifier lv2 = new Identifier(JsonHelper.getString(jsonObject, "potion"));
         lv = (Potion)Registries.POTION.getOrEmpty(lv2).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown potion '" + lv2 + "'");
         });
      }

      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, Potion potion) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(potion);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      @Nullable
      private final Potion potion;

      public Conditions(EntityPredicate.Extended player, @Nullable Potion potion) {
         super(BrewedPotionCriterion.ID, player);
         this.potion = potion;
      }

      public static Conditions any() {
         return new Conditions(EntityPredicate.Extended.EMPTY, (Potion)null);
      }

      public boolean matches(Potion potion) {
         return this.potion == null || this.potion == potion;
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         if (this.potion != null) {
            jsonObject.addProperty("potion", Registries.POTION.getId(this.potion).toString());
         }

         return jsonObject;
      }
   }
}
