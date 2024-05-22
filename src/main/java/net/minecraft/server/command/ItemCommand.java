/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ItemCommand {
    static final Dynamic3CommandExceptionType NOT_A_CONTAINER_TARGET_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> Text.stringifiedTranslatable("commands.item.target.not_a_container", x, y, z));
    static final Dynamic3CommandExceptionType NOT_A_CONTAINER_SOURCE_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> Text.stringifiedTranslatable("commands.item.source.not_a_container", x, y, z));
    static final DynamicCommandExceptionType NO_SUCH_SLOT_TARGET_EXCEPTION = new DynamicCommandExceptionType(slot -> Text.stringifiedTranslatable("commands.item.target.no_such_slot", slot));
    private static final DynamicCommandExceptionType NO_SUCH_SLOT_SOURCE_EXCEPTION = new DynamicCommandExceptionType(slot -> Text.stringifiedTranslatable("commands.item.source.no_such_slot", slot));
    private static final DynamicCommandExceptionType NO_CHANGES_EXCEPTION = new DynamicCommandExceptionType(slot -> Text.stringifiedTranslatable("commands.item.target.no_changes", slot));
    private static final Dynamic2CommandExceptionType KNOWN_ITEM_EXCEPTION = new Dynamic2CommandExceptionType((itemName, slot) -> Text.stringifiedTranslatable("commands.item.target.no_changed.known_item", itemName, slot));
    private static final SuggestionProvider<ServerCommandSource> MODIFIER_SUGGESTION_PROVIDER = (context, builder) -> {
        ReloadableRegistries.Lookup lv = ((ServerCommandSource)context.getSource()).getServer().getReloadableRegistries();
        return CommandSource.suggestIdentifiers(lv.getIds(RegistryKeys.ITEM_MODIFIER), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("item").requires(source -> source.hasPermissionLevel(2))).then(((LiteralArgumentBuilder)CommandManager.literal("replace").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("block").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("with").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> ItemCommand.executeBlockReplace((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false)))).then(CommandManager.argument("count", IntegerArgumentType.integer(1, 99)).executes(context -> ItemCommand.executeBlockReplace((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), true))))))).then(((LiteralArgumentBuilder)CommandManager.literal("from").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("block").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeBlockCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).suggests(MODIFIER_SUGGESTION_PROVIDER).executes(context -> ItemCommand.executeBlockCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), RegistryEntryArgumentType.getLootFunction(context, "modifier")))))))).then(CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", EntityArgumentType.entity()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeBlockCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).suggests(MODIFIER_SUGGESTION_PROVIDER).executes(context -> ItemCommand.executeBlockCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), RegistryEntryArgumentType.getLootFunction(context, "modifier")))))))))))).then(CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("with").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> ItemCommand.executeEntityReplace((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false)))).then(CommandManager.argument("count", IntegerArgumentType.integer(1, 99)).executes(context -> ItemCommand.executeEntityReplace((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger(context, "count"), true))))))).then(((LiteralArgumentBuilder)CommandManager.literal("from").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("block").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeEntityCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).suggests(MODIFIER_SUGGESTION_PROVIDER).executes(context -> ItemCommand.executeEntityCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), RegistryEntryArgumentType.getLootFunction(context, "modifier")))))))).then(CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("source", EntityArgumentType.entity()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeEntityCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).suggests(MODIFIER_SUGGESTION_PROVIDER).executes(context -> ItemCommand.executeEntityCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "source"), ItemSlotArgumentType.getItemSlot(context, "sourceSlot"), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), RegistryEntryArgumentType.getLootFunction(context, "modifier"))))))))))))).then(((LiteralArgumentBuilder)CommandManager.literal("modify").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("block").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).suggests(MODIFIER_SUGGESTION_PROVIDER).executes(context -> ItemCommand.executeBlockModify((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemSlotArgumentType.getItemSlot(context, "slot"), RegistryEntryArgumentType.getLootFunction(context, "modifier")))))))).then(CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targets", EntityArgumentType.entities()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).suggests(MODIFIER_SUGGESTION_PROVIDER).executes(context -> ItemCommand.executeEntityModify((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities(context, "targets"), ItemSlotArgumentType.getItemSlot(context, "slot"), RegistryEntryArgumentType.getLootFunction(context, "modifier")))))))));
    }

    private static int executeBlockModify(ServerCommandSource source, BlockPos pos, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        Inventory lv = ItemCommand.getInventoryAtPos(source, pos, NOT_A_CONTAINER_TARGET_EXCEPTION);
        if (slot < 0 || slot >= lv.size()) {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(slot);
        }
        ItemStack lv2 = ItemCommand.getStackWithModifier(source, lootFunction, lv.getStack(slot));
        lv.setStack(slot, lv2);
        source.sendFeedback(() -> Text.translatable("commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), lv2.toHoverableText()), true);
        return 1;
    }

    private static int executeEntityModify(ServerCommandSource source, Collection<? extends Entity> targets, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        HashMap<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(targets.size());
        for (Entity entity : targets) {
            ItemStack lv3;
            StackReference lv2 = entity.getStackReference(slot);
            if (lv2 == StackReference.EMPTY || !lv2.set(lv3 = ItemCommand.getStackWithModifier(source, lootFunction, lv2.get().copy()))) continue;
            map.put(entity, lv3);
            if (!(entity instanceof ServerPlayerEntity)) continue;
            ((ServerPlayerEntity)entity).currentScreenHandler.sendContentUpdates();
        }
        if (map.isEmpty()) {
            throw NO_CHANGES_EXCEPTION.create(slot);
        }
        if (map.size() == 1) {
            Map.Entry entry = map.entrySet().iterator().next();
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).toHoverableText()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.multiple", map.size()), true);
        }
        return map.size();
    }

    private static int executeBlockReplace(ServerCommandSource source, BlockPos pos, int slot, ItemStack stack) throws CommandSyntaxException {
        Inventory lv = ItemCommand.getInventoryAtPos(source, pos, NOT_A_CONTAINER_TARGET_EXCEPTION);
        if (slot < 0 || slot >= lv.size()) {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(slot);
        }
        lv.setStack(slot, stack);
        source.sendFeedback(() -> Text.translatable("commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), stack.toHoverableText()), true);
        return 1;
    }

    static Inventory getInventoryAtPos(ServerCommandSource source, BlockPos pos, Dynamic3CommandExceptionType exception) throws CommandSyntaxException {
        BlockEntity lv = source.getWorld().getBlockEntity(pos);
        if (!(lv instanceof Inventory)) {
            throw exception.create(pos.getX(), pos.getY(), pos.getZ());
        }
        return (Inventory)((Object)lv);
    }

    private static int executeEntityReplace(ServerCommandSource source, Collection<? extends Entity> targets, int slot, ItemStack stack) throws CommandSyntaxException {
        ArrayList<Entity> list = Lists.newArrayListWithCapacity(targets.size());
        for (Entity entity : targets) {
            StackReference lv2 = entity.getStackReference(slot);
            if (lv2 == StackReference.EMPTY || !lv2.set(stack.copy())) continue;
            list.add(entity);
            if (!(entity instanceof ServerPlayerEntity)) continue;
            ((ServerPlayerEntity)entity).currentScreenHandler.sendContentUpdates();
        }
        if (list.isEmpty()) {
            throw KNOWN_ITEM_EXCEPTION.create(stack.toHoverableText(), slot);
        }
        if (list.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.single", ((Entity)list.iterator().next()).getDisplayName(), stack.toHoverableText()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.multiple", list.size(), stack.toHoverableText()), true);
        }
        return list.size();
    }

    private static int executeEntityCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, Collection<? extends Entity> targets, int slot) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot));
    }

    private static int executeEntityCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, Collection<? extends Entity> targets, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot)));
    }

    private static int executeBlockCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot));
    }

    private static int executeBlockCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot)));
    }

    private static int executeBlockCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackInSlot(sourceEntity, sourceSlot));
    }

    private static int executeBlockCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlot(sourceEntity, sourceSlot)));
    }

    private static int executeEntityCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, Collection<? extends Entity> targets, int slot) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackInSlot(sourceEntity, sourceSlot));
    }

    private static int executeEntityCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, Collection<? extends Entity> targets, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlot(sourceEntity, sourceSlot)));
    }

    private static ItemStack getStackWithModifier(ServerCommandSource source, RegistryEntry<LootFunction> lootFunction, ItemStack stack) {
        ServerWorld lv = source.getWorld();
        LootContextParameterSet lv2 = new LootContextParameterSet.Builder(lv).add(LootContextParameters.ORIGIN, source.getPosition()).addOptional(LootContextParameters.THIS_ENTITY, source.getEntity()).build(LootContextTypes.COMMAND);
        LootContext lv3 = new LootContext.Builder(lv2).build(Optional.empty());
        lv3.markActive(LootContext.itemModifier(lootFunction.value()));
        ItemStack lv4 = (ItemStack)lootFunction.value().apply(stack, lv3);
        lv4.capCount(lv4.getMaxCount());
        return lv4;
    }

    private static ItemStack getStackInSlot(Entity entity, int slotId) throws CommandSyntaxException {
        StackReference lv = entity.getStackReference(slotId);
        if (lv == StackReference.EMPTY) {
            throw NO_SUCH_SLOT_SOURCE_EXCEPTION.create(slotId);
        }
        return lv.get().copy();
    }

    private static ItemStack getStackInSlotFromInventoryAt(ServerCommandSource source, BlockPos pos, int slotId) throws CommandSyntaxException {
        Inventory lv = ItemCommand.getInventoryAtPos(source, pos, NOT_A_CONTAINER_SOURCE_EXCEPTION);
        if (slotId < 0 || slotId >= lv.size()) {
            throw NO_SUCH_SLOT_SOURCE_EXCEPTION.create(slotId);
        }
        return lv.getStack(slotId).copy();
    }
}

