package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LootCommand {
   public static final SuggestionProvider SUGGESTION_PROVIDER = (context, builder) -> {
      LootManager lv = ((ServerCommandSource)context.getSource()).getServer().getLootManager();
      return CommandSource.suggestIdentifiers((Iterable)lv.getIds(LootDataType.LOOT_TABLES), builder);
   };
   private static final DynamicCommandExceptionType NO_HELD_ITEMS_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
      return Text.translatable("commands.drop.no_held_items", entityName);
   });
   private static final DynamicCommandExceptionType NO_LOOT_TABLE_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
      return Text.translatable("commands.drop.no_loot_table", entityName);
   });

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)addTargetArguments((LiteralArgumentBuilder)CommandManager.literal("loot").requires((source) -> {
         return source.hasPermissionLevel(2);
      }), (builder, constructor) -> {
         return builder.then(CommandManager.literal("fish").then(CommandManager.argument("loot_table", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) -> {
            return executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStack.EMPTY, constructor);
         })).then(CommandManager.argument("tool", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes((context) -> {
            return executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStackArgumentType.getItemStackArgument(context, "tool").createStack(1, false), constructor);
         }))).then(CommandManager.literal("mainhand").executes((context) -> {
            return executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.MAINHAND), constructor);
         }))).then(CommandManager.literal("offhand").executes((context) -> {
            return executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.OFFHAND), constructor);
         }))))).then(CommandManager.literal("loot").then(CommandManager.argument("loot_table", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes((context) -> {
            return executeLoot(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), constructor);
         }))).then(CommandManager.literal("kill").then(CommandManager.argument("target", EntityArgumentType.entity()).executes((context) -> {
            return executeKill(context, EntityArgumentType.getEntity(context, "target"), constructor);
         }))).then(CommandManager.literal("mine").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes((context) -> {
            return executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStack.EMPTY, constructor);
         })).then(CommandManager.argument("tool", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes((context) -> {
            return executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStackArgumentType.getItemStackArgument(context, "tool").createStack(1, false), constructor);
         }))).then(CommandManager.literal("mainhand").executes((context) -> {
            return executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.MAINHAND), constructor);
         }))).then(CommandManager.literal("offhand").executes((context) -> {
            return executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.OFFHAND), constructor);
         }))));
      }));
   }

   private static ArgumentBuilder addTargetArguments(ArgumentBuilder rootArgument, SourceConstructor sourceConstructor) {
      return rootArgument.then(((LiteralArgumentBuilder)CommandManager.literal("replace").then(CommandManager.literal("entity").then(CommandManager.argument("entities", EntityArgumentType.entities()).then(sourceConstructor.construct(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()), (context, stacks, messageSender) -> {
         return executeReplace(EntityArgumentType.getEntities(context, "entities"), ItemSlotArgumentType.getItemSlot(context, "slot"), stacks.size(), stacks, messageSender);
      }).then(sourceConstructor.construct(CommandManager.argument("count", IntegerArgumentType.integer(0)), (context, stacks, messageSender) -> {
         return executeReplace(EntityArgumentType.getEntities(context, "entities"), ItemSlotArgumentType.getItemSlot(context, "slot"), IntegerArgumentType.getInteger(context, "count"), stacks, messageSender);
      })))))).then(CommandManager.literal("block").then(CommandManager.argument("targetPos", BlockPosArgumentType.blockPos()).then(sourceConstructor.construct(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()), (context, stacks, messageSender) -> {
         return executeBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "targetPos"), ItemSlotArgumentType.getItemSlot(context, "slot"), stacks.size(), stacks, messageSender);
      }).then(sourceConstructor.construct(CommandManager.argument("count", IntegerArgumentType.integer(0)), (context, stacks, messageSender) -> {
         return executeBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "targetPos"), IntegerArgumentType.getInteger(context, "slot"), IntegerArgumentType.getInteger(context, "count"), stacks, messageSender);
      })))))).then(CommandManager.literal("insert").then(sourceConstructor.construct(CommandManager.argument("targetPos", BlockPosArgumentType.blockPos()), (context, stacks, messageSender) -> {
         return executeInsert((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "targetPos"), stacks, messageSender);
      }))).then(CommandManager.literal("give").then(sourceConstructor.construct(CommandManager.argument("players", EntityArgumentType.players()), (context, stacks, messageSender) -> {
         return executeGive(EntityArgumentType.getPlayers(context, "players"), stacks, messageSender);
      }))).then(CommandManager.literal("spawn").then(sourceConstructor.construct(CommandManager.argument("targetPos", Vec3ArgumentType.vec3()), (context, stacks, messageSender) -> {
         return executeSpawn((ServerCommandSource)context.getSource(), Vec3ArgumentType.getVec3(context, "targetPos"), stacks, messageSender);
      })));
   }

   private static Inventory getBlockInventory(ServerCommandSource source, BlockPos pos) throws CommandSyntaxException {
      BlockEntity lv = source.getWorld().getBlockEntity(pos);
      if (!(lv instanceof Inventory)) {
         throw ItemCommand.NOT_A_CONTAINER_TARGET_EXCEPTION.create(pos.getX(), pos.getY(), pos.getZ());
      } else {
         return (Inventory)lv;
      }
   }

   private static int executeInsert(ServerCommandSource source, BlockPos targetPos, List stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
      Inventory lv = getBlockInventory(source, targetPos);
      List list2 = Lists.newArrayListWithCapacity(stacks.size());
      Iterator var6 = stacks.iterator();

      while(var6.hasNext()) {
         ItemStack lv2 = (ItemStack)var6.next();
         if (insert(lv, lv2.copy())) {
            lv.markDirty();
            list2.add(lv2);
         }
      }

      messageSender.accept(list2);
      return list2.size();
   }

   private static boolean insert(Inventory inventory, ItemStack stack) {
      boolean bl = false;

      for(int i = 0; i < inventory.size() && !stack.isEmpty(); ++i) {
         ItemStack lv = inventory.getStack(i);
         if (inventory.isValid(i, stack)) {
            if (lv.isEmpty()) {
               inventory.setStack(i, stack);
               bl = true;
               break;
            }

            if (itemsMatch(lv, stack)) {
               int j = stack.getMaxCount() - lv.getCount();
               int k = Math.min(stack.getCount(), j);
               stack.decrement(k);
               lv.increment(k);
               bl = true;
            }
         }
      }

      return bl;
   }

   private static int executeBlock(ServerCommandSource source, BlockPos targetPos, int slot, int stackCount, List stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
      Inventory lv = getBlockInventory(source, targetPos);
      int k = lv.size();
      if (slot >= 0 && slot < k) {
         List list2 = Lists.newArrayListWithCapacity(stacks.size());

         for(int l = 0; l < stackCount; ++l) {
            int m = slot + l;
            ItemStack lv2 = l < stacks.size() ? (ItemStack)stacks.get(l) : ItemStack.EMPTY;
            if (lv.isValid(m, lv2)) {
               lv.setStack(m, lv2);
               list2.add(lv2);
            }
         }

         messageSender.accept(list2);
         return list2.size();
      } else {
         throw ItemCommand.NO_SUCH_SLOT_TARGET_EXCEPTION.create(slot);
      }
   }

   private static boolean itemsMatch(ItemStack first, ItemStack second) {
      return first.isOf(second.getItem()) && first.getDamage() == second.getDamage() && first.getCount() <= first.getMaxCount() && Objects.equals(first.getNbt(), second.getNbt());
   }

   private static int executeGive(Collection players, List stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
      List list2 = Lists.newArrayListWithCapacity(stacks.size());
      Iterator var4 = stacks.iterator();

      while(var4.hasNext()) {
         ItemStack lv = (ItemStack)var4.next();
         Iterator var6 = players.iterator();

         while(var6.hasNext()) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)var6.next();
            if (lv2.getInventory().insertStack(lv.copy())) {
               list2.add(lv);
            }
         }
      }

      messageSender.accept(list2);
      return list2.size();
   }

   private static void replace(Entity entity, List stacks, int slot, int stackCount, List addedStacks) {
      for(int k = 0; k < stackCount; ++k) {
         ItemStack lv = k < stacks.size() ? (ItemStack)stacks.get(k) : ItemStack.EMPTY;
         StackReference lv2 = entity.getStackReference(slot + k);
         if (lv2 != StackReference.EMPTY && lv2.set(lv.copy())) {
            addedStacks.add(lv);
         }
      }

   }

   private static int executeReplace(Collection targets, int slot, int stackCount, List stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
      List list2 = Lists.newArrayListWithCapacity(stacks.size());
      Iterator var6 = targets.iterator();

      while(var6.hasNext()) {
         Entity lv = (Entity)var6.next();
         if (lv instanceof ServerPlayerEntity lv2) {
            replace(lv, stacks, slot, stackCount, list2);
            lv2.currentScreenHandler.sendContentUpdates();
         } else {
            replace(lv, stacks, slot, stackCount, list2);
         }
      }

      messageSender.accept(list2);
      return list2.size();
   }

   private static int executeSpawn(ServerCommandSource source, Vec3d pos, List stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
      ServerWorld lv = source.getWorld();
      stacks.forEach((stack) -> {
         ItemEntity lvx = new ItemEntity(lv, pos.x, pos.y, pos.z, stack.copy());
         lvx.setToDefaultPickupDelay();
         lv.spawnEntity(lvx);
      });
      messageSender.accept(stacks);
      return stacks.size();
   }

   private static void sendDroppedFeedback(ServerCommandSource source, List stacks) {
      if (stacks.size() == 1) {
         ItemStack lv = (ItemStack)stacks.get(0);
         source.sendFeedback(Text.translatable("commands.drop.success.single", lv.getCount(), lv.toHoverableText()), false);
      } else {
         source.sendFeedback(Text.translatable("commands.drop.success.multiple", stacks.size()), false);
      }

   }

   private static void sendDroppedFeedback(ServerCommandSource source, List stacks, Identifier lootTable) {
      if (stacks.size() == 1) {
         ItemStack lv = (ItemStack)stacks.get(0);
         source.sendFeedback(Text.translatable("commands.drop.success.single_with_table", lv.getCount(), lv.toHoverableText(), lootTable), false);
      } else {
         source.sendFeedback(Text.translatable("commands.drop.success.multiple_with_table", stacks.size(), lootTable), false);
      }

   }

   private static ItemStack getHeldItem(ServerCommandSource source, EquipmentSlot slot) throws CommandSyntaxException {
      Entity lv = source.getEntityOrThrow();
      if (lv instanceof LivingEntity) {
         return ((LivingEntity)lv).getEquippedStack(slot);
      } else {
         throw NO_HELD_ITEMS_EXCEPTION.create(lv.getDisplayName());
      }
   }

   private static int executeMine(CommandContext context, BlockPos pos, ItemStack stack, Target constructor) throws CommandSyntaxException {
      ServerCommandSource lv = (ServerCommandSource)context.getSource();
      ServerWorld lv2 = lv.getWorld();
      BlockState lv3 = lv2.getBlockState(pos);
      BlockEntity lv4 = lv2.getBlockEntity(pos);
      LootContext.Builder lv5 = (new LootContext.Builder(lv2)).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.BLOCK_STATE, lv3).optionalParameter(LootContextParameters.BLOCK_ENTITY, lv4).optionalParameter(LootContextParameters.THIS_ENTITY, lv.getEntity()).parameter(LootContextParameters.TOOL, stack);
      List list = lv3.getDroppedStacks(lv5);
      return constructor.accept(context, list, (stacks) -> {
         sendDroppedFeedback(lv, stacks, lv3.getBlock().getLootTableId());
      });
   }

   private static int executeKill(CommandContext context, Entity entity, Target constructor) throws CommandSyntaxException {
      if (!(entity instanceof LivingEntity)) {
         throw NO_LOOT_TABLE_EXCEPTION.create(entity.getDisplayName());
      } else {
         Identifier lv = ((LivingEntity)entity).getLootTable();
         ServerCommandSource lv2 = (ServerCommandSource)context.getSource();
         LootContext.Builder lv3 = new LootContext.Builder(lv2.getWorld());
         Entity lv4 = lv2.getEntity();
         if (lv4 instanceof PlayerEntity) {
            lv3.parameter(LootContextParameters.LAST_DAMAGE_PLAYER, (PlayerEntity)lv4);
         }

         lv3.parameter(LootContextParameters.DAMAGE_SOURCE, entity.getDamageSources().magic());
         lv3.optionalParameter(LootContextParameters.DIRECT_KILLER_ENTITY, lv4);
         lv3.optionalParameter(LootContextParameters.KILLER_ENTITY, lv4);
         lv3.parameter(LootContextParameters.THIS_ENTITY, entity);
         lv3.parameter(LootContextParameters.ORIGIN, lv2.getPosition());
         LootTable lv5 = lv2.getServer().getLootManager().getLootTable(lv);
         List list = lv5.generateLoot(lv3.build(LootContextTypes.ENTITY));
         return constructor.accept(context, list, (stacks) -> {
            sendDroppedFeedback(lv2, stacks, lv);
         });
      }
   }

   private static int executeLoot(CommandContext context, Identifier lootTable, Target constructor) throws CommandSyntaxException {
      ServerCommandSource lv = (ServerCommandSource)context.getSource();
      LootContext.Builder lv2 = (new LootContext.Builder(lv.getWorld())).optionalParameter(LootContextParameters.THIS_ENTITY, lv.getEntity()).parameter(LootContextParameters.ORIGIN, lv.getPosition());
      return getFeedbackMessageSingle(context, lootTable, lv2.build(LootContextTypes.CHEST), constructor);
   }

   private static int executeFish(CommandContext context, Identifier lootTable, BlockPos pos, ItemStack stack, Target constructor) throws CommandSyntaxException {
      ServerCommandSource lv = (ServerCommandSource)context.getSource();
      LootContext lv2 = (new LootContext.Builder(lv.getWorld())).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, stack).optionalParameter(LootContextParameters.THIS_ENTITY, lv.getEntity()).build(LootContextTypes.FISHING);
      return getFeedbackMessageSingle(context, lootTable, lv2, constructor);
   }

   private static int getFeedbackMessageSingle(CommandContext context, Identifier lootTable, LootContext lootContext, Target constructor) throws CommandSyntaxException {
      ServerCommandSource lv = (ServerCommandSource)context.getSource();
      LootTable lv2 = lv.getServer().getLootManager().getLootTable(lootTable);
      List list = lv2.generateLoot(lootContext);
      return constructor.accept(context, list, (stacks) -> {
         sendDroppedFeedback(lv, stacks);
      });
   }

   @FunctionalInterface
   interface SourceConstructor {
      ArgumentBuilder construct(ArgumentBuilder builder, Target target);
   }

   @FunctionalInterface
   private interface Target {
      int accept(CommandContext context, List items, FeedbackMessage messageSender) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface FeedbackMessage {
      void accept(List items) throws CommandSyntaxException;
   }
}
