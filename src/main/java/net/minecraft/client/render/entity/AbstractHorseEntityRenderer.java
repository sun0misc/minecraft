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
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractHorseEntity;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractHorseEntityRenderer<T extends AbstractHorseEntity, M extends HorseEntityModel<T>>
extends MobEntityRenderer<T, M> {
    private final float scale;

    public AbstractHorseEntityRenderer(EntityRendererFactory.Context ctx, M model, float scale) {
        super(ctx, model, 0.75f);
        this.scale = scale;
    }

    @Override
    protected void scale(T arg, MatrixStack arg2, float f) {
        arg2.scale(this.scale, this.scale, this.scale);
        super.scale(arg, arg2, f);
    }
}

