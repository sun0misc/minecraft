/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class SeedCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("seed").requires(source -> !dedicated || source.hasPermissionLevel(2))).executes(context -> {
            long l = ((ServerCommandSource)context.getSource()).getWorld().getSeed();
            MutableText lv = Texts.bracketedCopyable(String.valueOf(l));
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.seed.success", lv), false);
            return (int)l;
        }));
    }
}

