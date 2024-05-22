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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class AllayEntityModel
extends SinglePartEntityModel<AllayEntity>
implements ModelWithArms {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private static final float field_38999 = 0.7853982f;
    private static final float field_39000 = -1.134464f;
    private static final float field_39001 = -1.0471976f;

    public AllayEntityModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.root = root.getChild(EntityModelPartNames.ROOT);
        this.head = this.root.getChild(EntityModelPartNames.HEAD);
        this.body = this.root.getChild(EntityModelPartNames.BODY);
        this.rightArm = this.body.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = this.body.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightWing = this.body.getChild(EntityModelPartNames.RIGHT_WING);
        this.leftWing = this.body.getChild(EntityModelPartNames.LEFT_WING);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        ModelPartData lv3 = lv2.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0f, 23.5f, 0.0f));
        lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f, new Dilation(0.0f)), ModelTransform.pivot(0.0f, -3.99f, 0.0f));
        ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 10).cuboid(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f, new Dilation(0.0f)).uv(0, 16).cuboid(-1.5f, 0.0f, -1.0f, 3.0f, 5.0f, 2.0f, new Dilation(-0.2f)), ModelTransform.pivot(0.0f, -4.0f, 0.0f));
        lv4.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(23, 0).cuboid(-0.75f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new Dilation(-0.01f)), ModelTransform.pivot(-1.75f, 0.5f, 0.0f));
        lv4.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(23, 6).cuboid(-0.25f, -0.5f, -1.0f, 1.0f, 4.0f, 2.0f, new Dilation(-0.01f)), ModelTransform.pivot(1.75f, 0.5f, 0.0f));
        lv4.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(16, 14).cuboid(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(-0.5f, 0.0f, 0.6f));
        lv4.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(16, 14).cuboid(0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 8.0f, new Dilation(0.0f)), ModelTransform.pivot(0.5f, 0.0f, 0.6f));
        return TexturedModelData.of(lv, 32, 32);
    }

    @Override
    public void setAngles(AllayEntity arg, float f, float g, float h, float i, float j) {
        float t;
        float s;
        float r;
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        float k = h * 20.0f * ((float)Math.PI / 180) + f;
        float l = MathHelper.cos(k) * (float)Math.PI * 0.15f + g;
        float m = h - (float)arg.age;
        float n = h * 9.0f * ((float)Math.PI / 180);
        float o = Math.min(g / 0.3f, 1.0f);
        float p = 1.0f - o;
        float q = arg.method_43397(m);
        if (arg.isDancing()) {
            r = h * 8.0f * ((float)Math.PI / 180) + g;
            s = MathHelper.cos(r) * 16.0f * ((float)Math.PI / 180);
            t = arg.method_44368(m);
            float u = MathHelper.cos(r) * 14.0f * ((float)Math.PI / 180);
            float v = MathHelper.cos(r) * 30.0f * ((float)Math.PI / 180);
            this.root.yaw = arg.isSpinning() ? (float)Math.PI * 4 * t : this.root.yaw;
            this.root.roll = s * (1.0f - t);
            this.head.yaw = v * (1.0f - t);
            this.head.roll = u * (1.0f - t);
        } else {
            this.head.pitch = j * ((float)Math.PI / 180);
            this.head.yaw = i * ((float)Math.PI / 180);
        }
        this.rightWing.pitch = 0.43633232f * (1.0f - o);
        this.rightWing.yaw = -0.7853982f + l;
        this.leftWing.pitch = 0.43633232f * (1.0f - o);
        this.leftWing.yaw = 0.7853982f - l;
        this.body.pitch = o * 0.7853982f;
        r = q * MathHelper.lerp(o, -1.0471976f, -1.134464f);
        this.root.pivotY += (float)Math.cos(n) * 0.25f * p;
        this.rightArm.pitch = r;
        this.leftArm.pitch = r;
        s = p * (1.0f - q);
        t = 0.43633232f - MathHelper.cos(n + 4.712389f) * (float)Math.PI * 0.075f * s;
        this.leftArm.roll = -t;
        this.rightArm.roll = t;
        this.rightArm.yaw = 0.27925268f * q;
        this.leftArm.yaw = -0.27925268f * q;
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        float f = 1.0f;
        float g = 3.0f;
        this.root.rotate(matrices);
        this.body.rotate(matrices);
        matrices.translate(0.0f, 0.0625f, 0.1875f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(this.rightArm.pitch));
        matrices.scale(0.7f, 0.7f, 0.7f);
        matrices.translate(0.0625f, 0.0f, 0.0f);
    }
}

