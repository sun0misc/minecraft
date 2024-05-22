/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public record class_9791<T>(Optional<RegistryEntry<T>> holder, RegistryKey<T> key) {
    public class_9791(RegistryEntry<T> arg) {
        this(Optional.of(arg), arg.getKey().orElseThrow());
    }

    public class_9791(RegistryKey<T> arg) {
        this(Optional.empty(), arg);
    }

    public static <T> Codec<class_9791<T>> method_60736(RegistryKey<Registry<T>> arg2, Codec<RegistryEntry<T>> codec) {
        return Codec.either(codec, RegistryKey.createCodec(arg2).comapFlatMap(arg -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity())).xmap(class_9791::method_60738, class_9791::method_60734);
    }

    public static <T> PacketCodec<RegistryByteBuf, class_9791<T>> method_60737(RegistryKey<Registry<T>> arg, PacketCodec<RegistryByteBuf, RegistryEntry<T>> arg2) {
        return PacketCodec.tuple(PacketCodecs.either(arg2, RegistryKey.createPacketCodec(arg)), class_9791::method_60734, class_9791::method_60738);
    }

    public Either<RegistryEntry<T>, RegistryKey<T>> method_60734() {
        return this.holder.map(Either::left).orElseGet(() -> Either.right(this.key));
    }

    public static <T> class_9791<T> method_60738(Either<RegistryEntry<T>, RegistryKey<T>> either) {
        return either.map(class_9791::new, class_9791::new);
    }

    public Optional<T> method_60740(Registry<T> arg) {
        return this.holder.map(RegistryEntry::value).or(() -> arg.getOrEmpty(this.key));
    }

    public Optional<RegistryEntry<T>> method_60739(RegistryWrapper.WrapperLookup arg) {
        return this.holder.or(() -> arg.getWrapperOrThrow(this.key.getRegistryRef()).getOptional(this.key));
    }
}

