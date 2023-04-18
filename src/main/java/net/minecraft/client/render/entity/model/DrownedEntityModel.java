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
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class DrownedEntityModel extends ZombieEntityModel {
   public DrownedEntityModel(ModelPart arg) {
      super(arg);
   }

   public static TexturedModelData getTexturedModelData(Dilation dilation) {
      ModelData lv = BipedEntityModel.getModelData(dilation, 0.0F);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(16, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void animateModel(ZombieEntity arg, float f, float g, float h) {
      this.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
      ItemStack lv = arg.getStackInHand(Hand.MAIN_HAND);
      if (lv.isOf(Items.TRIDENT) && arg.isAttacking()) {
         if (arg.getMainArm() == Arm.RIGHT) {
            this.rightArmPose = BipedEntityModel.ArmPose.THROW_SPEAR;
         } else {
            this.leftArmPose = BipedEntityModel.ArmPose.THROW_SPEAR;
         }
      }

      super.animateModel(arg, f, g, h);
   }

   public void setAngles(ZombieEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles(arg, f, g, h, i, j);
      if (this.leftArmPose == BipedEntityModel.ArmPose.THROW_SPEAR) {
         this.leftArm.pitch = this.leftArm.pitch * 0.5F - 3.1415927F;
         this.leftArm.yaw = 0.0F;
      }

      if (this.rightArmPose == BipedEntityModel.ArmPose.THROW_SPEAR) {
         this.rightArm.pitch = this.rightArm.pitch * 0.5F - 3.1415927F;
         this.rightArm.yaw = 0.0F;
      }

      if (this.leaningPitch > 0.0F) {
         this.rightArm.pitch = this.lerpAngle(this.leaningPitch, this.rightArm.pitch, -2.5132742F) + this.leaningPitch * 0.35F * MathHelper.sin(0.1F * h);
         this.leftArm.pitch = this.lerpAngle(this.leaningPitch, this.leftArm.pitch, -2.5132742F) - this.leaningPitch * 0.35F * MathHelper.sin(0.1F * h);
         this.rightArm.roll = this.lerpAngle(this.leaningPitch, this.rightArm.roll, -0.15F);
         this.leftArm.roll = this.lerpAngle(this.leaningPitch, this.leftArm.roll, 0.15F);
         ModelPart var10000 = this.leftLeg;
         var10000.pitch -= this.leaningPitch * 0.55F * MathHelper.sin(0.1F * h);
         var10000 = this.rightLeg;
         var10000.pitch += this.leaningPitch * 0.55F * MathHelper.sin(0.1F * h);
         this.head.pitch = 0.0F;
      }

   }
}
