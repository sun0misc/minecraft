/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class OverlayVertexConsumer
implements VertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f inverseTextureMatrix;
    private final Matrix3f inverseNormalMatrix;
    private final float textureScale;
    private final Vector3f field_52096 = new Vector3f();
    private final Vector3f field_52097 = new Vector3f();
    private float x;
    private float y;
    private float z;

    public OverlayVertexConsumer(VertexConsumer arg, MatrixStack.Entry matrix, float textureScale) {
        this.delegate = arg;
        this.inverseTextureMatrix = new Matrix4f(matrix.getPositionMatrix()).invert();
        this.inverseNormalMatrix = new Matrix3f(matrix.getNormalMatrix()).invert();
        this.textureScale = textureScale;
    }

    @Override
    public VertexConsumer vertex(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
        this.delegate.vertex(f, g, h);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.delegate.color(Colors.WHITE);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer method_60796(int i, int j) {
        this.delegate.method_60796(i, j);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        Vector3f vector3f = this.inverseNormalMatrix.transform(x, y, z, this.field_52097);
        Direction lv = Direction.getFacing(vector3f.x(), vector3f.y(), vector3f.z());
        Vector3f vector3f2 = this.inverseTextureMatrix.transformPosition(this.x, this.y, this.z, this.field_52096);
        vector3f2.rotateY((float)Math.PI);
        vector3f2.rotateX(-1.5707964f);
        vector3f2.rotate(lv.getRotationQuaternion());
        this.delegate.texture(-vector3f2.x() * this.textureScale, -vector3f2.y() * this.textureScale);
        return this;
    }
}

