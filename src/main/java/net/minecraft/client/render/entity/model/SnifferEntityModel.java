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
import net.minecraft.client.render.entity.animation.SnifferAnimations;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModelWithChildTransform;
import net.minecraft.entity.passive.SnifferEntity;

@Environment(value=EnvType.CLIENT)
public class SnifferEntityModel<T extends SnifferEntity>
extends SinglePartEntityModelWithChildTransform<T> {
    private static final float LIMB_ANGLE_SCALE = 9.0f;
    private static final float LIMB_DISTANCE_SCALE = 100.0f;
    private final ModelPart root;
    private final ModelPart head;

    public SnifferEntityModel(ModelPart root) {
        super(0.5f, 24.0f);
        this.root = root.getChild(EntityModelPartNames.ROOT);
        this.head = this.root.getChild(EntityModelPartNames.BONE).getChild(EntityModelPartNames.BODY).getChild(EntityModelPartNames.HEAD);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot().addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 5.0f, 0.0f));
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BONE, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(62, 68).cuboid(-12.5f, -14.0f, -20.0f, 25.0f, 29.0f, 40.0f, new Dilation(0.0f)).uv(62, 0).cuboid(-12.5f, -14.0f, -20.0f, 25.0f, 24.0f, 40.0f, new Dilation(0.5f)).uv(87, 68).cuboid(-12.5f, 12.0f, -20.0f, 25.0f, 0.0f, 40.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 0.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(32, 87).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(-7.5f, 10.0f, -15.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_MID_LEG, ModelPartBuilder.create().uv(32, 105).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(-7.5f, 10.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(32, 123).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(-7.5f, 10.0f, 15.0f));
        lv3.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(0, 87).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(7.5f, 10.0f, -15.0f));
        lv3.addChild(EntityModelPartNames.LEFT_MID_LEG, ModelPartBuilder.create().uv(0, 105).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(7.5f, 10.0f, 0.0f));
        lv3.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(0, 123).cuboid(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(7.5f, 10.0f, 15.0f));
        ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(8, 15).cuboid(-6.5f, -7.5f, -11.5f, 13.0f, 18.0f, 11.0f, new Dilation(0.0f)).uv(8, 4).cuboid(-6.5f, 7.5f, -11.5f, 13.0f, 0.0f, 11.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 6.5f, -19.48f));
        lv5.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(2, 0).cuboid(0.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new Dilation(0.0f)), ModelTransform.pivot(6.51f, -7.5f, -4.51f));
        lv5.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(48, 0).cuboid(-1.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new Dilation(0.0f)), ModelTransform.pivot(-6.51f, -7.5f, -4.51f));
        lv5.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(10, 45).cuboid(-6.5f, -2.0f, -9.0f, 13.0f, 2.0f, 9.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, -4.5f, -11.5f));
        lv5.addChild("lower_beak", ModelPartBuilder.create().uv(10, 57).cuboid(-6.5f, -7.0f, -8.0f, 13.0f, 12.0f, 9.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 2.5f, -12.5f));
        return TexturedModelData.of(lv, 192, 192);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.head.pitch = j * ((float)Math.PI / 180);
        this.head.yaw = i * ((float)Math.PI / 180);
        if (((SnifferEntity)arg).isSearching()) {
            this.animateMovement(SnifferAnimations.SEARCHING, f, g, 9.0f, 100.0f);
        } else {
            this.animateMovement(SnifferAnimations.WALKING, f, g, 9.0f, 100.0f);
        }
        this.updateAnimation(((SnifferEntity)arg).diggingAnimationState, SnifferAnimations.DIGGING, h);
        this.updateAnimation(((SnifferEntity)arg).sniffingAnimationState, SnifferAnimations.SNIFFING, h);
        this.updateAnimation(((SnifferEntity)arg).risingAnimationState, SnifferAnimations.RISING, h);
        this.updateAnimation(((SnifferEntity)arg).feelingHappyAnimationState, SnifferAnimations.FEELING_HAPPY, h);
        this.updateAnimation(((SnifferEntity)arg).scentingAnimationState, SnifferAnimations.SCENTING, h);
        if (this.child) {
            this.animate(SnifferAnimations.BABY_GROWTH);
        }
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}

