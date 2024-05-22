/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;

public abstract class CuttingRecipe
implements Recipe<SingleStackRecipeInput> {
    protected final Ingredient ingredient;
    protected final ItemStack result;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    protected final String group;

    public CuttingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient ingredient, ItemStack result) {
        this.type = type;
        this.serializer = serializer;
        this.group = group;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return this.result;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> lv = DefaultedList.of();
        lv.add(this.ingredient);
        return lv;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack craft(SingleStackRecipeInput arg, RegistryWrapper.WrapperLookup arg2) {
        return this.result.copy();
    }

    public static interface RecipeFactory<T extends CuttingRecipe> {
        public T create(String var1, Ingredient var2, ItemStack var3);
    }

    public static class Serializer<T extends CuttingRecipe>
    implements RecipeSerializer<T> {
        final RecipeFactory<T> recipeFactory;
        private final MapCodec<T> codec;
        private final PacketCodec<RegistryByteBuf, T> packetCodec;

        protected Serializer(RecipeFactory<T> recipeFactory) {
            this.recipeFactory = recipeFactory;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group), ((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient")).forGetter(recipe -> recipe.ingredient), ((MapCodec)ItemStack.VALIDATED_CODEC.fieldOf("result")).forGetter(recipe -> recipe.result)).apply((Applicative<CuttingRecipe, ?>)instance, recipeFactory::create));
            this.packetCodec = PacketCodec.tuple(PacketCodecs.STRING, recipe -> recipe.group, Ingredient.PACKET_CODEC, recipe -> recipe.ingredient, ItemStack.PACKET_CODEC, recipe -> recipe.result, recipeFactory::create);
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, T> packetCodec() {
            return this.packetCodec;
        }
    }
}

