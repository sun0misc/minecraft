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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.IronGolemCrackFeatureRenderer;
import net.minecraft.client.render.entity.feature.IronGolemFlowerFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class IronGolemEntityRenderer
extends MobEntityRenderer<IronGolemEntity, IronGolemEntityModel<IronGolemEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/iron_golem/iron_golem.png");

    public IronGolemEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new IronGolemEntityModel(arg.getPart(EntityModelLayers.IRON_GOLEM)), 0.7f);
        this.addFeature(new IronGolemCrackFeatureRenderer(this));
        this.addFeature(new IronGolemFlowerFeatureRenderer(this, arg.getBlockRenderManager()));
    }

    @Override
    public Identifier getTexture(IronGolemEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(IronGolemEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        if ((double)arg.limbAnimator.getSpeed() < 0.01) {
            return;
        }
        float j = 13.0f;
        float k = arg.limbAnimator.getPos(h) + 6.0f;
        float l = (Math.abs(k % 13.0f - 6.5f) - 3.25f) / 3.25f;
        arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(6.5f * l));
    }
}

