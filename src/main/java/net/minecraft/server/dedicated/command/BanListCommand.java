package net.minecraft.server.dedicated.command;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.server.BanEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class BanListCommand {
   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("banlist").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).executes((context) -> {
         PlayerManager lv = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
         return execute((ServerCommandSource)context.getSource(), Lists.newArrayList(Iterables.concat(lv.getUserBanList().values(), lv.getIpBanList().values())));
      })).then(CommandManager.literal("ips").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getIpBanList().values());
      }))).then(CommandManager.literal("players").executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getUserBanList().values());
      })));
   }

   private static int execute(ServerCommandSource source, Collection targets) {
      if (targets.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.banlist.none"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.banlist.list", targets.size()), false);
         Iterator var2 = targets.iterator();

         while(var2.hasNext()) {
            BanEntry lv = (BanEntry)var2.next();
            source.sendFeedback(Text.translatable("commands.banlist.entry", lv.toText(), lv.getSource(), lv.getReason()), false);
         }
      }

      return targets.size();
   }
}
