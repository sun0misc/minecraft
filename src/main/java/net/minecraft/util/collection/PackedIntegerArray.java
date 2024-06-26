/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.util.collection;

import java.util.function.IntConsumer;
import net.minecraft.util.collection.PaletteStorage;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class PackedIntegerArray
implements PaletteStorage {
    private static final int[] INDEX_PARAMETERS = new int[]{-1, -1, 0, Integer.MIN_VALUE, 0, 0, 0x55555555, 0x55555555, 0, Integer.MIN_VALUE, 0, 1, 0x33333333, 0x33333333, 0, 0x2AAAAAAA, 0x2AAAAAAA, 0, 0x24924924, 0x24924924, 0, Integer.MIN_VALUE, 0, 2, 0x1C71C71C, 0x1C71C71C, 0, 0x19999999, 0x19999999, 0, 390451572, 390451572, 0, 0x15555555, 0x15555555, 0, 0x13B13B13, 0x13B13B13, 0, 306783378, 306783378, 0, 0x11111111, 0x11111111, 0, Integer.MIN_VALUE, 0, 3, 0xF0F0F0F, 0xF0F0F0F, 0, 0xE38E38E, 0xE38E38E, 0, 226050910, 226050910, 0, 0xCCCCCCC, 0xCCCCCCC, 0, 0xC30C30C, 0xC30C30C, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 0xAAAAAAA, 0xAAAAAAA, 0, 171798691, 171798691, 0, 0x9D89D89, 0x9D89D89, 0, 159072862, 159072862, 0, 0x9249249, 0x9249249, 0, 148102320, 148102320, 0, 0x8888888, 0x8888888, 0, 138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 0x7878787, 0x7878787, 0, 0x7507507, 0x7507507, 0, 0x71C71C7, 0x71C71C7, 0, 116080197, 116080197, 0, 113025455, 113025455, 0, 0x6906906, 0x6906906, 0, 0x6666666, 0x6666666, 0, 104755299, 104755299, 0, 0x6186186, 0x6186186, 0, 99882960, 99882960, 0, 97612893, 97612893, 0, 0x5B05B05, 0x5B05B05, 0, 93368854, 93368854, 0, 91382282, 91382282, 0, 0x5555555, 0x5555555, 0, 87652393, 87652393, 0, 85899345, 85899345, 0, 0x5050505, 0x5050505, 0, 0x4EC4EC4, 0x4EC4EC4, 0, 81037118, 81037118, 0, 79536431, 79536431, 0, 78090314, 78090314, 0, 0x4924924, 0x4924924, 0, 75350303, 75350303, 0, 74051160, 74051160, 0, 72796055, 72796055, 0, 0x4444444, 0x4444444, 0, 70409299, 70409299, 0, 69273666, 69273666, 0, 0x4104104, 0x4104104, 0, Integer.MIN_VALUE, 0, 5};
    private final long[] data;
    private final int elementBits;
    private final long maxValue;
    private final int size;
    private final int elementsPerLong;
    private final int indexScale;
    private final int indexOffset;
    private final int indexShift;

    public PackedIntegerArray(int elementBits, int size, int[] data) {
        this(elementBits, size);
        int l;
        int k = 0;
        for (l = 0; l <= size - this.elementsPerLong; l += this.elementsPerLong) {
            long m = 0L;
            for (int n = this.elementsPerLong - 1; n >= 0; --n) {
                m <<= elementBits;
                m |= (long)data[l + n] & this.maxValue;
            }
            this.data[k++] = m;
        }
        int o = size - l;
        if (o > 0) {
            long p = 0L;
            for (int q = o - 1; q >= 0; --q) {
                p <<= elementBits;
                p |= (long)data[l + q] & this.maxValue;
            }
            this.data[k] = p;
        }
    }

    public PackedIntegerArray(int elementBits, int size) {
        this(elementBits, size, (long[])null);
    }

    public PackedIntegerArray(int elementBits, int size, @Nullable long[] data) {
        Validate.inclusiveBetween(1L, 32L, elementBits);
        this.size = size;
        this.elementBits = elementBits;
        this.maxValue = (1L << elementBits) - 1L;
        this.elementsPerLong = (char)(64 / elementBits);
        int k = 3 * (this.elementsPerLong - 1);
        this.indexScale = INDEX_PARAMETERS[k + 0];
        this.indexOffset = INDEX_PARAMETERS[k + 1];
        this.indexShift = INDEX_PARAMETERS[k + 2];
        int l = (size + this.elementsPerLong - 1) / this.elementsPerLong;
        if (data != null) {
            if (data.length != l) {
                throw new InvalidLengthException("Invalid length given for storage, got: " + data.length + " but expected: " + l);
            }
            this.data = data;
        } else {
            this.data = new long[l];
        }
    }

    private int getStorageIndex(int index) {
        long l = Integer.toUnsignedLong(this.indexScale);
        long m = Integer.toUnsignedLong(this.indexOffset);
        return (int)((long)index * l + m >> 32 >> this.indexShift);
    }

    @Override
    public int swap(int index, int value) {
        Validate.inclusiveBetween(0L, this.size - 1, index);
        Validate.inclusiveBetween(0L, this.maxValue, value);
        int k = this.getStorageIndex(index);
        long l = this.data[k];
        int m = (index - k * this.elementsPerLong) * this.elementBits;
        int n = (int)(l >> m & this.maxValue);
        this.data[k] = l & (this.maxValue << m ^ 0xFFFFFFFFFFFFFFFFL) | ((long)value & this.maxValue) << m;
        return n;
    }

    @Override
    public void set(int index, int value) {
        Validate.inclusiveBetween(0L, this.size - 1, index);
        Validate.inclusiveBetween(0L, this.maxValue, value);
        int k = this.getStorageIndex(index);
        long l = this.data[k];
        int m = (index - k * this.elementsPerLong) * this.elementBits;
        this.data[k] = l & (this.maxValue << m ^ 0xFFFFFFFFFFFFFFFFL) | ((long)value & this.maxValue) << m;
    }

    @Override
    public int get(int index) {
        Validate.inclusiveBetween(0L, this.size - 1, index);
        int j = this.getStorageIndex(index);
        long l = this.data[j];
        int k = (index - j * this.elementsPerLong) * this.elementBits;
        return (int)(l >> k & this.maxValue);
    }

    @Override
    public long[] getData() {
        return this.data;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getElementBits() {
        return this.elementBits;
    }

    @Override
    public void forEach(IntConsumer action) {
        int i = 0;
        for (long l : this.data) {
            for (int j = 0; j < this.elementsPerLong; ++j) {
                action.accept((int)(l & this.maxValue));
                l >>= this.elementBits;
                if (++i < this.size) continue;
                return;
            }
        }
    }

    @Override
    public void writePaletteIndices(int[] out) {
        int m;
        long l;
        int k;
        int i = this.data.length;
        int j = 0;
        for (k = 0; k < i - 1; ++k) {
            l = this.data[k];
            for (m = 0; m < this.elementsPerLong; ++m) {
                out[j + m] = (int)(l & this.maxValue);
                l >>= this.elementBits;
            }
            j += this.elementsPerLong;
        }
        k = this.size - j;
        if (k > 0) {
            l = this.data[i - 1];
            for (m = 0; m < k; ++m) {
                out[j + m] = (int)(l & this.maxValue);
                l >>= this.elementBits;
            }
        }
    }

    @Override
    public PaletteStorage copy() {
        return new PackedIntegerArray(this.elementBits, this.size, (long[])this.data.clone());
    }

    public static class InvalidLengthException
    extends RuntimeException {
        InvalidLengthException(String message) {
            super(message);
        }
    }
}

