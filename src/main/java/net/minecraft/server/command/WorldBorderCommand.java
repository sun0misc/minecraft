package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderCommand {
   private static final SimpleCommandExceptionType CENTER_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.center.failed"));
   private static final SimpleCommandExceptionType SET_FAILED_NO_CHANGE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.set.failed.nochange"));
   private static final SimpleCommandExceptionType SET_FAILED_SMALL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.set.failed.small"));
   private static final SimpleCommandExceptionType SET_FAILED_BIG_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.set.failed.big", 5.9999968E7));
   private static final SimpleCommandExceptionType SET_FAILED_FAR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.set.failed.far", 2.9999984E7));
   private static final SimpleCommandExceptionType WARNING_TIME_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.warning.time.failed"));
   private static final SimpleCommandExceptionType WARNING_DISTANCE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.warning.distance.failed"));
   private static final SimpleCommandExceptionType DAMAGE_BUFFER_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.damage.buffer.failed"));
   private static final SimpleCommandExceptionType DAMAGE_AMOUNT_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.worldborder.damage.amount.failed"));

   public static void register(CommandDispatcher dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("worldborder").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.literal("add").then(((RequiredArgumentBuilder)CommandManager.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7, 5.9999968E7)).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getWorld().getWorldBorder().getSize() + DoubleArgumentType.getDouble(context, "distance"), 0L);
      })).then(CommandManager.argument("time", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getWorld().getWorldBorder().getSize() + DoubleArgumentType.getDouble(context, "distance"), ((ServerCommandSource)context.getSource()).getWorld().getWorldBorder().getSizeLerpTime() + (long)IntegerArgumentType.getInteger(context, "time") * 1000L);
      }))))).then(CommandManager.literal("set").then(((RequiredArgumentBuilder)CommandManager.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7, 5.9999968E7)).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), DoubleArgumentType.getDouble(context, "distance"), 0L);
      })).then(CommandManager.argument("time", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeSet((ServerCommandSource)context.getSource(), DoubleArgumentType.getDouble(context, "distance"), (long)IntegerArgumentType.getInteger(context, "time") * 1000L);
      }))))).then(CommandManager.literal("center").then(CommandManager.argument("pos", Vec2ArgumentType.vec2()).executes((context) -> {
         return executeCenter((ServerCommandSource)context.getSource(), Vec2ArgumentType.getVec2(context, "pos"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("damage").then(CommandManager.literal("amount").then(CommandManager.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F)).executes((context) -> {
         return executeDamage((ServerCommandSource)context.getSource(), FloatArgumentType.getFloat(context, "damagePerBlock"));
      })))).then(CommandManager.literal("buffer").then(CommandManager.argument("distance", FloatArgumentType.floatArg(0.0F)).executes((context) -> {
         return executeBuffer((ServerCommandSource)context.getSource(), FloatArgumentType.getFloat(context, "distance"));
      }))))).then(CommandManager.literal("get").executes((context) -> {
         return executeGet((ServerCommandSource)context.getSource());
      }))).then(((LiteralArgumentBuilder)CommandManager.literal("warning").then(CommandManager.literal("distance").then(CommandManager.argument("distance", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeWarningDistance((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "distance"));
      })))).then(CommandManager.literal("time").then(CommandManager.argument("time", IntegerArgumentType.integer(0)).executes((context) -> {
         return executeWarningTime((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger(context, "time"));
      })))));
   }

   private static int executeBuffer(ServerCommandSource source, float distance) throws CommandSyntaxException {
      WorldBorder lv = source.getServer().getOverworld().getWorldBorder();
      if (lv.getSafeZone() == (double)distance) {
         throw DAMAGE_BUFFER_FAILED_EXCEPTION.create();
      } else {
         lv.setSafeZone((double)distance);
         source.sendFeedback(Text.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", distance)), true);
         return (int)distance;
      }
   }

   private static int executeDamage(ServerCommandSource source, float damagePerBlock) throws CommandSyntaxException {
      WorldBorder lv = source.getServer().getOverworld().getWorldBorder();
      if (lv.getDamagePerBlock() == (double)damagePerBlock) {
         throw DAMAGE_AMOUNT_FAILED_EXCEPTION.create();
      } else {
         lv.setDamagePerBlock((double)damagePerBlock);
         source.sendFeedback(Text.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", damagePerBlock)), true);
         return (int)damagePerBlock;
      }
   }

   private static int executeWarningTime(ServerCommandSource source, int time) throws CommandSyntaxException {
      WorldBorder lv = source.getServer().getOverworld().getWorldBorder();
      if (lv.getWarningTime() == time) {
         throw WARNING_TIME_FAILED_EXCEPTION.create();
      } else {
         lv.setWarningTime(time);
         source.sendFeedback(Text.translatable("commands.worldborder.warning.time.success", time), true);
         return time;
      }
   }

   private static int executeWarningDistance(ServerCommandSource source, int distance) throws CommandSyntaxException {
      WorldBorder lv = source.getServer().getOverworld().getWorldBorder();
      if (lv.getWarningBlocks() == distance) {
         throw WARNING_DISTANCE_FAILED_EXCEPTION.create();
      } else {
         lv.setWarningBlocks(distance);
         source.sendFeedback(Text.translatable("commands.worldborder.warning.distance.success", distance), true);
         return distance;
      }
   }

   private static int executeGet(ServerCommandSource source) {
      double d = source.getServer().getOverworld().getWorldBorder().getSize();
      source.sendFeedback(Text.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d)), false);
      return MathHelper.floor(d + 0.5);
   }

   private static int executeCenter(ServerCommandSource source, Vec2f pos) throws CommandSyntaxException {
      WorldBorder lv = source.getServer().getOverworld().getWorldBorder();
      if (lv.getCenterX() == (double)pos.x && lv.getCenterZ() == (double)pos.y) {
         throw CENTER_FAILED_EXCEPTION.create();
      } else if (!((double)Math.abs(pos.x) > 2.9999984E7) && !((double)Math.abs(pos.y) > 2.9999984E7)) {
         lv.setCenter((double)pos.x, (double)pos.y);
         source.sendFeedback(Text.translatable("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", pos.x), String.format(Locale.ROOT, "%.2f", pos.y)), true);
         return 0;
      } else {
         throw SET_FAILED_FAR_EXCEPTION.create();
      }
   }

   private static int executeSet(ServerCommandSource source, double distance, long time) throws CommandSyntaxException {
      WorldBorder lv = source.getServer().getOverworld().getWorldBorder();
      double e = lv.getSize();
      if (e == distance) {
         throw SET_FAILED_NO_CHANGE_EXCEPTION.create();
      } else if (distance < 1.0) {
         throw SET_FAILED_SMALL_EXCEPTION.create();
      } else if (distance > 5.9999968E7) {
         throw SET_FAILED_BIG_EXCEPTION.create();
      } else {
         if (time > 0L) {
            lv.interpolateSize(e, distance, time);
            if (distance > e) {
               source.sendFeedback(Text.translatable("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", distance), Long.toString(time / 1000L)), true);
            } else {
               source.sendFeedback(Text.translatable("commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", distance), Long.toString(time / 1000L)), true);
            }
         } else {
            lv.setSize(distance);
            source.sendFeedback(Text.translatable("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", distance)), true);
         }

         return (int)(distance - e);
      }
   }
}
