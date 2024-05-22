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

public class NbtByte
extends AbstractNbtNumber {
    private static final int SIZE = 9;
    public static final NbtType<NbtByte> TYPE = new NbtType.OfFixedSize<NbtByte>(){

        @Override
        public NbtByte read(DataInput dataInput, NbtSizeTracker arg) throws IOException {
            return NbtByte.of(1.readByte(dataInput, arg));
        }

        @Override
        public NbtScanner.Result doAccept(DataInput input, NbtScanner visitor, NbtSizeTracker tracker) throws IOException {
            return visitor.visitByte(1.readByte(input, tracker));
        }

        private static byte readByte(DataInput input, NbtSizeTracker tracker) throws IOException {
            tracker.add(9L);
            return input.readByte();
        }

        @Override
        public int getSizeInBytes() {
            return 1;
        }

        @Override
        public String getCrashReportName() {
            return "BYTE";
        }

        @Override
        public String getCommandFeedbackName() {
            return "TAG_Byte";
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
    public static final NbtByte ZERO = NbtByte.of((byte)0);
    public static final NbtByte ONE = NbtByte.of((byte)1);
    private final byte value;

    NbtByte(byte value) {
        this.value = value;
    }

    public static NbtByte of(byte value) {
        return Cache.VALUES[128 + value];
    }

    public static NbtByte of(boolean value) {
        return value ? ONE : ZERO;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        output.writeByte(this.value);
    }

    @Override
    public int getSizeInBytes() {
        return 9;
    }

    @Override
    public byte getType() {
        return NbtElement.BYTE_TYPE;
    }

    public NbtType<NbtByte> getNbtType() {
        return TYPE;
    }

    @Override
    public NbtByte copy() {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof NbtByte && this.value == ((NbtByte)o).value;
    }

    public int hashCode() {
        return this.value;
    }

    @Override
    public void accept(NbtElementVisitor visitor) {
        visitor.visitByte(this);
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
        return this.value;
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
        return visitor.visitByte(this.value);
    }

    @Override
    public /* synthetic */ NbtElement copy() {
        return this.copy();
    }

    static class Cache {
        static final NbtByte[] VALUES = new NbtByte[256];

        private Cache() {
        }

        static {
            for (int i = 0; i < VALUES.length; ++i) {
                Cache.VALUES[i] = new NbtByte((byte)(i - 128));
            }
        }
    }
}

