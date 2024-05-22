/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.structure.Structure;

public class RegistryEntryReferenceArgumentType<T>
implements ArgumentType<RegistryEntry.Reference<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType NOT_SUMMONABLE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("entity.not_summonable", id));
    public static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((element, type) -> Text.stringifiedTranslatable("argument.resource.not_found", element, type));
    public static final Dynamic3CommandExceptionType INVALID_TYPE_EXCEPTION = new Dynamic3CommandExceptionType((element, type, expectedType) -> Text.stringifiedTranslatable("argument.resource.invalid_type", element, type, expectedType));
    final RegistryKey<? extends Registry<T>> registryRef;
    private final RegistryWrapper<T> registryWrapper;

    public RegistryEntryReferenceArgumentType(CommandRegistryAccess registryAccess, RegistryKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
        this.registryWrapper = registryAccess.getWrapperOrThrow(registryRef);
    }

    public static <T> RegistryEntryReferenceArgumentType<T> registryEntry(CommandRegistryAccess registryAccess, RegistryKey<? extends Registry<T>> registryRef) {
        return new RegistryEntryReferenceArgumentType<T>(registryAccess, registryRef);
    }

    public static <T> RegistryEntry.Reference<T> getRegistryEntry(CommandContext<ServerCommandSource> context, String name, RegistryKey<Registry<T>> registryRef) throws CommandSyntaxException {
        RegistryEntry.Reference lv = context.getArgument(name, RegistryEntry.Reference.class);
        RegistryKey lv2 = lv.registryKey();
        if (lv2.isOf(registryRef)) {
            return lv;
        }
        throw INVALID_TYPE_EXCEPTION.create(lv2.getValue(), lv2.getRegistry(), registryRef.getValue());
    }

    public static RegistryEntry.Reference<EntityAttribute> getEntityAttribute(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.ATTRIBUTE);
    }

    public static RegistryEntry.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.CONFIGURED_FEATURE);
    }

    public static RegistryEntry.Reference<Structure> getStructure(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.STRUCTURE);
    }

    public static RegistryEntry.Reference<EntityType<?>> getEntityType(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.ENTITY_TYPE);
    }

    public static RegistryEntry.Reference<EntityType<?>> getSummonableEntityType(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        RegistryEntry.Reference<EntityType<?>> lv = RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.ENTITY_TYPE);
        if (!((EntityType)lv.value()).isSummonable()) {
            throw NOT_SUMMONABLE_EXCEPTION.create(lv.registryKey().getValue().toString());
        }
        return lv;
    }

    public static RegistryEntry.Reference<StatusEffect> getStatusEffect(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.STATUS_EFFECT);
    }

    public static RegistryEntry.Reference<Enchantment> getEnchantment(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return RegistryEntryReferenceArgumentType.getRegistryEntry(context, name, RegistryKeys.ENCHANTMENT);
    }

    @Override
    public RegistryEntry.Reference<T> parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier lv = Identifier.fromCommandInput(stringReader);
        RegistryKey lv2 = RegistryKey.of(this.registryRef, lv);
        return this.registryWrapper.getOptional(lv2).orElseThrow(() -> NOT_FOUND_EXCEPTION.createWithContext(stringReader, lv, this.registryRef.getValue()));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(this.registryWrapper.streamKeys().map(RegistryKey::getValue), builder);
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
    implements ArgumentSerializer<RegistryEntryReferenceArgumentType<T>, Properties> {
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
        public Properties getArgumentTypeProperties(RegistryEntryReferenceArgumentType<T> arg) {
            return new Properties(arg.registryRef);
        }

        @Override
        public /* synthetic */ ArgumentSerializer.ArgumentTypeProperties fromPacket(PacketByteBuf buf) {
            return this.fromPacket(buf);
        }

        public final class Properties
        implements ArgumentSerializer.ArgumentTypeProperties<RegistryEntryReferenceArgumentType<T>> {
            final RegistryKey<? extends Registry<T>> registryRef;

            Properties(RegistryKey<? extends Registry<T>> registryRef) {
                this.registryRef = registryRef;
            }

            @Override
            public RegistryEntryReferenceArgumentType<T> createType(CommandRegistryAccess arg) {
                return new RegistryEntryReferenceArgumentType(arg, this.registryRef);
            }

            @Override
            public ArgumentSerializer<RegistryEntryReferenceArgumentType<T>, ?> getSerializer() {
                return Serializer.this;
            }

            @Override
            public /* synthetic */ ArgumentType createType(CommandRegistryAccess commandRegistryAccess) {
                return this.createType(commandRegistryAccess);
            }
        }
    }
}

