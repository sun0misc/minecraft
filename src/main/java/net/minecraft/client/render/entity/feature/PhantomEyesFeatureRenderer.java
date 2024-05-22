/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class PhantomEyesFeatureRenderer<T extends PhantomEntity>
extends EyesFeatureRenderer<T, PhantomEntityModel<T>> {
    private static final RenderLayer SKIN = RenderLayer.getEyes(Identifier.method_60656("textures/entity/phantom_eyes.png"));

    public PhantomEyesFeatureRenderer(FeatureRendererContext<T, PhantomEntityModel<T>> arg) {
        super(arg);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return SKIN;
    }
}

