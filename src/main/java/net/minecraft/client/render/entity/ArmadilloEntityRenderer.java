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
import net.minecraft.client.render.entity.model.ArmadilloEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ArmadilloEntityRenderer
extends MobEntityRenderer<ArmadilloEntity, ArmadilloEntityModel> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/armadillo.png");

    public ArmadilloEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new ArmadilloEntityModel(arg.getPart(EntityModelLayers.ARMADILLO)), 0.4f);
    }

    @Override
    public Identifier getTexture(ArmadilloEntity arg) {
        return TEXTURE;
    }
}

