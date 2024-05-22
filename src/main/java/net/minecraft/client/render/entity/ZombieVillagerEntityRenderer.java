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
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieVillagerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerEntityRenderer
extends BipedEntityRenderer<ZombieVillagerEntity, ZombieVillagerEntityModel<ZombieVillagerEntity>> {
    private static final Identifier TEXTURE = Identifier.method_60656("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieVillagerEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new ZombieVillagerEntityModel(arg.getPart(EntityModelLayers.ZOMBIE_VILLAGER)), 0.5f);
        this.addFeature(new ArmorFeatureRenderer(this, new ZombieVillagerEntityModel(arg.getPart(EntityModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)), new ZombieVillagerEntityModel(arg.getPart(EntityModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)), arg.getModelManager()));
        this.addFeature(new VillagerClothingFeatureRenderer<ZombieVillagerEntity, ZombieVillagerEntityModel<ZombieVillagerEntity>>(this, arg.getResourceManager(), "zombie_villager"));
    }

    @Override
    public Identifier getTexture(ZombieVillagerEntity arg) {
        return TEXTURE;
    }

    @Override
    protected boolean isShaking(ZombieVillagerEntity arg) {
        return super.isShaking(arg) || arg.isConverting();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity entity) {
        return this.isShaking((ZombieVillagerEntity)entity);
    }
}

