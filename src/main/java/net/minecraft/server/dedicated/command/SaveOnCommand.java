package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Iterator;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class SaveOnCommand {
   private static final SimpleCommandExceptionType ALREADY_ON_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.save.alreadyOn"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("save-on").requires((source) -> {
         return source.hasPermissionLevel(4);
      })).executes((context) -> {
         ServerCommandSource lv = (ServerCommandSource)context.getSource();
         boolean bl = false;
         Iterator var3 = lv.getServer().getWorlds().iterator();

         while(var3.hasNext()) {
            ServerWorld lv2 = (ServerWorld)var3.next();
            if (lv2 != null && lv2.savingDisabled) {
               lv2.savingDisabled = false;
               bl = true;
            }
         }

         if (!bl) {
            throw ALREADY_ON_EXCEPTION.create();
         } else {
            lv.sendFeedback(Text.translatable("commands.save.enabled"), true);
            return 1;
         }
      }));
   }
}
