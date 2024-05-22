/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TransferCommand {
    private static final SimpleCommandExceptionType NO_PLAYERS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.transfer.error.no_players"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("transfer").requires(source -> source.hasPermissionLevel(3))).then(((RequiredArgumentBuilder)CommandManager.argument("hostname", StringArgumentType.string()).executes(context -> TransferCommand.executeTransfer((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "hostname"), 25565, List.of(((ServerCommandSource)context.getSource()).getPlayerOrThrow())))).then(((RequiredArgumentBuilder)CommandManager.argument("port", IntegerArgumentType.integer(1, 65535)).executes(context -> TransferCommand.executeTransfer((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "hostname"), IntegerArgumentType.getInteger(context, "port"), List.of(((ServerCommandSource)context.getSource()).getPlayerOrThrow())))).then(CommandManager.argument("players", EntityArgumentType.players()).executes(context -> TransferCommand.executeTransfer((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "hostname"), IntegerArgumentType.getInteger(context, "port"), EntityArgumentType.getPlayers(context, "players")))))));
    }

    private static int executeTransfer(ServerCommandSource source, String host, int port, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        if (players.isEmpty()) {
            throw NO_PLAYERS_EXCEPTION.create();
        }
        for (ServerPlayerEntity lv : players) {
            lv.networkHandler.sendPacket(new ServerTransferS2CPacket(host, port));
        }
        if (players.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.transfer.success.single", ((ServerPlayerEntity)players.iterator().next()).getDisplayName(), host, port), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.transfer.success.multiple", players.size(), host, port), true);
        }
        return players.size();
    }
}

