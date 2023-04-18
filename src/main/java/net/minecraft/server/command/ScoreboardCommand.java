package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.OperationArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardCriterionArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class ScoreboardCommand {
   private static final SimpleCommandExceptionType OBJECTIVES_ADD_DUPLICATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.scoreboard.objectives.add.duplicate"));
   private static final SimpleCommandExceptionType OBJECTIVES_DISPLAY_ALREADY_EMPTY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.scoreboard.objectives.display.alreadyEmpty"));
   private static final SimpleCommandExceptionType OBJECTIVES_DISPLAY_ALREADY_SET_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.scoreboard.objectives.display.alreadySet"));
   private static final SimpleCommandExceptionType PLAYERS_ENABLE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.scoreboard.players.enable.failed"));
   private static final SimpleCommandExceptionType PLAYERS_ENABLE_INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.scoreboard.players.enable.invalid"));
   private static final Dynamic2CommandExceptionType PLAYERS_GET_NULL_EXCEPTION = new Dynamic2CommandExceptionType((objective, target) -> {
      return Text.translatable("commands.scoreboard.players.get.null", objective, target);
   });

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("scoreboard").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("objectives").then(CommandManager.literal("list").executes((context) -> {
         return executeListObjectives((ServerCommandSource)context.getSource());
      }))).then(CommandManager.literal("add").then(CommandManager.argument("objective", StringArgumentType.word()).then(((RequiredArgumentBuilder)CommandManager.argument("criteria", ScoreboardCriterionArgumentType.scoreboardCriterion()).executes((context) -> {
         return executeAddObjective((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "objective"), ScoreboardCriterionArgumentType.getCriterion(context, "criteria"), Text.literal(StringArgumentType.getString(context, "objective")));
      })).then(CommandManager.argument("displayName", TextArgumentType.text()).executes((context) -> {
         return executeAddObjective((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "objective"), ScoreboardCriterionArgumentType.getCriterion(context, "criteria"), TextArgumentType.getTextArgument(context, "displayName"));
      })))))).then(CommandManager.literal("modify").then(((RequiredArgumentBuilder)CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.literal("displayname").then(CommandManager.argument("displayName", TextArgumentType.text()).executes((context) -> {
         return executeModifyObjective((ServerCommandSource)context.getSource(), ScoreboardObjectiveArgumentType.getObjective(context, "objective"), TextArgumentType.getTextArgument(context, "displayName"));
      })))).then(makeRenderTypeArguments())))).then(CommandManager.literal("remove").then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes((context) -> {
         return executeRemoveObjective((ServerCommandSource)context.getSource(), ScoreboardObjectiveArgumentType.getObjective(context, "objective"));
      })))).then(CommandManager.literal("setdisplay").then(((RequiredArgumentBuilder)CommandManager.argument("slot", ScoreboardSlotArgumentType.scoreboardSlot()).executes((context) -> {
         return executeClearDisplay((ServerCommandSource)context.getSource(), ScoreboardSlotArgumentType.getScoreboardSlot(context, "slot"));
      })).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes((context) -> {
         return executeSetDisplay((ServerCommandSource)context.getSource(), ScoreboardSlotArgumentType.getScoreboardSlot(context, "slot"), ScoreboardObjectiveArgumentType.getObjective(context, "objective"));
      })))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("players").then(((LiteralArgumentBuilder)CommandManager.literal("list").executes((context) -> {
         return executeListPlayers((ServerCommandSource)context.getSource());
      })).then(CommandManager.argument("target", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).executes((context) -> {
         return executeListScores((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreHolder(context, "target"));
      })))).then(CommandManager.literal("set").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.argument("score", IntegerArgumentType.integer()).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getWritableObjective(context, "objective"), IntegerArgumentType.getInteger(context, "score"));
      })))))).then(CommandManager.literal("get").then(CommandManager.argument("target", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes((context) -> {
         return executeGet((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreHolder(context, "target"), ScoreboardObjectiveArgumentType.getObjective(context, "objective"));
      }))))).then(CommandManager.literal("add").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.argument("score", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getWritableObjective(context, "objective"), IntegerArgumentType.getInteger(context, "score"));
      })))))).then(CommandManager.literal("remove").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.argument("score", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeRemove((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getWritableObjective(context, "objective"), IntegerArgumentType.getInteger(context, "score"));
      })))))).then(CommandManager.literal("reset").then(((RequiredArgumentBuilder)CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).executes((context) -> {
         return executeReset((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"));
      })).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes((context) -> {
         return executeReset((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getObjective(context, "objective"));
      }))))).then(CommandManager.literal("enable").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).suggests((context, builder) -> {
         return suggestDisabled((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), builder);
      }).executes((context) -> {
         return executeEnable((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getObjective(context, "objective"));
      }))))).then(CommandManager.literal("operation").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("targetObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.argument("operation", OperationArgumentType.operation()).then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes((context) -> {
         return executeOperation((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"), ScoreboardObjectiveArgumentType.getWritableObjective(context, "targetObjective"), OperationArgumentType.getOperation(context, "operation"), ScoreHolderArgumentType.getScoreboardScoreHolders(context, "source"), ScoreboardObjectiveArgumentType.getObjective(context, "sourceObjective"));
      })))))))));
   }

   private static LiteralArgumentBuilder makeRenderTypeArguments() {
      LiteralArgumentBuilder literalArgumentBuilder = CommandManager.literal("rendertype");
      ScoreboardCriterion.RenderType[] var1 = ScoreboardCriterion.RenderType.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         ScoreboardCriterion.RenderType lv = var1[var3];
         literalArgumentBuilder.then(CommandManager.literal(lv.getName()).executes((context) -> {
            return executeModifyRenderType((ServerCommandSource)context.getSource(), ScoreboardObjectiveArgumentType.getObjective(context, "objective"), lv);
         }));
      }

      return literalArgumentBuilder;
   }

   private static CompletableFuture suggestDisabled(ServerCommandSource source, Collection targets, SuggestionsBuilder builder) {
      List list = Lists.newArrayList();
      Scoreboard lv = source.getServer().getScoreboard();
      Iterator var5 = lv.getObjectives().iterator();

      while(true) {
         ScoreboardObjective lv2;
         do {
            if (!var5.hasNext()) {
               return CommandSource.suggestMatching((Iterable)list, builder);
            }

            lv2 = (ScoreboardObjective)var5.next();
         } while(lv2.getCriterion() != ScoreboardCriterion.TRIGGER);

         boolean bl = false;
         Iterator var8 = targets.iterator();

         label32: {
            String string;
            do {
               if (!var8.hasNext()) {
                  break label32;
               }

               string = (String)var8.next();
            } while(lv.playerHasObjective(string, lv2) && !lv.getPlayerScore(string, lv2).isLocked());

            bl = true;
         }

         if (bl) {
            list.add(lv2.getName());
         }
      }
   }

   private static int executeGet(ServerCommandSource source, String target, ScoreboardObjective objective) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      if (!lv.playerHasObjective(target, objective)) {
         throw PLAYERS_GET_NULL_EXCEPTION.create(objective.getName(), target);
      } else {
         ScoreboardPlayerScore lv2 = lv.getPlayerScore(target, objective);
         source.sendFeedback(Text.translatable("commands.scoreboard.players.get.success", target, lv2.getScore(), objective.toHoverableText()), false);
         return lv2.getScore();
      }
   }

   private static int executeOperation(ServerCommandSource source, Collection targets, ScoreboardObjective targetObjective, OperationArgumentType.Operation operation, Collection sources, ScoreboardObjective sourceObjectives) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      int i = 0;

      ScoreboardPlayerScore lv2;
      for(Iterator var8 = targets.iterator(); var8.hasNext(); i += lv2.getScore()) {
         String string = (String)var8.next();
         lv2 = lv.getPlayerScore(string, targetObjective);
         Iterator var11 = sources.iterator();

         while(var11.hasNext()) {
            String string2 = (String)var11.next();
            ScoreboardPlayerScore lv3 = lv.getPlayerScore(string2, sourceObjectives);
            operation.apply(lv2, lv3);
         }
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.operation.success.single", targetObjective.toHoverableText(), targets.iterator().next(), i), true);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.operation.success.multiple", targetObjective.toHoverableText(), targets.size()), true);
      }

      return i;
   }

   private static int executeEnable(ServerCommandSource source, Collection targets, ScoreboardObjective objective) throws CommandSyntaxException {
      if (objective.getCriterion() != ScoreboardCriterion.TRIGGER) {
         throw PLAYERS_ENABLE_INVALID_EXCEPTION.create();
      } else {
         Scoreboard lv = source.getServer().getScoreboard();
         int i = 0;
         Iterator var5 = targets.iterator();

         while(var5.hasNext()) {
            String string = (String)var5.next();
            ScoreboardPlayerScore lv2 = lv.getPlayerScore(string, objective);
            if (lv2.isLocked()) {
               lv2.setLocked(false);
               ++i;
            }
         }

         if (i == 0) {
            throw PLAYERS_ENABLE_FAILED_EXCEPTION.create();
         } else {
            if (targets.size() == 1) {
               source.sendFeedback(Text.translatable("commands.scoreboard.players.enable.success.single", objective.toHoverableText(), targets.iterator().next()), true);
            } else {
               source.sendFeedback(Text.translatable("commands.scoreboard.players.enable.success.multiple", objective.toHoverableText(), targets.size()), true);
            }

            return i;
         }
      }
   }

   private static int executeReset(ServerCommandSource source, Collection targets) {
      Scoreboard lv = source.getServer().getScoreboard();
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         String string = (String)var3.next();
         lv.resetPlayerScore(string, (ScoreboardObjective)null);
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.reset.all.single", targets.iterator().next()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.reset.all.multiple", targets.size()), true);
      }

      return targets.size();
   }

   private static int executeReset(ServerCommandSource source, Collection targets, ScoreboardObjective objective) {
      Scoreboard lv = source.getServer().getScoreboard();
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         lv.resetPlayerScore(string, objective);
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.reset.specific.single", objective.toHoverableText(), targets.iterator().next()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.reset.specific.multiple", objective.toHoverableText(), targets.size()), true);
      }

      return targets.size();
   }

   private static int executeSet(ServerCommandSource source, Collection targets, ScoreboardObjective objective, int score) {
      Scoreboard lv = source.getServer().getScoreboard();
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         String string = (String)var5.next();
         ScoreboardPlayerScore lv2 = lv.getPlayerScore(string, objective);
         lv2.setScore(score);
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.set.success.single", objective.toHoverableText(), targets.iterator().next(), score), true);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.set.success.multiple", objective.toHoverableText(), targets.size(), score), true);
      }

      return score * targets.size();
   }

   private static int executeAdd(ServerCommandSource source, Collection targets, ScoreboardObjective objective, int score) {
      Scoreboard lv = source.getServer().getScoreboard();
      int j = 0;

      ScoreboardPlayerScore lv2;
      for(Iterator var6 = targets.iterator(); var6.hasNext(); j += lv2.getScore()) {
         String string = (String)var6.next();
         lv2 = lv.getPlayerScore(string, objective);
         lv2.setScore(lv2.getScore() + score);
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.add.success.single", score, objective.toHoverableText(), targets.iterator().next(), j), true);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.add.success.multiple", score, objective.toHoverableText(), targets.size()), true);
      }

      return j;
   }

   private static int executeRemove(ServerCommandSource source, Collection targets, ScoreboardObjective objective, int score) {
      Scoreboard lv = source.getServer().getScoreboard();
      int j = 0;

      ScoreboardPlayerScore lv2;
      for(Iterator var6 = targets.iterator(); var6.hasNext(); j += lv2.getScore()) {
         String string = (String)var6.next();
         lv2 = lv.getPlayerScore(string, objective);
         lv2.setScore(lv2.getScore() - score);
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.remove.success.single", score, objective.toHoverableText(), targets.iterator().next(), j), true);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.remove.success.multiple", score, objective.toHoverableText(), targets.size()), true);
      }

      return j;
   }

   private static int executeListPlayers(ServerCommandSource source) {
      Collection collection = source.getServer().getScoreboard().getKnownPlayers();
      if (collection.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.list.empty"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.list.success", collection.size(), Texts.joinOrdered(collection)), false);
      }

      return collection.size();
   }

   private static int executeListScores(ServerCommandSource source, String target) {
      Map map = source.getServer().getScoreboard().getPlayerObjectives(target);
      if (map.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.list.entity.empty", target), false);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.players.list.entity.success", target, map.size()), false);
         Iterator var3 = map.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            source.sendFeedback(Text.translatable("commands.scoreboard.players.list.entity.entry", ((ScoreboardObjective)entry.getKey()).toHoverableText(), ((ScoreboardPlayerScore)entry.getValue()).getScore()), false);
         }
      }

      return map.size();
   }

   private static int executeClearDisplay(ServerCommandSource source, int slot) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      if (lv.getObjectiveForSlot(slot) == null) {
         throw OBJECTIVES_DISPLAY_ALREADY_EMPTY_EXCEPTION.create();
      } else {
         lv.setObjectiveSlot(slot, (ScoreboardObjective)null);
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.display.cleared", Scoreboard.getDisplaySlotNames()[slot]), true);
         return 0;
      }
   }

   private static int executeSetDisplay(ServerCommandSource source, int slot, ScoreboardObjective objective) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      if (lv.getObjectiveForSlot(slot) == objective) {
         throw OBJECTIVES_DISPLAY_ALREADY_SET_EXCEPTION.create();
      } else {
         lv.setObjectiveSlot(slot, objective);
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.display.set", Scoreboard.getDisplaySlotNames()[slot], objective.getDisplayName()), true);
         return 0;
      }
   }

   private static int executeModifyObjective(ServerCommandSource source, ScoreboardObjective objective, Text displayName) {
      if (!objective.getDisplayName().equals(displayName)) {
         objective.setDisplayName(displayName);
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.modify.displayname", objective.getName(), objective.toHoverableText()), true);
      }

      return 0;
   }

   private static int executeModifyRenderType(ServerCommandSource source, ScoreboardObjective objective, ScoreboardCriterion.RenderType type) {
      if (objective.getRenderType() != type) {
         objective.setRenderType(type);
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.modify.rendertype", objective.toHoverableText()), true);
      }

      return 0;
   }

   private static int executeRemoveObjective(ServerCommandSource source, ScoreboardObjective objective) {
      Scoreboard lv = source.getServer().getScoreboard();
      lv.removeObjective(objective);
      source.sendFeedback(Text.translatable("commands.scoreboard.objectives.remove.success", objective.toHoverableText()), true);
      return lv.getObjectives().size();
   }

   private static int executeAddObjective(ServerCommandSource source, String objective, ScoreboardCriterion criteria, Text displayName) throws CommandSyntaxException {
      Scoreboard lv = source.getServer().getScoreboard();
      if (lv.getNullableObjective(objective) != null) {
         throw OBJECTIVES_ADD_DUPLICATE_EXCEPTION.create();
      } else {
         lv.addObjective(objective, criteria, displayName, criteria.getDefaultRenderType());
         ScoreboardObjective lv2 = lv.getNullableObjective(objective);
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.add.success", lv2.toHoverableText()), true);
         return lv.getObjectives().size();
      }
   }

   private static int executeListObjectives(ServerCommandSource source) {
      Collection collection = source.getServer().getScoreboard().getObjectives();
      if (collection.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.list.empty"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.scoreboard.objectives.list.success", collection.size(), Texts.join(collection, ScoreboardObjective::toHoverableText)), false);
      }

      return collection.size();
   }
}
