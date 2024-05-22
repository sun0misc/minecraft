/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.data.server.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class StonecuttingRecipeJsonBuilder
implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item output;
    private final Ingredient input;
    private final int count;
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap();
    @Nullable
    private String group;
    private final CuttingRecipe.RecipeFactory<?> recipeFactory;

    public StonecuttingRecipeJsonBuilder(RecipeCategory category, CuttingRecipe.RecipeFactory<?> recipeFactory, Ingredient input, ItemConvertible output, int count) {
        this.category = category;
        this.recipeFactory = recipeFactory;
        this.output = output.asItem();
        this.input = input;
        this.count = count;
    }

    public static StonecuttingRecipeJsonBuilder createStonecutting(Ingredient input, RecipeCategory category, ItemConvertible output) {
        return new StonecuttingRecipeJsonBuilder(category, StonecuttingRecipe::new, input, output, 1);
    }

    public static StonecuttingRecipeJsonBuilder createStonecutting(Ingredient input, RecipeCategory category, ItemConvertible output, int count) {
        return new StonecuttingRecipeJsonBuilder(category, StonecuttingRecipe::new, input, output, count);
    }

    @Override
    public StonecuttingRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> arg) {
        this.criteria.put(string, arg);
        return this;
    }

    @Override
    public StonecuttingRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        this.validate(recipeId);
        Advancement.Builder lv = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.criteria.forEach(lv::criterion);
        Object lv2 = this.recipeFactory.create(Objects.requireNonNullElse(this.group, ""), this.input, new ItemStack(this.output, this.count));
        exporter.accept(recipeId, (Recipe<?>)lv2, lv.build(recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(Identifier recipeId) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + String.valueOf(recipeId));
        }
    }

    @Override
    public /* synthetic */ CraftingRecipeJsonBuilder group(@Nullable String group) {
        return this.group(group);
    }

    public /* synthetic */ CraftingRecipeJsonBuilder criterion(String name, AdvancementCriterion criterion) {
        return this.criterion(name, criterion);
    }
}

