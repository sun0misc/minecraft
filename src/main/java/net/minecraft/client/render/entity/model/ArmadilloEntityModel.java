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
import net.minecraft.client.render.entity.animation.ArmadilloAnimations;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SinglePartEntityModelWithChildTransform;
import net.minecraft.entity.passive.ArmadilloEntity;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class ArmadilloEntityModel
extends SinglePartEntityModelWithChildTransform<ArmadilloEntity> {
    private static final float field_47858 = 16.02f;
    private static final float field_47860 = 25.0f;
    private static final float field_47861 = 22.5f;
    private static final float field_47862 = 16.5f;
    private static final float field_47863 = 2.5f;
    private static final String HEAD_CUBE = "head_cube";
    private static final String RIGHT_EAR_CUBE = "right_ear_cube";
    private static final String LEFT_EAR_CUBE = "left_ear_cube";
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart cube;
    private final ModelPart head;
    private final ModelPart tail;

    public ArmadilloEntityModel(ModelPart root) {
        super(0.6f, 16.02f);
        this.root = root;
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
        this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
        this.head = this.body.getChild(EntityModelPartNames.HEAD);
        this.tail = this.body.getChild(EntityModelPartNames.TAIL);
        this.cube = root.getChild(EntityModelPartNames.CUBE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 20).cuboid(-4.0f, -7.0f, -10.0f, 8.0f, 8.0f, 12.0f, new Dilation(0.3f)).uv(0, 40).cuboid(-4.0f, -7.0f, -10.0f, 8.0f, 8.0f, 12.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 21.0f, 4.0f));
        lv3.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(44, 53).cuboid(-0.5f, -0.0865f, 0.0933f, 1.0f, 6.0f, 1.0f, new Dilation(0.0f)), ModelTransform.of(0.0f, -3.0f, 1.0f, 0.5061f, 0.0f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, -2.0f, -11.0f));
        lv4.addChild(HEAD_CUBE, ModelPartBuilder.create().uv(43, 15).cuboid(-1.5f, -1.0f, -1.0f, 3.0f, 5.0f, 2.0f, new Dilation(0.0f)), ModelTransform.of(0.0f, 0.0f, 0.0f, -0.3927f, 0.0f, 0.0f));
        ModelPartData lv5 = lv4.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create(), ModelTransform.pivot(-1.0f, -1.0f, 0.0f));
        lv5.addChild(RIGHT_EAR_CUBE, ModelPartBuilder.create().uv(43, 10).cuboid(-2.0f, -3.0f, 0.0f, 2.0f, 5.0f, 0.0f, new Dilation(0.0f)), ModelTransform.of(-0.5f, 0.0f, -0.6f, 0.1886f, -0.3864f, -0.0718f));
        ModelPartData lv6 = lv4.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create(), ModelTransform.pivot(1.0f, -2.0f, 0.0f));
        lv6.addChild(LEFT_EAR_CUBE, ModelPartBuilder.create().uv(47, 10).cuboid(0.0f, -3.0f, 0.0f, 2.0f, 5.0f, 0.0f, new Dilation(0.0f)), ModelTransform.of(0.5f, 1.0f, -0.6f, 0.1886f, 0.3864f, 0.0718f));
        lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(51, 31).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)), ModelTransform.pivot(-2.0f, 21.0f, 4.0f));
        lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(42, 31).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)), ModelTransform.pivot(2.0f, 21.0f, 4.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(51, 43).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)), ModelTransform.pivot(-2.0f, 21.0f, -4.0f));
        lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(42, 43).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new Dilation(0.0f)), ModelTransform.pivot(2.0f, 21.0f, -4.0f));
        lv2.addChild(EntityModelPartNames.CUBE, ModelPartBuilder.create().uv(0, 0).cuboid(-5.0f, -10.0f, -6.0f, 10.0f, 10.0f, 10.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, 24.0f, 0.0f));
        return TexturedModelData.of(lv, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(ArmadilloEntity arg, float f, float g, float h, float i, float j) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        if (arg.isRolledUp()) {
            this.body.hidden = true;
            this.leftHindLeg.visible = false;
            this.rightHindLeg.visible = false;
            this.tail.visible = false;
            this.cube.visible = true;
        } else {
            this.body.hidden = false;
            this.leftHindLeg.visible = true;
            this.rightHindLeg.visible = true;
            this.tail.visible = true;
            this.cube.visible = false;
            this.head.pitch = MathHelper.clamp(j, -22.5f, 25.0f) * ((float)Math.PI / 180);
            this.head.yaw = MathHelper.clamp(i, -32.5f, 32.5f) * ((float)Math.PI / 180);
        }
        this.animateMovement(ArmadilloAnimations.IDLE, f, g, 16.5f, 2.5f);
        this.updateAnimation(arg.unrollingAnimationState, ArmadilloAnimations.UNROLLING, h, 1.0f);
        this.updateAnimation(arg.rollingAnimationState, ArmadilloAnimations.ROLLING, h, 1.0f);
        this.updateAnimation(arg.scaredAnimationState, ArmadilloAnimations.SCARED, h, 1.0f);
    }
}

