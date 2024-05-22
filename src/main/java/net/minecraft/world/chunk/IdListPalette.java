/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.EntryMissingException;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;

public class IdListPalette<T>
implements Palette<T> {
    private final IndexedIterable<T> idList;

    public IdListPalette(IndexedIterable<T> idList) {
        this.idList = idList;
    }

    public static <A> Palette<A> create(int bits, IndexedIterable<A> idList, PaletteResizeListener<A> listener, List<A> list) {
        return new IdListPalette<A>(idList);
    }

    @Override
    public int index(T object) {
        int i = this.idList.getRawId(object);
        return i == -1 ? 0 : i;
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        return true;
    }

    @Override
    public T get(int id) {
        T object = this.idList.get(id);
        if (object == null) {
            throw new EntryMissingException(id);
        }
        return object;
    }

    @Override
    public void readPacket(PacketByteBuf buf) {
    }

    @Override
    public void writePacket(PacketByteBuf buf) {
    }

    @Override
    public int getPacketSize() {
        return 0;
    }

    @Override
    public int getSize() {
        return this.idList.size();
    }

    @Override
    public Palette<T> copy() {
        return this;
    }
}

