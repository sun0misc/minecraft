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

public class NbtShort
extends AbstractNbtNumber {
    private static final int SIZE = 10;
    public static final NbtType<NbtShort> TYPE = new NbtType.OfFixedSize<NbtShort>(){

        @Override
        public NbtShort read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            return NbtShort.of(1.readShort(dataInput, arg));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitShort(1.readShort(input, tracker));
        }

        private static short readShort(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(10L);
            return input.readShort();
        }

        @Override
        public int getSizeInBytes() {
            return 2;
        }

        @Override
        public String getCrashReportName() {
            return "SHORT";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Short";
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
    private final short value;

    NbtShort(short value) {
        this.value = value;
    }

    public static NbtShort of(short value) {
        if (value >= -128 && value <= 1024) {
            return Cache.VALUES[value - -128];
        }
        return new NbtShort(value);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeShort(this.value);
    }

    @Override
    public int getSizeInBytes() {
        return 10;
    }

    @Override
    public byte getType() {
        return NbtElement.SHORT_TYPE;
    }

    public NbtType<NbtShort> getNbtType() {
        return TYPE;
    }

    @Override
    public NbtShort copy() {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtShort && this.value == ((NbtShort)o).value;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitShort(this);
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return this.value;
    }

    @Override
    public byte byteValue() {
        return (byte)(this.value & 0xFF);
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
        return visitor.visitShort(this.value);
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    static class Cache {
        private static final int MAX = 1024;
        private static final int MIN = -128;
        static final NbtShort[] VALUES = new NbtShort[1153];

        private Cache() {
        }

        static {
            for (int i = 0; i < VALUES.length; ++i) {
                Cache.VALUES[i] = new NbtShort((short)(-128 + i));
            }
        }
    }
}

