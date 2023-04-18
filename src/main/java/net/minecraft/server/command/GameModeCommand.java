package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class GameModeCommand {
   public static final int REQUIRED_PERMISSION_LEVEL = 2;

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("gamemode").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("gamemode", GameModeArgumentType.gameMode()).executes((commandContext) -> {
         return execute(commandContext, Collections.singleton(((ServerCommandSource)commandContext.getSource()).getPlayerOrThrow()), GameModeArgumentType.getGameMode(commandContext, "gamemode"));
      })).then(CommandManager.argument("target", EntityArgumentType.players()).executes((commandContext) -> {
         return execute(commandContext, EntityArgumentType.getPlayers(commandContext, "target"), GameModeArgumentType.getGameMode(commandContext, "gamemode"));
      }))));
   }

   private static void sendFeedback(ServerCommandSource source, ServerPlayerEntity player, GameMode gameMode) {
      Text lv = Text.translatable("gameMode." + gameMode.getName());
      if (source.getEntity() == player) {
         source.sendFeedback(Text.translatable("commands.gamemode.success.self", lv), true);
      } else {
         if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            player.sendMessage(Text.translatable("gameMode.changed", lv));
         }

         source.sendFeedback(Text.translatable("commands.gamemode.success.other", player.getDisplayName(), lv), true);
      }

   }

   private static int execute(CommandContext context, Collection targets, GameMode gameMode) {
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var4.next();
         if (lv.changeGameMode(gameMode)) {
            sendFeedback((ServerCommandSource)context.getSource(), lv, gameMode);
            ++i;
         }
      }

      return i;
   }
}
