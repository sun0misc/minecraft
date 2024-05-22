/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;

public class ScoreboardCriterionArgumentType
implements ArgumentType<ScoreboardCriterion> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar.baz", "minecraft:foo");
    public static final DynamicCommandExceptionType INVALID_CRITERION_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("argument.criteria.invalid", name));

    private ScoreboardCriterionArgumentType() {
    }

    public static ScoreboardCriterionArgumentType scoreboardCriterion() {
        return new ScoreboardCriterionArgumentType();
    }

    public static ScoreboardCriterion getCriterion(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, ScoreboardCriterion.class);
    }

    @Override
    public ScoreboardCriterion parse(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        return ScoreboardCriterion.getOrCreateStatCriterion(string).orElseThrow(() -> {
            stringReader.setCursor(i);
            return INVALID_CRITERION_EXCEPTION.createWithContext(stringReader, string);
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        ArrayList<String> list = Lists.newArrayList(ScoreboardCriterion.getAllSimpleCriteria());
        for (StatType statType : Registries.STAT_TYPE) {
            for (Object object : statType.getRegistry()) {
                String string = this.getStatName(statType, object);
                list.add(string);
            }
        }
        return CommandSource.suggestMatching(list, builder);
    }

    public <T> String getStatName(StatType<T> stat, Object value) {
        return Stat.getName(stat, value);
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

