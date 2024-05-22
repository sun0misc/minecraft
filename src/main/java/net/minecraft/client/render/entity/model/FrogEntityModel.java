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
import net.minecraft.client.render.entity.animation.FrogAnimations;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FrogEntity;

@Environment(value=EnvType.CLIENT)
public class FrogEntityModel<T extends FrogEntity>
extends SinglePartEntityModel<T> {
    private static final float WALKING_LIMB_ANGLE_SCALE = 1.5f;
    private static final float SWIMMING_LIMB_ANGLE_SCALE = 1.0f;
    private static final float LIMB_DISTANCE_SCALE = 2.5f;
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart tongue;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart croakingBody;

    public FrogEntityModel(ModelPart root) {
        this.root = root.getChild(EntityModelPartNames.ROOT);
        this.body = this.root.getChild(EntityModelPartNames.BODY);
        this.head = this.body.getChild(EntityModelPartNames.HEAD);
        this.eyes = this.head.getChild(EntityModelPartNames.EYES);
        this.tongue = this.body.getChild(EntityModelPartNames.TONGUE);
        this.leftArm = this.body.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightArm = this.body.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftLeg = this.root.getChild(EntityModelPartNames.LEFT_LEG);
        this.rightLeg = this.root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.croakingBody = this.body.getChild(EntityModelPartNames.CROAKING_BODY);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 24.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(3, 1).cuboid(-3.5f, -2.0f, -8.0f, 7.0f, 3.0f, 9.0f).uv(23, 22).cuboid(-3.5f, -1.0f, -8.0f, 7.0f, 0.0f, 9.0f), ModelTransform.pivot(0.0f, -2.0f, 4.0f));
        ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(23, 13).cuboid(-3.5f, -1.0f, -7.0f, 7.0f, 0.0f, 9.0f).uv(0, 13).cuboid(-3.5f, -2.0f, -7.0f, 7.0f, 3.0f, 9.0f), ModelTransform.pivot(0.0f, -2.0f, -1.0f));
        ModelPartData lv6 = lv5.addChild(EntityModelPartNames.EYES, ModelPartBuilder.create(), ModelTransform.pivot(-0.5f, 0.0f, 2.0f));
        lv6.addChild(EntityModelPartNames.RIGHT_EYE, ModelPartBuilder.create().uv(0, 0).cuboid(-1.5f, -1.0f, -1.5f, 3.0f, 2.0f, 3.0f), ModelTransform.pivot(-1.5f, -3.0f, -6.5f));
        lv6.addChild(EntityModelPartNames.LEFT_EYE, ModelPartBuilder.create().uv(0, 5).cuboid(-1.5f, -1.0f, -1.5f, 3.0f, 2.0f, 3.0f), ModelTransform.pivot(2.5f, -3.0f, -6.5f));
        lv4.addChild(EntityModelPartNames.CROAKING_BODY, ModelPartBuilder.create().uv(26, 5).cuboid(-3.5f, -0.1f, -2.9f, 7.0f, 2.0f, 3.0f, new Dilation(-0.1f)), ModelTransform.pivot(0.0f, -1.0f, -5.0f));
        ModelPartData lv7 = lv4.addChild(EntityModelPartNames.TONGUE, ModelPartBuilder.create().uv(17, 13).cuboid(-2.0f, 0.0f, -7.1f, 4.0f, 0.0f, 7.0f), ModelTransform.pivot(0.0f, -1.01f, 1.0f));
        ModelPartData lv8 = lv4.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 32).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 3.0f), ModelTransform.pivot(4.0f, -1.0f, -6.5f));
        lv8.addChild(EntityModelPartNames.LEFT_HAND, ModelPartBuilder.create().uv(18, 40).cuboid(-4.0f, 0.01f, -4.0f, 8.0f, 0.0f, 8.0f), ModelTransform.pivot(0.0f, 3.0f, -1.0f));
        ModelPartData lv9 = lv4.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(0, 38).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 3.0f), ModelTransform.pivot(-4.0f, -1.0f, -6.5f));
        lv9.addChild(EntityModelPartNames.RIGHT_HAND, ModelPartBuilder.create().uv(2, 40).cuboid(-4.0f, 0.01f, -5.0f, 8.0f, 0.0f, 8.0f), ModelTransform.pivot(0.0f, 3.0f, 0.0f));
        ModelPartData lv10 = lv3.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(14, 25).cuboid(-1.0f, 0.0f, -2.0f, 3.0f, 3.0f, 4.0f), ModelTransform.pivot(3.5f, -3.0f, 4.0f));
        lv10.addChild(EntityModelPartNames.LEFT_FOOT, ModelPartBuilder.create().uv(2, 32).cuboid(-4.0f, 0.01f, -4.0f, 8.0f, 0.0f, 8.0f), ModelTransform.pivot(2.0f, 3.0f, 0.0f));
        ModelPartData lv11 = lv3.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 25).cuboid(-2.0f, 0.0f, -2.0f, 3.0f, 3.0f, 4.0f), ModelTransform.pivot(-3.5f, -3.0f, 4.0f));
        lv11.addChild(EntityModelPartNames.RIGHT_FOOT, ModelPartBuilder.create().uv(18, 32).cuboid(-4.0f, 0.01f, -4.0f, 8.0f, 0.0f, 8.0f), ModelTransform.pivot(-2.0f, 3.0f, 0.0f));
        return TexturedModelData.of(lv, 48, 48);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.updateAnimation(((FrogEntity)arg).longJumpingAnimationState, FrogAnimations.LONG_JUMPING, h);
        this.updateAnimation(((FrogEntity)arg).croakingAnimationState, FrogAnimations.CROAKING, h);
        this.updateAnimation(((FrogEntity)arg).usingTongueAnimationState, FrogAnimations.USING_TONGUE, h);
        if (((Entity)arg).isInsideWaterOrBubbleColumn()) {
            this.animateMovement(FrogAnimations.SWIMMING, f, g, 1.0f, 2.5f);
        } else {
            this.animateMovement(FrogAnimations.WALKING, f, g, 1.5f, 2.5f);
        }
        this.updateAnimation(((FrogEntity)arg).idlingInWaterAnimationState, FrogAnimations.IDLING_IN_WATER, h);
        this.croakingBody.visible = ((FrogEntity)arg).croakingAnimationState.isRunning();
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}

