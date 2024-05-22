/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.structure.Structure;

public class RegistryKeyArgumentType<T>
implements ArgumentType<RegistryKey<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType INVALID_FEATURE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.place.feature.invalid", id));
    private static final DynamicCommandExceptionType INVALID_STRUCTURE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.place.structure.invalid", id));
    private static final DynamicCommandExceptionType INVALID_JIGSAW_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.place.jigsaw.invalid", id));
    final RegistryKey<? extends Registry<T>> registryRef;

    public RegistryKeyArgumentType(RegistryKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public static <T> RegistryKeyArgumentType<T> registryKey(RegistryKey<? extends Registry<T>> registryRef) {
        return new RegistryKeyArgumentType<T>(registryRef);
    }

    private static <T> RegistryKey<T> getKey(CommandContext<ServerCommandSource> context, String name, RegistryKey<Registry<T>> registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        RegistryKey lv = context.getArgument(name, RegistryKey.class);
        Optional<RegistryKey<T>> optional = lv.tryCast(registryRef);
        return optional.orElseThrow(() -> invalidException.create(lv));
    }

    private static <T> Registry<T> getRegistry(CommandContext<ServerCommandSource> context, RegistryKey<? extends Registry<T>> registryRef) {
        return context.getSource().getServer().getRegistryManager().get(registryRef);
    }

    private static <T> RegistryEntry.Reference<T> getRegistryEntry(CommandContext<ServerCommandSource> context, String name, RegistryKey<Registry<T>> registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        RegistryKey lv = RegistryKeyArgumentType.getKey(context, name, registryRef, invalidException);
        return RegistryKeyArgumentType.getRegistry(context, registryRef).getEntry(lv).orElseThrow(() -> invalidException.create(lv.getValue()));
    }

    public static RegistryEntry.Reference<ConfiguredFeature<?, ?>> getConfiguredFeatureEntry(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryKeyArgumentType.getRegistryEntry(context, name, RegistryKeys.CONFIGURED_FEATURE, INVALID_FEATURE_EXCEPTION);
    }

    public static RegistryEntry.Reference<Structure> getStructureEntry(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryKeyArgumentType.getRegistryEntry(context, name, RegistryKeys.STRUCTURE, INVALID_STRUCTURE_EXCEPTION);
    }

    public static RegistryEntry.Reference<StructurePool> getStructurePoolEntry(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryKeyArgumentType.getRegistryEntry(context, name, RegistryKeys.TEMPLATE_POOL, INVALID_JIGSAW_EXCEPTION);
    }

    @Override
    public RegistryKey<T> parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier lv = Identifier.fromCommandInput(stringReader);
        return RegistryKey.of(this.registryRef, lv);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S s = context.getSource();
        if (s instanceof CommandSource) {
            CommandSource lv = (CommandSource)s;
            return lv.listIdSuggestions(this.registryRef, CommandSource.SuggestedIdType.ELEMENTS, builder, context);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static class Serializer<T>
    implements ArgumentSerializer<RegistryKeyArgumentType<T>, Properties> {
        @Override
        public void writePacket(Properties arg, PacketByteBuf arg2) {
            arg2.writeRegistryKey(arg.registryRef);
        }

        @Override
        public Properties fromPacket(PacketByteBuf arg) {
            return new Properties(arg.readRegistryRefKey());
        }

        @Override
        public void writeJson(Properties arg, JsonObject jsonObject) {
            jsonObject.addProperty("registry", arg.registryRef.getValue().toString());
        }

        @Override
        public Properties getArgumentTypeProperties(RegistryKeyArgumentType<T> arg) {
            return new Properties(arg.registryRef);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<RegistryKeyArgumentType<T>> {
            final RegistryKey<? extends Registry<T>> registryRef;

            Properties(RegistryKey<? extends Registry<T>> registryRef) {
                this.registryRef = registryRef;
            }

            @Override
            public RegistryKeyArgumentType<T> createType(CommandRegistryAccess arg) {
                return new RegistryKeyArgumentType(this.registryRef);
            }

            @Override
            public ArgumentSerializer<RegistryKeyArgumentType<T>, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

