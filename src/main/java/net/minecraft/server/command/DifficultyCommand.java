/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
    private static final DynamicCommandExceptionType FAILURE_EXCEPTION = new DynamicCommandExceptionType(difficulty -> Text.stringifiedTranslatable("commands.difficulty.failure", difficulty));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("difficulty");
        for (Difficulty lv : Difficulty.values()) {
            literalArgumentBuilder.then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal(lv.getName()).executes(context -> DifficultyCommand.execute((ServerCommandSource)context.getSource(), lv)));
        }
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.requires(source -> source.hasPermissionLevel(2))).executes(context -> {
            Difficulty lv = ((ServerCommandSource)context.getSource()).getWorld().getDifficulty();
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.difficulty.query", lv.getTranslatableName()), false);
            return lv.getId();
        }));
    }

    public static int execute(ServerCommandSource source, Difficulty difficulty) throws CommandSyntaxException {
        MinecraftServer minecraftServer = source.getServer();
        if (minecraftServer.getSaveProperties().getDifficulty() == difficulty) {
            throw FAILURE_EXCEPTION.create(difficulty.getName());
        }
        minecraftServer.setDifficulty(difficulty, true);
        source.sendFeedback(() -> Text.translatable("commands.difficulty.success", difficulty.getTranslatableName()), true);
        return 0;
    }
}

