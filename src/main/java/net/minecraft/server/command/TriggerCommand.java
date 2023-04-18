package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TriggerCommand {
   private static final SimpleCommandExceptionType FAILED_UNPRIMED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.trigger.failed.unprimed"));
   private static final SimpleCommandExceptionType FAILED_INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.trigger.failed.invalid"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("trigger").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).suggests((context, builder) -> {
         return suggestObjectives((ServerCommandSource)context.getSource(), builder);
      }).executes((context) -> {
         return executeSimple((ServerCommandSource)context.getSource(), getScore(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), ScoreboardObjectiveArgumentType.getObjective(context, "objective")));
      })).then(CommandManager.literal("add").then(CommandManager.argument("value", IntegerArgumentType.integer()).executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), getScore(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), ScoreboardObjectiveArgumentType.getObjective(context, "objective")), IntegerArgumentType.getInteger(context, "value"));
      })))).then(CommandManager.literal("set").then(CommandManager.argument("value", IntegerArgumentType.integer()).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), getScore(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), ScoreboardObjectiveArgumentType.getObjective(context, "objective")), IntegerArgumentType.getInteger(context, "value"));
      })))));
   }

   public static CompletableFuture suggestObjectives(ServerCommandSource source, SuggestionsBuilder builder) {
      Entity lv = source.getEntity();
      List list = Lists.newArrayList();
      if (lv != null) {
         Scoreboard lv2 = source.getServer().getScoreboard();
         String string = lv.getEntityName();
         Iterator var6 = lv2.getObjectives().iterator();

         while(var6.hasNext()) {
            ScoreboardObjective lv3 = (ScoreboardObjective)var6.next();
            if (lv3.getCriterion() == ScoreboardCriterion.TRIGGER && lv2.playerHasObjective(string, lv3)) {
               ScoreboardPlayerScore lv4 = lv2.getPlayerScore(string, lv3);
               if (!lv4.isLocked()) {
                  list.add(lv3.getName());
               }
            }
         }
      }

      return CommandSource.suggestMatching((Iterable)list, builder);
   }

   private static int executeAdd(ServerCommandSource source, ScoreboardPlayerScore score, int value) {
      score.incrementScore(value);
      source.sendFeedback(Text.translatable("commands.trigger.add.success", score.getObjective().toHoverableText(), value), true);
      return score.getScore();
   }

   private static int executeSet(ServerCommandSource source, ScoreboardPlayerScore score, int value) {
      score.setScore(value);
      source.sendFeedback(Text.translatable("commands.trigger.set.success", score.getObjective().toHoverableText(), value), true);
      return value;
   }

   private static int executeSimple(ServerCommandSource source, ScoreboardPlayerScore score) {
      score.incrementScore(1);
      source.sendFeedback(Text.translatable("commands.trigger.simple.success", score.getObjective().toHoverableText()), true);
      return score.getScore();
   }

   private static ScoreboardPlayerScore getScore(ServerPlayerEntity player, ScoreboardObjective objective) throws CommandSyntaxException {
      if (objective.getCriterion() != ScoreboardCriterion.TRIGGER) {
         throw FAILED_INVALID_EXCEPTION.create();
      } else {
         Scoreboard lv = player.getScoreboard();
         String string = player.getEntityName();
         if (!lv.playerHasObjective(string, objective)) {
            throw FAILED_UNPRIMED_EXCEPTION.create();
         } else {
            ScoreboardPlayerScore lv2 = lv.getPlayerScore(string, objective);
            if (lv2.isLocked()) {
               throw FAILED_UNPRIMED_EXCEPTION.create();
            } else {
               lv2.setLocked(true);
               return lv2;
            }
         }
      }
   }
}
