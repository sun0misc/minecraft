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
import net.minecraft.client.render.entity.model.BatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BatEntityRenderer
extends MobEntityRenderer<BatEntity, BatEntityModel> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/bat.png");

    public BatEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new BatEntityModel(arg.getPart(EntityModelLayers.BAT)), 0.25f);
    }

    @Override
    public Identifier getTexture(BatEntity arg) {
        return TEXTURE;
    }
}

