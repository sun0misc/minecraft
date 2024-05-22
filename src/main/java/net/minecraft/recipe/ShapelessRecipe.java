/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ShapelessRecipe
implements CraftingRecipe {
    final String group;
    final CraftingRecipeCategory category;
    final ItemStack result;
    final DefaultedList<Ingredient> ingredients;

    public ShapelessRecipe(String group, CraftingRecipeCategory category, ItemStack result, DefaultedList<Ingredient> ingredients) {
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.result;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean matches(CraftingRecipeInput arg, World arg2) {
        if (arg.getStackCount() != this.ingredients.size()) {
            return false;
        }
        if (arg.getSize() == 1 && this.ingredients.size() == 1) {
            return ((Ingredient)this.ingredients.getFirst()).test(arg.getStackInSlot(0));
        }
        return arg.getRecipeMatcher().match(this, null);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        return this.result.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    public static class Serializer
    implements RecipeSerializer<ShapelessRecipe> {
        private static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group), ((MapCodec)CraftingRecipeCategory.CODEC.fieldOf("category")).orElse(CraftingRecipeCategory.MISC).forGetter(recipe -> recipe.category), ((MapCodec)ItemStack.VALIDATED_CODEC.fieldOf("result")).forGetter(recipe -> recipe.result), ((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients")).flatXmap(ingredients -> {
            Ingredient[] lvs = (Ingredient[])ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
            if (lvs.length == 0) {
                return DataResult.error(() -> "No ingredients for shapeless recipe");
            }
            if (lvs.length > 9) {
                return DataResult.error(() -> "Too many ingredients for shapeless recipe");
            }
            return DataResult.success(DefaultedList.copyOf(Ingredient.EMPTY, lvs));
        }, DataResult::success).forGetter(recipe -> recipe.ingredients)).apply((Applicative<ShapelessRecipe, ?>)instance, ShapelessRecipe::new));
        public static final PacketCodec<RegistryByteBuf, ShapelessRecipe> PACKET_CODEC = PacketCodec.ofStatic(Serializer::write, Serializer::read);

        @Override
        public MapCodec<ShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShapelessRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static ShapelessRecipe read(RegistryByteBuf buf) {
            String string = buf.readString();
            CraftingRecipeCategory lv = buf.readEnumConstant(CraftingRecipeCategory.class);
            int i = buf.readVarInt();
            DefaultedList<Ingredient> lv2 = DefaultedList.ofSize(i, Ingredient.EMPTY);
            lv2.replaceAll(empty -> (Ingredient)Ingredient.PACKET_CODEC.decode(buf));
            ItemStack lv3 = (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
            return new ShapelessRecipe(string, lv, lv3, lv2);
        }

        private static void write(RegistryByteBuf buf, ShapelessRecipe recipe) {
            buf.writeString(recipe.group);
            buf.writeEnumConstant(recipe.category);
            buf.writeVarInt(recipe.ingredients.size());
            for (Ingredient lv : recipe.ingredients) {
                Ingredient.PACKET_CODEC.encode(buf, lv);
            }
            ItemStack.PACKET_CODEC.encode(buf, recipe.result);
        }
    }
}

