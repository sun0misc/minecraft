/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;

public class ItemStackArgumentType
implements ArgumentType<ItemStackArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");
    private final ItemStringReader reader;

    public ItemStackArgumentType(CommandRegistryAccess commandRegistryAccess) {
        this.reader = new ItemStringReader(commandRegistryAccess);
    }

    public static ItemStackArgumentType itemStack(CommandRegistryAccess commandRegistryAccess) {
        return new ItemStackArgumentType(commandRegistryAccess);
    }

    @Override
    public ItemStackArgument parse(StringReader stringReader) throws CommandSyntaxException {
        ItemStringReader.ItemResult lv = this.reader.consume(stringReader);
        return new ItemStackArgument(lv.item(), lv.components());
    }

    public static <S> ItemStackArgument getItemStackArgument(CommandContext<S> context, String name) {
        return context.getArgument(name, ItemStackArgument.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.reader.getSuggestions(builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

