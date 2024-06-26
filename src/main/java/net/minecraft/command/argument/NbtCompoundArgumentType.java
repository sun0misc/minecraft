/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class NbtCompoundArgumentType
implements ArgumentType<NbtCompound> {
    private static final Collection<String> EXAMPLES = Arrays.asList("{}", "{foo=bar}");

    private NbtCompoundArgumentType() {
    }

    public static NbtCompoundArgumentType nbtCompound() {
        return new NbtCompoundArgumentType();
    }

    public static <S> NbtCompound getNbtCompound(CommandContext<S> context, String name) {
        return context.getArgument(name, NbtCompound.class);
    }

    @Override
    public NbtCompound parse(StringReader stringReader) throws CommandSyntaxException {
        return new StringNbtReader(stringReader).parseCompound();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

