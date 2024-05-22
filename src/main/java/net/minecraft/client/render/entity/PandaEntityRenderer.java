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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.PandaHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PandaEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class PandaEntityRenderer
extends MobEntityRenderer<PandaEntity, PandaEntityModel<PandaEntity>> {
    private static final Map<PandaEntity.Gene, Identifier> TEXTURES = Util.make(Maps.newEnumMap(PandaEntity.Gene.class), map -> {
        map.put(PandaEntity.Gene.NORMAL, Identifier.method_60656("textures/entity/panda/panda.png"));
        map.put(PandaEntity.Gene.LAZY, Identifier.method_60656("textures/entity/panda/lazy_panda.png"));
        map.put(PandaEntity.Gene.WORRIED, Identifier.method_60656("textures/entity/panda/worried_panda.png"));
        map.put(PandaEntity.Gene.PLAYFUL, Identifier.method_60656("textures/entity/panda/playful_panda.png"));
        map.put(PandaEntity.Gene.BROWN, Identifier.method_60656("textures/entity/panda/brown_panda.png"));
        map.put(PandaEntity.Gene.WEAK, Identifier.method_60656("textures/entity/panda/weak_panda.png"));
        map.put(PandaEntity.Gene.AGGRESSIVE, Identifier.method_60656("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaEntityRenderer(EntityRendererFactory.Context arg) {
        super(arg, new PandaEntityModel(arg.getPart(EntityModelLayers.PANDA)), 0.9f);
        this.addFeature(new PandaHeldItemFeatureRenderer(this, arg.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(PandaEntity arg) {
        return TEXTURES.getOrDefault(arg.getProductGene(), TEXTURES.get(PandaEntity.Gene.NORMAL));
    }

    @Override
    protected void setupTransforms(PandaEntity arg, MatrixStack arg2, float f, float g, float h, float i) {
        float s;
        float r;
        float l;
        super.setupTransforms(arg, arg2, f, g, h, i);
        if (arg.playingTicks > 0) {
            float m;
            int j = arg.playingTicks;
            int k = j + 1;
            l = 7.0f;
            float f2 = m = arg.isBaby() ? 0.3f : 0.8f;
            if (j < 8) {
                float n = (float)(90 * j) / 7.0f;
                float o = (float)(90 * k) / 7.0f;
                float p = this.getAngle(n, o, k, h, 8.0f);
                arg2.translate(0.0f, (m + 0.2f) * (p / 90.0f), 0.0f);
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            } else if (j < 16) {
                float n = ((float)j - 8.0f) / 7.0f;
                float o = 90.0f + 90.0f * n;
                float q = 90.0f + 90.0f * ((float)k - 8.0f) / 7.0f;
                float p = this.getAngle(o, q, k, h, 16.0f);
                arg2.translate(0.0f, m + 0.2f + (m - 0.2f) * (p - 90.0f) / 90.0f, 0.0f);
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            } else if ((float)j < 24.0f) {
                float n = ((float)j - 16.0f) / 7.0f;
                float o = 180.0f + 90.0f * n;
                float q = 180.0f + 90.0f * ((float)k - 16.0f) / 7.0f;
                float p = this.getAngle(o, q, k, h, 24.0f);
                arg2.translate(0.0f, m + m * (270.0f - p) / 90.0f, 0.0f);
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            } else if (j < 32) {
                float n = ((float)j - 24.0f) / 7.0f;
                float o = 270.0f + 90.0f * n;
                float q = 270.0f + 90.0f * ((float)k - 24.0f) / 7.0f;
                float p = this.getAngle(o, q, k, h, 32.0f);
                arg2.translate(0.0f, m * ((360.0f - p) / 90.0f), 0.0f);
                arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-p));
            }
        }
        if ((r = arg.getSittingAnimationProgress(h)) > 0.0f) {
            arg2.translate(0.0f, 0.8f * r, 0.0f);
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(r, arg.getPitch(), arg.getPitch() + 90.0f)));
            arg2.translate(0.0f, -1.0f * r, 0.0f);
            if (arg.isScaredByThunderstorm()) {
                float s2 = (float)(Math.cos((double)arg.age * 1.25) * Math.PI * (double)0.05f);
                arg2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(s2));
                if (arg.isBaby()) {
                    arg2.translate(0.0f, 0.8f, 0.55f);
                }
            }
        }
        if ((s = arg.getLieOnBackAnimationProgress(h)) > 0.0f) {
            l = arg.isBaby() ? 0.5f : 1.3f;
            arg2.translate(0.0f, l * s, 0.0f);
            arg2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(s, arg.getPitch(), arg.getPitch() + 180.0f)));
        }
    }

    private float getAngle(float f, float g, int i, float h, float j) {
        if ((float)i < j) {
            return MathHelper.lerp(h, f, g);
        }
        return f;
    }
}

