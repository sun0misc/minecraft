package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class SeedCommand {
   public static void register(CommandDispatcher dispatcher, boolean dedicated) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("seed").requires((source) -> {
         return !dedicated || source.hasPermissionLevel(2);
      })).executes((context) -> {
         long l = ((ServerCommandSource)context.getSource()).getWorld().getSeed();
         Text lv = Texts.bracketedCopyable(String.valueOf(l));
         ((ServerCommandSource)context.getSource()).sendFeedback(Text.translatable("commands.seed.success", lv), false);
         return (int)l;
      }));
   }
}
