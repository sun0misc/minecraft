/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.nbt.visitor.NbtElementVisitor;

public class NbtLong
extends AbstractNbtNumber {
    private static final int SIZE = 16;
    public static final NbtType<NbtLong> TYPE = new NbtType.OfFixedSize<NbtLong>(){

        @Override
        public NbtLong read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            return NbtLong.of(1.readLong(dataInput, arg));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitLong(1.readLong(input, tracker));
        }

        private static long readLong(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(16L);
            return input.readLong();
        }

        @Override
        public int getSizeInBytes() {
            return 8;
        }

        @Override
        public String getCrashReportName() {
            return "LONG";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Long";
        }

        @Override
        public boolean isImmutable() {
            return true;
        }

        @Override
        public /* synthetic */ NbtElement read(DataInput input, NbtSizeTracker tracker) throws IOException {
            return this.read(input, tracker);
        }
    };
    private final long value;

    NbtLong(long value) {
        this.value = value;
    }

    public static NbtLong of(long value) {
        if (value >= -128L && value <= 1024L) {
            return Cache.VALUES[(int)value - -128];
        }
        return new NbtLong(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeLong(this.value);
    }

    @Override
    public int getSizeInBytes() {
        return 16;
    }

    @Override
    public byte getType() {
        return NbtElement.LONG_TYPE;
    }

    public NbtType<NbtLong> getNbtType() {
        return TYPE;
    }

    @Override
    public NbtLong copy() {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtLong && this.value == ((NbtLong)o).value;
    }

    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitLong(this);
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return (int)(this.value & 0xFFFFFFFFFFFFFFFFL);
    }

    @Override
    public short shortValue() {
        return (short)(this.value & 0xFFFFL);
    }

    @Override
    public byte byteValue() {
        return (byte)(this.value & 0xFFL);
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public Number numberValue() {
        return this.value;
    }

    @Override
    public NbtScanner.Result doAccept(NbtScanner visitor) {
        return visitor.visitLong(this.value);
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    static class Cache {
        private static final int MAX = 1024;
        private static final int MIN = -128;
        static final NbtLong[] VALUES = new NbtLong[1153];

        private Cache() {
        }

        static {
            for (int i = 0; i < VALUES.length; ++i) {
                Cache.VALUES[i] = new NbtLong(-128 + i);
            }
        }
    }
}

