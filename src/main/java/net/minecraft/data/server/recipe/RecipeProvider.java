package net.minecraft.data.server.recipe;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class RecipeProvider implements DataProvider {
   private final DataOutput.PathResolver recipesPathResolver;
   private final DataOutput.PathResolver advancementsPathResolver;
   private static final Map VARIANT_FACTORIES;

   public RecipeProvider(DataOutput output) {
      this.recipesPathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "recipes");
      this.advancementsPathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "advancements");
   }

   public CompletableFuture run(DataWriter writer) {
      Set set = Sets.newHashSet();
      List list = new ArrayList();
      this.generate((jsonProvider) -> {
         if (!set.add(jsonProvider.getRecipeId())) {
            throw new IllegalStateException("Duplicate recipe " + jsonProvider.getRecipeId());
         } else {
            list.add(DataProvider.writeToPath(writer, jsonProvider.toJson(), this.recipesPathResolver.resolveJson(jsonProvider.getRecipeId())));
            JsonObject jsonObject = jsonProvider.toAdvancementJson();
            if (jsonObject != null) {
               list.add(DataProvider.writeToPath(writer, jsonObject, this.advancementsPathResolver.resolveJson(jsonProvider.getAdvancementId())));
            }

         }
      });
      return CompletableFuture.allOf((CompletableFuture[])list.toArray((i) -> {
         return new CompletableFuture[i];
      }));
   }

   protected CompletableFuture saveRecipeAdvancement(DataWriter cache, Identifier advancementId, Advancement.Builder advancementBuilder) {
      return DataProvider.writeToPath(cache, advancementBuilder.toJson(), this.advancementsPathResolver.resolveJson(advancementId));
   }

   protected abstract void generate(Consumer exporter);

   protected static void generateFamilies(Consumer exporter, FeatureSet enabledFeatures) {
      BlockFamilies.getFamilies().filter((family) -> {
         return family.shouldGenerateRecipes(enabledFeatures);
      }).forEach((family) -> {
         generateFamily(exporter, family);
      });
   }

   protected static void offerSingleOutputShapelessRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input, @Nullable String group) {
      offerShapelessRecipe(exporter, output, input, group, 1);
   }

   protected static void offerShapelessRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input, @Nullable String group, int outputCount) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, output, outputCount).input(input).group(group).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter, convertBetween(output, input));
   }

   protected static void offerSmelting(Consumer exporter, List inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group) {
      offerMultipleOptions(exporter, RecipeSerializer.SMELTING, inputs, category, output, experience, cookingTime, group, "_from_smelting");
   }

   protected static void offerBlasting(Consumer exporter, List inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group) {
      offerMultipleOptions(exporter, RecipeSerializer.BLASTING, inputs, category, output, experience, cookingTime, group, "_from_blasting");
   }

   private static void offerMultipleOptions(Consumer exporter, RecipeSerializer serializer, List inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group, String method) {
      Iterator var9 = inputs.iterator();

      while(var9.hasNext()) {
         ItemConvertible lv = (ItemConvertible)var9.next();
         CookingRecipeJsonBuilder.create(Ingredient.ofItems(lv), category, output, experience, cookingTime, serializer).group(group).criterion(hasItem(lv), conditionsFromItem(lv)).offerTo(exporter, getItemPath(output) + method + "_" + getItemPath(lv));
      }

   }

   protected static void offerNetheriteUpgradeRecipe(Consumer exporter, Item input, RecipeCategory category, Item result) {
      SmithingTransformRecipeJsonBuilder.create(Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.ofItems(input), Ingredient.ofItems(Items.NETHERITE_INGOT), category, result).criterion("has_netherite_ingot", conditionsFromItem(Items.NETHERITE_INGOT)).offerTo(exporter, getItemPath(result) + "_smithing");
   }

   protected static void offerSmithingTrimRecipe(Consumer exporter, Item template) {
      SmithingTrimRecipeJsonBuilder.create(Ingredient.ofItems(template), Ingredient.fromTag(ItemTags.TRIMMABLE_ARMOR), Ingredient.fromTag(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).criterion("has_smithing_trim_template", conditionsFromItem(template)).offerTo(exporter, getItemPath(template) + "_smithing_trim");
   }

   protected static void offer2x2CompactingRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(category, output, 1).input('#', input).pattern("##").pattern("##").criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerCompactingRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, String criterionName) {
      ShapelessRecipeJsonBuilder.create(category, output).input((ItemConvertible)input, 9).criterion(criterionName, conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerCompactingRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      offerCompactingRecipe(exporter, category, output, input, hasItem(input));
   }

   protected static void offerPlanksRecipe2(Consumer exporter, ItemConvertible output, TagKey input, int count) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, count).input(input).group("planks").criterion("has_log", conditionsFromTag(input)).offerTo(exporter);
   }

   protected static void offerPlanksRecipe(Consumer exporter, ItemConvertible output, TagKey input, int count) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, count).input(input).group("planks").criterion("has_logs", conditionsFromTag(input)).offerTo(exporter);
   }

   protected static void offerBarkBlockRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 3).input('#', input).pattern("##").pattern("##").group("bark").criterion("has_log", conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerBoatRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output).input('#', input).pattern("# #").pattern("###").group("boat").criterion("in_water", requireEnteringFluid(Blocks.WATER)).offerTo(exporter);
   }

   protected static void offerChestBoatRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output).input((ItemConvertible)Blocks.CHEST).input(input).group("chest_boat").criterion("has_boat", conditionsFromTag(ItemTags.BOATS)).offerTo(exporter);
   }

   private static CraftingRecipeJsonBuilder createTransmutationRecipe(ItemConvertible output, Ingredient input) {
      return ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output).input(input);
   }

   protected static CraftingRecipeJsonBuilder createDoorRecipe(ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 3).input('#', input).pattern("##").pattern("##").pattern("##");
   }

   private static CraftingRecipeJsonBuilder createFenceRecipe(ItemConvertible output, Ingredient input) {
      int i = output == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
      Item lv = output == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
      return ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, i).input('W', input).input('#', (ItemConvertible)lv).pattern("W#W").pattern("W#W");
   }

   private static CraftingRecipeJsonBuilder createFenceGateRecipe(ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output).input('#', (ItemConvertible)Items.STICK).input('W', input).pattern("#W#").pattern("#W#");
   }

   protected static void offerPressurePlateRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      createPressurePlateRecipe(RecipeCategory.REDSTONE, output, Ingredient.ofItems(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   private static CraftingRecipeJsonBuilder createPressurePlateRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(category, output).input('#', input).pattern("##");
   }

   protected static void offerSlabRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      createSlabRecipe(category, output, Ingredient.ofItems(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static CraftingRecipeJsonBuilder createSlabRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(category, output, 6).input('#', input).pattern("###");
   }

   protected static CraftingRecipeJsonBuilder createStairsRecipe(ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 4).input('#', input).pattern("#  ").pattern("## ").pattern("###");
   }

   private static CraftingRecipeJsonBuilder createTrapdoorRecipe(ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 2).input('#', input).pattern("###").pattern("###");
   }

   private static CraftingRecipeJsonBuilder createSignRecipe(ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 3).group("sign").input('#', input).input('X', (ItemConvertible)Items.STICK).pattern("###").pattern("###").pattern(" X ");
   }

   protected static void offerHangingSignRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 6).group("hanging_sign").input('#', input).input('X', (ItemConvertible)Items.CHAIN).pattern("X X").pattern("###").pattern("###").criterion("has_stripped_logs", conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerWoolDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output).input(input).input((ItemConvertible)Blocks.WHITE_WOOL).group("wool").criterion("has_white_wool", conditionsFromItem(Blocks.WHITE_WOOL)).offerTo(exporter);
   }

   protected static void offerCarpetRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 3).input('#', input).pattern("##").group("carpet").criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerCarpetDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 8).input('#', (ItemConvertible)Blocks.WHITE_CARPET).input('$', input).pattern("###").pattern("#$#").pattern("###").group("carpet").criterion("has_white_carpet", conditionsFromItem(Blocks.WHITE_CARPET)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter, convertBetween(output, Blocks.WHITE_CARPET));
   }

   protected static void offerBedRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input('#', input).input('X', ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerBedDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input((ItemConvertible)Items.WHITE_BED).input(input).group("dyed_bed").criterion("has_bed", conditionsFromItem(Items.WHITE_BED)).offerTo(exporter, convertBetween(output, Items.WHITE_BED));
   }

   protected static void offerBannerRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input('#', input).input('|', (ItemConvertible)Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerStainedGlassDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input('#', (ItemConvertible)Blocks.GLASS).input('X', input).pattern("###").pattern("#X#").pattern("###").group("stained_glass").criterion("has_glass", conditionsFromItem(Blocks.GLASS)).offerTo(exporter);
   }

   protected static void offerStainedGlassPaneRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 16).input('#', input).pattern("###").pattern("###").group("stained_glass_pane").criterion("has_glass", conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerStainedGlassPaneDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 8).input('#', (ItemConvertible)Blocks.GLASS_PANE).input('$', input).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").criterion("has_glass_pane", conditionsFromItem(Blocks.GLASS_PANE)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter, convertBetween(output, Blocks.GLASS_PANE));
   }

   protected static void offerTerracottaDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input('#', (ItemConvertible)Blocks.TERRACOTTA).input('X', input).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").criterion("has_terracotta", conditionsFromItem(Blocks.TERRACOTTA)).offerTo(exporter);
   }

   protected static void offerConcretePowderDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(input).input((ItemConvertible)Blocks.SAND, 4).input((ItemConvertible)Blocks.GRAVEL, 4).group("concrete_powder").criterion("has_sand", conditionsFromItem(Blocks.SAND)).criterion("has_gravel", conditionsFromItem(Blocks.GRAVEL)).offerTo(exporter);
   }

   protected static void offerCandleDyeingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      ShapelessRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input((ItemConvertible)Blocks.CANDLE).input(input).group("dyed_candle").criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerWallRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      getWallRecipe(category, output, Ingredient.ofItems(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   private static CraftingRecipeJsonBuilder getWallRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(category, output, 6).input('#', input).pattern("###").pattern("###");
   }

   protected static void offerPolishedStoneRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      createCondensingRecipe(category, output, Ingredient.ofItems(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   private static CraftingRecipeJsonBuilder createCondensingRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(category, output, 4).input('S', input).pattern("SS").pattern("SS");
   }

   protected static void offerCutCopperRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      createCutCopperRecipe(category, output, Ingredient.ofItems(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   private static ShapedRecipeJsonBuilder createCutCopperRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(category, output, 4).input('#', input).pattern("##").pattern("##");
   }

   protected static void offerChiseledBlockRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      createChiseledBlockRecipe(category, output, Ingredient.ofItems(input)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerMosaicRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      ShapedRecipeJsonBuilder.create(category, output).input('#', input).pattern("#").pattern("#").criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static ShapedRecipeJsonBuilder createChiseledBlockRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
      return ShapedRecipeJsonBuilder.create(category, output).input('#', input).pattern("#").pattern("#");
   }

   protected static void offerStonecuttingRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
      offerStonecuttingRecipe(exporter, category, output, input, 1);
   }

   protected static void offerStonecuttingRecipe(Consumer exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, int count) {
      SingleItemRecipeJsonBuilder var10000 = SingleItemRecipeJsonBuilder.createStonecutting(Ingredient.ofItems(input), category, output, count).criterion(hasItem(input), conditionsFromItem(input));
      String var10002 = convertBetween(output, input);
      var10000.offerTo(exporter, var10002 + "_stonecutting");
   }

   private static void offerCrackingRecipe(Consumer exporter, ItemConvertible output, ItemConvertible input) {
      CookingRecipeJsonBuilder.createSmelting(Ingredient.ofItems(input), RecipeCategory.BUILDING_BLOCKS, output, 0.1F, 200).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter);
   }

   protected static void offerReversibleCompactingRecipes(Consumer exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem) {
      offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, getRecipeName(compactItem), (String)null, getRecipeName(baseItem), (String)null);
   }

   protected static void offerReversibleCompactingRecipesWithCompactingRecipeGroup(Consumer exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String compactingId, String compactingGroup) {
      offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, compactingId, compactingGroup, getRecipeName(baseItem), (String)null);
   }

   protected static void offerReversibleCompactingRecipesWithReverseRecipeGroup(Consumer exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String reverseId, String reverseGroup) {
      offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, getRecipeName(compactItem), (String)null, reverseId, reverseGroup);
   }

   private static void offerReversibleCompactingRecipes(Consumer exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String compactingId, @Nullable String compactingGroup, String reverseId, @Nullable String reverseGroup) {
      ShapelessRecipeJsonBuilder.create(reverseCategory, baseItem, 9).input(compactItem).group(reverseGroup).criterion(hasItem(compactItem), conditionsFromItem(compactItem)).offerTo(exporter, new Identifier(reverseId));
      ShapedRecipeJsonBuilder.create(compactingCategory, compactItem).input('#', baseItem).pattern("###").pattern("###").pattern("###").group(compactingGroup).criterion(hasItem(baseItem), conditionsFromItem(baseItem)).offerTo(exporter, new Identifier(compactingId));
   }

   protected static void offerSmithingTemplateCopyingRecipe(Consumer exporter, ItemConvertible template, TagKey resource) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input('#', (ItemConvertible)Items.DIAMOND).input('C', resource).input('S', template).pattern("#S#").pattern("#C#").pattern("###").criterion(hasItem(template), conditionsFromItem(template)).offerTo(exporter);
   }

   protected static void offerSmithingTemplateCopyingRecipe(Consumer exporter, ItemConvertible template, ItemConvertible resource) {
      ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input('#', (ItemConvertible)Items.DIAMOND).input('C', resource).input('S', template).pattern("#S#").pattern("#C#").pattern("###").criterion(hasItem(template), conditionsFromItem(template)).offerTo(exporter);
   }

   protected static void generateCookingRecipes(Consumer exporter, String cooker, RecipeSerializer serializer, int cookingTime) {
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.BEEF, Items.COOKED_BEEF, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.COD, Items.COOKED_COD, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.KELP, Items.DRIED_KELP, 0.1F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.SALMON, Items.COOKED_SALMON, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.POTATO, Items.BAKED_POTATO, 0.35F);
      offerFoodCookingRecipe(exporter, cooker, serializer, cookingTime, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
   }

   private static void offerFoodCookingRecipe(Consumer exporter, String cooker, RecipeSerializer serializer, int cookingTime, ItemConvertible input, ItemConvertible output, float experience) {
      CookingRecipeJsonBuilder var10000 = CookingRecipeJsonBuilder.create(Ingredient.ofItems(input), RecipeCategory.FOOD, output, experience, cookingTime, serializer).criterion(hasItem(input), conditionsFromItem(input));
      String var10002 = getItemPath(output);
      var10000.offerTo(exporter, var10002 + "_from_" + cooker);
   }

   protected static void offerWaxingRecipes(Consumer exporter) {
      ((BiMap)HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get()).forEach((input, output) -> {
         ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output).input((ItemConvertible)input).input((ItemConvertible)Items.HONEYCOMB).group(getItemPath(output)).criterion(hasItem(input), conditionsFromItem(input)).offerTo(exporter, convertBetween(output, Items.HONEYCOMB));
      });
   }

   protected static void generateFamily(Consumer exporter, BlockFamily family) {
      family.getVariants().forEach((variant, block) -> {
         BiFunction biFunction = (BiFunction)VARIANT_FACTORIES.get(variant);
         ItemConvertible lv = getVariantRecipeInput(family, variant);
         if (biFunction != null) {
            CraftingRecipeJsonBuilder lv2 = (CraftingRecipeJsonBuilder)biFunction.apply(block, lv);
            family.getGroup().ifPresent((group) -> {
               lv2.group(group + (variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getName()));
            });
            lv2.criterion((String)family.getUnlockCriterionName().orElseGet(() -> {
               return hasItem(lv);
            }), conditionsFromItem(lv));
            lv2.offerTo(exporter);
         }

         if (variant == BlockFamily.Variant.CRACKED) {
            offerCrackingRecipe(exporter, block, lv);
         }

      });
   }

   private static Block getVariantRecipeInput(BlockFamily family, BlockFamily.Variant variant) {
      if (variant == BlockFamily.Variant.CHISELED) {
         if (!family.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
            throw new IllegalStateException("Slab is not defined for the family.");
         } else {
            return family.getVariant(BlockFamily.Variant.SLAB);
         }
      } else {
         return family.getBaseBlock();
      }
   }

   private static EnterBlockCriterion.Conditions requireEnteringFluid(Block block) {
      return new EnterBlockCriterion.Conditions(EntityPredicate.Extended.EMPTY, block, StatePredicate.ANY);
   }

   private static InventoryChangedCriterion.Conditions conditionsFromItem(NumberRange.IntRange count, ItemConvertible item) {
      return conditionsFromItemPredicates(ItemPredicate.Builder.create().items(item).count(count).build());
   }

   protected static InventoryChangedCriterion.Conditions conditionsFromItem(ItemConvertible item) {
      return conditionsFromItemPredicates(ItemPredicate.Builder.create().items(item).build());
   }

   protected static InventoryChangedCriterion.Conditions conditionsFromTag(TagKey tag) {
      return conditionsFromItemPredicates(ItemPredicate.Builder.create().tag(tag).build());
   }

   private static InventoryChangedCriterion.Conditions conditionsFromItemPredicates(ItemPredicate... predicates) {
      return new InventoryChangedCriterion.Conditions(EntityPredicate.Extended.EMPTY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, predicates);
   }

   protected static String hasItem(ItemConvertible item) {
      return "has_" + getItemPath(item);
   }

   protected static String getItemPath(ItemConvertible item) {
      return Registries.ITEM.getId(item.asItem()).getPath();
   }

   protected static String getRecipeName(ItemConvertible item) {
      return getItemPath(item);
   }

   protected static String convertBetween(ItemConvertible to, ItemConvertible from) {
      String var10000 = getItemPath(to);
      return var10000 + "_from_" + getItemPath(from);
   }

   protected static String getSmeltingItemPath(ItemConvertible item) {
      return getItemPath(item) + "_from_smelting";
   }

   protected static String getBlastingItemPath(ItemConvertible item) {
      return getItemPath(item) + "_from_blasting";
   }

   public final String getName() {
      return "Recipes";
   }

   static {
      VARIANT_FACTORIES = ImmutableMap.builder().put(BlockFamily.Variant.BUTTON, (output, input) -> {
         return createTransmutationRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.CHISELED, (output, input) -> {
         return createChiseledBlockRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.CUT, (arg, arg2) -> {
         return createCutCopperRecipe(RecipeCategory.BUILDING_BLOCKS, arg, Ingredient.ofItems(arg2));
      }).put(BlockFamily.Variant.DOOR, (output, input) -> {
         return createDoorRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.CUSTOM_FENCE, (output, input) -> {
         return createFenceRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.FENCE, (output, input) -> {
         return createFenceRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (arg, arg2) -> {
         return createFenceGateRecipe(arg, Ingredient.ofItems(arg2));
      }).put(BlockFamily.Variant.FENCE_GATE, (output, input) -> {
         return createFenceGateRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.SIGN, (output, input) -> {
         return createSignRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.SLAB, (output, input) -> {
         return createSlabRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.STAIRS, (output, input) -> {
         return createStairsRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.PRESSURE_PLATE, (output, input) -> {
         return createPressurePlateRecipe(RecipeCategory.REDSTONE, output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.POLISHED, (output, input) -> {
         return createCondensingRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.TRAPDOOR, (output, input) -> {
         return createTrapdoorRecipe(output, Ingredient.ofItems(input));
      }).put(BlockFamily.Variant.WALL, (output, input) -> {
         return getWallRecipe(RecipeCategory.DECORATIONS, output, Ingredient.ofItems(input));
      }).build();
   }
}
