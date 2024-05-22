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
import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.WitherEntityModel;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WitherArmorFeatureRenderer
extends EnergySwirlOverlayFeatureRenderer<WitherEntity, WitherEntityModel<WitherEntity>> {
    private static final Identifier SKIN = Identifier.method_60656("textures/entity/wither/wither_armor.png");
    private final WitherEntityModel<WitherEntity> model;

    public WitherArmorFeatureRenderer(FeatureRendererContext<WitherEntity, WitherEntityModel<WitherEntity>> context, EntityModelLoader loader) {
        super(context);
        this.model = new WitherEntityModel(loader.getModelPart(EntityModelLayers.WITHER_ARMOR));
    }

    @Override
    protected float getEnergySwirlX(float partialAge) {
        return MathHelper.cos(partialAge * 0.02f) * 3.0f;
    }

    @Override
    protected Identifier getEnergySwirlTexture() {
        return SKIN;
    }

    @Override
    protected EntityModel<WitherEntity> getEnergySwirlModel() {
        return this.model;
    }
}

