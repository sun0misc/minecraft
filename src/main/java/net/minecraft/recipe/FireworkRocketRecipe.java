/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import java.util.ArrayList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class FireworkRocketRecipe
extends SpecialCraftingRecipe {
    private static final Ingredient PAPER = Ingredient.ofItems(Items.PAPER);
    private static final Ingredient DURATION_MODIFIER = Ingredient.ofItems(Items.GUNPOWDER);
    private static final Ingredient FIREWORK_STAR = Ingredient.ofItems(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        boolean bl = false;
        int i = 0;
        for (int j = 0; j < arg.getSize(); ++j) {
            ItemStack lv = arg.getStackInSlot(j);
            if (lv.isEmpty()) continue;
            if (PAPER.test(lv)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!(DURATION_MODIFIER.test(lv) ? ++i > 3 : !FIREWORK_STAR.test(lv))) continue;
            return false;
        }
        return bl && i >= 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        ArrayList<FireworkExplosionComponent> list = new ArrayList<FireworkExplosionComponent>();
        int i = 0;
        for (int j = 0; j < arg.getSize(); ++j) {
            FireworkExplosionComponent lv2;
            ItemStack lv = arg.getStackInSlot(j);
            if (lv.isEmpty()) continue;
            if (DURATION_MODIFIER.test(lv)) {
                ++i;
                continue;
            }
            if (!FIREWORK_STAR.test(lv) || (lv2 = lv.get(DataComponentTypes.FIREWORK_EXPLOSION)) == null) continue;
            list.add(lv2);
        }
        ItemStack lv3 = new ItemStack(Items.FIREWORK_ROCKET, 3);
        lv3.set(DataComponentTypes.FIREWORKS, new FireworksComponent(i, list));
        return lv3;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}

