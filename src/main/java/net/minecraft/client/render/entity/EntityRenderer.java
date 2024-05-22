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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected static final float field_32921 = 0.025f;
    protected final EntityRenderDispatcher dispatcher;
    private final TextRenderer textRenderer;
    protected float shadowRadius;
    protected float shadowOpacity = 1.0f;

    protected EntityRenderer(EntityRendererFactory.Context ctx) {
        this.dispatcher = ctx.getRenderDispatcher();
        this.textRenderer = ctx.getTextRenderer();
    }

    public final int getLight(T entity, float tickDelta) {
        BlockPos lv = BlockPos.ofFloored(((Entity)entity).getClientCameraPosVec(tickDelta));
        return LightmapTextureManager.pack(this.getBlockLight(entity, lv), this.getSkyLight(entity, lv));
    }

    protected int getSkyLight(T entity, BlockPos pos) {
        return ((Entity)entity).getWorld().getLightLevel(LightType.SKY, pos);
    }

    protected int getBlockLight(T entity, BlockPos pos) {
        if (((Entity)entity).isOnFire()) {
            return 15;
        }
        return ((Entity)entity).getWorld().getLightLevel(LightType.BLOCK, pos);
    }

    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if (!((Entity)entity).shouldRender(x, y, z)) {
            return false;
        }
        if (((Entity)entity).ignoreCameraFrustum) {
            return true;
        }
        Box lv = ((Entity)entity).getVisibilityBoundingBox().expand(0.5);
        if (lv.isNaN() || lv.getAverageSideLength() == 0.0) {
            lv = new Box(((Entity)entity).getX() - 2.0, ((Entity)entity).getY() - 2.0, ((Entity)entity).getZ() - 2.0, ((Entity)entity).getX() + 2.0, ((Entity)entity).getY() + 2.0, ((Entity)entity).getZ() + 2.0);
        }
        return frustum.isVisible(lv);
    }

    public Vec3d getPositionOffset(T entity, float tickDelta) {
        return Vec3d.ZERO;
    }

    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!this.hasLabel(entity)) {
            return;
        }
        this.renderLabelIfPresent(entity, ((Entity)entity).getDisplayName(), matrices, vertexConsumers, light, tickDelta);
    }

    protected boolean hasLabel(T entity) {
        return ((Entity)entity).shouldRenderName() || ((Entity)entity).hasCustomName() && entity == this.dispatcher.targetedEntity;
    }

    public abstract Identifier getTexture(T var1);

    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    protected void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        double d = this.dispatcher.getSquaredDistanceToCamera((Entity)entity);
        if (d > 4096.0) {
            return;
        }
        Vec3d lv = ((Entity)entity).getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, ((Entity)entity).getYaw(tickDelta));
        if (lv == null) {
            return;
        }
        boolean bl = !((Entity)entity).isSneaky();
        int j = "deadmau5".equals(text.getString()) ? -10 : 0;
        matrices.push();
        matrices.translate(lv.x, lv.y + 0.5, lv.z);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int k = (int)(g * 255.0f) << 24;
        TextRenderer lv2 = this.getTextRenderer();
        float h = -lv2.getWidth(text) / 2;
        lv2.draw(text, h, (float)j, 0x20FFFFFF, false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, k, light);
        if (bl) {
            lv2.draw(text, h, (float)j, Colors.WHITE, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
        matrices.pop();
    }

    protected float getShadowRadius(T entity) {
        return this.shadowRadius;
    }
}

