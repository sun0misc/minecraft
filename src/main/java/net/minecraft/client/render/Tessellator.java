/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9799;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Tessellator {
    private static final int field_46841 = 786432;
    private final class_9799 field_52098;
    @Nullable
    private static Tessellator INSTANCE;

    public static void initialize() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Tesselator has already been initialized");
        }
        INSTANCE = new Tessellator();
    }

    public static Tessellator getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Tesselator has not been initialized");
        }
        return INSTANCE;
    }

    public Tessellator(int bufferCapacity) {
        this.field_52098 = new class_9799(bufferCapacity);
    }

    public Tessellator() {
        this(786432);
    }

    public BufferBuilder method_60827(VertexFormat.DrawMode arg, VertexFormat arg2) {
        return new BufferBuilder(this.field_52098, arg, arg2);
    }

    public void method_60828() {
        this.field_52098.method_60809();
    }
}

