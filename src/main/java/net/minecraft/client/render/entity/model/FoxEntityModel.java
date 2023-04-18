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
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class FoxEntityModel extends AnimalModel {
   public final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;
   private static final int field_32477 = 6;
   private static final float HEAD_Y_PIVOT = 16.5F;
   private static final float LEG_Y_PIVOT = 17.5F;
   private float legPitchModifier;

   public FoxEntityModel(ModelPart root) {
      super(true, 8.0F, 3.35F);
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
      this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
      this.tail = this.body.getChild(EntityModelPartNames.TAIL);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(1, 5).cuboid(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F), ModelTransform.pivot(-1.0F, 16.5F, -3.0F));
      lv3.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(8, 1).cuboid(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), ModelTransform.NONE);
      lv3.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(15, 1).cuboid(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), ModelTransform.NONE);
      lv3.addChild(EntityModelPartNames.NOSE, ModelPartBuilder.create().uv(6, 18).cuboid(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F), ModelTransform.NONE);
      ModelPartData lv4 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(24, 15).cuboid(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F), ModelTransform.of(0.0F, 16.0F, -6.0F, 1.5707964F, 0.0F, 0.0F));
      Dilation lv5 = new Dilation(0.001F);
      ModelPartBuilder lv6 = ModelPartBuilder.create().uv(4, 24).cuboid(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, lv5);
      ModelPartBuilder lv7 = ModelPartBuilder.create().uv(13, 24).cuboid(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, lv5);
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv7, ModelTransform.pivot(-5.0F, 17.5F, 7.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv6, ModelTransform.pivot(-1.0F, 17.5F, 7.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv7, ModelTransform.pivot(-5.0F, 17.5F, 0.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv6, ModelTransform.pivot(-1.0F, 17.5F, 0.0F));
      lv4.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(30, 0).cuboid(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F), ModelTransform.of(-4.0F, 15.0F, -1.0F, -0.05235988F, 0.0F, 0.0F));
      return TexturedModelData.of(lv, 48, 32);
   }

   public void animateModel(FoxEntity arg, float f, float g, float h) {
      this.body.pitch = 1.5707964F;
      this.tail.pitch = -0.05235988F;
      this.rightHindLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      this.leftHindLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
      this.rightFrontLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
      this.leftFrontLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      this.head.setPivot(-1.0F, 16.5F, -3.0F);
      this.head.yaw = 0.0F;
      this.head.roll = arg.getHeadRoll(h);
      this.rightHindLeg.visible = true;
      this.leftHindLeg.visible = true;
      this.rightFrontLeg.visible = true;
      this.leftFrontLeg.visible = true;
      this.body.setPivot(0.0F, 16.0F, -6.0F);
      this.body.roll = 0.0F;
      this.rightHindLeg.setPivot(-5.0F, 17.5F, 7.0F);
      this.leftHindLeg.setPivot(-1.0F, 17.5F, 7.0F);
      if (arg.isInSneakingPose()) {
         this.body.pitch = 1.6755161F;
         float i = arg.getBodyRotationHeightOffset(h);
         this.body.setPivot(0.0F, 16.0F + arg.getBodyRotationHeightOffset(h), -6.0F);
         this.head.setPivot(-1.0F, 16.5F + i, -3.0F);
         this.head.yaw = 0.0F;
      } else if (arg.isSleeping()) {
         this.body.roll = -1.5707964F;
         this.body.setPivot(0.0F, 21.0F, -6.0F);
         this.tail.pitch = -2.6179938F;
         if (this.child) {
            this.tail.pitch = -2.1816616F;
            this.body.setPivot(0.0F, 21.0F, -2.0F);
         }

         this.head.setPivot(1.0F, 19.49F, -3.0F);
         this.head.pitch = 0.0F;
         this.head.yaw = -2.0943952F;
         this.head.roll = 0.0F;
         this.rightHindLeg.visible = false;
         this.leftHindLeg.visible = false;
         this.rightFrontLeg.visible = false;
         this.leftFrontLeg.visible = false;
      } else if (arg.isSitting()) {
         this.body.pitch = 0.5235988F;
         this.body.setPivot(0.0F, 9.0F, -3.0F);
         this.tail.pitch = 0.7853982F;
         this.tail.setPivot(-4.0F, 15.0F, -2.0F);
         this.head.setPivot(-1.0F, 10.0F, -0.25F);
         this.head.pitch = 0.0F;
         this.head.yaw = 0.0F;
         if (this.child) {
            this.head.setPivot(-1.0F, 13.0F, -3.75F);
         }

         this.rightHindLeg.pitch = -1.3089969F;
         this.rightHindLeg.setPivot(-5.0F, 21.5F, 6.75F);
         this.leftHindLeg.pitch = -1.3089969F;
         this.leftHindLeg.setPivot(-1.0F, 21.5F, 6.75F);
         this.rightFrontLeg.pitch = -0.2617994F;
         this.leftFrontLeg.pitch = -0.2617994F;
      }

   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
   }

   public void setAngles(FoxEntity arg, float f, float g, float h, float i, float j) {
      if (!arg.isSleeping() && !arg.isWalking() && !arg.isInSneakingPose()) {
         this.head.pitch = j * 0.017453292F;
         this.head.yaw = i * 0.017453292F;
      }

      if (arg.isSleeping()) {
         this.head.pitch = 0.0F;
         this.head.yaw = -2.0943952F;
         this.head.roll = MathHelper.cos(h * 0.027F) / 22.0F;
      }

      float k;
      if (arg.isInSneakingPose()) {
         k = MathHelper.cos(h) * 0.01F;
         this.body.yaw = k;
         this.rightHindLeg.roll = k;
         this.leftHindLeg.roll = k;
         this.rightFrontLeg.roll = k / 2.0F;
         this.leftFrontLeg.roll = k / 2.0F;
      }

      if (arg.isWalking()) {
         k = 0.1F;
         this.legPitchModifier += 0.67F;
         this.rightHindLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F) * 0.1F;
         this.leftHindLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F + 3.1415927F) * 0.1F;
         this.rightFrontLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F + 3.1415927F) * 0.1F;
         this.leftFrontLeg.pitch = MathHelper.cos(this.legPitchModifier * 0.4662F) * 0.1F;
      }

   }
}
