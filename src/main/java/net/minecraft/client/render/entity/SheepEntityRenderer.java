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
import net.minecraft.client.render.entity.feature.SheepWoolFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SheepEntityRenderer
extends MobEntityRenderer<SheepEntity, SheepEntityModel<SheepEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/sheep/sheep.png");

    public SheepEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new SheepEntityModel(arg.getPart(EntityModelLayers.SHEEP)), 0.7f);
        this.addFeature(new SheepWoolFeatureRenderer(this, arg.getModelLoader()));
    }

    @Override
    public Identifier getTexture(SheepEntity arg) {
        return TEXTURE;
    }
}

