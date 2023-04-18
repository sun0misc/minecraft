package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.ModelUtil;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class BeeEntityModel extends AnimalModel {
   private static final float BONE_BASE_Y_PIVOT = 19.0F;
   private static final String BONE = "bone";
   private static final String STINGER = "stinger";
   private static final String LEFT_ANTENNA = "left_antenna";
   private static final String RIGHT_ANTENNA = "right_antenna";
   private static final String FRONT_LEGS = "front_legs";
   private static final String MIDDLE_LEGS = "middle_legs";
   private static final String BACK_LEGS = "back_legs";
   private final ModelPart bone;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart frontLegs;
   private final ModelPart middleLegs;
   private final ModelPart backLegs;
   private final ModelPart stinger;
   private final ModelPart leftAntenna;
   private final ModelPart rightAntenna;
   private float bodyPitch;

   public BeeEntityModel(ModelPart root) {
      super(false, 24.0F, 0.0F);
      this.bone = root.getChild(EntityModelPartNames.BONE);
      ModelPart lv = this.bone.getChild(EntityModelPartNames.BODY);
      this.stinger = lv.getChild("stinger");
      this.leftAntenna = lv.getChild("left_antenna");
      this.rightAntenna = lv.getChild("right_antenna");
      this.rightWing = this.bone.getChild(EntityModelPartNames.RIGHT_WING);
      this.leftWing = this.bone.getChild(EntityModelPartNames.LEFT_WING);
      this.frontLegs = this.bone.getChild("front_legs");
      this.middleLegs = this.bone.getChild("middle_legs");
      this.backLegs = this.bone.getChild("back_legs");
   }

   public static TexturedModelData getTexturedModelData() {
      float f = 19.0F;
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BONE, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 19.0F, 0.0F));
      ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 0).cuboid(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F), ModelTransform.NONE);
      lv4.addChild("stinger", ModelPartBuilder.create().uv(26, 7).cuboid(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F), ModelTransform.NONE);
      lv4.addChild("left_antenna", ModelPartBuilder.create().uv(2, 0).cuboid(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), ModelTransform.pivot(0.0F, -2.0F, -5.0F));
      lv4.addChild("right_antenna", ModelPartBuilder.create().uv(2, 3).cuboid(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), ModelTransform.pivot(0.0F, -2.0F, -5.0F));
      Dilation lv5 = new Dilation(0.001F);
      lv3.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(0, 18).cuboid(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, lv5), ModelTransform.of(-1.5F, -4.0F, -3.0F, 0.0F, -0.2618F, 0.0F));
      lv3.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(0, 18).mirrored().cuboid(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, lv5), ModelTransform.of(1.5F, -4.0F, -3.0F, 0.0F, 0.2618F, 0.0F));
      lv3.addChild("front_legs", ModelPartBuilder.create().cuboid("front_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 1), ModelTransform.pivot(1.5F, 3.0F, -2.0F));
      lv3.addChild("middle_legs", ModelPartBuilder.create().cuboid("middle_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 3), ModelTransform.pivot(1.5F, 3.0F, 0.0F));
      lv3.addChild("back_legs", ModelPartBuilder.create().cuboid("back_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 5), ModelTransform.pivot(1.5F, 3.0F, 2.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void animateModel(BeeEntity arg, float f, float g, float h) {
      super.animateModel(arg, f, g, h);
      this.bodyPitch = arg.getBodyPitch(h);
      this.stinger.visible = !arg.hasStung();
   }

   public void setAngles(BeeEntity arg, float f, float g, float h, float i, float j) {
      this.rightWing.pitch = 0.0F;
      this.leftAntenna.pitch = 0.0F;
      this.rightAntenna.pitch = 0.0F;
      this.bone.pitch = 0.0F;
      boolean bl = arg.isOnGround() && arg.getVelocity().lengthSquared() < 1.0E-7;
      float k;
      if (bl) {
         this.rightWing.yaw = -0.2618F;
         this.rightWing.roll = 0.0F;
         this.leftWing.pitch = 0.0F;
         this.leftWing.yaw = 0.2618F;
         this.leftWing.roll = 0.0F;
         this.frontLegs.pitch = 0.0F;
         this.middleLegs.pitch = 0.0F;
         this.backLegs.pitch = 0.0F;
      } else {
         k = h * 120.32113F * 0.017453292F;
         this.rightWing.yaw = 0.0F;
         this.rightWing.roll = MathHelper.cos(k) * 3.1415927F * 0.15F;
         this.leftWing.pitch = this.rightWing.pitch;
         this.leftWing.yaw = this.rightWing.yaw;
         this.leftWing.roll = -this.rightWing.roll;
         this.frontLegs.pitch = 0.7853982F;
         this.middleLegs.pitch = 0.7853982F;
         this.backLegs.pitch = 0.7853982F;
         this.bone.pitch = 0.0F;
         this.bone.yaw = 0.0F;
         this.bone.roll = 0.0F;
      }

      if (!arg.hasAngerTime()) {
         this.bone.pitch = 0.0F;
         this.bone.yaw = 0.0F;
         this.bone.roll = 0.0F;
         if (!bl) {
            k = MathHelper.cos(h * 0.18F);
            this.bone.pitch = 0.1F + k * 3.1415927F * 0.025F;
            this.leftAntenna.pitch = k * 3.1415927F * 0.03F;
            this.rightAntenna.pitch = k * 3.1415927F * 0.03F;
            this.frontLegs.pitch = -k * 3.1415927F * 0.1F + 0.3926991F;
            this.backLegs.pitch = -k * 3.1415927F * 0.05F + 0.7853982F;
            this.bone.pivotY = 19.0F - MathHelper.cos(h * 0.18F) * 0.9F;
         }
      }

      if (this.bodyPitch > 0.0F) {
         this.bone.pitch = ModelUtil.interpolateAngle(this.bone.pitch, 3.0915928F, this.bodyPitch);
      }

   }

   protected Iterable getHeadParts() {
      return ImmutableList.of();
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.bone);
   }
}
