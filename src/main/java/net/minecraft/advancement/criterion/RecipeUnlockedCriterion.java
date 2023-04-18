package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class RecipeUnlockedCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("recipe_unlocked");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      Identifier lv = new Identifier(JsonHelper.getString(jsonObject, "recipe"));
      return new Conditions(arg, lv);
   }

   public void trigger(ServerPlayerEntity player, Recipe recipe) {
      this.trigger(player, (conditions) -> {
         return conditions.matches(recipe);
      });
   }

   public static Conditions create(Identifier id) {
      return new Conditions(EntityPredicate.Extended.EMPTY, id);
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      private final Identifier recipe;

      public Conditions(EntityPredicate.Extended player, Identifier recipe) {
         super(RecipeUnlockedCriterion.ID, player);
         this.recipe = recipe;
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         jsonObject.addProperty("recipe", this.recipe.toString());
         return jsonObject;
      }

      public boolean matches(Recipe recipe) {
         return this.recipe.equals(recipe.getId());
      }
   }
}
