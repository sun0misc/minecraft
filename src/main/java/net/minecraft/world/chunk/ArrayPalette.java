/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.EntryMissingException;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.apache.commons.lang3.Validate;

public class ArrayPalette<T>
implements Palette<T> {
    private final IndexedIterable<T> idList;
    private final T[] array;
    private final PaletteResizeListener<T> listener;
    private final int indexBits;
    private int size;

    private ArrayPalette(IndexedIterable<T> idList, int bits, PaletteResizeListener<T> listener, List<T> list) {
        this.idList = idList;
        this.array = new Object[1 << bits];
        this.indexBits = bits;
        this.listener = listener;
        Validate.isTrue(list.size() <= this.array.length, "Can't initialize LinearPalette of size %d with %d entries", this.array.length, list.size());
        for (int j = 0; j < list.size(); ++j) {
            this.array[j] = list.get(j);
        }
        this.size = list.size();
    }

    private ArrayPalette(IndexedIterable<T> idList, T[] array, PaletteResizeListener<T> listener, int indexBits, int size) {
        this.idList = idList;
        this.array = array;
        this.listener = listener;
        this.indexBits = indexBits;
        this.size = size;
    }

    public static <A> Palette<A> create(int bits, IndexedIterable<A> idList, PaletteResizeListener<A> listener, List<A> list) {
        return new ArrayPalette<A>(idList, bits, listener, list);
    }

    @Override
    public int index(T object) {
        int i;
        for (i = 0; i < this.size; ++i) {
            if (this.array[i] != object) continue;
            return i;
        }
        if ((i = this.size++) < this.array.length) {
            this.array[i] = object;
            return i;
        }
        return this.listener.onResize(this.indexBits + 1, object);
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        for (int i = 0; i < this.size; ++i) {
            if (!predicate.test(this.array[i])) continue;
            return true;
        }
        return false;
    }

    @Override
    public T get(int id) {
        if (id >= 0 && id < this.size) {
            return this.array[id];
        }
        throw new EntryMissingException(id);
    }

    @Override
    public void readPacket(PacketByteBuf buf) {
        this.size = buf.readVarInt();
        for (int i = 0; i < this.size; ++i) {
            this.array[i] = this.idList.getOrThrow(buf.readVarInt());
        }
    }

    @Override
    public void writePacket(PacketByteBuf buf) {
        buf.writeVarInt(this.size);
        for (int i = 0; i < this.size; ++i) {
            buf.writeVarInt(this.idList.getRawId(this.array[i]));
        }
    }

    @Override
    public int getPacketSize() {
        int i = VarInts.getSizeInBytes(this.getSize());
        for (int j = 0; j < this.getSize(); ++j) {
            i += VarInts.getSizeInBytes(this.idList.getRawId(this.array[j]));
        }
        return i;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public Palette<T> copy() {
        return new ArrayPalette<Object>(this.idList, (Object[])this.array.clone(), this.listener, this.indexBits, this.size);
    }
}

