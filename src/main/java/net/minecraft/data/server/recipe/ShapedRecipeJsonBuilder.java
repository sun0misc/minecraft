package net.minecraft.data.server.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class ShapedRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
   private final RecipeCategory category;
   private final Item output;
   private final int count;
   private final List pattern = Lists.newArrayList();
   private final Map inputs = Maps.newLinkedHashMap();
   private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
   @Nullable
   private String group;
   private boolean showNotification = true;

   public ShapedRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
      this.category = category;
      this.output = output.asItem();
      this.count = count;
   }

   public static ShapedRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
      return create(category, output, 1);
   }

   public static ShapedRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
      return new ShapedRecipeJsonBuilder(category, output, count);
   }

   public ShapedRecipeJsonBuilder input(Character c, TagKey tag) {
      return this.input(c, Ingredient.fromTag(tag));
   }

   public ShapedRecipeJsonBuilder input(Character c, ItemConvertible itemProvider) {
      return this.input(c, Ingredient.ofItems(itemProvider));
   }

   public ShapedRecipeJsonBuilder input(Character c, Ingredient ingredient) {
      if (this.inputs.containsKey(c)) {
         throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
      } else if (c == ' ') {
         throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
      } else {
         this.inputs.put(c, ingredient);
         return this;
      }
   }

   public ShapedRecipeJsonBuilder pattern(String patternStr) {
      if (!this.pattern.isEmpty() && patternStr.length() != ((String)this.pattern.get(0)).length()) {
         throw new IllegalArgumentException("Pattern must be the same width on every line!");
      } else {
         this.pattern.add(patternStr);
         return this;
      }
   }

   public ShapedRecipeJsonBuilder criterion(String string, CriterionConditions arg) {
      this.advancementBuilder.criterion(string, arg);
      return this;
   }

   public ShapedRecipeJsonBuilder group(@Nullable String string) {
      this.group = string;
      return this;
   }

   public ShapedRecipeJsonBuilder showNotification(boolean showNotification) {
      this.showNotification = showNotification;
      return this;
   }

   public Item getOutputItem() {
      return this.output;
   }

   public void offerTo(Consumer exporter, Identifier recipeId) {
      this.validate(recipeId);
      this.advancementBuilder.parent(ROOT).criterion("has_the_recipe", (CriterionConditions)RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
      exporter.accept(new ShapedRecipeJsonProvider(recipeId, this.output, this.count, this.group == null ? "" : this.group, getCraftingCategory(this.category), this.pattern, this.inputs, this.advancementBuilder, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"), this.showNotification));
   }

   private void validate(Identifier recipeId) {
      if (this.pattern.isEmpty()) {
         throw new IllegalStateException("No pattern is defined for shaped recipe " + recipeId + "!");
      } else {
         Set set = Sets.newHashSet(this.inputs.keySet());
         set.remove(' ');
         Iterator var3 = this.pattern.iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();

            for(int i = 0; i < string.length(); ++i) {
               char c = string.charAt(i);
               if (!this.inputs.containsKey(c) && c != ' ') {
                  throw new IllegalStateException("Pattern in recipe " + recipeId + " uses undefined symbol '" + c + "'");
               }

               set.remove(c);
            }
         }

         if (!set.isEmpty()) {
            throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + recipeId);
         } else if (this.pattern.size() == 1 && ((String)this.pattern.get(0)).length() == 1) {
            throw new IllegalStateException("Shaped recipe " + recipeId + " only takes in a single item - should it be a shapeless recipe instead?");
         } else if (this.advancementBuilder.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
         }
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

   static class ShapedRecipeJsonProvider extends RecipeJsonBuilder.CraftingRecipeJsonProvider {
      private final Identifier recipeId;
      private final Item output;
      private final int resultCount;
      private final String group;
      private final List pattern;
      private final Map inputs;
      private final Advancement.Builder advancementBuilder;
      private final Identifier advancementId;
      private final boolean showNotification;

      public ShapedRecipeJsonProvider(Identifier recipeId, Item output, int resultCount, String group, CraftingRecipeCategory craftingCategory, List pattern, Map inputs, Advancement.Builder advancementBuilder, Identifier advancementId, boolean showNotification) {
         super(craftingCategory);
         this.recipeId = recipeId;
         this.output = output;
         this.resultCount = resultCount;
         this.group = group;
         this.pattern = pattern;
         this.inputs = inputs;
         this.advancementBuilder = advancementBuilder;
         this.advancementId = advancementId;
         this.showNotification = showNotification;
      }

      public void serialize(JsonObject json) {
         super.serialize(json);
         if (!this.group.isEmpty()) {
            json.addProperty("group", this.group);
         }

         JsonArray jsonArray = new JsonArray();
         Iterator var3 = this.pattern.iterator();

         while(var3.hasNext()) {
            String string = (String)var3.next();
            jsonArray.add(string);
         }

         json.add("pattern", jsonArray);
         JsonObject jsonObject2 = new JsonObject();
         Iterator var7 = this.inputs.entrySet().iterator();

         while(var7.hasNext()) {
            Map.Entry entry = (Map.Entry)var7.next();
            jsonObject2.add(String.valueOf(entry.getKey()), ((Ingredient)entry.getValue()).toJson());
         }

         json.add("key", jsonObject2);
         JsonObject jsonObject3 = new JsonObject();
         jsonObject3.addProperty("item", Registries.ITEM.getId(this.output).toString());
         if (this.resultCount > 1) {
            jsonObject3.addProperty("count", this.resultCount);
         }

         json.add("result", jsonObject3);
         json.addProperty("show_notification", this.showNotification);
      }

      public RecipeSerializer getSerializer() {
         return RecipeSerializer.SHAPED;
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
