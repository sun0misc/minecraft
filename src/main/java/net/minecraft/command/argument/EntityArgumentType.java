/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EntityArgumentType
implements ArgumentType<EntitySelector> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
    public static final SimpleCommandExceptionType TOO_MANY_ENTITIES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.toomany"));
    public static final SimpleCommandExceptionType TOO_MANY_PLAYERS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.player.toomany"));
    public static final SimpleCommandExceptionType PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.player.entities"));
    public static final SimpleCommandExceptionType ENTITY_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.notfound.entity"));
    public static final SimpleCommandExceptionType PLAYER_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.notfound.player"));
    public static final SimpleCommandExceptionType NOT_ALLOWED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.entity.selector.not_allowed"));
    final boolean singleTarget;
    final boolean playersOnly;

    protected EntityArgumentType(boolean singleTarget, boolean playersOnly) {
        this.singleTarget = singleTarget;
        this.playersOnly = playersOnly;
    }

    public static EntityArgumentType entity() {
        return new EntityArgumentType(true, false);
    }

    public static Entity getEntity(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, EntitySelector.class).getEntity(context.getSource());
    }

    public static EntityArgumentType entities() {
        return new EntityArgumentType(false, false);
    }

    public static Collection<? extends Entity> getEntities(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Collection<? extends Entity> collection = EntityArgumentType.getOptionalEntities(context, name);
        if (collection.isEmpty()) {
            throw ENTITY_NOT_FOUND_EXCEPTION.create();
        }
        return collection;
    }

    public static Collection<? extends Entity> getOptionalEntities(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, EntitySelector.class).getEntities(context.getSource());
    }

    public static Collection<ServerPlayerEntity> getOptionalPlayers(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, EntitySelector.class).getPlayers(context.getSource());
    }

    public static EntityArgumentType player() {
        return new EntityArgumentType(true, true);
    }

    public static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, EntitySelector.class).getPlayer(context.getSource());
    }

    public static EntityArgumentType players() {
        return new EntityArgumentType(false, true);
    }

    public static Collection<ServerPlayerEntity> getPlayers(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        List<ServerPlayerEntity> list = context.getArgument(name, EntitySelector.class).getPlayers(context.getSource());
        if (list.isEmpty()) {
            throw PLAYER_NOT_FOUND_EXCEPTION.create();
        }
        return list;
    }

    @Override
    public EntitySelector parse(StringReader stringReader) throws CommandSyntaxException {
        boolean i = false;
        EntitySelectorReader lv = new EntitySelectorReader(stringReader);
        EntitySelector lv2 = lv.read();
        if (lv2.getLimit() > 1 && this.singleTarget) {
            if (this.playersOnly) {
                stringReader.setCursor(0);
                throw TOO_MANY_PLAYERS_EXCEPTION.createWithContext(stringReader);
            }
            stringReader.setCursor(0);
            throw TOO_MANY_ENTITIES_EXCEPTION.createWithContext(stringReader);
        }
        if (lv2.includesNonPlayers() && this.playersOnly && !lv2.isSenderOnly()) {
            stringReader.setCursor(0);
            throw PLAYER_SELECTOR_HAS_ENTITIES_EXCEPTION.createWithContext(stringReader);
        }
        return lv2;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder2) {
        S s = context.getSource();
        if (s instanceof CommandSource) {
            CommandSource lv = (CommandSource)s;
            StringReader stringReader = new StringReader(builder2.getInput());
            stringReader.setCursor(builder2.getStart());
            EntitySelectorReader lv2 = new EntitySelectorReader(stringReader, lv.hasPermissionLevel(2));
            try {
                lv2.read();
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
            return lv2.listSuggestions(builder2, (SuggestionsBuilder builder) -> {
                Collection<String> collection = lv.getPlayerNames();
                Collection<String> iterable = this.playersOnly ? collection : Iterables.concat(collection, lv.getEntitySuggestions());
                CommandSource.suggestMatching(iterable, builder);
            });
        }
        return Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static class Serializer
    implements ArgumentSerializer<EntityArgumentType, Properties> {
        private static final byte SINGLE_FLAG = 1;
        private static final byte PLAYERS_ONLY_FLAG = 2;

        @Override
        public void writePacket(Properties arg, PacketByteBuf arg2) {
            int i = 0;
            if (arg.single) {
                i |= 1;
            }
            if (arg.playersOnly) {
                i |= 2;
            }
            arg2.writeByte(i);
        }

        @Override
        public Properties fromPacket(PacketByteBuf arg) {
            byte b = arg.readByte();
            return new Properties((b & 1) != 0, (b & 2) != 0);
        }

        @Override
        public void writeJson(Properties arg, JsonObject jsonObject) {
            jsonObject.addProperty("amount", arg.single ? "single" : "multiple");
            jsonObject.addProperty("type", arg.playersOnly ? "players" : "entities");
        }

        @Override
        public Properties getArgumentTypeProperties(EntityArgumentType arg) {
            return new Properties(arg.singleTarget, arg.playersOnly);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<EntityArgumentType> {
            final boolean single;
            final boolean playersOnly;

            Properties(boolean single, boolean playersOnly) {
                this.single = single;
                this.playersOnly = playersOnly;
            }

            @Override
            public EntityArgumentType createType(CommandRegistryAccess arg) {
                return new EntityArgumentType(this.single, this.playersOnly);
            }

            @Override
            public ArgumentSerializer<EntityArgumentType, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

