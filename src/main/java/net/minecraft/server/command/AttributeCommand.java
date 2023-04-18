package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.UUID;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class AttributeCommand {
   private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return Text.translatable("commands.attribute.failed.entity", name);
   });
   private static final Dynamic2CommandExceptionType NO_ATTRIBUTE_EXCEPTION = new Dynamic2CommandExceptionType((entityName, attributeName) -> {
      return Text.translatable("commands.attribute.failed.no_attribute", entityName, attributeName);
   });
   private static final Dynamic3CommandExceptionType NO_MODIFIER_EXCEPTION = new Dynamic3CommandExceptionType((entityName, attributeName, uuid) -> {
      return Text.translatable("commands.attribute.failed.no_modifier", attributeName, entityName, uuid);
   });
   private static final Dynamic3CommandExceptionType MODIFIER_ALREADY_PRESENT_EXCEPTION = new Dynamic3CommandExceptionType((entityName, attributeName, uuid) -> {
      return Text.translatable("commands.attribute.failed.modifier_already_present", uuid, attributeName, entityName);
   });

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess registryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("attribute").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("target", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE)).then(((LiteralArgumentBuilder)CommandManager.literal("get").executes((context) -> {
         return executeValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), 1.0);
      })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((context) -> {
         return executeValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), DoubleArgumentType.getDouble(context, "scale"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("base").then(CommandManager.literal("set").then(CommandManager.argument("value", DoubleArgumentType.doubleArg()).executes((context) -> {
         return executeBaseValueSet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), DoubleArgumentType.getDouble(context, "value"));
      })))).then(((LiteralArgumentBuilder)CommandManager.literal("get").executes((context) -> {
         return executeBaseValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), 1.0);
      })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((context) -> {
         return executeBaseValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), DoubleArgumentType.getDouble(context, "scale"));
      }))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("modifier").then(CommandManager.literal("add").then(CommandManager.argument("uuid", UuidArgumentType.uuid()).then(CommandManager.argument("name", StringArgumentType.string()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("value", DoubleArgumentType.doubleArg()).then(CommandManager.literal("add").executes((context) -> {
         return executeModifierAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), UuidArgumentType.getUuid(context, "uuid"), StringArgumentType.getString(context, "name"), DoubleArgumentType.getDouble(context, "value"), EntityAttributeModifier.Operation.ADDITION);
      }))).then(CommandManager.literal("multiply").executes((context) -> {
         return executeModifierAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), UuidArgumentType.getUuid(context, "uuid"), StringArgumentType.getString(context, "name"), DoubleArgumentType.getDouble(context, "value"), EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
      }))).then(CommandManager.literal("multiply_base").executes((context) -> {
         return executeModifierAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), UuidArgumentType.getUuid(context, "uuid"), StringArgumentType.getString(context, "name"), DoubleArgumentType.getDouble(context, "value"), EntityAttributeModifier.Operation.MULTIPLY_BASE);
      }))))))).then(CommandManager.literal("remove").then(CommandManager.argument("uuid", UuidArgumentType.uuid()).executes((context) -> {
         return executeModifierRemove((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), UuidArgumentType.getUuid(context, "uuid"));
      })))).then(CommandManager.literal("value").then(CommandManager.literal("get").then(((RequiredArgumentBuilder)CommandManager.argument("uuid", UuidArgumentType.uuid()).executes((context) -> {
         return executeModifierValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), UuidArgumentType.getUuid(context, "uuid"), 1.0);
      })).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes((context) -> {
         return executeModifierValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryArgumentType.getEntityAttribute(context, "attribute"), UuidArgumentType.getUuid(context, "uuid"), DoubleArgumentType.getDouble(context, "scale"));
      })))))))));
   }

   private static EntityAttributeInstance getAttributeInstance(Entity entity, RegistryEntry attribute) throws CommandSyntaxException {
      EntityAttributeInstance lv = getLivingEntity(entity).getAttributes().getCustomInstance(attribute);
      if (lv == null) {
         throw NO_ATTRIBUTE_EXCEPTION.create(entity.getName(), getName(attribute));
      } else {
         return lv;
      }
   }

   private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
      if (!(entity instanceof LivingEntity)) {
         throw ENTITY_FAILED_EXCEPTION.create(entity.getName());
      } else {
         return (LivingEntity)entity;
      }
   }

   private static LivingEntity getLivingEntityWithAttribute(Entity entity, RegistryEntry attribute) throws CommandSyntaxException {
      LivingEntity lv = getLivingEntity(entity);
      if (!lv.getAttributes().hasAttribute(attribute)) {
         throw NO_ATTRIBUTE_EXCEPTION.create(entity.getName(), getName(attribute));
      } else {
         return lv;
      }
   }

   private static int executeValueGet(ServerCommandSource source, Entity target, RegistryEntry attribute, double multiplier) throws CommandSyntaxException {
      LivingEntity lv = getLivingEntityWithAttribute(target, attribute);
      double e = lv.getAttributeValue(attribute);
      source.sendFeedback(Text.translatable("commands.attribute.value.get.success", getName(attribute), target.getName(), e), false);
      return (int)(e * multiplier);
   }

   private static int executeBaseValueGet(ServerCommandSource source, Entity target, RegistryEntry attribute, double multiplier) throws CommandSyntaxException {
      LivingEntity lv = getLivingEntityWithAttribute(target, attribute);
      double e = lv.getAttributeBaseValue(attribute);
      source.sendFeedback(Text.translatable("commands.attribute.base_value.get.success", getName(attribute), target.getName(), e), false);
      return (int)(e * multiplier);
   }

   private static int executeModifierValueGet(ServerCommandSource source, Entity target, RegistryEntry attribute, UUID uuid, double multiplier) throws CommandSyntaxException {
      LivingEntity lv = getLivingEntityWithAttribute(target, attribute);
      AttributeContainer lv2 = lv.getAttributes();
      if (!lv2.hasModifierForAttribute(attribute, uuid)) {
         throw NO_MODIFIER_EXCEPTION.create(target.getName(), getName(attribute), uuid);
      } else {
         double e = lv2.getModifierValue(attribute, uuid);
         source.sendFeedback(Text.translatable("commands.attribute.modifier.value.get.success", uuid, getName(attribute), target.getName(), e), false);
         return (int)(e * multiplier);
      }
   }

   private static int executeBaseValueSet(ServerCommandSource source, Entity target, RegistryEntry attribute, double value) throws CommandSyntaxException {
      getAttributeInstance(target, attribute).setBaseValue(value);
      source.sendFeedback(Text.translatable("commands.attribute.base_value.set.success", getName(attribute), target.getName(), value), false);
      return 1;
   }

   private static int executeModifierAdd(ServerCommandSource source, Entity target, RegistryEntry attribute, UUID uuid, String name, double value, EntityAttributeModifier.Operation operation) throws CommandSyntaxException {
      EntityAttributeInstance lv = getAttributeInstance(target, attribute);
      EntityAttributeModifier lv2 = new EntityAttributeModifier(uuid, name, value, operation);
      if (lv.hasModifier(lv2)) {
         throw MODIFIER_ALREADY_PRESENT_EXCEPTION.create(target.getName(), getName(attribute), uuid);
      } else {
         lv.addPersistentModifier(lv2);
         source.sendFeedback(Text.translatable("commands.attribute.modifier.add.success", uuid, getName(attribute), target.getName()), false);
         return 1;
      }
   }

   private static int executeModifierRemove(ServerCommandSource source, Entity target, RegistryEntry attribute, UUID uuid) throws CommandSyntaxException {
      EntityAttributeInstance lv = getAttributeInstance(target, attribute);
      if (lv.tryRemoveModifier(uuid)) {
         source.sendFeedback(Text.translatable("commands.attribute.modifier.remove.success", uuid, getName(attribute), target.getName()), false);
         return 1;
      } else {
         throw NO_MODIFIER_EXCEPTION.create(target.getName(), getName(attribute), uuid);
      }
   }

   private static Text getName(RegistryEntry attribute) {
      return Text.translatable(((EntityAttribute)attribute.value()).getTranslationKey());
   }
}
