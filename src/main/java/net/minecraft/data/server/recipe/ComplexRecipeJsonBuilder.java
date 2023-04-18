package net.minecraft.data.server.recipe;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ComplexRecipeJsonBuilder extends RecipeJsonBuilder {
   final RecipeSerializer serializer;

   public ComplexRecipeJsonBuilder(RecipeSerializer serializer) {
      this.serializer = serializer;
   }

   public static ComplexRecipeJsonBuilder create(RecipeSerializer serializer) {
      return new ComplexRecipeJsonBuilder(serializer);
   }

   public void offerTo(Consumer exporter, final String recipeId) {
      exporter.accept(new RecipeJsonBuilder.CraftingRecipeJsonProvider(CraftingRecipeCategory.MISC) {
         public RecipeSerializer getSerializer() {
            return ComplexRecipeJsonBuilder.this.serializer;
         }

         public Identifier getRecipeId() {
            return new Identifier(recipeId);
         }

         @Nullable
         public JsonObject toAdvancementJson() {
            return null;
         }

         public Identifier getAdvancementId() {
            return new Identifier("");
         }
      });
   }
}
