package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CrossbowPosing {
   public static void hold(ModelPart holdingArm, ModelPart otherArm, ModelPart head, boolean rightArmed) {
      ModelPart lv = rightArmed ? holdingArm : otherArm;
      ModelPart lv2 = rightArmed ? otherArm : holdingArm;
      lv.yaw = (rightArmed ? -0.3F : 0.3F) + head.yaw;
      lv2.yaw = (rightArmed ? 0.6F : -0.6F) + head.yaw;
      lv.pitch = -1.5707964F + head.pitch + 0.1F;
      lv2.pitch = -1.5F + head.pitch;
   }

   public static void charge(ModelPart holdingArm, ModelPart pullingArm, LivingEntity actor, boolean rightArmed) {
      ModelPart lv = rightArmed ? holdingArm : pullingArm;
      ModelPart lv2 = rightArmed ? pullingArm : holdingArm;
      lv.yaw = rightArmed ? -0.8F : 0.8F;
      lv.pitch = -0.97079635F;
      lv2.pitch = lv.pitch;
      float f = (float)CrossbowItem.getPullTime(actor.getActiveItem());
      float g = MathHelper.clamp((float)actor.getItemUseTime(), 0.0F, f);
      float h = g / f;
      lv2.yaw = MathHelper.lerp(h, 0.4F, 0.85F) * (float)(rightArmed ? 1 : -1);
      lv2.pitch = MathHelper.lerp(h, lv2.pitch, -1.5707964F);
   }

   public static void meleeAttack(ModelPart leftArm, ModelPart rightArm, MobEntity actor, float swingProgress, float animationProgress) {
      float h = MathHelper.sin(swingProgress * 3.1415927F);
      float i = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * 3.1415927F);
      leftArm.roll = 0.0F;
      rightArm.roll = 0.0F;
      leftArm.yaw = 0.15707964F;
      rightArm.yaw = -0.15707964F;
      if (actor.getMainArm() == Arm.RIGHT) {
         leftArm.pitch = -1.8849558F + MathHelper.cos(animationProgress * 0.09F) * 0.15F;
         rightArm.pitch = -0.0F + MathHelper.cos(animationProgress * 0.19F) * 0.5F;
         leftArm.pitch += h * 2.2F - i * 0.4F;
         rightArm.pitch += h * 1.2F - i * 0.4F;
      } else {
         leftArm.pitch = -0.0F + MathHelper.cos(animationProgress * 0.19F) * 0.5F;
         rightArm.pitch = -1.8849558F + MathHelper.cos(animationProgress * 0.09F) * 0.15F;
         leftArm.pitch += h * 1.2F - i * 0.4F;
         rightArm.pitch += h * 2.2F - i * 0.4F;
      }

      swingArms(leftArm, rightArm, animationProgress);
   }

   public static void swingArm(ModelPart arm, float animationProgress, float sigma) {
      arm.roll += sigma * (MathHelper.cos(animationProgress * 0.09F) * 0.05F + 0.05F);
      arm.pitch += sigma * MathHelper.sin(animationProgress * 0.067F) * 0.05F;
   }

   public static void swingArms(ModelPart leftArm, ModelPart rightArm, float animationProgress) {
      swingArm(leftArm, animationProgress, 1.0F);
      swingArm(rightArm, animationProgress, -1.0F);
   }

   public static void meleeAttack(ModelPart leftArm, ModelPart rightArm, boolean attacking, float swingProgress, float animationProgress) {
      float h = MathHelper.sin(swingProgress * 3.1415927F);
      float i = MathHelper.sin((1.0F - (1.0F - swingProgress) * (1.0F - swingProgress)) * 3.1415927F);
      rightArm.roll = 0.0F;
      leftArm.roll = 0.0F;
      rightArm.yaw = -(0.1F - h * 0.6F);
      leftArm.yaw = 0.1F - h * 0.6F;
      float j = -3.1415927F / (attacking ? 1.5F : 2.25F);
      rightArm.pitch = j;
      leftArm.pitch = j;
      rightArm.pitch += h * 1.2F - i * 0.4F;
      leftArm.pitch += h * 1.2F - i * 0.4F;
      swingArms(rightArm, leftArm, animationProgress);
   }
}
