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
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.IllagerEntity;

@Environment(value=EnvType.CLIENT)
public abstract class IllagerEntityRenderer<T extends IllagerEntity>
extends MobEntityRenderer<T, IllagerEntityModel<T>> {
    protected IllagerEntityRenderer(EntityRendererFactory.Context ctx, IllagerEntityModel<T> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
        this.addFeature(new HeadFeatureRenderer(this, ctx.getModelLoader(), ctx.getHeldItemRenderer()));
    }

    @Override
    protected void scale(T arg, MatrixStack arg2, float f) {
        float g = 0.9375f;
        arg2.scale(0.9375f, 0.9375f, 0.9375f);
    }
}

