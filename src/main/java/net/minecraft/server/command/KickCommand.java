package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class KickCommand {
   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("kick").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), Text.translatable("multiplayer.disconnect.kicked"));
      })).then(CommandManager.argument("reason", MessageArgumentType.message()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), MessageArgumentType.getMessage(context, "reason"));
      }))));
   }

   private static int execute(ServerCommandSource source, Collection targets, Text reason) {
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         ServerPlayerEntity lv = (ServerPlayerEntity)var3.next();
         lv.networkHandler.disconnect(reason);
         source.sendFeedback(Text.translatable("commands.kick.success", lv.getDisplayName(), reason), true);
      }

      return targets.size();
   }
}
