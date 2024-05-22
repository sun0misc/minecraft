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
import net.minecraft.client.render.entity.feature.DolphinHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.DolphinEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class DolphinEntityRenderer
extends MobEntityRenderer<DolphinEntity, DolphinEntityModel<DolphinEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/dolphin.png");

    public DolphinEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new DolphinEntityModel(arg.getPart(EntityModelLayers.DOLPHIN)), 0.7f);
        this.addFeature(new DolphinHeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(DolphinEntity arg) {
        return TEXTURE;
    }
}

