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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ElytraEntityModel extends AnimalModel {
   private final ModelPart rightWing;
   private final ModelPart leftWing;

   public ElytraEntityModel(ModelPart root) {
      this.leftWing = root.getChild(EntityModelPartNames.LEFT_WING);
      this.rightWing = root.getChild(EntityModelPartNames.RIGHT_WING);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      Dilation lv3 = new Dilation(1.0F);
      lv2.addChild(EntityModelPartNames.LEFT_WING, ModelPartBuilder.create().uv(22, 0).cuboid(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, lv3), ModelTransform.of(5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, -0.2617994F));
      lv2.addChild(EntityModelPartNames.RIGHT_WING, ModelPartBuilder.create().uv(22, 0).mirrored().cuboid(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, lv3), ModelTransform.of(-5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, 0.2617994F));
      return TexturedModelData.of(lv, 64, 32);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of();
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.leftWing, this.rightWing);
   }

   public void setAngles(LivingEntity arg, float f, float g, float h, float i, float j) {
      float k = 0.2617994F;
      float l = -0.2617994F;
      float m = 0.0F;
      float n = 0.0F;
      if (arg.isFallFlying()) {
         float o = 1.0F;
         Vec3d lv = arg.getVelocity();
         if (lv.y < 0.0) {
            Vec3d lv2 = lv.normalize();
            o = 1.0F - (float)Math.pow(-lv2.y, 1.5);
         }

         k = o * 0.34906584F + (1.0F - o) * k;
         l = o * -1.5707964F + (1.0F - o) * l;
      } else if (arg.isInSneakingPose()) {
         k = 0.6981317F;
         l = -0.7853982F;
         m = 3.0F;
         n = 0.08726646F;
      }

      this.leftWing.pivotY = m;
      if (arg instanceof AbstractClientPlayerEntity lv3) {
         lv3.elytraPitch += (k - lv3.elytraPitch) * 0.1F;
         lv3.elytraYaw += (n - lv3.elytraYaw) * 0.1F;
         lv3.elytraRoll += (l - lv3.elytraRoll) * 0.1F;
         this.leftWing.pitch = lv3.elytraPitch;
         this.leftWing.yaw = lv3.elytraYaw;
         this.leftWing.roll = lv3.elytraRoll;
      } else {
         this.leftWing.pitch = k;
         this.leftWing.roll = l;
         this.leftWing.yaw = n;
      }

      this.rightWing.yaw = -this.leftWing.yaw;
      this.rightWing.pivotY = this.leftWing.pivotY;
      this.rightWing.pitch = this.leftWing.pitch;
      this.rightWing.roll = -this.leftWing.roll;
   }
}
