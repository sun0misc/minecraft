/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

public class ArgumentHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte MIN_FLAG = 1;
    private static final byte MAX_FLAG = 2;

    public static int getMinMaxFlag(boolean hasMin, boolean hasMax) {
        int i = 0;
        if (hasMin) {
            i |= 1;
        }
        if (hasMax) {
            i |= 2;
        }
        return i;
    }

    public static boolean hasMinFlag(byte flags) {
        return (flags & 1) != 0;
    }

    public static boolean hasMaxFlag(byte flags) {
        return (flags & 2) != 0;
    }

    private static <A extends ArgumentType<?>> void writeArgumentProperties(JsonObject json, ArgumentSerializer.ArgumentTypeProperties<A> properties) {
        ArgumentHelper.writeArgumentProperties(json, properties.getSerializer(), properties);
    }

    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void writeArgumentProperties(JsonObject json, ArgumentSerializer<A, T> serializer, ArgumentSerializer.ArgumentTypeProperties<A> properties) {
        serializer.writeJson(properties, json);
    }

    private static <T extends ArgumentType<?>> void writeArgument(JsonObject json, T argumentType) {
        ArgumentSerializer.ArgumentTypeProperties<T> lv = ArgumentTypes.getArgumentTypeProperties(argumentType);
        json.addProperty("type", "argument");
        json.addProperty("parser", Registries.COMMAND_ARGUMENT_TYPE.getId(lv.getSerializer()).toString());
        JsonObject jsonObject2 = new JsonObject();
        ArgumentHelper.writeArgumentProperties(jsonObject2, lv);
        if (jsonObject2.size() > 0) {
            json.add("properties", jsonObject2);
        }
    }

    public static <S> JsonObject toJson(CommandDispatcher<S> dispatcher, CommandNode<S> rootNode) {
        Collection<String> collection;
        JsonObject jsonObject = new JsonObject();
        if (rootNode instanceof RootCommandNode) {
            jsonObject.addProperty("type", "root");
        } else if (rootNode instanceof LiteralCommandNode) {
            jsonObject.addProperty("type", "literal");
        } else if (rootNode instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)rootNode;
            ArgumentHelper.writeArgument(jsonObject, argumentCommandNode.getType());
        } else {
            LOGGER.error("Could not serialize node {} ({})!", (Object)rootNode, (Object)rootNode.getClass());
            jsonObject.addProperty("type", "unknown");
        }
        JsonObject jsonObject2 = new JsonObject();
        for (CommandNode<S> commandNode2 : rootNode.getChildren()) {
            jsonObject2.add(commandNode2.getName(), ArgumentHelper.toJson(dispatcher, commandNode2));
        }
        if (jsonObject2.size() > 0) {
            jsonObject.add("children", jsonObject2);
        }
        if (rootNode.getCommand() != null) {
            jsonObject.addProperty("executable", true);
        }
        if (rootNode.getRedirect() != null && !(collection = dispatcher.getPath(rootNode.getRedirect())).isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            for (String string : collection) {
                jsonArray.add(string);
            }
            jsonObject.add("redirect", jsonArray);
        }
        return jsonObject;
    }

    public static <T> Set<ArgumentType<?>> collectUsedArgumentTypes(CommandNode<T> rootNode) {
        Set<CommandNode<T>> set = Sets.newIdentityHashSet();
        HashSet<ArgumentType<?>> set2 = Sets.newHashSet();
        ArgumentHelper.collectUsedArgumentTypes(rootNode, set2, set);
        return set2;
    }

    private static <T> void collectUsedArgumentTypes(CommandNode<T> node, Set<ArgumentType<?>> usedArgumentTypes, Set<CommandNode<T>> visitedNodes) {
        if (!visitedNodes.add(node)) {
            return;
        }
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)node;
            usedArgumentTypes.add(argumentCommandNode.getType());
        }
        node.getChildren().forEach(child -> ArgumentHelper.collectUsedArgumentTypes(child, usedArgumentTypes, visitedNodes));
        CommandNode<T> commandNode2 = node.getRedirect();
        if (commandNode2 != null) {
            ArgumentHelper.collectUsedArgumentTypes(commandNode2, usedArgumentTypes, visitedNodes);
        }
    }
}

