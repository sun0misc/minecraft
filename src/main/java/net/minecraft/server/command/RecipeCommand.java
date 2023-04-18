package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RecipeCommand {
   private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.recipe.give.failed"));
   private static final SimpleCommandExceptionType TAKE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.recipe.take.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("recipe").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("give").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), Collections.singleton(IdentifierArgumentType.getRecipeArgument(context, "recipe")));
      }))).then(CommandManager.literal("*").executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), ((ServerCommandSource)context.getSource()).getServer().getRecipeManager().values());
      }))))).then(CommandManager.literal("take").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("recipe", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.ALL_RECIPES).executes((context) -> {
         return executeTake((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), Collections.singleton(IdentifierArgumentType.getRecipeArgument(context, "recipe")));
      }))).then(CommandManager.literal("*").executes((context) -> {
         return executeTake((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), ((ServerCommandSource)context.getSource()).getServer().getRecipeManager().values());
      })))));
   }

   private static int executeGive(ServerCommandSource source, Collection targets, Collection recipes) throws CommandSyntaxException {
      int i = 0;

      ServerPlayerEntity lv;
      for(Iterator var4 = targets.iterator(); var4.hasNext(); i += lv.unlockRecipes(recipes)) {
         lv = (ServerPlayerEntity)var4.next();
      }

      if (i == 0) {
         throw GIVE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.recipe.give.success.single", recipes.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.recipe.give.success.multiple", recipes.size(), targets.size()), true);
         }

         return i;
      }
   }

   private static int executeTake(ServerCommandSource source, Collection targets, Collection recipes) throws CommandSyntaxException {
      int i = 0;

      ServerPlayerEntity lv;
      for(Iterator var4 = targets.iterator(); var4.hasNext(); i += lv.lockRecipes(recipes)) {
         lv = (ServerPlayerEntity)var4.next();
      }

      if (i == 0) {
         throw TAKE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.recipe.take.success.single", recipes.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.recipe.take.success.multiple", recipes.size(), targets.size()), true);
         }

         return i;
      }
   }
}
