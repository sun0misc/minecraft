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
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public class ShapelessRecipeJsonBuilder
implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final DefaultedList<Ingredient> inputs = DefaultedList.of();
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap();
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

    public ShapelessRecipeJsonBuilder input(TagKey<Item> tag) {
        return this.input(Ingredient.fromTag(tag));
    }

    public ShapelessRecipeJsonBuilder input(ItemConvertible itemProvider) {
        return this.input(itemProvider, 1);
    }

    public ShapelessRecipeJsonBuilder input(ItemConvertible itemProvider, int size) {
        for (int j = 0; j < size; ++j) {
            this.input(Ingredient.ofItems(itemProvider));
        }
        return this;
    }

    public ShapelessRecipeJsonBuilder input(Ingredient ingredient) {
        return this.input(ingredient, 1);
    }

    public ShapelessRecipeJsonBuilder input(Ingredient ingredient, int size) {
        for (int j = 0; j < size; ++j) {
            this.inputs.add(ingredient);
        }
        return this;
    }

    @Override
    public ShapelessRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> arg) {
        this.advancementBuilder.put(string, arg);
        return this;
    }

    @Override
    public ShapelessRecipeJsonBuilder group(@Nullable String string) {
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
        this.advancementBuilder.forEach(lv::criterion);
        ShapelessRecipe lv2 = new ShapelessRecipe(Objects.requireNonNullElse(this.group, ""), CraftingRecipeJsonBuilder.toCraftingCategory(this.category), new ItemStack(this.output, this.count), this.inputs);
        exporter.accept(recipeId, lv2, lv.build(recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(Identifier recipeId) {
        if (this.advancementBuilder.isEmpty()) {
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

