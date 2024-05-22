/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class class_9799
implements AutoCloseable {
    private static final Logger field_52078 = LogUtils.getLogger();
    private static final MemoryUtil.MemoryAllocator field_52079 = MemoryUtil.getAllocator(false);
    private static final int field_52080 = 0x200000;
    private static final int field_52081 = -1;
    long field_52082;
    private int field_52083;
    private int field_52084;
    private int field_52085;
    private int field_52086;
    private int field_52087;

    public class_9799(int i) {
        this.field_52083 = i;
        this.field_52082 = field_52079.malloc(i);
        if (this.field_52082 == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + i + " bytes");
        }
    }

    public long method_60808(int i) {
        int j = this.field_52084;
        int k = j + i;
        this.method_60810(k);
        this.field_52084 = k;
        return this.field_52082 + (long)j;
    }

    private void method_60810(int i) {
        if (i > this.field_52083) {
            int j = Math.min(this.field_52083, 0x200000);
            int k = Math.max(this.field_52083 + j, i);
            this.method_60812(k);
        }
    }

    private void method_60812(int i) {
        this.field_52082 = field_52079.realloc(this.field_52082, i);
        field_52078.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)this.field_52083, (Object)i);
        if (this.field_52082 == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.field_52083 + " bytes to " + i + " bytes");
        }
        this.field_52083 = i;
    }

    @Nullable
    public class_9800 method_60807() {
        this.method_60816();
        int i = this.field_52085;
        int j = this.field_52084 - i;
        if (j == 0) {
            return null;
        }
        this.field_52085 = this.field_52084;
        ++this.field_52086;
        return new class_9800(i, j, this.field_52087);
    }

    public void method_60809() {
        if (this.field_52086 > 0) {
            field_52078.warn("Clearing BufferBuilder with unused batches");
        }
        this.method_60811();
    }

    public void method_60811() {
        this.method_60816();
        if (this.field_52086 > 0) {
            this.method_60815();
            this.field_52086 = 0;
        }
    }

    boolean method_60814(int i) {
        return i == this.field_52087;
    }

    void method_60813() {
        if (--this.field_52086 <= 0) {
            this.method_60815();
        }
    }

    private void method_60815() {
        int i = this.field_52084 - this.field_52085;
        if (i > 0) {
            MemoryUtil.memCopy(this.field_52082 + (long)this.field_52085, this.field_52082, i);
        }
        this.field_52084 = i;
        this.field_52085 = 0;
        ++this.field_52087;
    }

    @Override
    public void close() {
        if (this.field_52082 != 0L) {
            field_52079.free(this.field_52082);
            this.field_52082 = 0L;
            this.field_52087 = -1;
        }
    }

    private void method_60816() {
        if (this.field_52082 == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class class_9800
    implements AutoCloseable {
        private final int field_52089;
        private final int field_52090;
        private final int field_52091;
        private boolean field_52092;

        class_9800(int i, int j, int k) {
            this.field_52089 = i;
            this.field_52090 = j;
            this.field_52091 = k;
        }

        public ByteBuffer method_60817() {
            if (!class_9799.this.method_60814(this.field_52091)) {
                throw new IllegalStateException("Buffer is no longer valid");
            }
            return MemoryUtil.memByteBuffer(class_9799.this.field_52082 + (long)this.field_52089, this.field_52090);
        }

        @Override
        public void close() {
            if (this.field_52092) {
                return;
            }
            this.field_52092 = true;
            if (class_9799.this.method_60814(this.field_52091)) {
                class_9799.this.method_60813();
            }
        }
    }
}

