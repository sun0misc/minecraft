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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BreezeEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BreezeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BreezeWindFeatureRenderer
extends FeatureRenderer<BreezeEntity, BreezeEntityModel<BreezeEntity>> {
    private static final Identifier texture = Identifier.method_60656("textures/entity/breeze/breeze_wind.png");
    private final BreezeEntityModel<BreezeEntity> model;

    public BreezeWindFeatureRenderer(EntityRendererFactory.Context arg, FeatureRendererContext<BreezeEntity, BreezeEntityModel<BreezeEntity>> arg2) {
        super(arg2);
        this.model = new BreezeEntityModel(arg.getPart(EntityModelLayers.BREEZE_WIND));
    }

    @Override
    public void render(MatrixStack arg, VertexConsumerProvider arg2, int i, BreezeEntity arg3, float f, float g, float h, float j, float k, float l) {
        float m = (float)arg3.age + h;
        VertexConsumer lv = arg2.getBuffer(RenderLayer.getBreezeWind(texture, this.getXOffset(m) % 1.0f, 0.0f));
        this.model.setAngles(arg3, f, g, j, k, l);
        BreezeEntityRenderer.updatePartVisibility(this.model, this.model.getWindBody()).method_60879(arg, lv, i, OverlayTexture.DEFAULT_UV);
    }

    private float getXOffset(float tickDelta) {
        return tickDelta * 0.02f;
    }
}

