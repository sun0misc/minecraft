/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft;

import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class class_9801
implements AutoCloseable {
    private final class_9799.class_9800 field_52093;
    @Nullable
    private class_9799.class_9800 field_52094;
    private final DrawParameters field_52095;

    public class_9801(class_9799.class_9800 arg, DrawParameters arg2) {
        this.field_52093 = arg;
        this.field_52095 = arg2;
    }

    private static Vector3f[] method_60820(ByteBuffer byteBuffer, int i, VertexFormat arg) {
        int j = arg.method_60835(VertexFormatElement.field_52107);
        if (j == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        }
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        int k = arg.getVertexSizeByte() / 4;
        int l = k * 4;
        int m = i / 4;
        Vector3f[] vector3fs = new Vector3f[m];
        for (int n = 0; n < m; ++n) {
            int o = n * l + j;
            int p = o + k * 2;
            float f = floatBuffer.get(o + 0);
            float g = floatBuffer.get(o + 1);
            float h = floatBuffer.get(o + 2);
            float q = floatBuffer.get(p + 0);
            float r = floatBuffer.get(p + 1);
            float s = floatBuffer.get(p + 2);
            vector3fs[n] = new Vector3f((f + q) / 2.0f, (g + r) / 2.0f, (h + s) / 2.0f);
        }
        return vector3fs;
    }

    public ByteBuffer method_60818() {
        return this.field_52093.method_60817();
    }

    @Nullable
    public ByteBuffer method_60821() {
        return this.field_52094 != null ? this.field_52094.method_60817() : null;
    }

    public DrawParameters method_60822() {
        return this.field_52095;
    }

    @Nullable
    public class_9802 method_60819(class_9799 arg, VertexSorter arg2) {
        if (this.field_52095.mode() != VertexFormat.DrawMode.QUADS) {
            return null;
        }
        Vector3f[] vector3fs = class_9801.method_60820(this.field_52093.method_60817(), this.field_52095.vertexCount(), this.field_52095.format());
        class_9802 lv = new class_9802(vector3fs, this.field_52095.indexType());
        this.field_52094 = lv.method_60824(arg, arg2);
        return lv;
    }

    @Override
    public void close() {
        this.field_52093.close();
        if (this.field_52094 != null) {
            this.field_52094.close();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record DrawParameters(VertexFormat format, int vertexCount, int indexCount, VertexFormat.DrawMode mode, VertexFormat.IndexType indexType) {
    }

    @Environment(value=EnvType.CLIENT)
    public record class_9802(Vector3f[] centroids, VertexFormat.IndexType indexType) {
        @Nullable
        public class_9799.class_9800 method_60824(class_9799 arg, VertexSorter arg2) {
            int[] is = arg2.sort(this.centroids);
            long l = arg.method_60808(is.length * 6 * this.indexType.size);
            IntConsumer intConsumer = this.method_60823(l, this.indexType);
            for (int i : is) {
                intConsumer.accept(i * 4 + 0);
                intConsumer.accept(i * 4 + 1);
                intConsumer.accept(i * 4 + 2);
                intConsumer.accept(i * 4 + 2);
                intConsumer.accept(i * 4 + 3);
                intConsumer.accept(i * 4 + 0);
            }
            return arg.method_60807();
        }

        private IntConsumer method_60823(long l, VertexFormat.IndexType arg) {
            MutableLong mutableLong = new MutableLong(l);
            return switch (arg) {
                default -> throw new MatchException(null, null);
                case VertexFormat.IndexType.SHORT -> i -> MemoryUtil.memPutShort(mutableLong.getAndAdd(2L), (short)i);
                case VertexFormat.IndexType.INT -> i -> MemoryUtil.memPutInt(mutableLong.getAndAdd(4L), i);
            };
        }
    }
}

