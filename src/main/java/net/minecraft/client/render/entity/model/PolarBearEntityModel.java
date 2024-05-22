/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.passive.PolarBearEntity;

@Environment(value=EnvType.CLIENT)
public class PolarBearEntityModel<T extends PolarBearEntity>
extends QuadrupedEntityModel<T> {
    public PolarBearEntityModel(ModelPart root) {
        super(root, true, 16.0f, 4.0f, 2.25f, 2.0f, 24);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.5f, -3.0f, -3.0f, 7.0f, 7.0f, 7.0f).uv(0, 44).cuboid(EntityModelPartNames.MOUTH, -2.5f, 1.0f, -6.0f, 5.0f, 3.0f, 3.0f).uv(26, 0).cuboid(EntityModelPartNames.RIGHT_EAR, -4.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f).uv(26, 0).mirrored().cuboid(EntityModelPartNames.LEFT_EAR, 2.5f, -4.0f, -1.0f, 2.0f, 2.0f, 1.0f), ModelTransform.pivot(0.0f, 10.0f, -16.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 19).cuboid(-5.0f, -13.0f, -7.0f, 14.0f, 14.0f, 11.0f).uv(39, 0).cuboid(-4.0f, -25.0f, -7.0f, 12.0f, 12.0f, 10.0f), ModelTransform.of(-2.0f, 9.0f, 12.0f, 1.5707964f, 0.0f, 0.0f));
        int i = 10;
        ModelPartBuilder lv3 = ModelPartBuilder.create().uv(50, 22).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 8.0f);
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-4.5f, 14.0f, 6.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(4.5f, 14.0f, 6.0f));
        ModelPartBuilder lv4 = ModelPartBuilder.create().uv(50, 40).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 6.0f);
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv4, ModelTransform.pivot(-3.5f, 14.0f, -8.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv4, ModelTransform.pivot(3.5f, 14.0f, -8.0f));
        return TexturedModelData.of(lv, 128, 64);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        super.setAngles(arg, f, g, h, i, j);
        float k = h - (float)((PolarBearEntity)arg).age;
        float l = ((PolarBearEntity)arg).getWarningAnimationProgress(k);
        l *= l;
        float m = 1.0f - l;
        this.body.pitch = 1.5707964f - l * (float)Math.PI * 0.35f;
        this.body.pivotY = 9.0f * m + 11.0f * l;
        this.rightFrontLeg.pivotY = 14.0f * m - 6.0f * l;
        this.rightFrontLeg.pivotZ = -8.0f * m - 4.0f * l;
        this.rightFrontLeg.pitch -= l * (float)Math.PI * 0.45f;
        this.leftFrontLeg.pivotY = this.rightFrontLeg.pivotY;
        this.leftFrontLeg.pivotZ = this.rightFrontLeg.pivotZ;
        this.leftFrontLeg.pitch -= l * (float)Math.PI * 0.45f;
        if (this.child) {
            this.head.pivotY = 10.0f * m - 9.0f * l;
            this.head.pivotZ = -16.0f * m - 7.0f * l;
        } else {
            this.head.pivotY = 10.0f * m - 14.0f * l;
            this.head.pivotZ = -16.0f * m - 3.0f * l;
        }
        this.head.pitch += l * (float)Math.PI * 0.15f;
    }
}

