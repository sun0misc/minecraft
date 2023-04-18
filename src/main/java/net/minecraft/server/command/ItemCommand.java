package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ItemCommand {
   static final Dynamic3CommandExceptionType NOT_A_CONTAINER_TARGET_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> {
      return Text.translatable("commands.item.target.not_a_container", x, y, z);
   });
   private static final Dynamic3CommandExceptionType NOT_A_CONTAINER_SOURCE_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> {
      return Text.translatable("commands.item.source.not_a_container", x, y, z);
   });
   static final DynamicCommandExceptionType NO_SUCH_SLOT_TARGET_EXCEPTION = new DynamicCommandExceptionType((slot) -> {
      return Text.translatable("commands.item.target.no_such_slot", slot);
   });
   private static final DynamicCommandExceptionType NO_SUCH_SLOT_SOURCE_EXCEPTION = new DynamicCommandExceptionType((slot) -> {
      return Text.translatable("commands.item.source.no_such_slot", slot);
   });
   private static final DynamicCommandExceptionType NO_CHANGES_EXCEPTION = new DynamicCommandExceptionType((slot) -> {
      return Text.translatable("commands.item.target.no_changes", slot);
   });
   private static final Dynamic2CommandExceptionType KNOWN_ITEM_EXCEPTION = new Dynamic2CommandExceptionType((itemName, slot) -> {
      return Text.translatable("commands.item.target.no_changed.known_item", itemName, slot);
   });
   private static final SuggestionProvider MODIFIER_SUGGESTION_PROVIDER = (context, builder) -> {
      LootManager lv = ((ServerCommandSource)context.getSource()).getServer().getLootManager();
      return CommandSource.suggestIdentifiers((Iterable)lv.getIds(LootDataType.ITEM_MODIFIERS), builder);
   };

   public static void register(CommandDispatcher dispatcher, CommandRegistryAccess commandRegistryAccess) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("item").requires((source) -> {
         return source.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)CommandManager.literal("replace").then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.literal("with").then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes((context) -> {
         return executeBlockReplace((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false));
      })).then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64)).executes((context) -> {
         return executeBlockReplace((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), true));
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("from").then(CommandManager.literal("block").then(CommandManager.argument("source", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes((context) -> {
         return executeBlockCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"));
      })).then(CommandManager.argument("modifier", IdentifierArgumentType.identifier()).suggests(MODIFIER_SUGGESTION_PROVIDER).executes((context) -> {
         return executeBlockCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), IdentifierArgumentType.getItemModifierArgument(context, "modifier"));
      })))))).then(CommandManager.literal("entity").then(CommandManager.argument("source", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes((context) -> {
         return executeBlockCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"));
      })).then(CommandManager.argument("modifier", IdentifierArgumentType.identifier()).suggests(MODIFIER_SUGGESTION_PROVIDER).executes((context) -> {
         return executeBlockCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), IdentifierArgumentType.getItemModifierArgument(context, "modifier"));
      })))))))))).then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.literal("with").then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes((context) -> {
         return executeEntityReplace((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false));
      })).then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64)).executes((context) -> {
         return executeEntityReplace((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), true));
      }))))).then(((LiteralArgumentBuilder)CommandManager.literal("from").then(CommandManager.literal("block").then(CommandManager.argument("source", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes((context) -> {
         return executeEntityCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"));
      })).then(CommandManager.argument("modifier", IdentifierArgumentType.identifier()).suggests(MODIFIER_SUGGESTION_PROVIDER).executes((context) -> {
         return executeEntityCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), IdentifierArgumentType.getItemModifierArgument(context, "modifier"));
      })))))).then(CommandManager.literal("entity").then(CommandManager.argument("source", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes((context) -> {
         return executeEntityCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"));
      })).then(CommandManager.argument("modifier", IdentifierArgumentType.identifier()).suggests(MODIFIER_SUGGESTION_PROVIDER).executes((context) -> {
         return executeEntityCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), IdentifierArgumentType.getItemModifierArgument(context, "modifier"));
      }))))))))))).then(((LiteralArgumentBuilder)CommandManager.literal("modify").then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.argument("modifier", IdentifierArgumentType.identifier()).suggests(MODIFIER_SUGGESTION_PROVIDER).executes((context) -> {
         return executeBlockModify((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), IdentifierArgumentType.getItemModifierArgument(context, "modifier"));
      })))))).then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.argument("modifier", IdentifierArgumentType.identifier()).suggests(MODIFIER_SUGGESTION_PROVIDER).executes((context) -> {
         return executeEntityModify((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), IdentifierArgumentType.getItemModifierArgument(context, "modifier"));
      })))))));
   }

   private static int executeBlockModify(ServerCommandSource source, BlockPos pos, int slot, LootFunction modifier) throws CommandSyntaxException {
      Inventory lv = getInventoryAtPos(source, pos, NOT_A_CONTAINER_TARGET_EXCEPTION);
      if (slot >= 0 && slot < lv.size()) {
         ItemStack lv2 = getStackWithModifier(source, modifier, lv.getStack(slot));
         lv.setStack(slot, lv2);
         source.sendFeedback(Text.translatable("commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), lv2.toHoverableText()), true);
         return 1;
      } else {
         throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(slot);
      }
   }

   private static int executeEntityModify(ServerCommandSource source, Collection targets, int slot, LootFunction modifier) throws CommandSyntaxException {
      Map map = Maps.newHashMapWithExpectedSize(targets.size());
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         Entity lv = (Entity)var5.next();
         StackReference lv2 = lv.getStackReference(slot);
         if (lv2 != StackReference.EMPTY) {
            ItemStack lv3 = getStackWithModifier(source, modifier, lv2.get().copy());
            if (lv2.set(lv3)) {
               map.put(lv, lv3);
               if (lv instanceof ServerPlayerEntity) {
                  ((ServerPlayerEntity)lv).currentScreenHandler.sendContentUpdates();
               }
            }
         }
      }

      if (map.isEmpty()) {
         throw NO_CHANGES_EXCEPTION.create(slot);
      } else {
         if (map.size() == 1) {
            Map.Entry entry = (Map.Entry)map.entrySet().iterator().next();
            source.sendFeedback(Text.translatable("commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).toHoverableText()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.item.entity.set.success.multiple", map.size()), true);
         }

         return map.size();
      }
   }

   private static int executeBlockReplace(ServerCommandSource source, BlockPos pos, int slot, ItemStack stack) throws CommandSyntaxException {
      Inventory lv = getInventoryAtPos(source, pos, NOT_A_CONTAINER_TARGET_EXCEPTION);
      if (slot >= 0 && slot < lv.size()) {
         lv.setStack(slot, stack);
         source.sendFeedback(Text.translatable("commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), stack.toHoverableText()), true);
         return 1;
      } else {
         throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(slot);
      }
   }

   private static Inventory getInventoryAtPos(ServerCommandSource source, BlockPos pos, Dynamic3CommandExceptionType exception) throws CommandSyntaxException {
      BlockEntity lv = source.getWorld().getBlockEntity(pos);
      if (!(lv instanceof Inventory)) {
         throw exception.create(pos.getX(), pos.getY(), pos.getZ());
      } else {
         return (Inventory)lv;
      }
   }

   private static int executeEntityReplace(ServerCommandSource source, Collection targets, int slot, ItemStack stack) throws CommandSyntaxException {
      List list = Lists.newArrayListWithCapacity(targets.size());
      Iterator var5 = targets.iterator();

      while(var5.hasNext()) {
         Entity lv = (Entity)var5.next();
         StackReference lv2 = lv.getStackReference(slot);
         if (lv2 != StackReference.EMPTY && lv2.set(stack.copy())) {
            list.add(lv);
            if (lv instanceof ServerPlayerEntity) {
               ((ServerPlayerEntity)lv).currentScreenHandler.sendContentUpdates();
            }
         }
      }

      if (list.isEmpty()) {
         throw KNOWN_ITEM_EXCEPTION.create(stack.toHoverableText(), slot);
      } else {
         if (list.size() == 1) {
            source.sendFeedback(Text.translatable("commands.item.entity.set.success.single", ((Entity)list.iterator().next()).getDisplayName(), stack.toHoverableText()), true);
         } else {
            source.sendFeedback(Text.translatable("commands.item.entity.set.success.multiple", list.size(), stack.toHoverableText()), true);
         }

         return list.size();
      }
   }

   private static int executeEntityCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, Collection targets, int slot) throws CommandSyntaxException {
      return executeEntityReplace(source, targets, slot, getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot));
   }

   private static int executeEntityCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, Collection targets, int slot, LootFunction modifier) throws CommandSyntaxException {
      return executeEntityReplace(source, targets, slot, getStackWithModifier(source, modifier, getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot)));
   }

   private static int executeBlockCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
      return executeBlockReplace(source, pos, slot, getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot));
   }

   private static int executeBlockCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot, LootFunction modifier) throws CommandSyntaxException {
      return executeBlockReplace(source, pos, slot, getStackWithModifier(source, modifier, getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot)));
   }

   private static int executeBlockCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
      return executeBlockReplace(source, pos, slot, getStackInSlot(sourceEntity, sourceSlot));
   }

   private static int executeBlockCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot, LootFunction modifier) throws CommandSyntaxException {
      return executeBlockReplace(source, pos, slot, getStackWithModifier(source, modifier, getStackInSlot(sourceEntity, sourceSlot)));
   }

   private static int executeEntityCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, Collection targets, int slot) throws CommandSyntaxException {
      return executeEntityReplace(source, targets, slot, getStackInSlot(sourceEntity, sourceSlot));
   }

   private static int executeEntityCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, Collection targets, int slot, LootFunction modifier) throws CommandSyntaxException {
      return executeEntityReplace(source, targets, slot, getStackWithModifier(source, modifier, getStackInSlot(sourceEntity, sourceSlot)));
   }

   private static ItemStack getStackWithModifier(ServerCommandSource source, LootFunction modifier, ItemStack stack) {
      ServerWorld lv = source.getWorld();
      LootContext lv2 = (new LootContext.Builder(lv)).parameter(LootContextParameters.ORIGIN, source.getPosition()).optionalParameter(LootContextParameters.THIS_ENTITY, source.getEntity()).build(LootContextTypes.COMMAND);
      lv2.markActive(LootContext.itemModifier(modifier));
      return (ItemStack)modifier.apply(stack, lv2);
   }

   private static ItemStack getStackInSlot(Entity entity, int slotId) throws CommandSyntaxException {
      StackReference lv = entity.getStackReference(slotId);
      if (lv == StackReference.EMPTY) {
         throw NO_SUCH_SLOT_SOURCE_EXCEPTION.create(slotId);
      } else {
         return lv.get().copy();
      }
   }

   private static ItemStack getStackInSlotFromInventoryAt(ServerCommandSource source, BlockPos pos, int slotId) throws CommandSyntaxException {
      Inventory lv = getInventoryAtPos(source, pos, NOT_A_CONTAINER_SOURCE_EXCEPTION);
      if (slotId >= 0 && slotId < lv.size()) {
         return lv.getStack(slotId).copy();
      } else {
         throw NO_SUCH_SLOT_SOURCE_EXCEPTION.create(slotId);
      }
   }
}
