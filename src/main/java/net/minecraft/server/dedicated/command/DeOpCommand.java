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
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class DeOpCommand {
   private static final SimpleCommandExceptionType ALREADY_DEOPPED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.deop.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("deop").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
         return CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getOpNames(), builder);
      }).executes((context) -> {
         return deop((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"));
      })));
   }

   private static int deop(ServerCommandSource source, Collection targets) throws CommandSyntaxException {
      PlayerManager lv = source.getServer().getPlayerManager();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (lv.isOperator(gameProfile)) {
            lv.removeFromOperators(gameProfile);
            ++i;
            source.sendFeedback(Text.translatable("commands.deop.success", ((GameProfile)targets.iterator().next()).getName()), true);
         }
      }

      if (i == 0) {
         throw ALREADY_DEOPPED_EXCEPTION.create();
      } else {
         source.getServer().kickNonWhitelistedPlayers(source);
         return i;
      }
   }
}
