/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ParticleEffectArgumentType
implements ArgumentType<ParticleEffect> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle{foo:bar}");
    public static final DynamicCommandExceptionType UNKNOWN_PARTICLE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("particle.notFound", id));
    public static final DynamicCommandExceptionType INVALID_OPTIONS_EXCEPTION = new DynamicCommandExceptionType(error -> Text.stringifiedTranslatable("particle.invalidOptions", error));
    private final RegistryWrapper.WrapperLookup registryLookup;

    public ParticleEffectArgumentType(CommandRegistryAccess registryAccess) {
        this.registryLookup = registryAccess;
    }

    public static ParticleEffectArgumentType particleEffect(CommandRegistryAccess registryAccess) {
        return new ParticleEffectArgumentType(registryAccess);
    }

    public static ParticleEffect getParticle(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, ParticleEffect.class);
    }

    @Override
    public ParticleEffect parse(StringReader stringReader) throws CommandSyntaxException {
        return ParticleEffectArgumentType.readParameters(stringReader, this.registryLookup);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static ParticleEffect readParameters(StringReader reader, RegistryWrapper.WrapperLookup registryLookup) throws CommandSyntaxException {
        ParticleType<?> lv = ParticleEffectArgumentType.getType(reader, registryLookup.getWrapperOrThrow(RegistryKeys.PARTICLE_TYPE));
        return ParticleEffectArgumentType.readParameters(reader, lv, registryLookup);
    }

    private static ParticleType<?> getType(StringReader reader, RegistryWrapper<ParticleType<?>> registryWrapper) throws CommandSyntaxException {
        Identifier lv = Identifier.fromCommandInput(reader);
        RegistryKey<ParticleType<?>> lv2 = RegistryKey.of(RegistryKeys.PARTICLE_TYPE, lv);
        return registryWrapper.getOptional(lv2).orElseThrow(() -> UNKNOWN_PARTICLE_EXCEPTION.createWithContext(reader, lv)).value();
    }

    private static <T extends ParticleEffect> T readParameters(StringReader reader, ParticleType<T> type, RegistryWrapper.WrapperLookup registryLookup) throws CommandSyntaxException {
        NbtCompound lv = reader.canRead() && reader.peek() == '{' ? new StringNbtReader(reader).parseCompound() : new NbtCompound();
        return (T)((ParticleEffect)type.getCodec().codec().parse(registryLookup.getOps(NbtOps.INSTANCE), lv).getOrThrow(INVALID_OPTIONS_EXCEPTION::create));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        RegistryWrapper.Impl<ParticleType<?>> lv = this.registryLookup.getWrapperOrThrow(RegistryKeys.PARTICLE_TYPE);
        return CommandSource.suggestIdentifiers(lv.streamKeys().map(RegistryKey::getValue), builder);
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

