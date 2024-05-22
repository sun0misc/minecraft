/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.recipe;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RecipeCache {
    private final CachedRecipe[] cache;
    private WeakReference<RecipeManager> recipeManagerRef = new WeakReference<Object>(null);

    public RecipeCache(int size) {
        this.cache = new CachedRecipe[size];
    }

    public Optional<RecipeEntry<CraftingRecipe>> getRecipe(World world, CraftingRecipeInput input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        this.validateRecipeManager(world);
        for (int i = 0; i < this.cache.length; ++i) {
            CachedRecipe lv = this.cache[i];
            if (lv == null || !lv.matches(input)) continue;
            this.sendToFront(i);
            return Optional.ofNullable(lv.value());
        }
        return this.getAndCacheRecipe(input, world);
    }

    private void validateRecipeManager(World world) {
        RecipeManager lv = world.getRecipeManager();
        if (lv != this.recipeManagerRef.get()) {
            this.recipeManagerRef = new WeakReference<RecipeManager>(lv);
            Arrays.fill(this.cache, null);
        }
    }

    private Optional<RecipeEntry<CraftingRecipe>> getAndCacheRecipe(CraftingRecipeInput input, World world) {
        Optional<RecipeEntry<CraftingRecipe>> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world);
        this.cache(input, optional.orElse(null));
        return optional;
    }

    private void sendToFront(int index) {
        if (index > 0) {
            CachedRecipe lv = this.cache[index];
            System.arraycopy(this.cache, 0, this.cache, 1, index);
            this.cache[0] = lv;
        }
    }

    private void cache(CraftingRecipeInput input, @Nullable RecipeEntry<CraftingRecipe> recipe) {
        DefaultedList<ItemStack> lv = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < input.getSize(); ++i) {
            lv.set(i, input.getStackInSlot(i).copyWithCount(1));
        }
        System.arraycopy(this.cache, 0, this.cache, 1, this.cache.length - 1);
        this.cache[0] = new CachedRecipe(lv, input.getWidth(), input.getHeight(), recipe);
    }

    record CachedRecipe(DefaultedList<ItemStack> key, int width, int height, @Nullable RecipeEntry<CraftingRecipe> value) {
        public boolean matches(CraftingRecipeInput input) {
            if (this.width != input.getWidth() || this.height != input.getHeight()) {
                return false;
            }
            for (int i = 0; i < this.key.size(); ++i) {
                if (ItemStack.areItemsAndComponentsEqual(this.key.get(i), input.getStackInSlot(i))) continue;
                return false;
            }
            return true;
        }

        @Nullable
        public RecipeEntry<CraftingRecipe> value() {
            return this.value;
        }
    }
}

