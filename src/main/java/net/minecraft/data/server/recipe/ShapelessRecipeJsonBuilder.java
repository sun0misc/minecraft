package net.minecraft.data.server.recipe;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ShapelessRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
   private final RecipeCategory category;
   private final Item output;
   private final int count;
   private final List inputs = Lists.newArrayList();
   private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
   @Nullable
   private String group;

   public ShapelessRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
      this.category = category;
      this.output = output.asItem();
      this.count = count;
   }

   public static ShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
      return new ShapelessRecipeJsonBuilder(category, output, 1);
   }

   public static ShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
      return new ShapelessRecipeJsonBuilder(category, output, count);
   }

   public ShapelessRecipeJsonBuilder input(TagKey tag) {
      return this.input(Ingredient.fromTag(tag));
   }

   public ShapelessRecipeJsonBuilder input(ItemConvertible itemProvider) {
      return this.input((ItemConvertible)itemProvider, 1);
   }

   public ShapelessRecipeJsonBuilder input(ItemConvertible itemProvider, int size) {
      for(int j = 0; j < size; ++j) {
         this.input(Ingredient.ofItems(itemProvider));
      }

      return this;
   }

   public ShapelessRecipeJsonBuilder input(Ingredient ingredient) {
      return this.input((Ingredient)ingredient, 1);
   }

   public ShapelessRecipeJsonBuilder input(Ingredient ingredient, int size) {
      for(int j = 0; j < size; ++j) {
         this.inputs.add(ingredient);
      }

      return this;
   }

   public ShapelessRecipeJsonBuilder criterion(String string, CriterionConditions arg) {
      this.advancementBuilder.criterion(string, arg);
      return this;
   }

   public ShapelessRecipeJsonBuilder group(@Nullable String string) {
      this.group = string;
      return this;
   }

   public Item getOutputItem() {
      return this.output;
   }

   public void offerTo(Consumer exporter, Identifier recipeId) {
      this.validate(recipeId);
      this.advancementBuilder.parent(ROOT).criterion("has_the_recipe", (CriterionConditions)RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
      exporter.accept(new ShapelessRecipeJsonProvider(recipeId, this.output, this.count, this.group == null ? "" : this.group, getCraftingCategory(this.category), this.inputs, this.advancementBuilder, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
   }

   private void validate(Identifier recipeId) {
      if (this.advancementBuilder.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + recipeId);
      }
   }

   // $FF: synthetic method
   public CraftingRecipeJsonBuilder group(@Nullable String group) {
      return this.group(group);
   }

   // $FF: synthetic method
   public CraftingRecipeJsonBuilder criterion(String name, CriterionConditions conditions) {
      return this.criterion(name, conditions);
   }

   public static class ShapelessRecipeJsonProvider extends RecipeJsonBuilder.CraftingRecipeJsonProvider {
      private final Identifier recipeId;
      private final Item output;
      private final int count;
      private final String group;
      private final List inputs;
      private final Advancement.Builder advancementBuilder;
      private final Identifier advancementId;

      public ShapelessRecipeJsonProvider(Identifier recipeId, Item output, int outputCount, String group, CraftingRecipeCategory craftingCategory, List inputs, Advancement.Builder advancementBuilder, Identifier advancementId) {
         super(craftingCategory);
         this.recipeId = recipeId;
         this.output = output;
         this.count = outputCount;
         this.group = group;
         this.inputs = inputs;
         this.advancementBuilder = advancementBuilder;
         this.advancementId = advancementId;
      }

      public void serialize(JsonObject json) {
         super.serialize(json);
         if (!this.group.isEmpty()) {
            json.addProperty("group", this.group);
         }

         JsonArray jsonArray = new JsonArray();
         Iterator var3 = this.inputs.iterator();

         while(var3.hasNext()) {
            Ingredient lv = (Ingredient)var3.next();
            jsonArray.add(lv.toJson());
         }

         json.add("ingredients", jsonArray);
         JsonObject jsonObject2 = new JsonObject();
         jsonObject2.addProperty("item", Registries.ITEM.getId(this.output).toString());
         if (this.count > 1) {
            jsonObject2.addProperty("count", this.count);
         }

         json.add("result", jsonObject2);
      }

      public RecipeSerializer getSerializer() {
         return RecipeSerializer.SHAPELESS;
      }

      public Identifier getRecipeId() {
         return this.recipeId;
      }

      @Nullable
      public JsonObject toAdvancementJson() {
         return this.advancementBuilder.toJson();
      }

      @Nullable
      public Identifier getAdvancementId() {
         return this.advancementId;
      }
   }
}
