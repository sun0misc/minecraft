package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.PlayerManager;

public class MeCommand {
   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("me").then(CommandManager.argument("action", MessageArgumentType.message()).executes((context) -> {
         MessageArgumentType.getSignedMessage(context, "action", (message) -> {
            ServerCommandSource lv = (ServerCommandSource)context.getSource();
            PlayerManager lv2 = lv.getServer().getPlayerManager();
            lv2.broadcast(message, lv, MessageType.params(MessageType.EMOTE_COMMAND, lv));
         });
         return 1;
      })));
   }
}
