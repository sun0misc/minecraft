package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class DamageCommand {
   private static final SimpleCommandExceptionType INVULNERABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.damage.invulnerable"));

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess registryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("damage").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("target", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)CommandManager.argument("amount", FloatArgumentType.floatArg(0.0F)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), ((ServerCommandSource)context.getSource()).getWorld().getDamageSources().generic());
      })).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("damageType", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.DAMAGE_TYPE)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE)));
      })).then(CommandManager.literal("at").then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), Vec3ArgumentType.getVec3(context, "location")));
      })))).then(CommandManager.literal("by").then(((RequiredArgumentBuilder)CommandManager.argument("entity", EntityArgumentType.entity()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity(context, "entity")));
      })).then(CommandManager.literal("from").then(CommandManager.argument("cause", EntityArgumentType.entity()).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "amount"), new DamageSource(RegistryEntryArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity(context, "entity"), EntityArgumentType.getEntity(context, "cause")));
      })))))))));
   }

   private static int execute(ServerCommandSource source, Entity target, float amount, DamageSource damageSource) throws CommandSyntaxException {
      if (target.damage(damageSource, amount)) {
         source.sendFeedback(Text.translatable("commands.damage.success", amount, target.getDisplayName()), true);
         return 1;
      } else {
         throw INVULNERABLE_EXCEPTION.create();
      }
   }
}
