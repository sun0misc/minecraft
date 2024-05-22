/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IdentifierArgumentType
implements ArgumentType<Identifier> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType UNKNOWN_ADVANCEMENT_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("advancement.advancementNotFound", id));
    private static final DynamicCommandExceptionType UNKNOWN_RECIPE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("recipe.notFound", id));

    public static IdentifierArgumentType identifier() {
        return new IdentifierArgumentType();
    }

    public static AdvancementEntry getAdvancementArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        Identifier lv = IdentifierArgumentType.getIdentifier(context, argumentName);
        AdvancementEntry lv2 = context.getSource().getServer().getAdvancementLoader().get(lv);
        if (lv2 == null) {
            throw UNKNOWN_ADVANCEMENT_EXCEPTION.create(lv);
        }
        return lv2;
    }

    public static RecipeEntry<?> getRecipeArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        RecipeManager lv = context.getSource().getServer().getRecipeManager();
        Identifier lv2 = IdentifierArgumentType.getIdentifier(context, argumentName);
        return lv.get(lv2).orElseThrow(() -> UNKNOWN_RECIPE_EXCEPTION.create(lv2));
    }

    public static Identifier getIdentifier(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Identifier.class);
    }

    @Override
    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return Identifier.fromCommandInput(stringReader);
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

