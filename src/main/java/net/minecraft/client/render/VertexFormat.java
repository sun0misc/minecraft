/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormatElement;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VertexFormat {
    public static final int field_52099 = -1;
    private final List<VertexFormatElement> elements;
    private final List<String> field_52100;
    private final int vertexSizeByte;
    private final int field_52101;
    private final int[] field_52102 = new int[32];
    @Nullable
    private VertexBuffer buffer;

    VertexFormat(List<VertexFormatElement> list, List<String> list2, IntList intList, int i2) {
        this.elements = list;
        this.field_52100 = list2;
        this.vertexSizeByte = i2;
        this.field_52101 = list.stream().mapToInt(VertexFormatElement::method_60843).reduce(0, (i, j) -> i | j);
        for (int j2 = 0; j2 < this.field_52102.length; ++j2) {
            VertexFormatElement lv = VertexFormatElement.method_60844(j2);
            int k = lv != null ? list.indexOf(lv) : -1;
            this.field_52102[j2] = k != -1 ? intList.getInt(k) : -1;
        }
    }

    public static class_9803 method_60833() {
        return new class_9803();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Vertex format (").append(this.vertexSizeByte).append(" bytes):\n");
        for (int i = 0; i < this.elements.size(); ++i) {
            VertexFormatElement lv = this.elements.get(i);
            stringBuilder.append(i).append(". ").append(this.field_52100.get(i)).append(": ").append(lv).append(" @ ").append(this.method_60835(lv)).append('\n');
        }
        return stringBuilder.toString();
    }

    public int getVertexSizeByte() {
        return this.vertexSizeByte;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getAttributeNames() {
        return this.field_52100;
    }

    public int[] method_60838() {
        return this.field_52102;
    }

    public int method_60835(VertexFormatElement arg) {
        return this.field_52102[arg.id()];
    }

    public boolean method_60836(VertexFormatElement arg) {
        return (this.field_52101 & arg.method_60843()) != 0;
    }

    public int method_60839() {
        return this.field_52101;
    }

    public String method_60837(VertexFormatElement arg) {
        int i = this.elements.indexOf(arg);
        if (i == -1) {
            throw new IllegalArgumentException(String.valueOf(arg) + " is not contained in format");
        }
        return this.field_52100.get(i);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof VertexFormat)) return false;
        VertexFormat lv = (VertexFormat)object;
        if (this.field_52101 != lv.field_52101) return false;
        if (this.vertexSizeByte != lv.vertexSizeByte) return false;
        if (!this.field_52100.equals(lv.field_52100)) return false;
        if (!Arrays.equals(this.field_52102, lv.field_52102)) return false;
        return true;
    }

    public int hashCode() {
        return this.field_52101 * 31 + Arrays.hashCode(this.field_52102);
    }

    public void setupState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::setupStateInternal);
            return;
        }
        this.setupStateInternal();
    }

    private void setupStateInternal() {
        int i = this.getVertexSizeByte();
        for (int j = 0; j < this.elements.size(); ++j) {
            GlStateManager._enableVertexAttribArray(j);
            VertexFormatElement lv = this.elements.get(j);
            lv.setupState(j, this.method_60835(lv), i);
        }
    }

    public void clearState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::clearStateInternal);
            return;
        }
        this.clearStateInternal();
    }

    private void clearStateInternal() {
        for (int i = 0; i < this.elements.size(); ++i) {
            GlStateManager._disableVertexAttribArray(i);
        }
    }

    public VertexBuffer getBuffer() {
        VertexBuffer lv = this.buffer;
        if (lv == null) {
            this.buffer = lv = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        return lv;
    }

    @Environment(value=EnvType.CLIENT)
    public static class class_9803 {
        private final ImmutableMap.Builder<String, VertexFormatElement> field_52103 = ImmutableMap.builder();
        private final IntList field_52104 = new IntArrayList();
        private int field_52105;

        class_9803() {
        }

        public class_9803 method_60842(String string, VertexFormatElement arg) {
            this.field_52103.put(string, arg);
            this.field_52104.add(this.field_52105);
            this.field_52105 += arg.method_60847();
            return this;
        }

        public class_9803 method_60841(int i) {
            this.field_52105 += i;
            return this;
        }

        public VertexFormat method_60840() {
            ImmutableMap<String, VertexFormatElement> immutableMap = this.field_52103.buildOrThrow();
            ImmutableList<VertexFormatElement> immutableList = ((ImmutableCollection)immutableMap.values()).asList();
            ImmutableList<String> immutableList2 = ((ImmutableCollection)((Object)immutableMap.keySet())).asList();
            return new VertexFormat(immutableList, immutableList2, this.field_52104, this.field_52105);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum DrawMode {
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(4, 4, 4, false);

        public final int glMode;
        public final int firstVertexCount;
        public final int additionalVertexCount;
        public final boolean shareVertices;

        private DrawMode(int glMode, int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
            this.glMode = glMode;
            this.firstVertexCount = firstVertexCount;
            this.additionalVertexCount = additionalVertexCount;
            this.shareVertices = shareVertices;
        }

        public int getIndexCount(int vertexCount) {
            return switch (this.ordinal()) {
                case 1, 2, 3, 4, 5, 6 -> vertexCount;
                case 0, 7 -> vertexCount / 4 * 6;
                default -> 0;
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum IndexType {
        SHORT(GlConst.GL_UNSIGNED_SHORT, 2),
        INT(GlConst.GL_UNSIGNED_INT, 4);

        public final int glType;
        public final int size;

        private IndexType(int glType, int size) {
            this.glType = glType;
            this.size = size;
        }

        public static IndexType smallestFor(int indexCount) {
            if ((indexCount & 0xFFFF0000) != 0) {
                return INT;
            }
            return SHORT;
        }
    }
}

