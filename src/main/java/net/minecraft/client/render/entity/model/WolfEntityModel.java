package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WolfEntityModel extends TintableAnimalModel {
   private static final String REAL_HEAD = "real_head";
   private static final String UPPER_BODY = "upper_body";
   private static final String REAL_TAIL = "real_tail";
   private final ModelPart head;
   private final ModelPart realHead;
   private final ModelPart torso;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;
   private final ModelPart realTail;
   private final ModelPart neck;
   private static final int field_32580 = 8;

   public WolfEntityModel(ModelPart root) {
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.realHead = this.head.getChild("real_head");
      this.torso = root.getChild(EntityModelPartNames.BODY);
      this.neck = root.getChild("upper_body");
      this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
      this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
      this.tail = root.getChild(EntityModelPartNames.TAIL);
      this.realTail = this.tail.getChild("real_tail");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = 13.5F;
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, 13.5F, -7.0F));
      lv3.addChild("real_head", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F).uv(16, 14).cuboid(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).uv(16, 14).cuboid(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).uv(0, 10).cuboid(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F), ModelTransform.NONE);
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(18, 14).cuboid(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F), ModelTransform.of(0.0F, 14.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
      lv2.addChild("upper_body", ModelPartBuilder.create().uv(21, 0).cuboid(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F), ModelTransform.of(-1.0F, 14.0F, -3.0F, 1.5707964F, 0.0F, 0.0F));
      ModelPartBuilder lv4 = ModelPartBuilder.create().uv(0, 18).cuboid(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv4, ModelTransform.pivot(-2.5F, 16.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv4, ModelTransform.pivot(0.5F, 16.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv4, ModelTransform.pivot(-2.5F, 16.0F, -4.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv4, ModelTransform.pivot(0.5F, 16.0F, -4.0F));
      ModelPartData lv5 = lv2.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create(), ModelTransform.of(-1.0F, 12.0F, 8.0F, 0.62831855F, 0.0F, 0.0F));
      lv5.addChild("real_tail", ModelPartBuilder.create().uv(9, 18).cuboid(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), ModelTransform.NONE);
      return TexturedModelData.of(lv, 64, 32);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.torso, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.tail, this.neck);
   }

   public void animateModel(WolfEntity arg, float f, float g, float h) {
      if (arg.hasAngerTime()) {
         this.tail.yaw = 0.0F;
      } else {
         this.tail.yaw = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      }

      if (arg.isInSittingPose()) {
         this.neck.setPivot(-1.0F, 16.0F, -3.0F);
         this.neck.pitch = 1.2566371F;
         this.neck.yaw = 0.0F;
         this.torso.setPivot(0.0F, 18.0F, 0.0F);
         this.torso.pitch = 0.7853982F;
         this.tail.setPivot(-1.0F, 21.0F, 6.0F);
         this.rightHindLeg.setPivot(-2.5F, 22.7F, 2.0F);
         this.rightHindLeg.pitch = 4.712389F;
         this.leftHindLeg.setPivot(0.5F, 22.7F, 2.0F);
         this.leftHindLeg.pitch = 4.712389F;
         this.rightFrontLeg.pitch = 5.811947F;
         this.rightFrontLeg.setPivot(-2.49F, 17.0F, -4.0F);
         this.leftFrontLeg.pitch = 5.811947F;
         this.leftFrontLeg.setPivot(0.51F, 17.0F, -4.0F);
      } else {
         this.torso.setPivot(0.0F, 14.0F, 2.0F);
         this.torso.pitch = 1.5707964F;
         this.neck.setPivot(-1.0F, 14.0F, -3.0F);
         this.neck.pitch = this.torso.pitch;
         this.tail.setPivot(-1.0F, 12.0F, 8.0F);
         this.rightHindLeg.setPivot(-2.5F, 16.0F, 7.0F);
         this.leftHindLeg.setPivot(0.5F, 16.0F, 7.0F);
         this.rightFrontLeg.setPivot(-2.5F, 16.0F, -4.0F);
         this.leftFrontLeg.setPivot(0.5F, 16.0F, -4.0F);
         this.rightHindLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
         this.leftHindLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
         this.rightFrontLeg.pitch = MathHelper.cos(f * 0.6662F + 3.1415927F) * 1.4F * g;
         this.leftFrontLeg.pitch = MathHelper.cos(f * 0.6662F) * 1.4F * g;
      }

      this.realHead.roll = arg.getBegAnimationProgress(h) + arg.getShakeAnimationProgress(h, 0.0F);
      this.neck.roll = arg.getShakeAnimationProgress(h, -0.08F);
      this.torso.roll = arg.getShakeAnimationProgress(h, -0.16F);
      this.realTail.roll = arg.getShakeAnimationProgress(h, -0.2F);
   }

   public void setAngles(WolfEntity arg, float f, float g, float h, float i, float j) {
      this.head.pitch = j * 0.017453292F;
      this.head.yaw = i * 0.017453292F;
      this.tail.pitch = h;
   }
}
