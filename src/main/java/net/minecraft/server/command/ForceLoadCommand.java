/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.World;

public class ForceLoadCommand {
    private static final int MAX_CHUNKS = 256;
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.forceload.toobig", maxCount, count));
    private static final Dynamic2CommandExceptionType QUERY_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((chunkPos, registryKey) -> Text.stringifiedTranslatable("commands.forceload.query.failure", chunkPos, registryKey));
    private static final SimpleCommandExceptionType ADDED_FAILURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.forceload.added.failure"));
    private static final SimpleCommandExceptionType REMOVED_FAILURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.forceload.removed.failure"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("forceload").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.literal("add").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("from", ColumnPosArgumentType.columnPos()).executes(context -> ForceLoadCommand.executeChange((ServerCommandSource)context.getSource(), ColumnPosArgumentType.getColumnPos(context, "from"), ColumnPosArgumentType.getColumnPos(context, "from"), true))).then(CommandManager.argument("to", ColumnPosArgumentType.columnPos()).executes(context -> ForceLoadCommand.executeChange((ServerCommandSource)context.getSource(), ColumnPosArgumentType.getColumnPos(context, "from"), ColumnPosArgumentType.getColumnPos(context, "to"), true)))))).then(((LiteralArgumentBuilder)CommandManager.literal("remove").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("from", ColumnPosArgumentType.columnPos()).executes(context -> ForceLoadCommand.executeChange((ServerCommandSource)context.getSource(), ColumnPosArgumentType.getColumnPos(context, "from"), ColumnPosArgumentType.getColumnPos(context, "from"), false))).then(CommandManager.argument("to", ColumnPosArgumentType.columnPos()).executes(context -> ForceLoadCommand.executeChange((ServerCommandSource)context.getSource(), ColumnPosArgumentType.getColumnPos(context, "from"), ColumnPosArgumentType.getColumnPos(context, "to"), false))))).then(CommandManager.literal("all").executes(context -> ForceLoadCommand.executeRemoveAll((ServerCommandSource)context.getSource()))))).then(((LiteralArgumentBuilder)CommandManager.literal("query").executes(context -> ForceLoadCommand.executeQuery((ServerCommandSource)context.getSource()))).then(CommandManager.argument("pos", ColumnPosArgumentType.columnPos()).executes(context -> ForceLoadCommand.executeQuery((ServerCommandSource)context.getSource(), ColumnPosArgumentType.getColumnPos(context, "pos"))))));
    }

    private static int executeQuery(ServerCommandSource source, ColumnPos pos) throws CommandSyntaxException {
        ChunkPos lv = pos.toChunkPos();
        ServerWorld lv2 = source.getWorld();
        RegistryKey<World> lv3 = lv2.getRegistryKey();
        boolean bl = lv2.getForcedChunks().contains(lv.toLong());
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.forceload.query.success", Text.of(lv), Text.of(lv3.getValue())), false);
            return 1;
        }
        throw QUERY_FAILURE_EXCEPTION.create(lv, lv3.getValue());
    }

    private static int executeQuery(ServerCommandSource source) {
        ServerWorld lv = source.getWorld();
        RegistryKey<World> lv2 = lv.getRegistryKey();
        LongSet longSet = lv.getForcedChunks();
        int i = longSet.size();
        if (i > 0) {
            String string = Joiner.on(", ").join(longSet.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
            if (i == 1) {
                source.sendFeedback(() -> Text.translatable("commands.forceload.list.single", Text.of(lv2.getValue()), string), false);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.forceload.list.multiple", i, Text.of(lv2.getValue()), string), false);
            }
        } else {
            source.sendError(Text.translatable("commands.forceload.added.none", Text.of(lv2.getValue())));
        }
        return i;
    }

    private static int executeRemoveAll(ServerCommandSource source) {
        ServerWorld lv = source.getWorld();
        RegistryKey<World> lv2 = lv.getRegistryKey();
        LongSet longSet = lv.getForcedChunks();
        longSet.forEach(chunkPos -> lv.setChunkForced(ChunkPos.getPackedX(chunkPos), ChunkPos.getPackedZ(chunkPos), false));
        source.sendFeedback(() -> Text.translatable("commands.forceload.removed.all", Text.of(lv2.getValue())), true);
        return 0;
    }

    private static int executeChange(ServerCommandSource source, ColumnPos from, ColumnPos to, boolean forceLoaded) throws CommandSyntaxException {
        int t;
        int p;
        int i = Math.min(from.x(), to.x());
        int j = Math.min(from.z(), to.z());
        int k = Math.max(from.x(), to.x());
        int l = Math.max(from.z(), to.z());
        if (i < -30000000 || j < -30000000 || k >= 30000000 || l >= 30000000) {
            throw BlockPosArgumentType.OUT_OF_WORLD_EXCEPTION.create();
        }
        int m = ChunkSectionPos.getSectionCoord(i);
        int n = ChunkSectionPos.getSectionCoord(j);
        int o = ChunkSectionPos.getSectionCoord(k);
        long q = ((long)(o - m) + 1L) * ((long)((p = ChunkSectionPos.getSectionCoord(l)) - n) + 1L);
        if (q > 256L) {
            throw TOO_BIG_EXCEPTION.create(256, q);
        }
        ServerWorld lv = source.getWorld();
        RegistryKey<World> lv2 = lv.getRegistryKey();
        ChunkPos lv3 = null;
        int r = 0;
        for (int s = m; s <= o; ++s) {
            for (t = n; t <= p; ++t) {
                boolean bl2 = lv.setChunkForced(s, t, forceLoaded);
                if (!bl2) continue;
                ++r;
                if (lv3 != null) continue;
                lv3 = new ChunkPos(s, t);
            }
        }
        ChunkPos lv4 = lv3;
        t = r;
        if (t == 0) {
            throw (forceLoaded ? ADDED_FAILURE_EXCEPTION : REMOVED_FAILURE_EXCEPTION).create();
        }
        if (t == 1) {
            source.sendFeedback(() -> Text.translatable("commands.forceload." + (forceLoaded ? "added" : "removed") + ".single", Text.of(lv4), Text.of(lv2.getValue())), true);
        } else {
            ChunkPos lv5 = new ChunkPos(m, n);
            ChunkPos lv6 = new ChunkPos(o, p);
            source.sendFeedback(() -> Text.translatable("commands.forceload." + (forceLoaded ? "added" : "removed") + ".multiple", t, Text.of(lv2.getValue()), Text.of(lv5), Text.of(lv6)), true);
        }
        return t;
    }
}

