/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class MeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("me").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("action", MessageArgumentType.message()).executes(context -> {
            MessageArgumentType.getSignedMessage(context, "action", message -> {
                ServerCommandSource lv = (ServerCommandSource)context.getSource();
                PlayerManager lv2 = lv.getServer().getPlayerManager();
                lv2.broadcast((SignedMessage)message, lv, MessageType.params(MessageType.EMOTE_COMMAND, lv));
            });
            return 1;
        })));
    }
}

