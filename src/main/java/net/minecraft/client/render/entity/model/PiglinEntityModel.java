package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinActivity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PiglinEntityModel extends PlayerEntityModel {
   public final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelTransform bodyRotation;
   private final ModelTransform headRotation;
   private final ModelTransform leftArmRotation;
   private final ModelTransform rightArmRotation;

   public PiglinEntityModel(ModelPart arg) {
      super(arg, false);
      this.rightEar = this.head.getChild(EntityModelPartNames.RIGHT_EAR);
      this.leftEar = this.head.getChild(EntityModelPartNames.LEFT_EAR);
      this.bodyRotation = this.body.getTransform();
      this.headRotation = this.head.getTransform();
      this.leftArmRotation = this.leftArm.getTransform();
      this.rightArmRotation = this.rightArm.getTransform();
   }

   public static ModelData getModelData(Dilation dilation) {
      ModelData lv = PlayerEntityModel.getTexturedModelData(dilation, false);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation), ModelTransform.NONE);
      addHead(dilation, lv);
      lv2.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
      return lv;
   }

   public static void addHead(Dilation dilation, ModelData baseModelData) {
      ModelPartData lv = baseModelData.getRoot();
      ModelPartData lv2 = lv.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, dilation).uv(31, 1).cuboid(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, dilation).uv(2, 4).cuboid(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, dilation).uv(2, 0).cuboid(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, dilation), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(51, 6).cuboid(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, dilation), ModelTransform.of(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));
      lv2.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(39, 6).cuboid(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, dilation), ModelTransform.of(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
   }

   public void setAngles(MobEntity arg, float f, float g, float h, float i, float j) {
      this.body.setTransform(this.bodyRotation);
      this.head.setTransform(this.headRotation);
      this.leftArm.setTransform(this.leftArmRotation);
      this.rightArm.setTransform(this.rightArmRotation);
      super.setAngles((LivingEntity)arg, f, g, h, i, j);
      float k = 0.5235988F;
      float l = h * 0.1F + f * 0.5F;
      float m = 0.08F + g * 0.4F;
      this.leftEar.roll = -0.5235988F - MathHelper.cos(l * 1.2F) * m;
      this.rightEar.roll = 0.5235988F + MathHelper.cos(l) * m;
      if (arg instanceof AbstractPiglinEntity lv) {
         PiglinActivity lv2 = lv.getActivity();
         if (lv2 == PiglinActivity.DANCING) {
            float n = h / 60.0F;
            this.rightEar.roll = 0.5235988F + 0.017453292F * MathHelper.sin(n * 30.0F) * 10.0F;
            this.leftEar.roll = -0.5235988F - 0.017453292F * MathHelper.cos(n * 30.0F) * 10.0F;
            this.head.pivotX = MathHelper.sin(n * 10.0F);
            this.head.pivotY = MathHelper.sin(n * 40.0F) + 0.4F;
            this.rightArm.roll = 0.017453292F * (70.0F + MathHelper.cos(n * 40.0F) * 10.0F);
            this.leftArm.roll = this.rightArm.roll * -1.0F;
            this.rightArm.pivotY = MathHelper.sin(n * 40.0F) * 0.5F + 1.5F;
            this.leftArm.pivotY = MathHelper.sin(n * 40.0F) * 0.5F + 1.5F;
            this.body.pivotY = MathHelper.sin(n * 40.0F) * 0.35F;
         } else if (lv2 == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON && this.handSwingProgress == 0.0F) {
            this.rotateMainArm(arg);
         } else if (lv2 == PiglinActivity.CROSSBOW_HOLD) {
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, !arg.isLeftHanded());
         } else if (lv2 == PiglinActivity.CROSSBOW_CHARGE) {
            CrossbowPosing.charge(this.rightArm, this.leftArm, arg, !arg.isLeftHanded());
         } else if (lv2 == PiglinActivity.ADMIRING_ITEM) {
            this.head.pitch = 0.5F;
            this.head.yaw = 0.0F;
            if (arg.isLeftHanded()) {
               this.rightArm.yaw = -0.5F;
               this.rightArm.pitch = -0.9F;
            } else {
               this.leftArm.yaw = 0.5F;
               this.leftArm.pitch = -0.9F;
            }
         }
      } else if (arg.getType() == EntityType.ZOMBIFIED_PIGLIN) {
         CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, arg.isAttacking(), this.handSwingProgress, h);
      }

      this.leftPants.copyTransform(this.leftLeg);
      this.rightPants.copyTransform(this.rightLeg);
      this.leftSleeve.copyTransform(this.leftArm);
      this.rightSleeve.copyTransform(this.rightArm);
      this.jacket.copyTransform(this.body);
      this.hat.copyTransform(this.head);
   }

   protected void animateArms(MobEntity arg, float f) {
      if (this.handSwingProgress > 0.0F && arg instanceof PiglinEntity && ((PiglinEntity)arg).getActivity() == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON) {
         CrossbowPosing.meleeAttack(this.rightArm, this.leftArm, arg, this.handSwingProgress, f);
      } else {
         super.animateArms(arg, f);
      }
   }

   private void rotateMainArm(MobEntity entity) {
      if (entity.isLeftHanded()) {
         this.leftArm.pitch = -1.8F;
      } else {
         this.rightArm.pitch = -1.8F;
      }

   }
}
