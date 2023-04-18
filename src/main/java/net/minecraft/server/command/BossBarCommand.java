package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

public class BossBarCommand {
   private static final DynamicCommandExceptionType CREATE_FAILED_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("commands.bossbar.create.failed", name);
   });
   private static final DynamicCommandExceptionType UNKNOWN_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("commands.bossbar.unknown", name);
   });
   private static final SimpleCommandExceptionType SET_PLAYERS_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.players.unchanged"));
   private static final SimpleCommandExceptionType SET_NAME_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.name.unchanged"));
   private static final SimpleCommandExceptionType SET_COLOR_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.color.unchanged"));
   private static final SimpleCommandExceptionType SET_STYLE_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.style.unchanged"));
   private static final SimpleCommandExceptionType SET_VALUE_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.value.unchanged"));
   private static final SimpleCommandExceptionType SET_MAX_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.max.unchanged"));
   private static final SimpleCommandExceptionType SET_VISIBILITY_UNCHANGED_HIDDEN_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.visibility.unchanged.hidden"));
   private static final SimpleCommandExceptionType SET_VISIBILITY_UNCHANGED_VISIBLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.bossbar.set.visibility.unchanged.visible"));
   public static final SuggestionProvider SUGGESTION_PROVIDER = (context, builder) -> {
      return CommandSource.suggestIdentifiers((Iterable)((ServerCommandSource)context.getSource()).getServer().getBossBarManager().getIds(), builder);
   };

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("bossbar").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("add").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).then(CommandManager.argument("name", TextArgumentType.text()).executes((context) -> {
         return addBossBar((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier(context, "id"), TextArgumentType.getTextArgument(context, "name"));
      }))))).then(CommandManager.literal("remove").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
         return removeBossBar((ServerCommandSource)context.getSource(), getBossBar(context));
      })))).then(CommandManager.literal("list").executes((context) -> {
         return listBossBars((ServerCommandSource)context.getSource());
      }))).then(CommandManager.literal("set").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then(CommandManager.literal("name").then(CommandManager.argument("name", TextArgumentType.text()).executes((context) -> {
         return setName((ServerCommandSource)context.getSource(), getBossBar(context), TextArgumentType.getTextArgument(context, "name"));
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("color").then(CommandManager.literal("pink").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.PINK);
      }))).then(CommandManager.literal("blue").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.BLUE);
      }))).then(CommandManager.literal("red").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.RED);
      }))).then(CommandManager.literal("green").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.GREEN);
      }))).then(CommandManager.literal("yellow").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.YELLOW);
      }))).then(CommandManager.literal("purple").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.PURPLE);
      }))).then(CommandManager.literal("white").executes((context) -> {
         return setColor((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Color.WHITE);
      })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("style").then(CommandManager.literal("progress").executes((context) -> {
         return setStyle((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Style.PROGRESS);
      }))).then(CommandManager.literal("notched_6").executes((context) -> {
         return setStyle((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Style.NOTCHED_6);
      }))).then(CommandManager.literal("notched_10").executes((context) -> {
         return setStyle((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Style.NOTCHED_10);
      }))).then(CommandManager.literal("notched_12").executes((context) -> {
         return setStyle((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Style.NOTCHED_12);
      }))).then(CommandManager.literal("notched_20").executes((context) -> {
         return setStyle((ServerCommandSource)context.getSource(), getBossBar(context), BossBar.Style.NOTCHED_20);
      })))).then(CommandManager.literal("value").then(CommandManager.argument("value", IntegerArgumentType.integer(0)).executes((context) -> {
         return setValue((ServerCommandSource)context.getSource(), getBossBar(context), IntegerArgumentType.getInteger(context, "value"));
      })))).then(CommandManager.literal("max").then(CommandManager.argument("max", IntegerArgumentType.integer(1)).executes((context) -> {
         return setMaxValue((ServerCommandSource)context.getSource(), getBossBar(context), IntegerArgumentType.getInteger(context, "max"));
      })))).then(CommandManager.literal("visible").then(CommandManager.argument("visible", BoolArgumentType.bool()).executes((context) -> {
         return setVisible((ServerCommandSource)context.getSource(), getBossBar(context), BoolArgumentType.getBool(context, "visible"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("players").executes((context) -> {
         return setPlayers((ServerCommandSource)context.getSource(), getBossBar(context), Collections.emptyList());
      })).then(CommandManager.argument("targets", EntityArgumentType.players()).executes((context) -> {
         return setPlayers((ServerCommandSource)context.getSource(), getBossBar(context), EntityArgumentType.getOptionalPlayers(context, "targets"));
      })))))).then(CommandManager.literal("get").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then(CommandManager.literal("value").executes((context) -> {
         return getValue((ServerCommandSource)context.getSource(), getBossBar(context));
      }))).then(CommandManager.literal("max").executes((context) -> {
         return getMaxValue((ServerCommandSource)context.getSource(), getBossBar(context));
      }))).then(CommandManager.literal("visible").executes((context) -> {
         return isVisible((ServerCommandSource)context.getSource(), getBossBar(context));
      }))).then(CommandManager.literal("players").executes((context) -> {
         return getPlayers((ServerCommandSource)context.getSource(), getBossBar(context));
      })))));
   }

   private static int getValue(ServerCommandSource source, CommandBossBar bossBar) {
      source.sendFeedback(Text.translatable("commands.bossbar.get.value", bossBar.toHoverableText(), bossBar.getValue()), true);
      return bossBar.getValue();
   }

   private static int getMaxValue(ServerCommandSource source, CommandBossBar bossBar) {
      source.sendFeedback(Text.translatable("commands.bossbar.get.max", bossBar.toHoverableText(), bossBar.getMaxValue()), true);
      return bossBar.getMaxValue();
   }

   private static int isVisible(ServerCommandSource source, CommandBossBar bossBar) {
      if (bossBar.isVisible()) {
         source.sendFeedback(Text.translatable("commands.bossbar.get.visible.visible", bossBar.toHoverableText()), true);
         return 1;
      } else {
         source.sendFeedback(Text.translatable("commands.bossbar.get.visible.hidden", bossBar.toHoverableText()), true);
         return 0;
      }
   }

   private static int getPlayers(ServerCommandSource source, CommandBossBar bossBar) {
      if (bossBar.getPlayers().isEmpty()) {
         source.sendFeedback(Text.translatable("commands.bossbar.get.players.none", bossBar.toHoverableText()), true);
      } else {
         source.sendFeedback(Text.translatable("commands.bossbar.get.players.some", bossBar.toHoverableText(), bossBar.getPlayers().size(), Texts.join(bossBar.getPlayers(), PlayerEntity::getDisplayName)), true);
      }

      return bossBar.getPlayers().size();
   }

   private static int setVisible(ServerCommandSource source, CommandBossBar bossBar, boolean visible) throws CommandSyntaxException {
      if (bossBar.isVisible() == visible) {
         if (visible) {
            throw SET_VISIBILITY_UNCHANGED_VISIBLE_EXCEPTION.create();
         } else {
            throw SET_VISIBILITY_UNCHANGED_HIDDEN_EXCEPTION.create();
         }
      } else {
         bossBar.setVisible(visible);
         if (visible) {
            source.sendFeedback(Text.translatable("commands.bossbar.set.visible.success.visible", bossBar.toHoverableText()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.bossbar.set.visible.success.hidden", bossBar.toHoverableText()), true);
         }

         return 0;
      }
   }

   private static int setValue(ServerCommandSource source, CommandBossBar bossBar, int value) throws CommandSyntaxException {
      if (bossBar.getValue() == value) {
         throw SET_VALUE_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setValue(value);
         source.sendFeedback(Text.translatable("commands.bossbar.set.value.success", bossBar.toHoverableText(), value), true);
         return value;
      }
   }

   private static int setMaxValue(ServerCommandSource source, CommandBossBar bossBar, int value) throws CommandSyntaxException {
      if (bossBar.getMaxValue() == value) {
         throw SET_MAX_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setMaxValue(value);
         source.sendFeedback(Text.translatable("commands.bossbar.set.max.success", bossBar.toHoverableText(), value), true);
         return value;
      }
   }

   private static int setColor(ServerCommandSource source, CommandBossBar bossBar, BossBar.Color color) throws CommandSyntaxException {
      if (bossBar.getColor().equals(color)) {
         throw SET_COLOR_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setColor(color);
         source.sendFeedback(Text.translatable("commands.bossbar.set.color.success", bossBar.toHoverableText()), true);
         return 0;
      }
   }

   private static int setStyle(ServerCommandSource source, CommandBossBar bossBar, BossBar.Style style) throws CommandSyntaxException {
      if (bossBar.getStyle().equals(style)) {
         throw SET_STYLE_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setStyle(style);
         source.sendFeedback(Text.translatable("commands.bossbar.set.style.success", bossBar.toHoverableText()), true);
         return 0;
      }
   }

   private static int setName(ServerCommandSource source, CommandBossBar bossBar, Text name) throws CommandSyntaxException {
      Text lv = Texts.parse(source, (Text)name, (Entity)null, 0);
      if (bossBar.getName().equals(lv)) {
         throw SET_NAME_UNCHANGED_EXCEPTION.create();
      } else {
         bossBar.setName(lv);
         source.sendFeedback(Text.translatable("commands.bossbar.set.name.success", bossBar.toHoverableText()), true);
         return 0;
      }
   }

   private static int setPlayers(ServerCommandSource source, CommandBossBar bossBar, Collection players) throws CommandSyntaxException {
      boolean bl = bossBar.addPlayers(players);
      if (!bl) {
         throw SET_PLAYERS_UNCHANGED_EXCEPTION.create();
      } else {
         if (bossBar.getPlayers().isEmpty()) {
            source.sendFeedback(Text.translatable("commands.bossbar.set.players.success.none", bossBar.toHoverableText()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.bossbar.set.players.success.some", bossBar.toHoverableText(), players.size(), Texts.join(players, PlayerEntity::getDisplayName)), true);
         }

         return bossBar.getPlayers().size();
      }
   }

   private static int listBossBars(ServerCommandSource source) {
      Collection collection = source.getServer().getBossBarManager().getAll();
      if (collection.isEmpty()) {
         source.sendFeedback(Text.translatable("commands.bossbar.list.bars.none"), false);
      } else {
         source.sendFeedback(Text.translatable("commands.bossbar.list.bars.some", collection.size(), Texts.join(collection, CommandBossBar::toHoverableText)), false);
      }

      return collection.size();
   }

   private static int addBossBar(ServerCommandSource source, Identifier name, Text displayName) throws CommandSyntaxException {
      BossBarManager lv = source.getServer().getBossBarManager();
      if (lv.get(name) != null) {
         throw CREATE_FAILED_EXCEPTION.create(name.toString());
      } else {
         CommandBossBar lv2 = lv.add(name, Texts.parse(source, (Text)displayName, (Entity)null, 0));
         source.sendFeedback(Text.translatable("commands.bossbar.create.success", lv2.toHoverableText()), true);
         return lv.getAll().size();
      }
   }

   private static int removeBossBar(ServerCommandSource source, CommandBossBar bossBar) {
      BossBarManager lv = source.getServer().getBossBarManager();
      bossBar.clearPlayers();
      lv.remove(bossBar);
      source.sendFeedback(Text.translatable("commands.bossbar.remove.success", bossBar.toHoverableText()), true);
      return lv.getAll().size();
   }

   public static CommandBossBar getBossBar(CommandContext context) throws CommandSyntaxException {
      Identifier lv = IdentifierArgumentType.getIdentifier(context, "id");
      CommandBossBar lv2 = ((ServerCommandSource)context.getSource()).getServer().getBossBarManager().get(lv);
      if (lv2 == null) {
         throw UNKNOWN_EXCEPTION.create(lv.toString());
      } else {
         return lv2;
      }
   }
}
