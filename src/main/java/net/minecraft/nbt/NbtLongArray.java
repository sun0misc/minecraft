/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class NbtLongArray
extends AbstractNbtList<NbtLong> {
    private static final int SIZE = 24;
    public static final NbtType<NbtLongArray> TYPE = new NbtType.OfVariableSize<NbtLongArray>(){

        @Override
        public NbtLongArray read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            return new NbtLongArray(1.readLongArray(dataInput, arg));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitLongArray(1.readLongArray(input, tracker));
        }

        private static long[] readLongArray(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(24L);
            int i = input.readInt();
            tracker.add(8L, i);
            long[] ls = new long[i];
            for (int j = 0; j < i; ++j) {
                ls[j] = input.readLong();
            }
            return ls;
        }

        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            input.skipBytes(input.readInt() * 8);
        }

        @Override
        public String getCrashReportName() {
            return "LONG[]";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Long_Array";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private long[] value;

    public NbtLongArray(long[] value) {
        this.value = value;
    }

    public NbtLongArray(LongSet value) {
        this.value = value.toLongArray();
    }

    public NbtLongArray(List<Long> value) {
        this(NbtLongArray.toArray(value));
    }

    private static long[] toArray(List<Long> list) {
        long[] ls = new long[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Long long_ = list.get(i);
            ls[i] = long_ == null ? 0L : long_;
        }
        return ls;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        for (long l : this.value) {
            output.writeLong(l);
        }
    }

    @Override
    public int getSizeInBytes() {
        return 24 + 8 * this.value.length;
    }

    @Override
    public byte getType() {
        return NbtElement.LONG_ARRAY_TYPE;
    }

    public NbtType<NbtLongArray> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.asString();
    }

    @Override
    public NbtLongArray copy() {
        long[] ls = new long[this.value.length];
        System.arraycopy(this.value, 0, ls, 0, this.value.length);
        return new NbtLongArray(ls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtLongArray && Arrays.equals(this.value, ((NbtLongArray)o).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitLongArray(this);
    }

    public long[] getLongArray() {
        return this.value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public NbtLong get(int i) {
        return NbtLong.of(this.value[i]);
    }

    public NbtLong method_10606(int i, NbtLong arg) {
        long l = this.value[i];
        this.value[i] = arg.longValue();
        return NbtLong.of(l);
    }

    @Override
    public void add(int i, NbtLong arg) {
        this.value = ArrayUtils.add(this.value, i, arg.longValue());
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            this.value[index] = ((AbstractNbtNumber)element).longValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            this.value = ArrayUtils.add(this.value, index, ((AbstractNbtNumber)element).longValue());
            return true;
        }
        return false;
    }

    @Override
    public NbtLong remove(int i) {
        long l = this.value[i];
        this.value = ArrayUtils.remove(this.value, i);
        return NbtLong.of(l);
    }

    @Override
    public byte getHeldType() {
        return NbtElement.LONG_TYPE;
    }

    @Override
    public void clear() {
        this.value = new long[0];
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        return visitor.visitLongArray(this.value);
    }

    @Override
    public /* synthetic */ NbtElement remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, NbtElement arg) {
        this.add(i, (NbtLong)arg);
    }

    @Override
    public /* synthetic */ NbtElement set(int i, NbtElement arg) {
        return this.method_10606(i, (NbtLong)arg);
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (NbtLong)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.method_10606(i, (NbtLong)object);
    }

    @Override
    public /* synthetic */ Object get(int index) {
        return this.get(index);
    }
}

