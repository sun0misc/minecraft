/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record VertexFormatElement(int id, int uvIndex, ComponentType componentType, Type type, int componentCount) {
    public static final int field_52106 = 32;
    private static final VertexFormatElement[] field_52114 = new VertexFormatElement[32];
    private static final List<VertexFormatElement> field_52115 = new ArrayList<VertexFormatElement>(32);
    public static final VertexFormatElement field_52107 = VertexFormatElement.method_60845(0, 0, ComponentType.FLOAT, Type.POSITION, 3);
    public static final VertexFormatElement field_52108 = VertexFormatElement.method_60845(1, 0, ComponentType.UBYTE, Type.COLOR, 4);
    public static final VertexFormatElement field_52109;
    public static final VertexFormatElement field_52110;
    public static final VertexFormatElement field_52111;
    public static final VertexFormatElement field_52112;
    public static final VertexFormatElement field_52113;

    public VertexFormatElement(int uvIndex, int j, ComponentType arg, Type arg2, int k) {
        if (uvIndex < 0 || uvIndex >= field_52114.length) {
            throw new IllegalArgumentException("Element ID must be in range [0; " + field_52114.length + ")");
        }
        if (!this.isValidType(j, arg2)) {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        }
        this.id = uvIndex;
        this.uvIndex = j;
        this.componentType = arg;
        this.type = arg2;
        this.componentCount = k;
    }

    public static VertexFormatElement method_60845(int i, int j, ComponentType arg, Type arg2, int k) {
        VertexFormatElement lv = new VertexFormatElement(i, j, arg, arg2, k);
        if (field_52114[i] != null) {
            throw new IllegalArgumentException("Duplicate element registration for: " + i);
        }
        VertexFormatElement.field_52114[i] = lv;
        field_52115.add(lv);
        return lv;
    }

    private boolean isValidType(int uvIndex, Type type) {
        return uvIndex == 0 || type == Type.UV;
    }

    @Override
    public String toString() {
        return this.componentCount + "," + String.valueOf((Object)this.type) + "," + String.valueOf((Object)this.componentType) + " (" + this.id + ")";
    }

    public int method_60843() {
        return 1 << this.id;
    }

    public int method_60847() {
        return this.componentType.getByteLength() * this.componentCount;
    }

    public void setupState(int elementIndex, long offset, int stride) {
        this.type.setupTask.setupBufferState(this.componentCount, this.componentType.getGlType(), stride, offset, elementIndex);
    }

    @Nullable
    public static VertexFormatElement method_60844(int i) {
        return field_52114[i];
    }

    public static Stream<VertexFormatElement> method_60848(int i) {
        return field_52115.stream().filter(arg -> arg != null && (i & arg.method_60843()) != 0);
    }

    static {
        field_52110 = field_52109 = VertexFormatElement.method_60845(2, 0, ComponentType.FLOAT, Type.UV, 2);
        field_52111 = VertexFormatElement.method_60845(3, 1, ComponentType.SHORT, Type.UV, 2);
        field_52112 = VertexFormatElement.method_60845(4, 2, ComponentType.SHORT, Type.UV, 2);
        field_52113 = VertexFormatElement.method_60845(5, 0, ComponentType.BYTE, Type.NORMAL, 3);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ComponentType {
        FLOAT(4, "Float", GlConst.GL_FLOAT),
        UBYTE(1, "Unsigned Byte", GlConst.GL_UNSIGNED_BYTE),
        BYTE(1, "Byte", GlConst.GL_BYTE),
        USHORT(2, "Unsigned Short", GlConst.GL_UNSIGNED_SHORT),
        SHORT(2, "Short", GlConst.GL_SHORT),
        UINT(4, "Unsigned Int", GlConst.GL_UNSIGNED_INT),
        INT(4, "Int", GlConst.GL_INT);

        private final int byteLength;
        private final String name;
        private final int glType;

        private ComponentType(int byteLength, String name, int glType) {
            this.byteLength = byteLength;
            this.name = name;
            this.glType = glType;
        }

        public int getByteLength() {
            return this.byteLength;
        }

        public int getGlType() {
            return this.glType;
        }

        public String toString() {
            return this.name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        POSITION("Position", (componentCount, componentType, stride, offset, uvIndex) -> GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, false, stride, offset)),
        NORMAL("Normal", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, true, k, l)),
        COLOR("Vertex Color", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, true, k, l)),
        UV("UV", (componentCount, componentType, stride, offset, uvIndex) -> {
            if (componentType == 5126) {
                GlStateManager._vertexAttribPointer(uvIndex, componentCount, componentType, false, stride, offset);
            } else {
                GlStateManager._vertexAttribIPointer(uvIndex, componentCount, componentType, stride, offset);
            }
        }),
        GENERIC("Generic", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, false, k, l));

        private final String name;
        final SetupTask setupTask;

        private Type(String name, SetupTask setupTask) {
            this.name = name;
            this.setupTask = setupTask;
        }

        public String toString() {
            return this.name;
        }

        @FunctionalInterface
        @Environment(value=EnvType.CLIENT)
        static interface SetupTask {
            public void setupBufferState(int var1, int var2, int var3, long var4, int var6);
        }
    }
}

