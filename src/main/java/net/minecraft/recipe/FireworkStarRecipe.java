/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public class FireworkStarRecipe
extends SpecialCraftingRecipe {
    private static final Ingredient TYPE_MODIFIER = Ingredient.ofItems(Items.FIRE_CHARGE, Items.FEATHER, Items.GOLD_NUGGET, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.CREEPER_HEAD, Items.PLAYER_HEAD, Items.DRAGON_HEAD, Items.ZOMBIE_HEAD, Items.PIGLIN_HEAD);
    private static final Ingredient TRAIL_MODIFIER = Ingredient.ofItems(Items.DIAMOND);
    private static final Ingredient FLICKER_MODIFIER = Ingredient.ofItems(Items.GLOWSTONE_DUST);
    private static final Map<Item, FireworkExplosionComponent.Type> TYPE_MODIFIER_MAP = Util.make(Maps.newHashMap(), typeModifiers -> {
        typeModifiers.put(Items.FIRE_CHARGE, FireworkExplosionComponent.Type.LARGE_BALL);
        typeModifiers.put(Items.FEATHER, FireworkExplosionComponent.Type.BURST);
        typeModifiers.put(Items.GOLD_NUGGET, FireworkExplosionComponent.Type.STAR);
        typeModifiers.put(Items.SKELETON_SKULL, FireworkExplosionComponent.Type.CREEPER);
        typeModifiers.put(Items.WITHER_SKELETON_SKULL, FireworkExplosionComponent.Type.CREEPER);
        typeModifiers.put(Items.CREEPER_HEAD, FireworkExplosionComponent.Type.CREEPER);
        typeModifiers.put(Items.PLAYER_HEAD, FireworkExplosionComponent.Type.CREEPER);
        typeModifiers.put(Items.DRAGON_HEAD, FireworkExplosionComponent.Type.CREEPER);
        typeModifiers.put(Items.ZOMBIE_HEAD, FireworkExplosionComponent.Type.CREEPER);
        typeModifiers.put(Items.PIGLIN_HEAD, FireworkExplosionComponent.Type.CREEPER);
    });
    private static final Ingredient GUNPOWDER = Ingredient.ofItems(Items.GUNPOWDER);

    public FireworkStarRecipe(CraftingRecipeCategory arg) {
        super(arg);
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv = arg.getStackInSlot(i);
            if (lv.isEmpty()) continue;
            if (TYPE_MODIFIER.test(lv)) {
                if (bl3) {
                    return false;
                }
                bl3 = true;
                continue;
            }
            if (FLICKER_MODIFIER.test(lv)) {
                if (bl5) {
                    return false;
                }
                bl5 = true;
                continue;
            }
            if (TRAIL_MODIFIER.test(lv)) {
                if (bl4) {
                    return false;
                }
                bl4 = true;
                continue;
            }
            if (GUNPOWDER.test(lv)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (lv.getItem() instanceof DyeItem) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl && bl2;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        FireworkExplosionComponent.Type lv = FireworkExplosionComponent.Type.SMALL_BALL;
        boolean bl = false;
        boolean bl2 = false;
        IntArrayList intList = new IntArrayList();
        for (int i = 0; i < arg.getSize(); ++i) {
            ItemStack lv2 = arg.getStackInSlot(i);
            if (lv2.isEmpty()) continue;
            if (TYPE_MODIFIER.test(lv2)) {
                lv = TYPE_MODIFIER_MAP.get(lv2.getItem());
                continue;
            }
            if (FLICKER_MODIFIER.test(lv2)) {
                bl = true;
                continue;
            }
            if (TRAIL_MODIFIER.test(lv2)) {
                bl2 = true;
                continue;
            }
            if (!(lv2.getItem() instanceof DyeItem)) continue;
            intList.add(((DyeItem)lv2.getItem()).getColor().getFireworkColor());
        }
        ItemStack lv3 = new ItemStack(Items.FIREWORK_STAR);
        lv3.set(DataComponentTypes.FIREWORK_EXPLOSION, new FireworkExplosionComponent(lv, intList, IntList.of(), bl2, bl));
        return lv3;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return new ItemStack(Items.FIREWORK_STAR);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR;
    }
}

