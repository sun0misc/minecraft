package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Iterator;
import java.util.List;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TeamMsgCommand {
   private static final Style STYLE;
   private static final SimpleCommandExceptionType NO_TEAM_EXCEPTION;

   public static void register(CommandDispatcher dispatcher) {
      LiteralCommandNode literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("teammsg").then(CommandManager.argument("message", MessageArgumentType.message()).executes((context) -> {
         ServerCommandSource lv = (ServerCommandSource)context.getSource();
         Entity lv2 = lv.getEntityOrThrow();
         Team lv3 = (Team)lv2.getScoreboardTeam();
         if (lv3 == null) {
            throw NO_TEAM_EXCEPTION.create();
         } else {
            List list = lv.getServer().getPlayerManager().getPlayerList().stream().filter((player) -> {
               return player == lv2 || player.getScoreboardTeam() == lv3;
            }).toList();
            if (!list.isEmpty()) {
               MessageArgumentType.getSignedMessage(context, "message", (message) -> {
                  execute(lv, lv2, lv3, list, message);
               });
            }

            return list.size();
         }
      })));
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("tm").redirect(literalCommandNode));
   }

   private static void execute(ServerCommandSource source, Entity entity, Team team, List recipients, SignedMessage message) {
      Text lv = team.getFormattedName().fillStyle(STYLE);
      MessageType.Parameters lv2 = MessageType.params(MessageType.TEAM_MSG_COMMAND_INCOMING, source).withTargetName(lv);
      MessageType.Parameters lv3 = MessageType.params(MessageType.TEAM_MSG_COMMAND_OUTGOING, source).withTargetName(lv);
      SentMessage lv4 = SentMessage.of(message);
      boolean bl = false;

      boolean bl2;
      for(Iterator var10 = recipients.iterator(); var10.hasNext(); bl |= bl2 && message.isFullyFiltered()) {
         ServerPlayerEntity lv5 = (ServerPlayerEntity)var10.next();
         MessageType.Parameters lv6 = lv5 == entity ? lv3 : lv2;
         bl2 = source.shouldFilterText(lv5);
         lv5.sendChatMessage(lv4, bl2, lv6);
      }

      if (bl) {
         source.sendMessage(PlayerManager.FILTERED_FULL_TEXT);
      }

   }

   static {
      STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
      NO_TEAM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.teammsg.failed.noteam"));
   }
}
