/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.dedicated.command;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.server.BanEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class BanListCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("banlist").requires(source -> source.hasPermissionLevel(3))).executes(context -> {
            PlayerManager lv = ((ServerCommandSource)context.getSource()).getServer().getPlayerManager();
            return BanListCommand.execute((ServerCommandSource)context.getSource(), Lists.newArrayList(Iterables.concat(lv.getUserBanList().values(), lv.getIpBanList().values())));
        })).then(CommandManager.literal("ips").executes(context -> BanListCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getIpBanList().values())))).then(CommandManager.literal("players").executes(context -> BanListCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getUserBanList().values()))));
    }

    private static int execute(ServerCommandSource source, Collection<? extends BanEntry<?>> targets) {
        if (targets.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("commands.banlist.none"), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.banlist.list", targets.size()), false);
            for (BanEntry<?> lv : targets) {
                source.sendFeedback(() -> Text.translatable("commands.banlist.entry", lv.toText(), lv.getSource(), lv.getReason()), false);
            }
        }
        return targets.size();
    }
}

