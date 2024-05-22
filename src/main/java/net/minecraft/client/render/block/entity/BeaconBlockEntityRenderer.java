/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.block.entity;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class BeaconBlockEntityRenderer
implements BlockEntityRenderer<BeaconBlockEntity> {
    public static final Identifier BEAM_TEXTURE = Identifier.method_60656("textures/entity/beacon_beam.png");
    public static final int MAX_BEAM_HEIGHT = 1024;

    public BeaconBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(BeaconBlockEntity arg, float f, MatrixStack arg2, VertexConsumerProvider arg3, int i, int j) {
        long l = arg.getWorld().getTime();
        List<BeaconBlockEntity.BeamSegment> list = arg.getBeamSegments();
        int k = 0;
        for (int m = 0; m < list.size(); ++m) {
            BeaconBlockEntity.BeamSegment lv = list.get(m);
            BeaconBlockEntityRenderer.renderBeam(arg2, arg3, f, l, k, m == list.size() - 1 ? 1024 : lv.getHeight(), lv.getColor());
            k += lv.getHeight();
        }
    }

    private static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, int k) {
        BeaconBlockEntityRenderer.renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0f, worldTime, yOffset, maxY, k, 0.2f, 0.25f);
    }

    public static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, int k, float innerRadius, float outerRadius) {
        int n = yOffset + maxY;
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        float o = (float)Math.floorMod(worldTime, 40) + tickDelta;
        float p = maxY < 0 ? o : -o;
        float q = MathHelper.fractionalPart(p * 0.2f - (float)MathHelper.floor(p * 0.1f));
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(o * 2.25f - 45.0f));
        float r = 0.0f;
        float s = innerRadius;
        float t = innerRadius;
        float u = 0.0f;
        float v = -innerRadius;
        float w = 0.0f;
        float x = 0.0f;
        float y = -innerRadius;
        float z = 0.0f;
        float aa = 1.0f;
        float ab = -1.0f + q;
        float ac = (float)maxY * heightScale * (0.5f / innerRadius) + ab;
        BeaconBlockEntityRenderer.renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, false)), k, yOffset, n, 0.0f, s, t, 0.0f, v, 0.0f, 0.0f, y, 0.0f, 1.0f, ac, ab);
        matrices.pop();
        r = -outerRadius;
        s = -outerRadius;
        t = outerRadius;
        u = -outerRadius;
        v = -outerRadius;
        w = outerRadius;
        x = outerRadius;
        y = outerRadius;
        z = 0.0f;
        aa = 1.0f;
        ab = -1.0f + q;
        ac = (float)maxY * heightScale + ab;
        BeaconBlockEntityRenderer.renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, true)), ColorHelper.Argb.withAlpha(32, k), yOffset, n, r, s, t, u, v, w, x, y, 0.0f, 1.0f, ac, ab);
        matrices.pop();
    }

    private static void renderBeamLayer(MatrixStack matrices, VertexConsumer vertices, int i, int j, int k, float alpha, float g, float h, float x1, float m, float n, float z2, float p, float q, float r, float s, float t) {
        MatrixStack.Entry lv = matrices.peek();
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, i, j, k, alpha, g, h, x1, q, r, s, t);
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, i, j, k, z2, p, m, n, q, r, s, t);
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, i, j, k, h, x1, z2, p, q, r, s, t);
        BeaconBlockEntityRenderer.renderBeamFace(lv, vertices, i, j, k, m, n, alpha, g, q, r, s, t);
    }

    private static void renderBeamFace(MatrixStack.Entry matrix, VertexConsumer vertices, int i, int j, int k, float alpha, float g, float h, float l, float m, float n, float o, float p) {
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, i, k, alpha, g, n, o);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, i, j, alpha, g, n, p);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, i, j, h, l, m, p);
        BeaconBlockEntityRenderer.renderBeamVertex(matrix, vertices, i, k, h, l, m, o);
    }

    private static void renderBeamVertex(MatrixStack.Entry matrix, VertexConsumer vertices, int i, int j, float blue, float alpha, float h, float k) {
        vertices.vertex(matrix, blue, (float)j, alpha).color(i).texture(h, k).overlay(OverlayTexture.DEFAULT_UV).method_60803(0xF000F0).method_60831(matrix, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public boolean rendersOutsideBoundingBox(BeaconBlockEntity arg) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public boolean isInRenderDistance(BeaconBlockEntity arg, Vec3d arg2) {
        return Vec3d.ofCenter(arg.getPos()).multiply(1.0, 0.0, 1.0).isInRange(arg2.multiply(1.0, 0.0, 1.0), this.getRenderDistance());
    }
}

