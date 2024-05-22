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
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class WitchEntityModel<T extends Entity>
extends VillagerResemblingModel<T> {
    private boolean liftingNose;

    public WitchEntityModel(ModelPart arg) {
        super(arg);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = VillagerResemblingModel.getModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -10.0f, -4.0f, 8.0f, 10.0f, 8.0f), ModelTransform.NONE);
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(0, 64).cuboid(0.0f, 0.0f, 0.0f, 10.0f, 2.0f, 10.0f), ModelTransform.pivot(-5.0f, -10.03125f, -5.0f));
        ModelPartData lv5 = lv4.addChild("hat2", ModelPartBuilder.create().uv(0, 76).cuboid(0.0f, 0.0f, 0.0f, 7.0f, 4.0f, 7.0f), ModelTransform.of(1.75f, -4.0f, 2.0f, -0.05235988f, 0.0f, 0.02617994f));
        ModelPartData lv6 = lv5.addChild("hat3", ModelPartBuilder.create().uv(0, 87).cuboid(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f), ModelTransform.of(1.75f, -4.0f, 2.0f, -0.10471976f, 0.0f, 0.05235988f));
        lv6.addChild("hat4", ModelPartBuilder.create().uv(0, 95).cuboid(0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 1.0f, new Dilation(0.25f)), ModelTransform.of(1.75f, -2.0f, 2.0f, -0.20943952f, 0.0f, 0.10471976f));
        ModelPartData lv7 = lv3.getChild(EntityModelPartNames.NOSE);
        lv7.addChild("mole", ModelPartBuilder.create().uv(0, 0).cuboid(0.0f, 3.0f, -6.75f, 1.0f, 1.0f, 1.0f, new Dilation(-0.25f)), ModelTransform.pivot(0.0f, -2.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 128);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        this.nose.setPivot(0.0f, -2.0f, 0.0f);
        float k = 0.01f * (float)(((Entity)entity).getId() % 10);
        this.nose.pitch = MathHelper.sin((float)((Entity)entity).age * k) * 4.5f * ((float)Math.PI / 180);
        this.nose.yaw = 0.0f;
        this.nose.roll = MathHelper.cos((float)((Entity)entity).age * k) * 2.5f * ((float)Math.PI / 180);
        if (this.liftingNose) {
            this.nose.setPivot(0.0f, 1.0f, -1.5f);
            this.nose.pitch = -0.9f;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setLiftingNose(boolean liftingNose) {
        this.liftingNose = liftingNose;
    }
}

