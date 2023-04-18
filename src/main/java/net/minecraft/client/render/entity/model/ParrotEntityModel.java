package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class ParrotEntityModel extends SinglePartEntityModel {
   private static final String FEATHER = "feather";
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart tail;
   private final ModelPart leftWing;
   private final ModelPart rightWing;
   private final ModelPart head;
   private final ModelPart feather;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;

   public ParrotEntityModel(ModelPart root) {
      this.root = root;
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.tail = root.getChild(EntityModelPartNames.TAIL);
      this.leftWing = root.getChild(EntityModelPartNames.LEFT_WING);
      this.rightWing = root.getChild(EntityModelPartNames.RIGHT_WING);
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.feather = this.head.getChild("feather");
      this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
      this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(2, 8).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), ModelTransform.pivot(0.0F, 16.5F, -3.0F));
      lv2.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(22, 1).cuboid(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 21.07F, 1.16F));
      lv2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(19, 8).cuboid(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), ModelTransform.pivot(1.5F, 16.94F, -2.76F));
      lv2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(19, 8).cuboid(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), ModelTransform.pivot(-1.5F, 16.94F, -2.76F));
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(2, 2).cuboid(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F), ModelTransform.pivot(0.0F, 15.69F, -2.76F));
      lv3.addChild("head2", ModelPartBuilder.create().uv(10, 0).cuboid(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F), ModelTransform.pivot(0.0F, -2.0F, -1.0F));
      lv3.addChild("beak1", ModelPartBuilder.create().uv(11, 7).cuboid(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F), ModelTransform.pivot(0.0F, -0.5F, -1.5F));
      lv3.addChild("beak2", ModelPartBuilder.create().uv(16, 7).cuboid(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), ModelTransform.pivot(0.0F, -1.75F, -2.45F));
      lv3.addChild("feather", ModelPartBuilder.create().uv(2, 18).cuboid(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F), ModelTransform.pivot(0.0F, -2.15F, 0.15F));
      ModelPartBuilder lv4 = ModelPartBuilder.create().uv(14, 18).cuboid(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
      lv2.addChild(EntityModelPartNames.LEFT_LEG, lv4, ModelTransform.pivot(1.0F, 22.0F, -1.05F));
      lv2.addChild(EntityModelPartNames.RIGHT_LEG, lv4, ModelTransform.pivot(-1.0F, 22.0F, -1.05F));
      return TexturedModelData.of(lv, 32, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(ParrotEntity arg, float f, float g, float h, float i, float j) {
      this.setAngles(getPose(arg), arg.age, f, g, h, i, j);
   }

   public void animateModel(ParrotEntity arg, float f, float g, float h) {
      this.animateModel(getPose(arg));
   }

   public void poseOnShoulder(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float limbAngle, float limbDistance, float headYaw, float headPitch, int danceAngle) {
      this.animateModel(ParrotEntityModel.Pose.ON_SHOULDER);
      this.setAngles(ParrotEntityModel.Pose.ON_SHOULDER, danceAngle, limbAngle, limbDistance, 0.0F, headYaw, headPitch);
      this.root.render(matrices, vertexConsumer, light, overlay);
   }

   private void setAngles(Pose pose, int danceAngle, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
      this.head.pitch = headPitch * 0.017453292F;
      this.head.yaw = headYaw * 0.017453292F;
      this.head.roll = 0.0F;
      this.head.pivotX = 0.0F;
      this.body.pivotX = 0.0F;
      this.tail.pivotX = 0.0F;
      this.rightWing.pivotX = -1.5F;
      this.leftWing.pivotX = 1.5F;
      switch (pose) {
         case SITTING:
            break;
         case PARTY:
            float l = MathHelper.cos((float)danceAngle);
            float m = MathHelper.sin((float)danceAngle);
            this.head.pivotX = l;
            this.head.pivotY = 15.69F + m;
            this.head.pitch = 0.0F;
            this.head.yaw = 0.0F;
            this.head.roll = MathHelper.sin((float)danceAngle) * 0.4F;
            this.body.pivotX = l;
            this.body.pivotY = 16.5F + m;
            this.leftWing.roll = -0.0873F - age;
            this.leftWing.pivotX = 1.5F + l;
            this.leftWing.pivotY = 16.94F + m;
            this.rightWing.roll = 0.0873F + age;
            this.rightWing.pivotX = -1.5F + l;
            this.rightWing.pivotY = 16.94F + m;
            this.tail.pivotX = l;
            this.tail.pivotY = 21.07F + m;
            break;
         case STANDING:
            ModelPart var10000 = this.leftLeg;
            var10000.pitch += MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
            var10000 = this.rightLeg;
            var10000.pitch += MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
         case FLYING:
         case ON_SHOULDER:
         default:
            float n = age * 0.3F;
            this.head.pivotY = 15.69F + n;
            this.tail.pitch = 1.015F + MathHelper.cos(limbAngle * 0.6662F) * 0.3F * limbDistance;
            this.tail.pivotY = 21.07F + n;
            this.body.pivotY = 16.5F + n;
            this.leftWing.roll = -0.0873F - age;
            this.leftWing.pivotY = 16.94F + n;
            this.rightWing.roll = 0.0873F + age;
            this.rightWing.pivotY = 16.94F + n;
            this.leftLeg.pivotY = 22.0F + n;
            this.rightLeg.pivotY = 22.0F + n;
      }

   }

   private void animateModel(Pose pose) {
      this.feather.pitch = -0.2214F;
      this.body.pitch = 0.4937F;
      this.leftWing.pitch = -0.6981F;
      this.leftWing.yaw = -3.1415927F;
      this.rightWing.pitch = -0.6981F;
      this.rightWing.yaw = -3.1415927F;
      this.leftLeg.pitch = -0.0299F;
      this.rightLeg.pitch = -0.0299F;
      this.leftLeg.pivotY = 22.0F;
      this.rightLeg.pivotY = 22.0F;
      this.leftLeg.roll = 0.0F;
      this.rightLeg.roll = 0.0F;
      switch (pose) {
         case SITTING:
            float f = 1.9F;
            this.head.pivotY = 17.59F;
            this.tail.pitch = 1.5388988F;
            this.tail.pivotY = 22.97F;
            this.body.pivotY = 18.4F;
            this.leftWing.roll = -0.0873F;
            this.leftWing.pivotY = 18.84F;
            this.rightWing.roll = 0.0873F;
            this.rightWing.pivotY = 18.84F;
            ++this.leftLeg.pivotY;
            ++this.rightLeg.pivotY;
            ++this.leftLeg.pitch;
            ++this.rightLeg.pitch;
            break;
         case PARTY:
            this.leftLeg.roll = -0.34906584F;
            this.rightLeg.roll = 0.34906584F;
         case STANDING:
         case ON_SHOULDER:
         default:
            break;
         case FLYING:
            ModelPart var10000 = this.leftLeg;
            var10000.pitch += 0.6981317F;
            var10000 = this.rightLeg;
            var10000.pitch += 0.6981317F;
      }

   }

   private static Pose getPose(ParrotEntity parrot) {
      if (parrot.isSongPlaying()) {
         return ParrotEntityModel.Pose.PARTY;
      } else if (parrot.isInSittingPose()) {
         return ParrotEntityModel.Pose.SITTING;
      } else {
         return parrot.isInAir() ? ParrotEntityModel.Pose.FLYING : ParrotEntityModel.Pose.STANDING;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Pose {
      FLYING,
      STANDING,
      SITTING,
      PARTY,
      ON_SHOULDER;

      // $FF: synthetic method
      private static Pose[] method_36893() {
         return new Pose[]{FLYING, STANDING, SITTING, PARTY, ON_SHOULDER};
      }
   }
}
