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
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SpectralArrowEntityRenderer
extends ProjectileEntityRenderer<SpectralArrowEntity> {
    public static final Identifier TEXTURE = Identifier.method_60656("textures/entity/projectiles/spectral_arrow.png");

    public SpectralArrowEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg);
    }

    @Override
    public Identifier getTexture(SpectralArrowEntity arg) {
        return TEXTURE;
    }
}

