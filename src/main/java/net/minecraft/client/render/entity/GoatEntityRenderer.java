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
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GoatEntityModel;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class GoatEntityRenderer
extends MobEntityRenderer<GoatEntity, GoatEntityModel<GoatEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/goat/goat.png");

    public GoatEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new GoatEntityModel(arg.getPart(EntityModelLayers.GOAT)), 0.7f);
    }

    @Override
    public Identifier getTexture(GoatEntity arg) {
        return TEXTURE;
    }
}

