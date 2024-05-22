/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class NbtByteArray
extends AbstractNbtList<NbtByte> {
    private static final int SIZE = 24;
    public static final NbtType<NbtByteArray> TYPE = new NbtType.OfVariableSize<NbtByteArray>(){

        @Override
        public NbtByteArray read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            return new NbtByteArray(1.readByteArray(dataInput, arg));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitByteArray(1.readByteArray(input, tracker));
        }

        private static byte[] readByteArray(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(24L);
            int i = input.readInt();
            tracker.add(1L, i);
            byte[] bs = new byte[i];
            input.readFully(bs);
            return bs;
        }

        @Override
        public void skip(DataInput input, NbtSizeTracker tracker) throws IOException {
            input.skipBytes(input.readInt() * 1);
        }

        @Override
        public String getCrashReportName() {
            return "BYTE[]";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private byte[] value;

    public NbtByteArray(byte[] value) {
        this.value = value;
    }

    public NbtByteArray(List<Byte> value) {
        this(NbtByteArray.toArray(value));
    }

    private static byte[] toArray(List<Byte> list) {
        byte[] bs = new byte[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Byte byte_ = list.get(i);
            bs[i] = byte_ == null ? (byte)0 : byte_;
        }
        return bs;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        output.write(this.value);
    }

    @Override
    public int getSizeInBytes() {
        return 24 + 1 * this.value.length;
    }

    @Override
    public byte getType() {
        return NbtElement.BYTE_ARRAY_TYPE;
    }

    public NbtType<NbtByteArray> getNbtType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.asString();
    }

    @Override
    public NbtElement copy() {
        byte[] bs = new byte[this.value.length];
        System.arraycopy(this.value, 0, bs, 0, this.value.length);
        return new NbtByteArray(bs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtByteArray && Arrays.equals(this.value, ((NbtByteArray)o).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitByteArray(this);
    }

    public byte[] getByteArray() {
        return this.value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public NbtByte get(int i) {
        return NbtByte.of(this.value[i]);
    }

    @Override
    public NbtByte set(int i, NbtByte arg) {
        byte b = this.value[i];
        this.value[i] = arg.byteValue();
        return NbtByte.of(b);
    }

    public void method_10531(int i, NbtByte arg) {
        this.value = ArrayUtils.add(this.value, i, arg.byteValue());
    }

    @Override
    public boolean setElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            this.value[index] = ((AbstractNbtNumber)element).byteValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addElement(int index, NbtElement element) {
        if (element instanceof AbstractNbtNumber) {
            this.value = ArrayUtils.add(this.value, index, ((AbstractNbtNumber)element).byteValue());
            return true;
        }
        return false;
    }

    public NbtByte method_10536(int i) {
        byte b = this.value[i];
        this.value = ArrayUtils.remove(this.value, i);
        return NbtByte.of(b);
    }

    @Override
    public byte getHeldType() {
        return NbtElement.BYTE_TYPE;
    }

    @Override
    public void clear() {
        this.value = new byte[0];
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        return visitor.visitByteArray(this.value);
    }

    @Override
    public /* synthetic */ NbtElement remove(int i) {
        return this.method_10536(i);
    }

    @Override
    public /* synthetic */ void add(int i, NbtElement arg) {
        this.method_10531(i, (NbtByte)arg);
    }

    @Override
    public /* synthetic */ NbtElement set(int i, NbtElement arg) {
        return this.set(i, (NbtByte)arg);
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.method_10536(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.method_10531(i, (NbtByte)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (NbtByte)object);
    }

    @Override
    public /* synthetic */ Object get(int index) {
        return this.get(index);
    }
}

