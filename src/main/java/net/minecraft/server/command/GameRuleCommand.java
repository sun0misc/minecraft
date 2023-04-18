package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

public class GameRuleCommand {
   public static void register(CommandDispatcher dispatcher) {
      final LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("gamerule").requires((source) -> {
         return source.hasPermissionLevel(2);
      });
      GameRules.accept(new GameRules.Visitor() {
         public void visit(GameRules.Key key, GameRules.Type type) {
            literalArgumentBuilder.then(((LiteralArgumentBuilder)CommandManager.literal(key.getName()).executes((context) -> {
               return GameRuleCommand.executeQuery((ServerCommandSource)context.getSource(), key);
            })).then(type.argument("value").executes((context) -> {
               return GameRuleCommand.executeSet(context, key);
            })));
         }
      });
      dispatcher.register(literalArgumentBuilder);
   }

   static int executeSet(CommandContext context, GameRules.Key key) {
      ServerCommandSource lv = (ServerCommandSource)context.getSource();
      GameRules.Rule lv2 = lv.getServer().getGameRules().get(key);
      lv2.set(context, "value");
      lv.sendFeedback(Text.translatable("commands.gamerule.set", key.getName(), lv2.toString()), true);
      return lv2.getCommandResult();
   }

   static int executeQuery(ServerCommandSource source, GameRules.Key key) {
      GameRules.Rule lv = source.getServer().getGameRules().get(key);
      source.sendFeedback(Text.translatable("commands.gamerule.query", key.getName(), lv.toString()), false);
      return lv.getCommandResult();
   }
}
