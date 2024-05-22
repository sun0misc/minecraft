/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import java.io.IOException;
import java.io.InputStream;

public class FixedBufferInputStream
extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final InputStream stream;
    private final byte[] buf;
    private int end;
    private int start;

    public FixedBufferInputStream(InputStream stream) {
        this(stream, 8192);
    }

    public FixedBufferInputStream(InputStream stream, int size) {
        this.stream = stream;
        this.buf = new byte[size];
    }

    @Override
    public int read() throws IOException {
        if (this.start >= this.end) {
            this.fill();
            if (this.start >= this.end) {
                return -1;
            }
        }
        return Byte.toUnsignedInt(this.buf[this.start++]);
    }

    @Override
    public int read(byte[] buf, int offset, int length) throws IOException {
        int k = this.getAvailableBuffer();
        if (k <= 0) {
            if (length >= this.buf.length) {
                return this.stream.read(buf, offset, length);
            }
            this.fill();
            k = this.getAvailableBuffer();
            if (k <= 0) {
                return -1;
            }
        }
        if (length > k) {
            length = k;
        }
        System.arraycopy(this.buf, this.start, buf, offset, length);
        this.start += length;
        return length;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0L) {
            return 0L;
        }
        long m = this.getAvailableBuffer();
        if (m <= 0L) {
            return this.stream.skip(n);
        }
        if (n > m) {
            n = m;
        }
        this.start = (int)((long)this.start + n);
        return n;
    }

    @Override
    public int available() throws IOException {
        return this.getAvailableBuffer() + this.stream.available();
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    private int getAvailableBuffer() {
        return this.end - this.start;
    }

    private void fill() throws IOException {
        this.end = 0;
        this.start = 0;
        int i = this.stream.read(this.buf, 0, this.buf.length);
        if (i > 0) {
            this.end = i;
        }
    }
}

