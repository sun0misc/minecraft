/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class MobEntityRenderer<T extends MobEntity, M extends EntityModel<T>>
extends LivingEntityRenderer<T, M> {
    public static final int LEASH_PIECE_COUNT = 24;

    public MobEntityRenderer(EntityRendererFactory.Context arg, M arg2, float f) {
        super(arg, arg2, f);
    }

    @Override
    protected boolean hasLabel(T arg) {
        return super.hasLabel(arg) && (((LivingEntity)arg).shouldRenderName() || ((Entity)arg).hasCustomName() && arg == this.dispatcher.targetedEntity);
    }

    @Override
    public boolean shouldRender(T arg, Frustum arg2, double d, double e, double f) {
        if (super.shouldRender(arg, arg2, d, e, f)) {
            return true;
        }
        Entity lv = ((MobEntity)arg).getHoldingEntity();
        if (lv != null) {
            return arg2.isVisible(lv.getVisibilityBoundingBox());
        }
        return false;
    }

    @Override
    public void render(T arg, float f, float g, MatrixStack arg2, VertexConsumerProvider arg3, int i) {
        super.render(arg, f, g, arg2, arg3, i);
        Entity lv = ((MobEntity)arg).getHoldingEntity();
        if (lv == null) {
            return;
        }
        this.renderLeash(arg, g, arg2, arg3, lv);
    }

    private <E extends Entity> void renderLeash(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, E holdingEntity) {
        int v;
        matrices.push();
        Vec3d lv = holdingEntity.getLeashPos(tickDelta);
        double d = (double)(MathHelper.lerp(tickDelta, ((MobEntity)entity).prevBodyYaw, ((MobEntity)entity).bodyYaw) * ((float)Math.PI / 180)) + 1.5707963267948966;
        Vec3d lv2 = ((Entity)entity).getLeashOffset(tickDelta);
        double e = Math.cos(d) * lv2.z + Math.sin(d) * lv2.x;
        double g = Math.sin(d) * lv2.z - Math.cos(d) * lv2.x;
        double h = MathHelper.lerp((double)tickDelta, ((MobEntity)entity).prevX, ((Entity)entity).getX()) + e;
        double i = MathHelper.lerp((double)tickDelta, ((MobEntity)entity).prevY, ((Entity)entity).getY()) + lv2.y;
        double j = MathHelper.lerp((double)tickDelta, ((MobEntity)entity).prevZ, ((Entity)entity).getZ()) + g;
        matrices.translate(e, lv2.y, g);
        float k = (float)(lv.x - h);
        float l = (float)(lv.y - i);
        float m = (float)(lv.z - j);
        float n = 0.025f;
        VertexConsumer lv3 = provider.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float o = MathHelper.inverseSqrt(k * k + m * m) * 0.025f / 2.0f;
        float p = m * o;
        float q = k * o;
        BlockPos lv4 = BlockPos.ofFloored(((Entity)entity).getCameraPosVec(tickDelta));
        BlockPos lv5 = BlockPos.ofFloored(holdingEntity.getCameraPosVec(tickDelta));
        int r = this.getBlockLight(entity, lv4);
        int s = this.dispatcher.getRenderer(holdingEntity).getBlockLight(holdingEntity, lv5);
        int t = ((Entity)entity).getWorld().getLightLevel(LightType.SKY, lv4);
        int u = ((Entity)entity).getWorld().getLightLevel(LightType.SKY, lv5);
        for (v = 0; v <= 24; ++v) {
            MobEntityRenderer.renderLeashPiece(lv3, matrix4f, k, l, m, r, s, t, u, 0.025f, 0.025f, p, q, v, false);
        }
        for (v = 24; v >= 0; --v) {
            MobEntityRenderer.renderLeashPiece(lv3, matrix4f, k, l, m, r, s, t, u, 0.025f, 0.0f, p, q, v, true);
        }
        matrices.pop();
    }

    private static void renderLeashPiece(VertexConsumer vertexConsumer, Matrix4f positionMatrix, float f, float g, float h, int leashedEntityBlockLight, int holdingEntityBlockLight, int leashedEntitySkyLight, int holdingEntitySkyLight, float m, float n, float o, float p, int pieceIndex, boolean isLeashKnot) {
        float r = (float)pieceIndex / 24.0f;
        int s = (int)MathHelper.lerp(r, (float)leashedEntityBlockLight, (float)holdingEntityBlockLight);
        int t = (int)MathHelper.lerp(r, (float)leashedEntitySkyLight, (float)holdingEntitySkyLight);
        int u = LightmapTextureManager.pack(s, t);
        float v = pieceIndex % 2 == (isLeashKnot ? 1 : 0) ? 0.7f : 1.0f;
        float w = 0.5f * v;
        float x = 0.4f * v;
        float y = 0.3f * v;
        float z = f * r;
        float aa = g > 0.0f ? g * r * r : g - g * (1.0f - r) * (1.0f - r);
        float ab = h * r;
        vertexConsumer.vertex(positionMatrix, z - o, aa + n, ab + p).color(w, x, y, 1.0f).method_60803(u);
        vertexConsumer.vertex(positionMatrix, z + o, aa + m - n, ab - p).color(w, x, y, 1.0f).method_60803(u);
    }

    @Override
    protected float getShadowRadius(T arg) {
        return super.getShadowRadius(arg) * ((LivingEntity)arg).getScaleFactor();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntity arg) {
        return this.getShadowRadius((T)((MobEntity)arg));
    }

    @Override
    protected /* synthetic */ boolean hasLabel(LivingEntity arg) {
        return this.hasLabel((T)((MobEntity)arg));
    }

    @Override
    protected /* synthetic */ float getShadowRadius(Entity entity) {
        return this.getShadowRadius((T)((MobEntity)entity));
    }

    @Override
    protected /* synthetic */ boolean hasLabel(Entity entity) {
        return this.hasLabel((T)((MobEntity)entity));
    }
}

