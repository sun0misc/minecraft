/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TestFunctionArgumentType
implements ArgumentType<TestFunction> {
    private static final Collection<String> EXAMPLES = Arrays.asList("techtests.piston", "techtests");

    @Override
    public TestFunction parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        Optional<TestFunction> optional = TestFunctions.getTestFunction(string);
        if (optional.isPresent()) {
            return optional.get();
        }
        MutableText message = Text.literal("No such test: " + string);
        throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
    }

    public static TestFunctionArgumentType testFunction() {
        return new TestFunctionArgumentType();
    }

    public static TestFunction getFunction(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, TestFunction.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return TestFunctionArgumentType.suggestTestNames(context, builder);
    }

    public static <S> CompletableFuture<Suggestions> suggestTestNames(CommandContext<S> context, SuggestionsBuilder builder) {
        Stream<String> stream = TestFunctions.getTestFunctions().stream().map(TestFunction::templatePath);
        return CommandSource.suggestMatching(stream, builder);
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

