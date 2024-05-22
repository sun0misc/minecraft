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
import net.minecraft.client.render.entity.model.EndermiteEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class EndermiteEntityRenderer
extends MobEntityRenderer<EndermiteEntity, EndermiteEntityModel<EndermiteEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/endermite.png");

    public EndermiteEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new EndermiteEntityModel(arg.getPart(EntityModelLayers.ENDERMITE)), 0.3f);
    }

    @Override
    protected float getLyingAngle(EndermiteEntity arg) {
        return 180.0f;
    }

    @Override
    public Identifier getTexture(EndermiteEntity arg) {
        return TEXTURE;
    }

    @Override
    protected /* synthetic */ float getLyingAngle(LivingEntity entity) {
        return this.getLyingAngle((EndermiteEntity)entity);
    }

    @Override
    public /* synthetic */ Identifier getTexture(Entity entity) {
        return this.getTexture((EndermiteEntity)entity);
    }
}

