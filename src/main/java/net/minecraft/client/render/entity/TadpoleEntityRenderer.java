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
import net.minecraft.client.render.entity.model.TadpoleEntityModel;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class TadpoleEntityRenderer
extends MobEntityRenderer<TadpoleEntity, TadpoleEntityModel<TadpoleEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/tadpole/tadpole.png");

    public TadpoleEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new TadpoleEntityModel(arg.getPart(EntityModelLayers.TADPOLE)), 0.14f);
    }

    @Override
    public Identifier getTexture(TadpoleEntity arg) {
        return TEXTURE;
    }
}

