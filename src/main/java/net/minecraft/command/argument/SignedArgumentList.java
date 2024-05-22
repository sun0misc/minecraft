/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.argument.SignedArgumentType;
import org.jetbrains.annotations.Nullable;

public record SignedArgumentList<S>(List<ParsedArgument<S>> arguments) {
    public static <S> boolean isNotEmpty(ParseResults<S> parseResults) {
        return !SignedArgumentList.of(parseResults).arguments().isEmpty();
    }

    public static <S> SignedArgumentList<S> of(ParseResults<S> parseResults) {
        CommandContextBuilder<S> commandContextBuilder3;
        CommandContextBuilder<S> commandContextBuilder;
        String string = parseResults.getReader().getString();
        CommandContextBuilder<S> commandContextBuilder2 = commandContextBuilder = parseResults.getContext();
        List<ParsedArgument<S>> list = SignedArgumentList.collectDecoratableArguments(string, commandContextBuilder2);
        while ((commandContextBuilder3 = commandContextBuilder2.getChild()) != null && commandContextBuilder3.getRootNode() != commandContextBuilder.getRootNode()) {
            list.addAll(SignedArgumentList.collectDecoratableArguments(string, commandContextBuilder3));
            commandContextBuilder2 = commandContextBuilder3;
        }
        return new SignedArgumentList<S>(list);
    }

    private static <S> List<ParsedArgument<S>> collectDecoratableArguments(String argumentName, CommandContextBuilder<S> builder) {
        ArrayList<ParsedArgument<S>> list = new ArrayList<ParsedArgument<S>>();
        for (ParsedCommandNode<S> parsedCommandNode : builder.getNodes()) {
            com.mojang.brigadier.context.ParsedArgument<S, ?> parsedArgument;
            ArgumentCommandNode argumentCommandNode;
            CommandNode<S> commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((argumentCommandNode = (ArgumentCommandNode)commandNode).getType() instanceof SignedArgumentType) || (parsedArgument = builder.getArguments().get(argumentCommandNode.getName())) == null) continue;
            String string2 = parsedArgument.getRange().get(argumentName);
            list.add(new ParsedArgument(argumentCommandNode, string2));
        }
        return list;
    }

    @Nullable
    public ParsedArgument<S> get(String name) {
        for (ParsedArgument<S> lv : this.arguments) {
            if (!name.equals(lv.getNodeName())) continue;
            return lv;
        }
        return null;
    }

    public record ParsedArgument<S>(ArgumentCommandNode<S, ?> node, String value) {
        public String getNodeName() {
            return this.node.getName();
        }
    }
}

