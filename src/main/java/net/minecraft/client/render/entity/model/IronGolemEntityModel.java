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
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class IronGolemEntityModel<T extends IronGolemEntity>
extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public IronGolemEntityModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -12.0f, -5.5f, 8.0f, 10.0f, 8.0f).uv(24, 0).cuboid(-1.0f, -5.0f, -7.5f, 2.0f, 4.0f, 2.0f), ModelTransform.pivot(0.0f, -7.0f, -2.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 40).cuboid(-9.0f, -2.0f, -6.0f, 18.0f, 12.0f, 11.0f).uv(0, 70).cuboid(-4.5f, 10.0f, -3.0f, 9.0f, 5.0f, 6.0f, new Dilation(0.5f)), ModelTransform.pivot(0.0f, -7.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(60, 21).cuboid(-13.0f, -2.5f, -3.0f, 4.0f, 30.0f, 6.0f), ModelTransform.pivot(0.0f, -7.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(60, 58).cuboid(9.0f, -2.5f, -3.0f, 4.0f, 30.0f, 6.0f), ModelTransform.pivot(0.0f, -7.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(37, 0).cuboid(-3.5f, -3.0f, -3.0f, 6.0f, 16.0f, 5.0f), ModelTransform.pivot(-4.0f, 11.0f, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(60, 0).mirrored().cuboid(-3.5f, -3.0f, -3.0f, 6.0f, 16.0f, 5.0f), ModelTransform.pivot(5.0f, 11.0f, 0.0f));
        return TexturedModelData.of(lv, 128, 128);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        this.head.yaw = i * ((float)Math.PI / 180);
        this.head.pitch = j * ((float)Math.PI / 180);
        this.rightLeg.pitch = -1.5f * MathHelper.wrap(f, 13.0f) * g;
        this.leftLeg.pitch = 1.5f * MathHelper.wrap(f, 13.0f) * g;
        this.rightLeg.yaw = 0.0f;
        this.leftLeg.yaw = 0.0f;
    }

    @Override
    public void animateModel(T arg, float f, float g, float h) {
        int i = ((IronGolemEntity)arg).getAttackTicksLeft();
        if (i > 0) {
            this.rightArm.pitch = -2.0f + 1.5f * MathHelper.wrap((float)i - h, 10.0f);
            this.leftArm.pitch = -2.0f + 1.5f * MathHelper.wrap((float)i - h, 10.0f);
        } else {
            int j = ((IronGolemEntity)arg).getLookingAtVillagerTicks();
            if (j > 0) {
                this.rightArm.pitch = -0.8f + 0.025f * MathHelper.wrap(j, 70.0f);
                this.leftArm.pitch = 0.0f;
            } else {
                this.rightArm.pitch = (-0.2f + 1.5f * MathHelper.wrap(f, 13.0f)) * g;
                this.leftArm.pitch = (-0.2f - 1.5f * MathHelper.wrap(f, 13.0f)) * g;
            }
        }
    }

    public ModelPart getRightArm() {
        return this.rightArm;
    }
}

