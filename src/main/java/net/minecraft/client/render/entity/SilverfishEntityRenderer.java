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
import net.minecraft.client.render.entity.model.SilverfishEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SilverfishEntityRenderer
extends MobEntityRenderer<SilverfishEntity, SilverfishEntityModel<SilverfishEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/silverfish.png");

    public SilverfishEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new SilverfishEntityModel(arg.getPart(EntityModelLayers.SILVERFISH)), 0.3f);
    }

    @Override
    protected float getLyingAngle(SilverfishEntity arg) {
        return 180.0f;
    }

    @Override
    public Identifier getTexture(SilverfishEntity arg) {
        return TEXTURE;
    }

    @Override
    protected /* synthetic */ float getLyingAngle(LivingEntity entity) {
        return this.getLyingAngle((SilverfishEntity)entity);
    }

    @Override
    public /* synthetic */ Identifier getTexture(Entity entity) {
        return this.getTexture((SilverfishEntity)entity);
    }
}

