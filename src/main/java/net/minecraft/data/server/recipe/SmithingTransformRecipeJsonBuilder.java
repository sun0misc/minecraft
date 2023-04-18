package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SmithingTransformRecipeJsonBuilder {
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final RecipeCategory category;
   private final Item result;
   private final Advancement.Builder advancement = Advancement.Builder.create();
   private final RecipeSerializer serializer;

   public SmithingTransformRecipeJsonBuilder(RecipeSerializer serializer, Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
      this.category = category;
      this.serializer = serializer;
      this.template = template;
      this.base = base;
      this.addition = addition;
      this.result = result;
   }

   public static SmithingTransformRecipeJsonBuilder create(Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category, Item result) {
      return new SmithingTransformRecipeJsonBuilder(RecipeSerializer.SMITHING_TRANSFORM, template, base, addition, category, result);
   }

   public SmithingTransformRecipeJsonBuilder criterion(String name, CriterionConditions conditions) {
      this.advancement.criterion(name, conditions);
      return this;
   }

   public void offerTo(Consumer exporter, String recipeId) {
      this.offerTo(exporter, new Identifier(recipeId));
   }

   public void offerTo(Consumer exporter, Identifier recipeId) {
      this.validate(recipeId);
      this.advancement.parent(CraftingRecipeJsonBuilder.ROOT).criterion("has_the_recipe", (CriterionConditions)RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
      exporter.accept(new SmithingTransformRecipeJsonProvider(recipeId, this.serializer, this.template, this.base, this.addition, this.result, this.advancement, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
   }

   private void validate(Identifier recipeId) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + recipeId);
      }
   }

   public static record SmithingTransformRecipeJsonProvider(Identifier id, RecipeSerializer type, Ingredient template, Ingredient base, Ingredient addition, Item result, Advancement.Builder advancement, Identifier advancementId) implements RecipeJsonProvider {
      public SmithingTransformRecipeJsonProvider(Identifier arg, RecipeSerializer arg2, Ingredient arg3, Ingredient arg4, Ingredient arg5, Item arg6, Advancement.Builder arg7, Identifier arg8) {
         this.id = arg;
         this.type = arg2;
         this.template = arg3;
         this.base = arg4;
         this.addition = arg5;
         this.result = arg6;
         this.advancement = arg7;
         this.advancementId = arg8;
      }

      public void serialize(JsonObject json) {
         json.add("template", this.template.toJson());
         json.add("base", this.base.toJson());
         json.add("addition", this.addition.toJson());
         JsonObject jsonObject2 = new JsonObject();
         jsonObject2.addProperty("item", Registries.ITEM.getId(this.result).toString());
         json.add("result", jsonObject2);
      }

      public Identifier getRecipeId() {
         return this.id;
      }

      public RecipeSerializer getSerializer() {
         return this.type;
      }

      @Nullable
      public JsonObject toAdvancementJson() {
         return this.advancement.toJson();
      }

      @Nullable
      public Identifier getAdvancementId() {
         return this.advancementId;
      }

      public Identifier id() {
         return this.id;
      }

      public RecipeSerializer type() {
         return this.type;
      }

      public Ingredient template() {
         return this.template;
      }

      public Ingredient base() {
         return this.base;
      }

      public Ingredient addition() {
         return this.addition;
      }

      public Item result() {
         return this.result;
      }

      public Advancement.Builder advancement() {
         return this.advancement;
      }

      public Identifier advancementId() {
         return this.advancementId;
      }
   }
}
