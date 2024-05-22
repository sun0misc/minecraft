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
import net.minecraft.client.render.entity.model.CodEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class CodEntityRenderer
extends MobEntityRenderer<CodEntity, CodEntityModel<CodEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/fish/cod.png");

    public CodEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new CodEntityModel(arg.getPart(EntityModelLayers.COD)), 0.3f);
    }

    @Override
    public Identifier getTexture(CodEntity arg) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(CodEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        super.setupTransforms(arg, arg2, f, g, h, i);
        float j = 4.3f * MathHelper.sin(0.6f * f);
        arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        if (!arg.isTouchingWater()) {
            arg2.translate(0.1f, 0.1f, -0.1f);
            arg2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
        }
    }
}

