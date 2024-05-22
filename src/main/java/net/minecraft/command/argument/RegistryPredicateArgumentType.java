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
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class RegistryPredicateArgumentType<T>
implements ArgumentType<RegistryPredicate<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    final RegistryKey<? extends Registry<T>> registryRef;

    public RegistryPredicateArgumentType(RegistryKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public static <T> RegistryPredicateArgumentType<T> registryPredicate(RegistryKey<? extends Registry<T>> registryRef) {
        return new RegistryPredicateArgumentType<T>(registryRef);
    }

    public static <T> RegistryPredicate<T> getPredicate(CommandContext<ServerCommandSource> context, String name, RegistryKey<Registry<T>> registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        RegistryPredicate lv = context.getArgument(name, RegistryPredicate.class);
        Optional<RegistryPredicate<T>> optional = lv.tryCast(registryRef);
        return optional.orElseThrow(() -> invalidException.create(lv));
    }

    @Override
    public RegistryPredicate<T> parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int i = stringReader.getCursor();
            try {
                stringReader.skip();
                Identifier lv = Identifier.fromCommandInput(stringReader);
                return new TagBased(TagKey.of(this.registryRef, lv));
            } catch (CommandSyntaxException commandSyntaxException) {
                stringReader.setCursor(i);
                throw commandSyntaxException;
            }
        }
        Identifier lv2 = Identifier.fromCommandInput(stringReader);
        return new RegistryKeyBased(RegistryKey.of(this.registryRef, lv2));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S s = context.getSource();
        if (s instanceof CommandSource) {
            CommandSource lv = (CommandSource)s;
            return lv.listIdSuggestions(this.registryRef, CommandSource.SuggestedIdType.ALL, builder, context);
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

    public static interface RegistryPredicate<T>
    extends Predicate<RegistryEntry<T>> {
        public Either<RegistryKey<T>, TagKey<T>> getKey();

        public <E> Optional<RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> var1);

        public String asString();
    }

    record TagBased<T>(TagKey<T> key) implements RegistryPredicate<T>
    {
        @Override
        public Either<RegistryKey<T>, TagKey<T>> getKey() {
            return Either.right(this.key);
        }

        @Override
        public <E> Optional<RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.key.tryCast(registryRef).map(TagBased::new);
        }

        @Override
        public boolean test(RegistryEntry<T> arg) {
            return arg.isIn(this.key);
        }

        @Override
        public String asString() {
            return "#" + String.valueOf(this.key.id());
        }

        @Override
        public /* synthetic */ boolean test(Object entry) {
            return this.test((RegistryEntry)entry);
        }
    }

    record RegistryKeyBased<T>(RegistryKey<T> key) implements RegistryPredicate<T>
    {
        @Override
        public Either<RegistryKey<T>, TagKey<T>> getKey() {
            return Either.left(this.key);
        }

        @Override
        public <E> Optional<RegistryPredicate<E>> tryCast(RegistryKey<? extends Registry<E>> registryRef) {
            return this.key.tryCast(registryRef).map(RegistryKeyBased::new);
        }

        @Override
        public boolean test(RegistryEntry<T> arg) {
            return arg.matchesKey(this.key);
        }

        @Override
        public String asString() {
            return this.key.getValue().toString();
        }

        @Override
        public /* synthetic */ boolean test(Object entry) {
            return this.test((RegistryEntry)entry);
        }
    }

    public static class Serializer<T>
    implements ArgumentSerializer<RegistryPredicateArgumentType<T>, Properties> {
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
        public Properties getArgumentTypeProperties(RegistryPredicateArgumentType<T> arg) {
            return new Properties(arg.registryRef);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<RegistryPredicateArgumentType<T>> {
            final RegistryKey<? extends Registry<T>> registryRef;

            Properties(RegistryKey<? extends Registry<T>> registryRef) {
                this.registryRef = registryRef;
            }

            @Override
            public RegistryPredicateArgumentType<T> createType(CommandRegistryAccess arg) {
                return new RegistryPredicateArgumentType(this.registryRef);
            }

            @Override
            public ArgumentSerializer<RegistryPredicateArgumentType<T>, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

