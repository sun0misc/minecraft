package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SaveAllCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.save.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("save-all").requires((source) -> {
         return source.hasPermissionLevel(4);
      })).executes((context) -> {
         return saveAll((ServerCommandSource)context.getSource(), false);
      })).then(CommandManager.literal("flush").executes((context) -> {
         return saveAll((ServerCommandSource)context.getSource(), true);
      })));
   }

   private static int saveAll(ServerCommandSource source, boolean flush) throws CommandSyntaxException {
      source.sendFeedback(Text.translatable("commands.save.saving"), false);
      MinecraftServer minecraftServer = source.getServer();
      boolean bl2 = minecraftServer.saveAll(true, flush, true);
      if (!bl2) {
         throw FAILED_EXCEPTION.create();
      } else {
         source.sendFeedback(Text.translatable("commands.save.success"), true);
         return 1;
      }
   }
}
