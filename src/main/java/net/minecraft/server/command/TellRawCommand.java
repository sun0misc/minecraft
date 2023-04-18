package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Iterator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class TellRawCommand {
   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("tellraw").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("message", TextArgumentType.text()).executes((context) -> {
         int i = 0;

         for(Iterator var2 = EntityArgumentType.getPlayers(context, "targets").iterator(); var2.hasNext(); ++i) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var2.next();
            lv.sendMessageToClient(Texts.parse((ServerCommandSource)context.getSource(), (Text)TextArgumentType.getTextArgument(context, "message"), lv, 0), false);
         }

         return i;
      }))));
   }
}
