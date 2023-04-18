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

public class OpCommand {
   private static final SimpleCommandExceptionType ALREADY_OPPED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.op.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("op").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
         PlayerManager lv = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
         return CommandSource.suggestMatching(lv.getPlayerList().stream().filter((player) -> {
            return !lv.isOperator(player.getGameProfile());
         }).map((player) -> {
            return player.getGameProfile().getName();
         }), builder);
      }).executes((context) -> {
         return op((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"));
      })));
   }

   private static int op(ServerCommandSource source, Collection targets) throws CommandSyntaxException {
      PlayerManager lv = source.getServer().getPlayerManager();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (!lv.isOperator(gameProfile)) {
            lv.addToOperators(gameProfile);
            ++i;
            source.sendFeedback(Text.translatable("commands.op.success", ((GameProfile)targets.iterator().next()).getName()), true);
         }
      }

      if (i == 0) {
         throw ALREADY_OPPED_EXCEPTION.create();
      } else {
         return i;
      }
   }
}
