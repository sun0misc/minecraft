package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class GiveCommand {
   public static final int MAX_STACKS = 100;

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("give").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(CommandManager.argument("targets", EntityArgumentType.players()).then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), EntityArgumentType.getPlayers(context, "targets"), 1);
      })).then(CommandManager.argument("count", IntegerArgumentType.integer(1)).executes((context) -> {
         return execute((ServerCommandSource)context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "count"));
      })))));
   }

   private static int execute(ServerCommandSource source, ItemStackArgument item, Collection targets, int count) throws CommandSyntaxException {
      int j = item.getItem().getMaxCount();
      int k = j * 100;
      if (count > k) {
         source.sendError(Text.translatable("commands.give.failed.toomanyitems", k, item.createStack(count, false).toHoverableText()));
         return 0;
      } else {
         Iterator var6 = targets.iterator();

         label44:
         while(var6.hasNext()) {
            ServerPlayerEntity lv = (ServerPlayerEntity)var6.next();
            int l = count;

            while(true) {
               while(true) {
                  if (l <= 0) {
                     continue label44;
                  }

                  int m = Math.min(j, l);
                  l -= m;
                  ItemStack lv2 = item.createStack(m, false);
                  boolean bl = lv.getInventory().insertStack(lv2);
                  ItemEntity lv3;
                  if (bl && lv2.isEmpty()) {
                     lv2.setCount(1);
                     lv3 = lv.dropItem(lv2, false);
                     if (lv3 != null) {
                        lv3.setDespawnImmediately();
                     }

                     lv.world.playSound((PlayerEntity)null, lv.getX(), lv.getY(), lv.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((lv.getRandom().nextFloat() - lv.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                     lv.currentScreenHandler.sendContentUpdates();
                  } else {
                     lv3 = lv.dropItem(lv2, false);
                     if (lv3 != null) {
                        lv3.resetPickupDelay();
                        lv3.setOwner(lv.getUuid());
                     }
                  }
               }
            }
         }

         if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.give.success.single", count, item.createStack(count, false).toHoverableText(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.give.success.single", count, item.createStack(count, false).toHoverableText(), targets.size()), true);
         }

         return targets.size();
      }
   }
}
