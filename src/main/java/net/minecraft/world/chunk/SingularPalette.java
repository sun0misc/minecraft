/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class SingularPalette<T>
implements Palette<T> {
    private final IndexedIterable<T> idList;
    @Nullable
    private T entry;
    private final PaletteResizeListener<T> listener;

    public SingularPalette(IndexedIterable<T> idList, PaletteResizeListener<T> listener, List<T> entries) {
        this.idList = idList;
        this.listener = listener;
        if (entries.size() > 0) {
            Validate.isTrue(entries.size() <= 1, "Can't initialize SingleValuePalette with %d values.", entries.size());
            this.entry = entries.get(0);
        }
    }

    public static <A> Palette<A> create(int bitSize, IndexedIterable<A> idList, PaletteResizeListener<A> listener, List<A> entries) {
        return new SingularPalette<A>(idList, listener, entries);
    }

    @Override
    public int index(T object) {
        if (this.entry == null || this.entry == object) {
            this.entry = object;
            return 0;
        }
        return this.listener.onResize(1, object);
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return predicate.test(this.entry);
    }

    @Override
    public T get(int id) {
        if (this.entry == null || id != 0) {
            throw new IllegalStateException("Missing Palette entry for id " + id + ".");
        }
        return this.entry;
    }

    @Override
    public void readPacket(PacketByteBuf buf) {
        this.entry = this.idList.getOrThrow(buf.readVarInt());
    }

    @Override
    public void writePacket(PacketByteBuf buf) {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        buf.writeVarInt(this.idList.getRawId(this.entry));
    }

    @Override
    public int getPacketSize() {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return VarInts.getSizeInBytes(this.idList.getRawId(this.entry));
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Palette<T> copy() {
        if (this.entry == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return this;
    }
}

