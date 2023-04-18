package net.minecraft.data.server.recipe;

import java.util.function.Consumer;
import net.minecraft.data.DataOutput;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

public class BundleRecipeProvider extends RecipeProvider {
   public BundleRecipeProvider(DataOutput arg) {
      super(arg);
   }

   protected void generate(Consumer exporter) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.BUNDLE).input('#', (ItemConvertible)Items.RABBIT_HIDE).input('-', (ItemConvertible)Items.STRING).pattern("-#-").pattern("# #").pattern("###").criterion("has_string", conditionsFromItem(Items.STRING)).offerTo(exporter);
   }
}
