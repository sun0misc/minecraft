package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.text.Text;

public class FunctionCommand {
   public static final SuggestionProvider SUGGESTION_PROVIDER = (context, builder) -> {
      CommandFunctionManager lv = ((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager();
      CommandSource.suggestIdentifiers(lv.getFunctionTags(), builder, "#");
      return CommandSource.suggestIdentifiers(lv.getAllFunctions(), builder);
   };

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("function").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("name", CommandFunctionArgumentType.commandFunction()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), CommandFunctionArgumentType.getFunctions(context, "name"));
      })));
   }

   private static int execute(ServerCommandSource source, Collection functions) {
      int i = 0;

      CommandFunction lv;
      for(Iterator var3 = functions.iterator(); var3.hasNext(); i += source.getServer().getCommandFunctionManager().execute(lv, source.withSilent().withMaxLevel(2))) {
         lv = (CommandFunction)var3.next();
      }

      if (functions.size() == 1) {
         source.sendFeedback(Text.translatable("commands.function.success.single", i, ((CommandFunction)functions.iterator().next()).getId()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.function.success.multiple", i, functions.size()), true);
      }

      return i;
   }
}
