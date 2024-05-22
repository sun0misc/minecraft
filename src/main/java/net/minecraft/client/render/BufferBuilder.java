/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import java.nio.ByteOrder;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class BufferBuilder
implements VertexConsumer {
    private static final long field_52068 = -1L;
    private static final long field_52069 = -1L;
    private static final boolean field_52070 = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private final class_9799 field_52071;
    private long field_52072 = -1L;
    private int vertexCount;
    private final VertexFormat format;
    private final VertexFormat.DrawMode field_52073;
    private final boolean canSkipElementChecks;
    private final boolean hasOverlay;
    private final int field_52074;
    private final int field_52075;
    private final int[] field_52076;
    private int field_52077;
    private boolean building = true;

    public BufferBuilder(class_9799 arg, VertexFormat.DrawMode arg2, VertexFormat arg3) {
        if (!arg3.method_60836(VertexFormatElement.field_52107)) {
            throw new IllegalArgumentException("Cannot build mesh with no position element");
        }
        this.field_52071 = arg;
        this.field_52073 = arg2;
        this.format = arg3;
        this.field_52074 = arg3.getVertexSizeByte();
        this.field_52075 = arg3.method_60839() & ~VertexFormatElement.field_52107.method_60843();
        this.field_52076 = arg3.method_60838();
        boolean bl = arg3 == VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
        boolean bl2 = arg3 == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
        this.canSkipElementChecks = bl || bl2;
        this.hasOverlay = bl;
    }

    @Nullable
    public class_9801 method_60794() {
        this.method_60802();
        this.method_60806();
        class_9801 lv = this.method_60804();
        this.building = false;
        this.field_52072 = -1L;
        return lv;
    }

    public class_9801 method_60800() {
        class_9801 lv = this.method_60794();
        if (lv == null) {
            throw new IllegalStateException("BufferBuilder was empty");
        }
        return lv;
    }

    private void method_60802() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    @Nullable
    private class_9801 method_60804() {
        if (this.vertexCount == 0) {
            return null;
        }
        class_9799.class_9800 lv = this.field_52071.method_60807();
        if (lv == null) {
            return null;
        }
        int i = this.field_52073.getIndexCount(this.vertexCount);
        VertexFormat.IndexType lv2 = VertexFormat.IndexType.smallestFor(this.vertexCount);
        return new class_9801(lv, new class_9801.DrawParameters(this.format, this.vertexCount, i, this.field_52073, lv2));
    }

    private long method_60805() {
        long l;
        this.method_60802();
        this.method_60806();
        ++this.vertexCount;
        this.field_52072 = l = this.field_52071.method_60808(this.field_52074);
        return l;
    }

    private long method_60798(VertexFormatElement arg) {
        int i = this.field_52077;
        int j = i & ~arg.method_60843();
        if (j == i) {
            return -1L;
        }
        this.field_52077 = j;
        long l = this.field_52072;
        if (l == -1L) {
            throw new IllegalArgumentException("Not currently building vertex");
        }
        return l + (long)this.field_52076[arg.id()];
    }

    private void method_60806() {
        if (this.vertexCount == 0) {
            return;
        }
        if (this.field_52077 != 0) {
            String string = VertexFormatElement.method_60848(this.field_52077).map(this.format::method_60837).collect(Collectors.joining(", "));
            throw new IllegalStateException("Missing elements in vertex: " + string);
        }
        if (this.field_52073 == VertexFormat.DrawMode.LINES || this.field_52073 == VertexFormat.DrawMode.LINE_STRIP) {
            long l = this.field_52071.method_60808(this.field_52074);
            MemoryUtil.memCopy(l - (long)this.field_52074, l, this.field_52074);
            ++this.vertexCount;
        }
    }

    private static void method_60797(long l, int i) {
        int j = ColorHelper.Abgr.method_60675(i);
        MemoryUtil.memPutInt(l, field_52070 ? j : Integer.reverseBytes(j));
    }

    private static void method_60801(long l, int i) {
        if (field_52070) {
            MemoryUtil.memPutInt(l, i);
        } else {
            MemoryUtil.memPutShort(l, (short)(i & 0xFFFF));
            MemoryUtil.memPutShort(l + 2L, (short)(i >> 16 & 0xFFFF));
        }
    }

    @Override
    public VertexConsumer vertex(float f, float g, float h) {
        long l = this.method_60805() + (long)this.field_52076[VertexFormatElement.field_52107.id()];
        this.field_52077 = this.field_52075;
        MemoryUtil.memPutFloat(l, f);
        MemoryUtil.memPutFloat(l + 4L, g);
        MemoryUtil.memPutFloat(l + 8L, h);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        long m = this.method_60798(VertexFormatElement.field_52108);
        if (m != -1L) {
            MemoryUtil.memPutByte(m, (byte)red);
            MemoryUtil.memPutByte(m + 1L, (byte)green);
            MemoryUtil.memPutByte(m + 2L, (byte)blue);
            MemoryUtil.memPutByte(m + 3L, (byte)alpha);
        }
        return this;
    }

    @Override
    public VertexConsumer color(int argb) {
        long l = this.method_60798(VertexFormatElement.field_52108);
        if (l != -1L) {
            BufferBuilder.method_60797(l, argb);
        }
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        long l = this.method_60798(VertexFormatElement.field_52109);
        if (l != -1L) {
            MemoryUtil.memPutFloat(l, u);
            MemoryUtil.memPutFloat(l + 4L, v);
        }
        return this;
    }

    @Override
    public VertexConsumer method_60796(int i, int j) {
        return this.method_60799((short)i, (short)j, VertexFormatElement.field_52111);
    }

    @Override
    public VertexConsumer overlay(int uv) {
        long l = this.method_60798(VertexFormatElement.field_52111);
        if (l != -1L) {
            BufferBuilder.method_60801(l, uv);
        }
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this.method_60799((short)u, (short)v, VertexFormatElement.field_52112);
    }

    @Override
    public VertexConsumer method_60803(int i) {
        long l = this.method_60798(VertexFormatElement.field_52112);
        if (l != -1L) {
            BufferBuilder.method_60801(l, i);
        }
        return this;
    }

    private VertexConsumer method_60799(short s, short t, VertexFormatElement arg) {
        long l = this.method_60798(arg);
        if (l != -1L) {
            MemoryUtil.memPutShort(l, s);
            MemoryUtil.memPutShort(l + 2L, t);
        }
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        long l = this.method_60798(VertexFormatElement.field_52113);
        if (l != -1L) {
            MemoryUtil.memPutByte(l, BufferBuilder.method_60795(x));
            MemoryUtil.memPutByte(l + 1L, BufferBuilder.method_60795(y));
            MemoryUtil.memPutByte(l + 2L, BufferBuilder.method_60795(z));
        }
        return this;
    }

    private static byte method_60795(float f) {
        return (byte)((int)(MathHelper.clamp(f, -1.0f, 1.0f) * 127.0f) & 0xFF);
    }

    @Override
    public void vertex(float x, float y, float z, int i, float green, float blue, int l, int m, float v, float o, float p) {
        if (this.canSkipElementChecks) {
            long r;
            long q = this.method_60805();
            MemoryUtil.memPutFloat(q + 0L, x);
            MemoryUtil.memPutFloat(q + 4L, y);
            MemoryUtil.memPutFloat(q + 8L, z);
            BufferBuilder.method_60797(q + 12L, i);
            MemoryUtil.memPutFloat(q + 16L, green);
            MemoryUtil.memPutFloat(q + 20L, blue);
            if (this.hasOverlay) {
                BufferBuilder.method_60801(q + 24L, l);
                r = q + 28L;
            } else {
                r = q + 24L;
            }
            BufferBuilder.method_60801(r + 0L, m);
            MemoryUtil.memPutByte(r + 4L, BufferBuilder.method_60795(v));
            MemoryUtil.memPutByte(r + 5L, BufferBuilder.method_60795(o));
            MemoryUtil.memPutByte(r + 6L, BufferBuilder.method_60795(p));
            return;
        }
        VertexConsumer.super.vertex(x, y, z, i, green, blue, l, m, v, o, p);
    }
}

