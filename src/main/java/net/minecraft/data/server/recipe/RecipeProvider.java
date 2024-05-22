/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.data.server.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTrimRecipeJsonBuilder;
import net.minecraft.data.server.recipe.StonecuttingRecipeJsonBuilder;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class RecipeProvider
implements DataProvider {
    final DataOutput.PathResolver recipesPathResolver;
    final DataOutput.PathResolver advancementsPathResolver;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;
    private static final Map<BlockFamily.Variant, BiFunction<ItemConvertible, ItemConvertible, CraftingRecipeJsonBuilder>> VARIANT_FACTORIES = ImmutableMap.builder().put(BlockFamily.Variant.BUTTON, (output, input) -> RecipeProvider.createTransmutationRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CHISELED, (output, input) -> RecipeProvider.createChiseledBlockRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CUT, (output, input) -> RecipeProvider.createCutCopperRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.DOOR, (output, input) -> RecipeProvider.createDoorRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CUSTOM_FENCE, (output, input) -> RecipeProvider.createFenceRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.FENCE, (output, input) -> RecipeProvider.createFenceRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (output, input) -> RecipeProvider.createFenceGateRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.FENCE_GATE, (output, input) -> RecipeProvider.createFenceGateRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.SIGN, (output, input) -> RecipeProvider.createSignRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.SLAB, (output, input) -> RecipeProvider.createSlabRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.STAIRS, (output, input) -> RecipeProvider.createStairsRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.PRESSURE_PLATE, (output, input) -> RecipeProvider.createPressurePlateRecipe(RecipeCategory.REDSTONE, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.POLISHED, (output, input) -> RecipeProvider.createCondensingRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.TRAPDOOR, (output, input) -> RecipeProvider.createTrapdoorRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.WALL, (output, input) -> RecipeProvider.getWallRecipe(RecipeCategory.DECORATIONS, output, Ingredient.ofItems(input))).build();

    public RecipeProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.recipesPathResolver = output.method_60917(RegistryKeys.RECIPE);
        this.advancementsPathResolver = output.method_60917(RegistryKeys.ADVANCEMENT);
        this.registryLookupFuture = registryLookupFuture;
    }

    @Override
    public final CompletableFuture<?> run(DataWriter writer) {
        return this.registryLookupFuture.thenCompose(registryLookup -> this.run(writer, (RegistryWrapper.WrapperLookup)registryLookup));
    }

    protected CompletableFuture<?> run(final DataWriter writer, final RegistryWrapper.WrapperLookup registryLookup) {
        final HashSet set = Sets.newHashSet();
        final ArrayList list = new ArrayList();
        this.generate(new RecipeExporter(){

            @Override
            public void accept(Identifier recipeId, Recipe<?> recipe, @Nullable AdvancementEntry advancement) {
                if (!set.add(recipeId)) {
                    throw new IllegalStateException("Duplicate recipe " + String.valueOf(recipeId));
                }
                list.add(DataProvider.writeCodecToPath(writer, registryLookup, Recipe.CODEC, recipe, RecipeProvider.this.recipesPathResolver.resolveJson(recipeId)));
                if (advancement != null) {
                    list.add(DataProvider.writeCodecToPath(writer, registryLookup, Advancement.CODEC, advancement.value(), RecipeProvider.this.advancementsPathResolver.resolveJson(advancement.id())));
                }
            }

            @Override
            public Advancement.Builder getAdvancementBuilder() {
                return Advancement.Builder.createUntelemetered().parent(CraftingRecipeJsonBuilder.ROOT);
            }
        });
        return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
    }

    protected CompletableFuture<?> saveRecipeAdvancement(DataWriter cache, RegistryWrapper.WrapperLookup registryLookup, AdvancementEntry advancement) {
        return DataProvider.writeCodecToPath(cache, registryLookup, Advancement.CODEC, advancement.value(), this.advancementsPathResolver.resolveJson(advancement.id()));
    }

    protected abstract void generate(RecipeExporter var1);

    protected static void generateFamilies(RecipeExporter exporter, FeatureSet enabledFeatures) {
        BlockFamilies.getFamilies().filter(BlockFamily::shouldGenerateRecipes).forEach(family -> RecipeProvider.generateFamily(exporter, family, enabledFeatures));
    }

    protected static void offerSingleOutputShapelessRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input, @Nullable String group) {
        RecipeProvider.offerShapelessRecipe(exporter, output, input, group, 1);
    }

    protected static void offerShapelessRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input, @Nullable String group, int outputCount) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, output, outputCount).input(input).group(group).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter, RecipeProvider.convertBetween(output, input));
    }

    protected static void offerSmelting(RecipeExporter exporter, List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group) {
        RecipeProvider.offerMultipleOptions(exporter, RecipeSerializer.SMELTING, SmeltingRecipe::new, inputs, category, output, experience, cookingTime, group, "_from_smelting");
    }

    protected static void offerBlasting(RecipeExporter exporter, List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group) {
        RecipeProvider.offerMultipleOptions(exporter, RecipeSerializer.BLASTING, BlastingRecipe::new, inputs, category, output, experience, cookingTime, group, "_from_blasting");
    }

    private static <T extends AbstractCookingRecipe> void offerMultipleOptions(RecipeExporter exporter, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group, String suffix) {
        for (ItemConvertible lv : inputs) {
            CookingRecipeJsonBuilder.create(Ingredient.ofItems(lv), category, output, experience, cookingTime, serializer, recipeFactory).group(group).criterion(RecipeProvider.hasItem(lv), (AdvancementCriterion)RecipeProvider.conditionsFromItem(lv)).offerTo(exporter, RecipeProvider.getItemPath(output) + suffix + "_" + RecipeProvider.getItemPath(lv));
        }
    }

    protected static void offerNetheriteUpgradeRecipe(RecipeExporter exporter, Item input, RecipeCategory category, Item result) {
        SmithingTransformRecipeJsonBuilder.create(Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.ofItems(input), Ingredient.ofItems(Items.NETHERITE_INGOT), category, result).criterion("has_netherite_ingot", RecipeProvider.conditionsFromItem(Items.NETHERITE_INGOT)).offerTo(exporter, RecipeProvider.getItemPath(result) + "_smithing");
    }

    protected static void offerSmithingTrimRecipe(RecipeExporter exporter, Item template, Identifier recipeId) {
        SmithingTrimRecipeJsonBuilder.create(Ingredient.ofItems(template), Ingredient.fromTag(ItemTags.TRIMMABLE_ARMOR), Ingredient.fromTag(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).criterion("has_smithing_trim_template", RecipeProvider.conditionsFromItem(template)).offerTo(exporter, recipeId);
    }

    protected static void offer2x2CompactingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(category, output, 1).input(Character.valueOf('#'), input).pattern("##").pattern("##").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerCompactingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, String criterionName) {
        ShapelessRecipeJsonBuilder.create(category, output).input(input, 9).criterion(criterionName, (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerCompactingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.offerCompactingRecipe(exporter, category, output, input, RecipeProvider.hasItem(input));
    }

    protected static void offerPlanksRecipe2(RecipeExporter exporter, ItemConvertible output, TagKey<Item> input, int count) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, count).input(input).group("planks").criterion("has_log", (AdvancementCriterion)RecipeProvider.conditionsFromTag(input)).offerTo(exporter);
    }

    protected static void offerPlanksRecipe(RecipeExporter exporter, ItemConvertible output, TagKey<Item> input, int count) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, count).input(input).group("planks").criterion("has_logs", (AdvancementCriterion)RecipeProvider.conditionsFromTag(input)).offerTo(exporter);
    }

    protected static void offerBarkBlockRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 3).input(Character.valueOf('#'), input).pattern("##").pattern("##").group("bark").criterion("has_log", (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerBoatRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output).input(Character.valueOf('#'), input).pattern("# #").pattern("###").group("boat").criterion("in_water", (AdvancementCriterion)RecipeProvider.requireEnteringFluid(Blocks.WATER)).offerTo(exporter);
    }

    protected static void offerChestBoatRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output).input(Blocks.CHEST).input(input).group("chest_boat").criterion("has_boat", (AdvancementCriterion)RecipeProvider.conditionsFromTag(ItemTags.BOATS)).offerTo(exporter);
    }

    private static CraftingRecipeJsonBuilder createTransmutationRecipe(ItemConvertible output, Ingredient input) {
        return ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output).input(input);
    }

    protected static CraftingRecipeJsonBuilder createDoorRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 3).input(Character.valueOf('#'), input).pattern("##").pattern("##").pattern("##");
    }

    private static CraftingRecipeJsonBuilder createFenceRecipe(ItemConvertible output, Ingredient input) {
        int i = output == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item lv = output == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, i).input(Character.valueOf('W'), input).input(Character.valueOf('#'), lv).pattern("W#W").pattern("W#W");
    }

    private static CraftingRecipeJsonBuilder createFenceGateRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output).input(Character.valueOf('#'), Items.STICK).input(Character.valueOf('W'), input).pattern("#W#").pattern("#W#");
    }

    protected static void offerPressurePlateRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createPressurePlateRecipe(RecipeCategory.REDSTONE, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    private static CraftingRecipeJsonBuilder createPressurePlateRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output).input(Character.valueOf('#'), input).pattern("##");
    }

    protected static void offerSlabRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createSlabRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static CraftingRecipeJsonBuilder createSlabRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 6).input(Character.valueOf('#'), input).pattern("###");
    }

    protected static CraftingRecipeJsonBuilder createStairsRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 4).input(Character.valueOf('#'), input).pattern("#  ").pattern("## ").pattern("###");
    }

    protected static CraftingRecipeJsonBuilder createTrapdoorRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 2).input(Character.valueOf('#'), input).pattern("###").pattern("###");
    }

    private static CraftingRecipeJsonBuilder createSignRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 3).group("sign").input(Character.valueOf('#'), input).input(Character.valueOf('X'), Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    protected static void offerHangingSignRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 6).group("hanging_sign").input(Character.valueOf('#'), input).input(Character.valueOf('X'), Items.CHAIN).pattern("X X").pattern("###").pattern("###").criterion("has_stripped_logs", (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerDyeableRecipes(RecipeExporter exporter, List<Item> dyes, List<Item> dyeables, String group) {
        for (int i = 0; i < dyes.size(); ++i) {
            Item lv = dyes.get(i);
            Item lv2 = dyeables.get(i);
            ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, lv2).input(lv).input(Ingredient.ofStacks(dyeables.stream().filter(dyeable -> !dyeable.equals(lv2)).map(ItemStack::new))).group(group).criterion("has_needed_dye", (AdvancementCriterion)RecipeProvider.conditionsFromItem(lv)).offerTo(exporter, "dye_" + RecipeProvider.getItemPath(lv2));
        }
    }

    protected static void offerCarpetRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 3).input(Character.valueOf('#'), input).pattern("##").group("carpet").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerBedRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input(Character.valueOf('#'), input).input(Character.valueOf('X'), ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerBannerRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input(Character.valueOf('#'), input).input(Character.valueOf('|'), Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerStainedGlassDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(Character.valueOf('#'), Blocks.GLASS).input(Character.valueOf('X'), input).pattern("###").pattern("#X#").pattern("###").group("stained_glass").criterion("has_glass", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.GLASS)).offerTo(exporter);
    }

    protected static void offerStainedGlassPaneRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 16).input(Character.valueOf('#'), input).pattern("###").pattern("###").group("stained_glass_pane").criterion("has_glass", (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerStainedGlassPaneDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ((ShapedRecipeJsonBuilder)ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 8).input(Character.valueOf('#'), Blocks.GLASS_PANE).input(Character.valueOf('$'), input).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").criterion("has_glass_pane", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.GLASS_PANE))).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter, RecipeProvider.convertBetween(output, Blocks.GLASS_PANE));
    }

    protected static void offerTerracottaDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(Character.valueOf('#'), Blocks.TERRACOTTA).input(Character.valueOf('X'), input).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").criterion("has_terracotta", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.TERRACOTTA)).offerTo(exporter);
    }

    protected static void offerConcretePowderDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ((ShapelessRecipeJsonBuilder)ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(input).input(Blocks.SAND, 4).input(Blocks.GRAVEL, 4).group("concrete_powder").criterion("has_sand", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.SAND))).criterion("has_gravel", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.GRAVEL)).offerTo(exporter);
    }

    protected static void offerCandleDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input(Blocks.CANDLE).input(input).group("dyed_candle").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerWallRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.getWallRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    private static CraftingRecipeJsonBuilder getWallRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 6).input(Character.valueOf('#'), input).pattern("###").pattern("###");
    }

    protected static void offerPolishedStoneRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createCondensingRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    private static CraftingRecipeJsonBuilder createCondensingRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 4).input(Character.valueOf('S'), input).pattern("SS").pattern("SS");
    }

    protected static void offerCutCopperRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createCutCopperRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    private static ShapedRecipeJsonBuilder createCutCopperRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 4).input(Character.valueOf('#'), input).pattern("##").pattern("##");
    }

    protected static void offerChiseledBlockRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createChiseledBlockRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerMosaicRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(category, output).input(Character.valueOf('#'), input).pattern("#").pattern("#").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static ShapedRecipeJsonBuilder createChiseledBlockRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output).input(Character.valueOf('#'), input).pattern("#").pattern("#");
    }

    protected static void offerStonecuttingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.offerStonecuttingRecipe(exporter, category, output, input, 1);
    }

    protected static void offerStonecuttingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, int count) {
        StonecuttingRecipeJsonBuilder.createStonecutting(Ingredient.ofItems(input), category, output, count).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter, RecipeProvider.convertBetween(output, input) + "_stonecutting");
    }

    private static void offerCrackingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        CookingRecipeJsonBuilder.createSmelting(Ingredient.ofItems(input), RecipeCategory.BUILDING_BLOCKS, output, 0.1f, 200).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerReversibleCompactingRecipes(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem) {
        RecipeProvider.offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, RecipeProvider.getRecipeName(compactItem), null, RecipeProvider.getRecipeName(baseItem), null);
    }

    protected static void offerReversibleCompactingRecipesWithCompactingRecipeGroup(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String compactingId, String compactingGroup) {
        RecipeProvider.offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, compactingId, compactingGroup, RecipeProvider.getRecipeName(baseItem), null);
    }

    protected static void offerReversibleCompactingRecipesWithReverseRecipeGroup(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String reverseId, String reverseGroup) {
        RecipeProvider.offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, RecipeProvider.getRecipeName(compactItem), null, reverseId, reverseGroup);
    }

    private static void offerReversibleCompactingRecipes(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String compactingId, @Nullable String compactingGroup, String reverseId, @Nullable String reverseGroup) {
        ((ShapelessRecipeJsonBuilder)ShapelessRecipeJsonBuilder.create(reverseCategory, baseItem, 9).input(compactItem).group(reverseGroup).criterion(RecipeProvider.hasItem(compactItem), (AdvancementCriterion)RecipeProvider.conditionsFromItem(compactItem))).offerTo(exporter, Identifier.method_60654(reverseId));
        ((ShapedRecipeJsonBuilder)ShapedRecipeJsonBuilder.create(compactingCategory, compactItem).input(Character.valueOf('#'), baseItem).pattern("###").pattern("###").pattern("###").group(compactingGroup).criterion(RecipeProvider.hasItem(baseItem), (AdvancementCriterion)RecipeProvider.conditionsFromItem(baseItem))).offerTo(exporter, Identifier.method_60654(compactingId));
    }

    protected static void offerSmithingTemplateCopyingRecipe(RecipeExporter exporter, ItemConvertible template, TagKey<Item> resource) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input(Character.valueOf('#'), Items.DIAMOND).input(Character.valueOf('C'), resource).input(Character.valueOf('S'), template).pattern("#S#").pattern("#C#").pattern("###").criterion(RecipeProvider.hasItem(template), (AdvancementCriterion)RecipeProvider.conditionsFromItem(template)).offerTo(exporter);
    }

    protected static void offerSmithingTemplateCopyingRecipe(RecipeExporter exporter, ItemConvertible template, ItemConvertible resource) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input(Character.valueOf('#'), Items.DIAMOND).input(Character.valueOf('C'), resource).input(Character.valueOf('S'), template).pattern("#S#").pattern("#C#").pattern("###").criterion(RecipeProvider.hasItem(template), (AdvancementCriterion)RecipeProvider.conditionsFromItem(template)).offerTo(exporter);
    }

    protected static void method_60922(RecipeExporter arg, ItemConvertible arg2, Ingredient arg3) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, arg2, 2).input(Character.valueOf('#'), Items.DIAMOND).input(Character.valueOf('C'), arg3).input(Character.valueOf('S'), arg2).pattern("#S#").pattern("#C#").pattern("###").criterion(RecipeProvider.hasItem(arg2), (AdvancementCriterion)RecipeProvider.conditionsFromItem(arg2)).offerTo(arg);
    }

    protected static <T extends AbstractCookingRecipe> void generateCookingRecipes(RecipeExporter exporter, String cooker, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, int cookingTime) {
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.BEEF, Items.COOKED_BEEF, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.COD, Items.COOKED_COD, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.KELP, Items.DRIED_KELP, 0.1f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.SALMON, Items.COOKED_SALMON, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.MUTTON, Items.COOKED_MUTTON, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.POTATO, Items.BAKED_POTATO, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.RABBIT, Items.COOKED_RABBIT, 0.35f);
    }

    private static <T extends AbstractCookingRecipe> void offerFoodCookingRecipe(RecipeExporter exporter, String cooker, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, int cookingTime, ItemConvertible items, ItemConvertible output, float experience) {
        CookingRecipeJsonBuilder.create(Ingredient.ofItems(items), RecipeCategory.FOOD, output, experience, cookingTime, serializer, recipeFactory).criterion(RecipeProvider.hasItem(items), (AdvancementCriterion)RecipeProvider.conditionsFromItem(items)).offerTo(exporter, RecipeProvider.getItemPath(output) + "_from_" + cooker);
    }

    protected static void offerWaxingRecipes(RecipeExporter exporter, FeatureSet enabledFeatures) {
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forEach((unwaxed, waxed) -> {
            if (!waxed.getRequiredFeatures().isSubsetOf(enabledFeatures)) {
                return;
            }
            ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, waxed).input((ItemConvertible)unwaxed).input(Items.HONEYCOMB).group(RecipeProvider.getItemPath(waxed)).criterion(RecipeProvider.hasItem(unwaxed), (AdvancementCriterion)RecipeProvider.conditionsFromItem(unwaxed)).offerTo(exporter, RecipeProvider.convertBetween(waxed, Items.HONEYCOMB));
        });
    }

    protected static void offerGrateRecipe(RecipeExporter exporter, Block output, Block input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 4).input(Character.valueOf('M'), input).pattern(" M ").pattern("M M").pattern(" M ").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void offerBulbRecipe(RecipeExporter exporter, Block output, Block input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 4).input(Character.valueOf('C'), input).input(Character.valueOf('R'), Items.REDSTONE).input(Character.valueOf('B'), Items.BLAZE_ROD).pattern(" C ").pattern("CBC").pattern(" R ").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    protected static void generateFamily(RecipeExporter exporter, BlockFamily family, FeatureSet enabledFeatures) {
        family.getVariants().forEach((variant, block) -> {
            if (!block.getRequiredFeatures().isSubsetOf(enabledFeatures)) {
                return;
            }
            BiFunction<ItemConvertible, ItemConvertible, CraftingRecipeJsonBuilder> biFunction = VARIANT_FACTORIES.get(variant);
            Block lv = RecipeProvider.getVariantRecipeInput(family, variant);
            if (biFunction != null) {
                CraftingRecipeJsonBuilder lv2 = biFunction.apply((ItemConvertible)block, lv);
                family.getGroup().ifPresent(group -> lv2.group(group + (String)(variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getName())));
                lv2.criterion(family.getUnlockCriterionName().orElseGet(() -> RecipeProvider.hasItem(lv)), RecipeProvider.conditionsFromItem(lv));
                lv2.offerTo(exporter);
            }
            if (variant == BlockFamily.Variant.CRACKED) {
                RecipeProvider.offerCrackingRecipe(exporter, block, lv);
            }
        });
    }

    private static Block getVariantRecipeInput(BlockFamily family, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!family.getVariants().containsKey((Object)BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            }
            return family.getVariant(BlockFamily.Variant.SLAB);
        }
        return family.getBaseBlock();
    }

    private static AdvancementCriterion<EnterBlockCriterion.Conditions> requireEnteringFluid(Block block) {
        return Criteria.ENTER_BLOCK.create(new EnterBlockCriterion.Conditions(Optional.empty(), Optional.of(block.getRegistryEntry()), Optional.empty()));
    }

    private static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(NumberRange.IntRange count, ItemConvertible item) {
        return RecipeProvider.conditionsFromPredicates(ItemPredicate.Builder.create().items(item).count(count));
    }

    protected static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(ItemConvertible item) {
        return RecipeProvider.conditionsFromPredicates(ItemPredicate.Builder.create().items(item));
    }

    protected static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromTag(TagKey<Item> tag) {
        return RecipeProvider.conditionsFromPredicates(ItemPredicate.Builder.create().tag(tag));
    }

    private static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromPredicates(ItemPredicate.Builder ... predicates) {
        return RecipeProvider.conditionsFromItemPredicates((ItemPredicate[])Arrays.stream(predicates).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
    }

    private static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItemPredicates(ItemPredicate ... predicates) {
        return Criteria.INVENTORY_CHANGED.create(new InventoryChangedCriterion.Conditions(Optional.empty(), InventoryChangedCriterion.Conditions.Slots.ANY, List.of(predicates)));
    }

    protected static String hasItem(ItemConvertible item) {
        return "has_" + RecipeProvider.getItemPath(item);
    }

    protected static String getItemPath(ItemConvertible item) {
        return Registries.ITEM.getId(item.asItem()).getPath();
    }

    protected static String getRecipeName(ItemConvertible item) {
        return RecipeProvider.getItemPath(item);
    }

    protected static String convertBetween(ItemConvertible to, ItemConvertible from) {
        return RecipeProvider.getItemPath(to) + "_from_" + RecipeProvider.getItemPath(from);
    }

    protected static String getSmeltingItemPath(ItemConvertible item) {
        return RecipeProvider.getItemPath(item) + "_from_smelting";
    }

    protected static String getBlastingItemPath(ItemConvertible item) {
        return RecipeProvider.getItemPath(item) + "_from_blasting";
    }

    @Override
    public final String getName() {
        return "Recipes";
    }
}

