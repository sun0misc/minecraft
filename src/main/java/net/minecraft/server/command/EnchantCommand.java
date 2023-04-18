package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class EnchantCommand {
   private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
      return Text.translatable("commands.enchant.failed.entity", entityName);
   });
   private static final DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
      return Text.translatable("commands.enchant.failed.itemless", entityName);
   });
   private static final DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType((itemName) -> {
      return Text.translatable("commands.enchant.failed.incompatible", itemName);
   });
   private static final Dynamic2CommandExceptionType FAILED_LEVEL_EXCEPTION = new Dynamic2CommandExceptionType((level, maxLevel) -> {
      return Text.translatable("commands.enchant.failed.level", level, maxLevel);
   });
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.enchant.failed"));

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess registryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("enchant").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)CommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getEnchantment(context, "enchantment"), 1);
      })).then(CommandManager.argument("level", IntegerArgumentType.integer(0)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), RegistryEntryArgumentType.getEnchantment(context, "enchantment"), IntegerArgumentType.getInteger(context, "level"));
      })))));
   }

   private static int execute(ServerCommandSource source, Collection targets, RegistryEntry enchantment, int level) throws CommandSyntaxException {
      Enchantment lv = (Enchantment)enchantment.value();
      if (level > lv.getMaxLevel()) {
         throw FAILED_LEVEL_EXCEPTION.create(level, lv.getMaxLevel());
      } else {
         int j = 0;
         Iterator var6 = targets.iterator();

         while(true) {
            while(true) {
               while(true) {
                  while(var6.hasNext()) {
                     Entity lv2 = (Entity)var6.next();
                     if (lv2 instanceof LivingEntity) {
                        LivingEntity lv3 = (LivingEntity)lv2;
                        ItemStack lv4 = lv3.getMainHandStack();
                        if (!lv4.isEmpty()) {
                           if (lv.isAcceptableItem(lv4) && EnchantmentHelper.isCompatible(EnchantmentHelper.get(lv4).keySet(), lv)) {
                              lv4.addEnchantment(lv, level);
                              ++j;
                           } else if (targets.size() == 1) {
                              throw FAILED_INCOMPATIBLE_EXCEPTION.create(lv4.getItem().getName(lv4).getString());
                           }
                        } else if (targets.size() == 1) {
                           throw FAILED_ITEMLESS_EXCEPTION.create(lv3.getName().getString());
                        }
                     } else if (targets.size() == 1) {
                        throw FAILED_ENTITY_EXCEPTION.create(lv2.getName().getString());
                     }
                  }

                  if (j == 0) {
                     throw FAILED_EXCEPTION.create();
                  }

                  if (targets.size() == 1) {
                     source.sendFeedback(Text.translatable("commands.enchant.success.single", lv.getName(level), ((Entity)targets.iterator().next()).getDisplayName()), true);
                  } else {
                     source.sendFeedback(Text.translatable("commands.enchant.success.multiple", lv.getName(level), targets.size()), true);
                  }

                  return j;
               }
            }
         }
      }
   }
}
