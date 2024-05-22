/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.AbstractHorseEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.DonkeyEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class DonkeyEntityRenderer<T extends AbstractDonkeyEntity>
extends AbstractHorseEntityRenderer<T, DonkeyEntityModel<T>> {
    private static final Map<EntityType<?>, Identifier> TEXTURES = Maps.newHashMap(ImmutableMap.of(EntityType.DONKEY, Identifier.method_60656("textures/entity/horse/donkey.png"), EntityType.MULE, Identifier.method_60656("textures/entity/horse/mule.png")));

    public DonkeyEntityRenderer(EntityRendererFactory.Context ctx, float scale, EntityModelLayer layer) {
        super(ctx, new DonkeyEntityModel(ctx.getPart(layer)), scale);
    }

    @Override
    public Identifier getTexture(T arg) {
        return TEXTURES.get(((Entity)arg).getType());
    }
}

