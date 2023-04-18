package net.minecraft.server.dedicated.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class PardonCommand {
   private static final SimpleCommandExceptionType ALREADY_UNBANNED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.pardon.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("pardon").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
         return CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getUserBanList().getNames(), builder);
      }).executes((context) -> {
         return pardon((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"));
      })));
   }

   private static int pardon(ServerCommandSource source, Collection targets) throws CommandSyntaxException {
      BannedPlayerList lv = source.getServer().getPlayerManager().getUserBanList();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (lv.contains(gameProfile)) {
            lv.remove(gameProfile);
            ++i;
            source.sendFeedback(Text.translatable("commands.pardon.success", Texts.toText(gameProfile)), true);
         }
      }

      if (i == 0) {
         throw ALREADY_UNBANNED_EXCEPTION.create();
      } else {
         return i;
      }
   }
}
