/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class EyesFeatureRenderer<T extends Entity, M extends EntityModel<T>>
extends FeatureRenderer<T, M> {
    public EyesFeatureRenderer(FeatureRendererContext<T, M> arg) {
        super(arg);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        VertexConsumer lv = vertexConsumers.getBuffer(this.getEyesTexture());
        ((Model)this.getContextModel()).method_60879(matrices, lv, 0xF00000, OverlayTexture.DEFAULT_UV);
    }

    public abstract RenderLayer getEyesTexture();
}

