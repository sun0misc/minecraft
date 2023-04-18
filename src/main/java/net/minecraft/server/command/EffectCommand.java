package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class EffectCommand {
   private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.effect.give.failed"));
   private static final SimpleCommandExceptionType CLEAR_EVERYTHING_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.effect.clear.everything.failed"));
   private static final SimpleCommandExceptionType CLEAR_SPECIFIC_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.effect.clear.specific.failed"));

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess registryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("effect").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("clear").executes((context) -> {
         return executeClear((ServerCommandSource)context.getSource(), ImmutableList.of(((ServerCommandSource)context.getSource()).getEntityOrThrow()));
      })).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.entities()).executes((context) -> {
         return executeClear((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"));
      })).then(CommandManager.argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT)).executes((context) -> {
         return executeClear((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"));
      }))))).then(CommandManager.literal("give").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT)).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), (Integer)null, 0, true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("seconds", IntegerArgumentType.integer(1, 1000000)).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), IntegerArgumentType.getInteger(context, "seconds"), 0, true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), IntegerArgumentType.getInteger(context, "seconds"), IntegerArgumentType.getInteger(context, "amplifier"), true);
      })).then(CommandManager.argument("hideParticles", BoolArgumentType.bool()).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), IntegerArgumentType.getInteger(context, "seconds"), IntegerArgumentType.getInteger(context, "amplifier"), !BoolArgumentType.getBool(context, "hideParticles"));
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("infinite").executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), -1, 0, true);
      })).then(((RequiredArgumentBuilder)CommandManager.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), -1, IntegerArgumentType.getInteger(context, "amplifier"), true);
      })).then(CommandManager.argument("hideParticles", BoolArgumentType.bool()).executes((context) -> {
         return executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getStatusEffect(context, "effect"), -1, IntegerArgumentType.getInteger(context, "amplifier"), !BoolArgumentType.getBool(context, "hideParticles"));
      }))))))));
   }

   private static int executeGive(ServerCommandSource source, Collection targets, RegistryEntry statusEffect, @Nullable Integer seconds, int amplifier, boolean showParticles) throws CommandSyntaxException {
      StatusEffect lv = (StatusEffect)statusEffect.value();
      int j = 0;
      int k;
      if (seconds != null) {
         if (lv.isInstant()) {
            k = seconds;
         } else if (seconds == -1) {
            k = -1;
         } else {
            k = seconds * 20;
         }
      } else if (lv.isInstant()) {
         k = 1;
      } else {
         k = 600;
      }

      Iterator var9 = targets.iterator();

      while(var9.hasNext()) {
         Entity lv2 = (Entity)var9.next();
         if (lv2 instanceof LivingEntity) {
            StatusEffectInstance lv3 = new StatusEffectInstance(lv, k, amplifier, false, showParticles);
            if (((LivingEntity)lv2).addStatusEffect(lv3, source.getEntity())) {
               ++j;
            }
         }
      }

      if (j == 0) {
         throw GIVE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.effect.give.success.single", lv.getName(), ((Entity)targets.iterator().next()).getDisplayName(), k / 20), true);
         } else {
            source.sendFeedback(Text.translatable("commands.effect.give.success.multiple", lv.getName(), targets.size(), k / 20), true);
         }

         return j;
      }
   }

   private static int executeClear(ServerCommandSource source, Collection targets) throws CommandSyntaxException {
      int i = 0;
      Iterator var3 = targets.iterator();

      while(var3.hasNext()) {
         Entity lv = (Entity)var3.next();
         if (lv instanceof LivingEntity && ((LivingEntity)lv).clearStatusEffects()) {
            ++i;
         }
      }

      if (i == 0) {
         throw CLEAR_EVERYTHING_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.effect.clear.everything.success.single", ((Entity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.effect.clear.everything.success.multiple", targets.size()), true);
         }

         return i;
      }
   }

   private static int executeClear(ServerCommandSource source, Collection targets, RegistryEntry statusEffect) throws CommandSyntaxException {
      StatusEffect lv = (StatusEffect)statusEffect.value();
      int i = 0;
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         Entity lv2 = (Entity)var5.next();
         if (lv2 instanceof LivingEntity && ((LivingEntity)lv2).removeStatusEffect(lv)) {
            ++i;
         }
      }

      if (i == 0) {
         throw CLEAR_SPECIFIC_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.effect.clear.specific.success.single", lv.getName(), ((Entity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.effect.clear.specific.success.multiple", lv.getName(), targets.size()), true);
         }

         return i;
      }
   }
}
