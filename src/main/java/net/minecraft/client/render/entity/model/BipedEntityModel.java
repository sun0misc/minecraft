/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class BipedEntityModel<T extends LivingEntity>
extends AnimalModel<T>
implements ModelWithArms,
ModelWithHead {
    public static final float field_32505 = 0.25f;
    public static final float field_32506 = 0.5f;
    public static final float field_42513 = -0.1f;
    private static final float field_42512 = 0.005f;
    private static final float SPYGLASS_ARM_YAW_OFFSET = 0.2617994f;
    private static final float SPYGLASS_ARM_PITCH_OFFSET = 1.9198622f;
    private static final float SPYGLASS_SNEAKING_ARM_PITCH_OFFSET = 0.2617994f;
    private static final float field_46576 = -1.3962634f;
    private static final float field_46577 = 0.43633232f;
    private static final float field_46724 = 0.5235988f;
    public static final float field_39069 = 1.4835298f;
    public static final float field_39070 = 0.5235988f;
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public ArmPose leftArmPose = ArmPose.EMPTY;
    public ArmPose rightArmPose = ArmPose.EMPTY;
    public boolean sneaking;
    public float leaningPitch;

    public BipedEntityModel(ModelPart root) {
        this(root, RenderLayer::getEntityCutoutNoCull);
    }

    public BipedEntityModel(ModelPart root, Function<Identifier, RenderLayer> renderLayerFactory) {
        super(renderLayerFactory, true, 16.0f, 0.0f, 2.0f, 2.0f, 24.0f);
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.hat = root.getChild(EntityModelPartNames.HAT);
        this.body = root.getChild(EntityModelPartNames.BODY);
        this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
        this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
    }

    public static ModelData getModelData(Dilation dilation, float pivotOffsetY) {
        ModelData lv = new ModelData();
        ModelPartData lv2 = lv.getRoot();
        lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, dilation), ModelTransform.pivot(0.0f, 0.0f + pivotOffsetY, 0.0f));
        lv2.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, dilation.add(0.5f)), ModelTransform.pivot(0.0f, 0.0f + pivotOffsetY, 0.0f));
        lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, dilation), ModelTransform.pivot(0.0f, 0.0f + pivotOffsetY, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.pivot(-5.0f, 2.0f + pivotOffsetY, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.pivot(5.0f, 2.0f + pivotOffsetY, 0.0f));
        lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.pivot(-1.9f, 12.0f + pivotOffsetY, 0.0f));
        lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, dilation), ModelTransform.pivot(1.9f, 12.0f + pivotOffsetY, 0.0f));
        return lv;
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
    }

    @Override
    public void animateModel(T arg, float f, float g, float h) {
        this.leaningPitch = ((LivingEntity)arg).getLeaningPitch(h);
        super.animateModel(arg, f, g, h);
    }

    @Override
    public void setAngles(T arg, float f, float g, float h, float i, float j) {
        boolean bl3;
        boolean bl = ((LivingEntity)arg).getFallFlyingTicks() > 4;
        boolean bl2 = ((LivingEntity)arg).isInSwimmingPose();
        this.head.yaw = i * ((float)Math.PI / 180);
        this.head.pitch = bl ? -0.7853982f : (this.leaningPitch > 0.0f ? (bl2 ? this.lerpAngle(this.leaningPitch, this.head.pitch, -0.7853982f) : this.lerpAngle(this.leaningPitch, this.head.pitch, j * ((float)Math.PI / 180))) : j * ((float)Math.PI / 180));
        this.body.yaw = 0.0f;
        this.rightArm.pivotZ = 0.0f;
        this.rightArm.pivotX = -5.0f;
        this.leftArm.pivotZ = 0.0f;
        this.leftArm.pivotX = 5.0f;
        float k = 1.0f;
        if (bl) {
            k = (float)((Entity)arg).getVelocity().lengthSquared();
            k /= 0.2f;
            k *= k * k;
        }
        if (k < 1.0f) {
            k = 1.0f;
        }
        this.rightArm.pitch = MathHelper.cos(f * 0.6662f + (float)Math.PI) * 2.0f * g * 0.5f / k;
        this.leftArm.pitch = MathHelper.cos(f * 0.6662f) * 2.0f * g * 0.5f / k;
        this.rightArm.roll = 0.0f;
        this.leftArm.roll = 0.0f;
        this.rightLeg.pitch = MathHelper.cos(f * 0.6662f) * 1.4f * g / k;
        this.leftLeg.pitch = MathHelper.cos(f * 0.6662f + (float)Math.PI) * 1.4f * g / k;
        this.rightLeg.yaw = 0.005f;
        this.leftLeg.yaw = -0.005f;
        this.rightLeg.roll = 0.005f;
        this.leftLeg.roll = -0.005f;
        if (this.riding) {
            this.rightArm.pitch += -0.62831855f;
            this.leftArm.pitch += -0.62831855f;
            this.rightLeg.pitch = -1.4137167f;
            this.rightLeg.yaw = 0.31415927f;
            this.rightLeg.roll = 0.07853982f;
            this.leftLeg.pitch = -1.4137167f;
            this.leftLeg.yaw = -0.31415927f;
            this.leftLeg.roll = -0.07853982f;
        }
        this.rightArm.yaw = 0.0f;
        this.leftArm.yaw = 0.0f;
        boolean bl4 = bl3 = ((LivingEntity)arg).getMainArm() == Arm.RIGHT;
        if (((LivingEntity)arg).isUsingItem()) {
            boolean bl5 = bl4 = ((LivingEntity)arg).getActiveHand() == Hand.MAIN_HAND;
            if (bl4 == bl3) {
                this.positionRightArm(arg);
            } else {
                this.positionLeftArm(arg);
            }
        } else {
            boolean bl6 = bl4 = bl3 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
            if (bl3 != bl4) {
                this.positionLeftArm(arg);
                this.positionRightArm(arg);
            } else {
                this.positionRightArm(arg);
                this.positionLeftArm(arg);
            }
        }
        this.animateArms(arg, h);
        if (this.sneaking) {
            this.body.pitch = 0.5f;
            this.rightArm.pitch += 0.4f;
            this.leftArm.pitch += 0.4f;
            this.rightLeg.pivotZ = 4.0f;
            this.leftLeg.pivotZ = 4.0f;
            this.rightLeg.pivotY = 12.2f;
            this.leftLeg.pivotY = 12.2f;
            this.head.pivotY = 4.2f;
            this.body.pivotY = 3.2f;
            this.leftArm.pivotY = 5.2f;
            this.rightArm.pivotY = 5.2f;
        } else {
            this.body.pitch = 0.0f;
            this.rightLeg.pivotZ = 0.0f;
            this.leftLeg.pivotZ = 0.0f;
            this.rightLeg.pivotY = 12.0f;
            this.leftLeg.pivotY = 12.0f;
            this.head.pivotY = 0.0f;
            this.body.pivotY = 0.0f;
            this.leftArm.pivotY = 2.0f;
            this.rightArm.pivotY = 2.0f;
        }
        if (this.rightArmPose != ArmPose.SPYGLASS) {
            CrossbowPosing.swingArm(this.rightArm, h, 1.0f);
        }
        if (this.leftArmPose != ArmPose.SPYGLASS) {
            CrossbowPosing.swingArm(this.leftArm, h, -1.0f);
        }
        if (this.leaningPitch > 0.0f) {
            float o;
            float n;
            float l = f % 26.0f;
            Arm lv = this.getPreferredArm(arg);
            float m = lv == Arm.RIGHT && this.handSwingProgress > 0.0f ? 0.0f : this.leaningPitch;
            float f2 = n = lv == Arm.LEFT && this.handSwingProgress > 0.0f ? 0.0f : this.leaningPitch;
            if (!((LivingEntity)arg).isUsingItem()) {
                if (l < 14.0f) {
                    this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 0.0f);
                    this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 0.0f);
                    this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
                    this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, (float)Math.PI);
                    this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, (float)Math.PI + 1.8707964f * this.method_2807(l) / this.method_2807(14.0f));
                    this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, (float)Math.PI - 1.8707964f * this.method_2807(l) / this.method_2807(14.0f));
                } else if (l >= 14.0f && l < 22.0f) {
                    o = (l - 14.0f) / 8.0f;
                    this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 1.5707964f * o);
                    this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 1.5707964f * o);
                    this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
                    this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, (float)Math.PI);
                    this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, 5.012389f - 1.8707964f * o);
                    this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, 1.2707963f + 1.8707964f * o);
                } else if (l >= 22.0f && l < 26.0f) {
                    o = (l - 22.0f) / 4.0f;
                    this.leftArm.pitch = this.lerpAngle(n, this.leftArm.pitch, 1.5707964f - 1.5707964f * o);
                    this.rightArm.pitch = MathHelper.lerp(m, this.rightArm.pitch, 1.5707964f - 1.5707964f * o);
                    this.leftArm.yaw = this.lerpAngle(n, this.leftArm.yaw, (float)Math.PI);
                    this.rightArm.yaw = MathHelper.lerp(m, this.rightArm.yaw, (float)Math.PI);
                    this.leftArm.roll = this.lerpAngle(n, this.leftArm.roll, (float)Math.PI);
                    this.rightArm.roll = MathHelper.lerp(m, this.rightArm.roll, (float)Math.PI);
                }
            }
            o = 0.3f;
            float p = 0.33333334f;
            this.leftLeg.pitch = MathHelper.lerp(this.leaningPitch, this.leftLeg.pitch, 0.3f * MathHelper.cos(f * 0.33333334f + (float)Math.PI));
            this.rightLeg.pitch = MathHelper.lerp(this.leaningPitch, this.rightLeg.pitch, 0.3f * MathHelper.cos(f * 0.33333334f));
        }
        this.hat.copyTransform(this.head);
    }

    private void positionRightArm(T entity) {
        switch (this.rightArmPose.ordinal()) {
            case 0: {
                this.rightArm.yaw = 0.0f;
                break;
            }
            case 2: {
                this.positionBlockingArm(this.rightArm, true);
                break;
            }
            case 1: {
                this.rightArm.pitch = this.rightArm.pitch * 0.5f - 0.31415927f;
                this.rightArm.yaw = 0.0f;
                break;
            }
            case 4: {
                this.rightArm.pitch = this.rightArm.pitch * 0.5f - (float)Math.PI;
                this.rightArm.yaw = 0.0f;
                break;
            }
            case 3: {
                this.rightArm.yaw = -0.1f + this.head.yaw;
                this.leftArm.yaw = 0.1f + this.head.yaw + 0.4f;
                this.rightArm.pitch = -1.5707964f + this.head.pitch;
                this.leftArm.pitch = -1.5707964f + this.head.pitch;
                break;
            }
            case 5: {
                CrossbowPosing.charge(this.rightArm, this.leftArm, entity, true);
                break;
            }
            case 6: {
                CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, true);
                break;
            }
            case 9: {
                this.rightArm.pitch = this.rightArm.pitch * 0.5f - 0.62831855f;
                this.rightArm.yaw = 0.0f;
                break;
            }
            case 7: {
                this.rightArm.pitch = MathHelper.clamp(this.head.pitch - 1.9198622f - (((Entity)entity).isInSneakingPose() ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.rightArm.yaw = this.head.yaw - 0.2617994f;
                break;
            }
            case 8: {
                this.rightArm.pitch = MathHelper.clamp(this.head.pitch, -1.2f, 1.2f) - 1.4835298f;
                this.rightArm.yaw = this.head.yaw - 0.5235988f;
            }
        }
    }

    private void positionLeftArm(T entity) {
        switch (this.leftArmPose.ordinal()) {
            case 0: {
                this.leftArm.yaw = 0.0f;
                break;
            }
            case 2: {
                this.positionBlockingArm(this.leftArm, false);
                break;
            }
            case 1: {
                this.leftArm.pitch = this.leftArm.pitch * 0.5f - 0.31415927f;
                this.leftArm.yaw = 0.0f;
                break;
            }
            case 4: {
                this.leftArm.pitch = this.leftArm.pitch * 0.5f - (float)Math.PI;
                this.leftArm.yaw = 0.0f;
                break;
            }
            case 3: {
                this.rightArm.yaw = -0.1f + this.head.yaw - 0.4f;
                this.leftArm.yaw = 0.1f + this.head.yaw;
                this.rightArm.pitch = -1.5707964f + this.head.pitch;
                this.leftArm.pitch = -1.5707964f + this.head.pitch;
                break;
            }
            case 5: {
                CrossbowPosing.charge(this.rightArm, this.leftArm, entity, false);
                break;
            }
            case 6: {
                CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, false);
                break;
            }
            case 9: {
                this.leftArm.pitch = this.leftArm.pitch * 0.5f - 0.62831855f;
                this.leftArm.yaw = 0.0f;
                break;
            }
            case 7: {
                this.leftArm.pitch = MathHelper.clamp(this.head.pitch - 1.9198622f - (((Entity)entity).isInSneakingPose() ? 0.2617994f : 0.0f), -2.4f, 3.3f);
                this.leftArm.yaw = this.head.yaw + 0.2617994f;
                break;
            }
            case 8: {
                this.leftArm.pitch = MathHelper.clamp(this.head.pitch, -1.2f, 1.2f) - 1.4835298f;
                this.leftArm.yaw = this.head.yaw + 0.5235988f;
            }
        }
    }

    private void positionBlockingArm(ModelPart arm, boolean rightArm) {
        arm.pitch = arm.pitch * 0.5f - 0.9424779f + MathHelper.clamp(this.head.pitch, -1.3962634f, 0.43633232f);
        arm.yaw = (rightArm ? -30.0f : 30.0f) * ((float)Math.PI / 180) + MathHelper.clamp(this.head.yaw, -0.5235988f, 0.5235988f);
    }

    protected void animateArms(T entity, float animationProgress) {
        if (this.handSwingProgress <= 0.0f) {
            return;
        }
        Arm lv = this.getPreferredArm(entity);
        ModelPart lv2 = this.getArm(lv);
        float g = this.handSwingProgress;
        this.body.yaw = MathHelper.sin(MathHelper.sqrt(g) * ((float)Math.PI * 2)) * 0.2f;
        if (lv == Arm.LEFT) {
            this.body.yaw *= -1.0f;
        }
        this.rightArm.pivotZ = MathHelper.sin(this.body.yaw) * 5.0f;
        this.rightArm.pivotX = -MathHelper.cos(this.body.yaw) * 5.0f;
        this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * 5.0f;
        this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * 5.0f;
        this.rightArm.yaw += this.body.yaw;
        this.leftArm.yaw += this.body.yaw;
        this.leftArm.pitch += this.body.yaw;
        g = 1.0f - this.handSwingProgress;
        g *= g;
        g *= g;
        g = 1.0f - g;
        float h = MathHelper.sin(g * (float)Math.PI);
        float i = MathHelper.sin(this.handSwingProgress * (float)Math.PI) * -(this.head.pitch - 0.7f) * 0.75f;
        lv2.pitch -= h * 1.2f + i;
        lv2.yaw += this.body.yaw * 2.0f;
        lv2.roll += MathHelper.sin(this.handSwingProgress * (float)Math.PI) * -0.4f;
    }

    protected float lerpAngle(float angleOne, float angleTwo, float magnitude) {
        float i = (magnitude - angleTwo) % ((float)Math.PI * 2);
        if (i < (float)(-Math.PI)) {
            i += (float)Math.PI * 2;
        }
        if (i >= (float)Math.PI) {
            i -= (float)Math.PI * 2;
        }
        return angleTwo + angleOne * i;
    }

    private float method_2807(float f) {
        return -65.0f * f + f * f;
    }

    public void copyBipedStateTo(BipedEntityModel<T> model) {
        super.copyStateTo(model);
        model.leftArmPose = this.leftArmPose;
        model.rightArmPose = this.rightArmPose;
        model.sneaking = this.sneaking;
        model.head.copyTransform(this.head);
        model.hat.copyTransform(this.hat);
        model.body.copyTransform(this.body);
        model.rightArm.copyTransform(this.rightArm);
        model.leftArm.copyTransform(this.leftArm);
        model.rightLeg.copyTransform(this.rightLeg);
        model.leftLeg.copyTransform(this.leftLeg);
    }

    public void setVisible(boolean visible) {
        this.head.visible = visible;
        this.hat.visible = visible;
        this.body.visible = visible;
        this.rightArm.visible = visible;
        this.leftArm.visible = visible;
        this.rightLeg.visible = visible;
        this.leftLeg.visible = visible;
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        this.getArm(arm).rotate(matrices);
    }

    protected ModelPart getArm(Arm arm) {
        if (arm == Arm.LEFT) {
            return this.leftArm;
        }
        return this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    private Arm getPreferredArm(T entity) {
        Arm lv = ((LivingEntity)entity).getMainArm();
        return ((LivingEntity)entity).preferredHand == Hand.MAIN_HAND ? lv : lv.getOpposite();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ArmPose {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true),
        SPYGLASS(false),
        TOOT_HORN(false),
        BRUSH(false);

        private final boolean twoHanded;

        private ArmPose(boolean twoHanded) {
            this.twoHanded = twoHanded;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }
    }
}

