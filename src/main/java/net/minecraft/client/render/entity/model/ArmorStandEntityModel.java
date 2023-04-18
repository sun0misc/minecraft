package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ArmorStandEntityModel extends ArmorStandArmorEntityModel {
   private static final String RIGHT_BODY_STICK = "right_body_stick";
   private static final String LEFT_BODY_STICK = "left_body_stick";
   private static final String SHOULDER_STICK = "shoulder_stick";
   private static final String BASE_PLATE = "base_plate";
   private final ModelPart rightBodyStick;
   private final ModelPart leftBodyStick;
   private final ModelPart shoulderStick;
   private final ModelPart basePlate;

   public ArmorStandEntityModel(ModelPart arg) {
      super(arg);
      this.rightBodyStick = arg.getChild("right_body_stick");
      this.leftBodyStick = arg.getChild("left_body_stick");
      this.shoulderStick = arg.getChild("shoulder_stick");
      this.basePlate = arg.getChild("base_plate");
      this.hat.visible = false;
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = BipedEntityModel.getModelData(Dilation.NONE, 0.0F);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F), ModelTransform.pivot(0.0F, 1.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 26).cuboid(-6.0F, 0.0F, -1.5F, 12.0F, 3.0F, 3.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(24, 0).cuboid(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(32, 16).mirrored().cuboid(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(8, 0).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
      lv2.addChild("right_body_stick", ModelPartBuilder.create().uv(16, 0).cuboid(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild("left_body_stick", ModelPartBuilder.create().uv(48, 16).cuboid(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild("shoulder_stick", ModelPartBuilder.create().uv(0, 48).cuboid(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F), ModelTransform.NONE);
      lv2.addChild("base_plate", ModelPartBuilder.create().uv(0, 32).cuboid(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F), ModelTransform.pivot(0.0F, 12.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void animateModel(ArmorStandEntity arg, float f, float g, float h) {
      this.basePlate.pitch = 0.0F;
      this.basePlate.yaw = 0.017453292F * -MathHelper.lerpAngleDegrees(h, arg.prevYaw, arg.getYaw());
      this.basePlate.roll = 0.0F;
   }

   public void setAngles(ArmorStandEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles(arg, f, g, h, i, j);
      this.leftArm.visible = arg.shouldShowArms();
      this.rightArm.visible = arg.shouldShowArms();
      this.basePlate.visible = !arg.shouldHideBasePlate();
      this.rightBodyStick.pitch = 0.017453292F * arg.getBodyRotation().getPitch();
      this.rightBodyStick.yaw = 0.017453292F * arg.getBodyRotation().getYaw();
      this.rightBodyStick.roll = 0.017453292F * arg.getBodyRotation().getRoll();
      this.leftBodyStick.pitch = 0.017453292F * arg.getBodyRotation().getPitch();
      this.leftBodyStick.yaw = 0.017453292F * arg.getBodyRotation().getYaw();
      this.leftBodyStick.roll = 0.017453292F * arg.getBodyRotation().getRoll();
      this.shoulderStick.pitch = 0.017453292F * arg.getBodyRotation().getPitch();
      this.shoulderStick.yaw = 0.017453292F * arg.getBodyRotation().getYaw();
      this.shoulderStick.roll = 0.017453292F * arg.getBodyRotation().getRoll();
   }

   protected Iterable getBodyParts() {
      return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
   }

   public void setArmAngle(Arm arm, MatrixStack matrices) {
      ModelPart lv = this.getArm(arm);
      boolean bl = lv.visible;
      lv.visible = true;
      super.setArmAngle(arm, matrices);
      lv.visible = bl;
   }
}
