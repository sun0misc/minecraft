package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType SET_POINT_INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.experience.set.points.invalid"));

   public static void register(CommandDispatcher dispatcher) {
      LiteralCommandNode literalCommandNode = dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("experience").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("add").then(CommandManager.argument("targets", EntityArgumentType.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("amount", IntegerArgumentType.integer()).executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"), ExperienceCommand.Component.POINTS);
      })).then(CommandManager.literal("points").executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"), ExperienceCommand.Component.POINTS);
      }))).then(CommandManager.literal("levels").executes((context) -> {
         return executeAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"), ExperienceCommand.Component.LEVELS);
      })))))).then(CommandManager.literal("set").then(CommandManager.argument("targets", EntityArgumentType.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("amount", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"), ExperienceCommand.Component.POINTS);
      })).then(CommandManager.literal("points").executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"), ExperienceCommand.Component.POINTS);
      }))).then(CommandManager.literal("levels").executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"), ExperienceCommand.Component.LEVELS);
      })))))).then(CommandManager.literal("query").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.player()).then(CommandManager.literal("points").executes((context) -> {
         return executeQuery((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayer(context, "targets"), ExperienceCommand.Component.POINTS);
      }))).then(CommandManager.literal("levels").executes((context) -> {
         return executeQuery((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayer(context, "targets"), ExperienceCommand.Component.LEVELS);
      })))));
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("xp").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).redirect(literalCommandNode));
   }

   private static int executeQuery(ServerCommandSource source, ServerPlayerEntity player, Component component) {
      int i = component.getter.applyAsInt(player);
      source.sendFeedback(Text.translatable("commands.experience.query." + component.name, player.getDisplayName(), i), false);
      return i;
   }

   private static int executeAdd(ServerCommandSource source, Collection targets, int amount, Component component) {
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         component.adder.accept(lv, amount);
      }

      if (targets.size() == 1) {
         source.sendFeedback(Text.translatable("commands.experience.add." + component.name + ".success.single", amount, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.experience.add." + component.name + ".success.multiple", amount, targets.size()), true);
      }

      return targets.size();
   }

   private static int executeSet(ServerCommandSource source, Collection targets, int amount, Component component) throws CommandSyntaxException {
      int j = 0;
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var5.next();
         if (component.setter.test(lv, amount)) {
            ++j;
         }
      }

      if (j == 0) {
         throw SET_POINT_INVALID_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.experience.set." + component.name + ".success.single", amount, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.experience.set." + component.name + ".success.multiple", amount, targets.size()), true);
         }

         return targets.size();
      }
   }

   static enum Component {
      POINTS("points", PlayerEntity::addExperience, (player, xp) -> {
         if (xp >= player.getNextLevelExperience()) {
            return false;
         } else {
            player.setExperiencePoints(xp);
            return true;
         }
      }, (player) -> {
         return MathHelper.floor(player.experienceProgress * (float)player.getNextLevelExperience());
      }),
      LEVELS("levels", ServerPlayerEntity::addExperienceLevels, (player, level) -> {
         player.setExperienceLevel(level);
         return true;
      }, (player) -> {
         return player.experienceLevel;
      });

      public final BiConsumer adder;
      public final BiPredicate setter;
      public final String name;
      final ToIntFunction getter;

      private Component(String name, BiConsumer adder, BiPredicate setter, ToIntFunction getter) {
         this.adder = adder;
         this.name = name;
         this.setter = setter;
         this.getter = getter;
      }

      // $FF: synthetic method
      private static Component[] method_36967() {
         return new Component[]{POINTS, LEVELS};
      }
   }
}
