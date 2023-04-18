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
import net.minecraft.entity.decoration.ArmorStandEntity;

@Environment(EnvType.CLIENT)
public class ArmorStandArmorEntityModel extends BipedEntityModel {
   public ArmorStandArmorEntityModel(ModelPart arg) {
      super(arg);
   }

   public static TexturedModelData getTexturedModelData(Dilation dilation) {
      ModelData lv = BipedEntityModel.getModelData(dilation, 0.0F);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.add(0.5F)), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(-0.1F)), ModelTransform.pivot(-1.9F, 11.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(-0.1F)), ModelTransform.pivot(1.9F, 11.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(ArmorStandEntity arg, float f, float g, float h, float i, float j) {
      this.head.pitch = 0.017453292F * arg.getHeadRotation().getPitch();
      this.head.yaw = 0.017453292F * arg.getHeadRotation().getYaw();
      this.head.roll = 0.017453292F * arg.getHeadRotation().getRoll();
      this.body.pitch = 0.017453292F * arg.getBodyRotation().getPitch();
      this.body.yaw = 0.017453292F * arg.getBodyRotation().getYaw();
      this.body.roll = 0.017453292F * arg.getBodyRotation().getRoll();
      this.leftArm.pitch = 0.017453292F * arg.getLeftArmRotation().getPitch();
      this.leftArm.yaw = 0.017453292F * arg.getLeftArmRotation().getYaw();
      this.leftArm.roll = 0.017453292F * arg.getLeftArmRotation().getRoll();
      this.rightArm.pitch = 0.017453292F * arg.getRightArmRotation().getPitch();
      this.rightArm.yaw = 0.017453292F * arg.getRightArmRotation().getYaw();
      this.rightArm.roll = 0.017453292F * arg.getRightArmRotation().getRoll();
      this.leftLeg.pitch = 0.017453292F * arg.getLeftLegRotation().getPitch();
      this.leftLeg.yaw = 0.017453292F * arg.getLeftLegRotation().getYaw();
      this.leftLeg.roll = 0.017453292F * arg.getLeftLegRotation().getRoll();
      this.rightLeg.pitch = 0.017453292F * arg.getRightLegRotation().getPitch();
      this.rightLeg.yaw = 0.017453292F * arg.getRightLegRotation().getYaw();
      this.rightLeg.roll = 0.017453292F * arg.getRightLegRotation().getRoll();
      this.hat.copyTransform(this.head);
   }
}
