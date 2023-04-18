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
import java.util.Iterator;
import java.util.Set;
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

   private static void writeArgumentProperties(JsonObject json, ArgumentSerializer.ArgumentTypeProperties properties) {
      writeArgumentProperties(json, properties.getSerializer(), properties);
   }

   private static void writeArgumentProperties(JsonObject json, ArgumentSerializer serializer, ArgumentSerializer.ArgumentTypeProperties properties) {
      serializer.writeJson(properties, json);
   }

   private static void writeArgument(JsonObject json, ArgumentType argumentType) {
      ArgumentSerializer.ArgumentTypeProperties lv = ArgumentTypes.getArgumentTypeProperties(argumentType);
      json.addProperty("type", "argument");
      json.addProperty("parser", Registries.COMMAND_ARGUMENT_TYPE.getId(lv.getSerializer()).toString());
      JsonObject jsonObject2 = new JsonObject();
      writeArgumentProperties(jsonObject2, lv);
      if (jsonObject2.size() > 0) {
         json.add("properties", jsonObject2);
      }

   }

   public static JsonObject toJson(CommandDispatcher dispatcher, CommandNode rootNode) {
      JsonObject jsonObject = new JsonObject();
      if (rootNode instanceof RootCommandNode) {
         jsonObject.addProperty("type", "root");
      } else if (rootNode instanceof LiteralCommandNode) {
         jsonObject.addProperty("type", "literal");
      } else if (rootNode instanceof ArgumentCommandNode) {
         ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)rootNode;
         writeArgument(jsonObject, argumentCommandNode.getType());
      } else {
         LOGGER.error("Could not serialize node {} ({})!", rootNode, rootNode.getClass());
         jsonObject.addProperty("type", "unknown");
      }

      JsonObject jsonObject2 = new JsonObject();
      Iterator var4 = rootNode.getChildren().iterator();

      while(var4.hasNext()) {
         CommandNode commandNode2 = (CommandNode)var4.next();
         jsonObject2.add(commandNode2.getName(), toJson(dispatcher, commandNode2));
      }

      if (jsonObject2.size() > 0) {
         jsonObject.add("children", jsonObject2);
      }

      if (rootNode.getCommand() != null) {
         jsonObject.addProperty("executable", true);
      }

      if (rootNode.getRedirect() != null) {
         Collection collection = dispatcher.getPath(rootNode.getRedirect());
         if (!collection.isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            Iterator var6 = collection.iterator();

            while(var6.hasNext()) {
               String string = (String)var6.next();
               jsonArray.add(string);
            }

            jsonObject.add("redirect", jsonArray);
         }
      }

      return jsonObject;
   }

   public static Set collectUsedArgumentTypes(CommandNode rootNode) {
      Set set = Sets.newIdentityHashSet();
      Set set2 = Sets.newHashSet();
      collectUsedArgumentTypes(rootNode, set2, set);
      return set2;
   }

   private static void collectUsedArgumentTypes(CommandNode node, Set usedArgumentTypes, Set visitedNodes) {
      if (visitedNodes.add(node)) {
         if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)node;
            usedArgumentTypes.add(argumentCommandNode.getType());
         }

         node.getChildren().forEach((child) -> {
            collectUsedArgumentTypes(child, usedArgumentTypes, visitedNodes);
         });
         CommandNode commandNode2 = node.getRedirect();
         if (commandNode2 != null) {
            collectUsedArgumentTypes(commandNode2, usedArgumentTypes, visitedNodes);
         }

      }
   }
}
