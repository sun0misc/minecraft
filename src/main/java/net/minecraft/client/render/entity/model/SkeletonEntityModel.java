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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SkeletonEntityModel extends BipedEntityModel {
   public SkeletonEntityModel(ModelPart arg) {
      super(arg);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = BipedEntityModel.getModelData(Dilation.NONE, 0.0F);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), ModelTransform.pivot(-2.0F, 12.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), ModelTransform.pivot(2.0F, 12.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public void animateModel(MobEntity arg, float f, float g, float h) {
      this.rightArmPose = BipedEntityModel.ArmPose.EMPTY;
      this.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
      ItemStack lv = arg.getStackInHand(Hand.MAIN_HAND);
      if (lv.isOf(Items.BOW) && arg.isAttacking()) {
         if (arg.getMainArm() == Arm.RIGHT) {
            this.rightArmPose = BipedEntityModel.ArmPose.BOW_AND_ARROW;
         } else {
            this.leftArmPose = BipedEntityModel.ArmPose.BOW_AND_ARROW;
         }
      }

      super.animateModel((LivingEntity)arg, f, g, h);
   }

   public void setAngles(MobEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles((LivingEntity)arg, f, g, h, i, j);
      ItemStack lv = arg.getMainHandStack();
      if (arg.isAttacking() && (lv.isEmpty() || !lv.isOf(Items.BOW))) {
         float k = MathHelper.sin(this.handSwingProgress * 3.1415927F);
         float l = MathHelper.sin((1.0F - (1.0F - this.handSwingProgress) * (1.0F - this.handSwingProgress)) * 3.1415927F);
         this.rightArm.roll = 0.0F;
         this.leftArm.roll = 0.0F;
         this.rightArm.yaw = -(0.1F - k * 0.6F);
         this.leftArm.yaw = 0.1F - k * 0.6F;
         this.rightArm.pitch = -1.5707964F;
         this.leftArm.pitch = -1.5707964F;
         ModelPart var10000 = this.rightArm;
         var10000.pitch -= k * 1.2F - l * 0.4F;
         var10000 = this.leftArm;
         var10000.pitch -= k * 1.2F - l * 0.4F;
         CrossbowPosing.swingArms(this.rightArm, this.leftArm, h);
      }

   }

   public void setArmAngle(Arm arm, MatrixStack matrices) {
      float f = arm == Arm.RIGHT ? 1.0F : -1.0F;
      ModelPart lv = this.getArm(arm);
      lv.pivotX += f;
      lv.rotate(matrices);
      lv.pivotX -= f;
   }
}
