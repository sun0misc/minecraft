/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.dedicated.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BanCommand {
    private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.ban.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ban").requires(source -> source.hasPermissionLevel(3))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).executes(context -> BanCommand.ban((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"), null))).then(CommandManager.argument("reason", MessageArgumentType.message()).executes(context -> BanCommand.ban((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"), MessageArgumentType.getMessage(context, "reason"))))));
    }

    private static int ban(ServerCommandSource source, Collection<GameProfile> targets, @Nullable Text reason) throws CommandSyntaxException {
        BannedPlayerList lv = source.getServer().getPlayerManager().getUserBanList();
        int i = 0;
        for (GameProfile gameProfile : targets) {
            if (lv.contains(gameProfile)) continue;
            BannedPlayerEntry lv2 = new BannedPlayerEntry(gameProfile, null, source.getName(), null, reason == null ? null : reason.getString());
            lv.add(lv2);
            ++i;
            source.sendFeedback(() -> Text.translatable("commands.ban.success", Text.literal(gameProfile.getName()), lv2.getReason()), true);
            ServerPlayerEntity lv3 = source.getServer().getPlayerManager().getPlayer(gameProfile.getId());
            if (lv3 == null) continue;
            lv3.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
        }
        if (i == 0) {
            throw ALREADY_BANNED_EXCEPTION.create();
        }
        return i;
    }
}

