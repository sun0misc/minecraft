/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.AbstractHorseEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.HorseArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HorseMarkingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public final class HorseEntityRenderer
extends AbstractHorseEntityRenderer<HorseEntity, HorseEntityModel<HorseEntity>> {
    private static final Map<HorseColor, Identifier> TEXTURES = Util.make(Maps.newEnumMap(HorseColor.class), map -> {
        map.put(HorseColor.WHITE, Identifier.method_60656("textures/entity/horse/horse_white.png"));
        map.put(HorseColor.CREAMY, Identifier.method_60656("textures/entity/horse/horse_creamy.png"));
        map.put(HorseColor.CHESTNUT, Identifier.method_60656("textures/entity/horse/horse_chestnut.png"));
        map.put(HorseColor.BROWN, Identifier.method_60656("textures/entity/horse/horse_brown.png"));
        map.put(HorseColor.BLACK, Identifier.method_60656("textures/entity/horse/horse_black.png"));
        map.put(HorseColor.GRAY, Identifier.method_60656("textures/entity/horse/horse_gray.png"));
        map.put(HorseColor.DARK_BROWN, Identifier.method_60656("textures/entity/horse/horse_darkbrown.png"));
    });

    public HorseEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new HorseEntityModel(arg.getPart(EntityModelLayers.HORSE)), 1.1f);
        this.addFeature(new HorseMarkingFeatureRenderer(this));
        this.addFeature(new HorseArmorFeatureRenderer(this, arg.getModelLoader()));
    }

    @Override
    public Identifier getTexture(HorseEntity arg) {
        return TEXTURES.get(arg.getVariant());
    }
}

