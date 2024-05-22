/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.PalettedContainer;

public interface ReadableContainer<T> {
    public T get(int var1, int var2, int var3);

    public void forEachValue(Consumer<T> var1);

    public void writePacket(PacketByteBuf var1);

    public int getPacketSize();

    public boolean hasAny(Predicate<T> var1);

    public void count(PalettedContainer.Counter<T> var1);

    public PalettedContainer<T> slice();

    public Serialized<T> serialize(IndexedIterable<T> var1, PalettedContainer.PaletteProvider var2);

    public static interface Reader<T, C extends ReadableContainer<T>> {
        public DataResult<C> read(IndexedIterable<T> var1, PalettedContainer.PaletteProvider var2, Serialized<T> var3);
    }

    public record Serialized<T>(List<T> paletteEntries, Optional<LongStream> storage) {
    }
}

