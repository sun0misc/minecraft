package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;

@Environment(EnvType.CLIENT)
public class RecipeResultCollection {
   private final DynamicRegistryManager registryManager;
   private final List recipes;
   private final boolean singleOutput;
   private final Set craftableRecipes = Sets.newHashSet();
   private final Set fittingRecipes = Sets.newHashSet();
   private final Set unlockedRecipes = Sets.newHashSet();

   public RecipeResultCollection(DynamicRegistryManager registryManager, List recipes) {
      this.registryManager = registryManager;
      this.recipes = ImmutableList.copyOf(recipes);
      if (recipes.size() <= 1) {
         this.singleOutput = true;
      } else {
         this.singleOutput = shouldHaveSingleOutput(registryManager, recipes);
      }

   }

   private static boolean shouldHaveSingleOutput(DynamicRegistryManager registryManager, List recipes) {
      int i = recipes.size();
      ItemStack lv = ((Recipe)recipes.get(0)).getOutput(registryManager);

      for(int j = 1; j < i; ++j) {
         ItemStack lv2 = ((Recipe)recipes.get(j)).getOutput(registryManager);
         if (!ItemStack.areItemsEqual(lv, lv2) || !ItemStack.areNbtEqual(lv, lv2)) {
            return false;
         }
      }

      return true;
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.registryManager;
   }

   public boolean isInitialized() {
      return !this.unlockedRecipes.isEmpty();
   }

   public void initialize(RecipeBook recipeBook) {
      Iterator var2 = this.recipes.iterator();

      while(var2.hasNext()) {
         Recipe lv = (Recipe)var2.next();
         if (recipeBook.contains(lv)) {
            this.unlockedRecipes.add(lv);
         }
      }

   }

   public void computeCraftables(RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook) {
      Iterator var5 = this.recipes.iterator();

      while(true) {
         while(var5.hasNext()) {
            Recipe lv = (Recipe)var5.next();
            boolean bl = lv.fits(gridWidth, gridHeight) && recipeBook.contains(lv);
            if (bl) {
               this.fittingRecipes.add(lv);
            } else {
               this.fittingRecipes.remove(lv);
            }

            if (bl && recipeFinder.match(lv, (IntList)null)) {
               this.craftableRecipes.add(lv);
            } else {
               this.craftableRecipes.remove(lv);
            }
         }

         return;
      }
   }

   public boolean isCraftable(Recipe recipe) {
      return this.craftableRecipes.contains(recipe);
   }

   public boolean hasCraftableRecipes() {
      return !this.craftableRecipes.isEmpty();
   }

   public boolean hasFittingRecipes() {
      return !this.fittingRecipes.isEmpty();
   }

   public List getAllRecipes() {
      return this.recipes;
   }

   public List getResults(boolean craftableOnly) {
      List list = Lists.newArrayList();
      Set set = craftableOnly ? this.craftableRecipes : this.fittingRecipes;
      Iterator var4 = this.recipes.iterator();

      while(var4.hasNext()) {
         Recipe lv = (Recipe)var4.next();
         if (set.contains(lv)) {
            list.add(lv);
         }
      }

      return list;
   }

   public List getRecipes(boolean craftable) {
      List list = Lists.newArrayList();
      Iterator var3 = this.recipes.iterator();

      while(var3.hasNext()) {
         Recipe lv = (Recipe)var3.next();
         if (this.fittingRecipes.contains(lv) && this.craftableRecipes.contains(lv) == craftable) {
            list.add(lv);
         }
      }

      return list;
   }

   public boolean hasSingleOutput() {
      return this.singleOutput;
   }
}
