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
import net.minecraft.client.render.entity.feature.WardenFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WardenEntityRenderer
extends MobEntityRenderer<WardenEntity, WardenEntityModel<WardenEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/warden/warden.png");
    private static final Identifier BIOLUMINESCENT_LAYER_TEXTURE = Identifier.method_60656("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final Identifier HEART_TEXTURE = Identifier.method_60656("textures/entity/warden/warden_heart.png");
    private static final Identifier PULSATING_SPOTS_1_TEXTURE = Identifier.method_60656("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final Identifier PULSATING_SPOTS_2_TEXTURE = Identifier.method_60656("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new WardenEntityModel(arg.getPart(EntityModelLayers.WARDEN)), 0.9f);
        this.addFeature(new WardenFeatureRenderer<WardenEntity, WardenEntityModel>(this, BIOLUMINESCENT_LAYER_TEXTURE, (warden, tickDelta, animationProgress) -> 1.0f, WardenEntityModel::getHeadAndLimbs));
        this.addFeature(new WardenFeatureRenderer<WardenEntity, WardenEntityModel>(this, PULSATING_SPOTS_1_TEXTURE, (warden, tickDelta, animationProgress) -> Math.max(0.0f, MathHelper.cos(animationProgress * 0.045f) * 0.25f), WardenEntityModel::getBodyHeadAndLimbs));
        this.addFeature(new WardenFeatureRenderer<WardenEntity, WardenEntityModel>(this, PULSATING_SPOTS_2_TEXTURE, (warden, tickDelta, animationProgress) -> Math.max(0.0f, MathHelper.cos(animationProgress * 0.045f + (float)Math.PI) * 0.25f), WardenEntityModel::getBodyHeadAndLimbs));
        this.addFeature(new WardenFeatureRenderer<WardenEntity, WardenEntityModel>(this, TEXTURE, (warden, tickDelta, animationProgress) -> warden.getTendrilPitch(tickDelta), WardenEntityModel::getTendrils));
        this.addFeature(new WardenFeatureRenderer<WardenEntity, WardenEntityModel>(this, HEART_TEXTURE, (warden, tickDelta, animationProgress) -> warden.getHeartPitch(tickDelta), WardenEntityModel::getBody));
    }

    @Override
    public Identifier getTexture(WardenEntity arg) {
        return TEXTURE;
    }
}

