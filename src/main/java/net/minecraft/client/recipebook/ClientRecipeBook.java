package net.minecraft.client.recipebook;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map resultsByGroup = ImmutableMap.of();
   private List orderedResults = ImmutableList.of();

   public void reload(Iterable recipes, DynamicRegistryManager registryManager) {
      Map map = toGroupedMap(recipes);
      Map map2 = Maps.newHashMap();
      ImmutableList.Builder builder = ImmutableList.builder();
      map.forEach((recipeBookGroup, list) -> {
         Stream var10002 = list.stream().map((recipes) -> {
            return new RecipeResultCollection(registryManager, recipes);
         });
         Objects.requireNonNull(builder);
         map2.put(recipeBookGroup, (List)var10002.peek(builder::add).collect(ImmutableList.toImmutableList()));
      });
      RecipeBookGroup.SEARCH_MAP.forEach((group, searchGroups) -> {
         map2.put(group, (List)searchGroups.stream().flatMap((searchGroup) -> {
            return ((List)map2.getOrDefault(searchGroup, ImmutableList.of())).stream();
         }).collect(ImmutableList.toImmutableList()));
      });
      this.resultsByGroup = ImmutableMap.copyOf(map2);
      this.orderedResults = builder.build();
   }

   private static Map toGroupedMap(Iterable recipes) {
      Map map = Maps.newHashMap();
      Table table = HashBasedTable.create();
      Iterator var3 = recipes.iterator();

      while(var3.hasNext()) {
         Recipe lv = (Recipe)var3.next();
         if (!lv.isIgnoredInRecipeBook() && !lv.isEmpty()) {
            RecipeBookGroup lv2 = getGroupForRecipe(lv);
            String string = lv.getGroup();
            if (string.isEmpty()) {
               ((List)map.computeIfAbsent(lv2, (group) -> {
                  return Lists.newArrayList();
               })).add(ImmutableList.of(lv));
            } else {
               List list = (List)table.get(lv2, string);
               if (list == null) {
                  list = Lists.newArrayList();
                  table.put(lv2, string, list);
                  ((List)map.computeIfAbsent(lv2, (group) -> {
                     return Lists.newArrayList();
                  })).add(list);
               }

               ((List)list).add(lv);
            }
         }
      }

      return map;
   }

   private static RecipeBookGroup getGroupForRecipe(Recipe recipe) {
      RecipeBookGroup var4;
      if (recipe instanceof CraftingRecipe) {
         CraftingRecipe lv = (CraftingRecipe)recipe;
         switch (lv.getCategory()) {
            case BUILDING:
               var4 = RecipeBookGroup.CRAFTING_BUILDING_BLOCKS;
               break;
            case EQUIPMENT:
               var4 = RecipeBookGroup.CRAFTING_EQUIPMENT;
               break;
            case REDSTONE:
               var4 = RecipeBookGroup.CRAFTING_REDSTONE;
               break;
            case MISC:
               var4 = RecipeBookGroup.CRAFTING_MISC;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var4;
      } else {
         RecipeType lv2 = recipe.getType();
         if (recipe instanceof AbstractCookingRecipe) {
            AbstractCookingRecipe lv3 = (AbstractCookingRecipe)recipe;
            CookingRecipeCategory lv4 = lv3.getCategory();
            if (lv2 == RecipeType.SMELTING) {
               switch (lv4) {
                  case BLOCKS:
                     var4 = RecipeBookGroup.FURNACE_BLOCKS;
                     break;
                  case FOOD:
                     var4 = RecipeBookGroup.FURNACE_FOOD;
                     break;
                  case MISC:
                     var4 = RecipeBookGroup.FURNACE_MISC;
                     break;
                  default:
                     throw new IncompatibleClassChangeError();
               }

               return var4;
            }

            if (lv2 == RecipeType.BLASTING) {
               return lv4 == CookingRecipeCategory.BLOCKS ? RecipeBookGroup.BLAST_FURNACE_BLOCKS : RecipeBookGroup.BLAST_FURNACE_MISC;
            }

            if (lv2 == RecipeType.SMOKING) {
               return RecipeBookGroup.SMOKER_FOOD;
            }

            if (lv2 == RecipeType.CAMPFIRE_COOKING) {
               return RecipeBookGroup.CAMPFIRE;
            }
         }

         if (lv2 == RecipeType.STONECUTTING) {
            return RecipeBookGroup.STONECUTTER;
         } else if (lv2 == RecipeType.SMITHING) {
            return RecipeBookGroup.SMITHING;
         } else {
            Logger var10000 = LOGGER;
            Object var10002 = LogUtils.defer(() -> {
               return Registries.RECIPE_TYPE.getId(recipe.getType());
            });
            Objects.requireNonNull(recipe);
            var10000.warn("Unknown recipe category: {}/{}", var10002, LogUtils.defer(recipe::getId));
            return RecipeBookGroup.UNKNOWN;
         }
      }
   }

   public List getOrderedResults() {
      return this.orderedResults;
   }

   public List getResultsForGroup(RecipeBookGroup category) {
      return (List)this.resultsByGroup.getOrDefault(category, Collections.emptyList());
   }
}
