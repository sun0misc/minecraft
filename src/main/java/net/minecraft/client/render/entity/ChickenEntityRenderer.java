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
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ChickenEntityRenderer
extends MobEntityRenderer<ChickenEntity, ChickenEntityModel<ChickenEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/chicken.png");

    public ChickenEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new ChickenEntityModel(arg.getPart(EntityModelLayers.CHICKEN)), 0.3f);
    }

    @Override
    public Identifier getTexture(ChickenEntity arg) {
        return TEXTURE;
    }

    @Override
    protected float getAnimationProgress(ChickenEntity arg, float f) {
        float g = MathHelper.lerp(f, arg.prevFlapProgress, arg.flapProgress);
        float h = MathHelper.lerp(f, arg.prevMaxWingDeviation, arg.maxWingDeviation);
        return (MathHelper.sin(g) + 1.0f) * h;
    }
}

