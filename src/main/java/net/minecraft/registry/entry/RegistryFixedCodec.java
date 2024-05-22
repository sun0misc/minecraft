/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class RegistryFixedCodec<E>
implements Codec<RegistryEntry<E>> {
    private final RegistryKey<? extends Registry<E>> registry;

    public static <E> RegistryFixedCodec<E> of(RegistryKey<? extends Registry<E>> registry) {
        return new RegistryFixedCodec<E>(registry);
    }

    private RegistryFixedCodec(RegistryKey<? extends Registry<E>> registry) {
        this.registry = registry;
    }

    @Override
    public <T> DataResult<T> encode(RegistryEntry<E> arg, DynamicOps<T> dynamicOps, T object) {
        RegistryOps lv;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (lv = (RegistryOps)dynamicOps).getOwner(this.registry)).isPresent()) {
            if (!arg.ownerEquals(optional.get())) {
                return DataResult.error(() -> "Element " + String.valueOf(arg) + " is not valid in current registry set");
            }
            return arg.getKeyOrValue().map(registryKey -> Identifier.CODEC.encode(registryKey.getValue(), dynamicOps, object), value -> DataResult.error(() -> "Elements from registry " + String.valueOf(this.registry) + " can't be serialized to a value"));
        }
        return DataResult.error(() -> "Can't access registry " + String.valueOf(this.registry));
    }

    @Override
    public <T> DataResult<Pair<RegistryEntry<E>, T>> decode(DynamicOps<T> ops, T input) {
        RegistryOps lv;
        Optional optional;
        if (ops instanceof RegistryOps && (optional = (lv = (RegistryOps)ops).getEntryLookup(this.registry)).isPresent()) {
            return Identifier.CODEC.decode(ops, input).flatMap((? super R pair) -> {
                Identifier lv = (Identifier)pair.getFirst();
                return ((RegistryEntryLookup)optional.get()).getOptional(RegistryKey.of(this.registry, lv)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + String.valueOf(lv))).map((? super R value) -> Pair.of(value, pair.getSecond())).setLifecycle(Lifecycle.stable());
            });
        }
        return DataResult.error(() -> "Can't access registry " + String.valueOf(this.registry));
    }

    public String toString() {
        return "RegistryFixedCodec[" + String.valueOf(this.registry) + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object entry, DynamicOps ops, Object prefix) {
        return this.encode((RegistryEntry)entry, ops, prefix);
    }
}

