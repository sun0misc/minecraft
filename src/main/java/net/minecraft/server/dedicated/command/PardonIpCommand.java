package net.minecraft.server.dedicated.command;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PardonIpCommand {
   private static final SimpleCommandExceptionType INVALID_IP_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.pardonip.invalid"));
   private static final SimpleCommandExceptionType ALREADY_UNBANNED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.pardonip.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("pardon-ip").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).then(CommandManager.argument("target", StringArgumentType.word()).suggests((context, builder) -> {
         return CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getIpBanList().getNames(), builder);
      }).executes((context) -> {
         return pardonIp((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "target"));
      })));
   }

   private static int pardonIp(ServerCommandSource source, String target) throws CommandSyntaxException {
      if (!InetAddresses.isInetAddress(target)) {
         throw INVALID_IP_EXCEPTION.create();
      } else {
         BannedIpList lv = source.getServer().getPlayerManager().getIpBanList();
         if (!lv.isBanned(target)) {
            throw ALREADY_UNBANNED_EXCEPTION.create();
         } else {
            lv.remove(target);
            source.sendFeedback(Text.translatable("commands.pardonip.success", target), true);
            return 1;
         }
      }
   }
}
