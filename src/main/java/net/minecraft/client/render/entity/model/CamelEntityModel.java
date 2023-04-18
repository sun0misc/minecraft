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
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.CamelAnimations;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CamelEntityModel extends SinglePartEntityModel {
   private static final float field_40459 = 2.0F;
   private static final float field_42227 = 2.5F;
   private static final float field_43083 = 0.45F;
   private static final float field_43084 = 29.35F;
   private static final String SADDLE = "saddle";
   private static final String BRIDLE = "bridle";
   private static final String REINS = "reins";
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart[] saddleAndBridle;
   private final ModelPart[] reins;

   public CamelEntityModel(ModelPart root) {
      this.root = root;
      ModelPart lv = root.getChild(EntityModelPartNames.BODY);
      this.head = lv.getChild(EntityModelPartNames.HEAD);
      this.saddleAndBridle = new ModelPart[]{lv.getChild("saddle"), this.head.getChild("bridle")};
      this.reins = new ModelPart[]{this.head.getChild("reins")};
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      Dilation lv3 = new Dilation(0.1F);
      ModelPartData lv4 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 25).cuboid(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F), ModelTransform.pivot(0.0F, 4.0F, 9.5F));
      lv4.addChild("hump", ModelPartBuilder.create().uv(74, 0).cuboid(-4.5F, -5.0F, -5.5F, 9.0F, 5.0F, 11.0F), ModelTransform.pivot(0.0F, -12.0F, -10.0F));
      lv4.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(122, 0).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 0.0F), ModelTransform.pivot(0.0F, -9.0F, 3.5F));
      ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(60, 24).cuboid(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F).uv(21, 0).cuboid(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F).uv(50, 0).cuboid(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F), ModelTransform.pivot(0.0F, -3.0F, -19.5F));
      lv5.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(45, 0).cuboid(-0.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), ModelTransform.pivot(3.0F, -21.0F, -9.5F));
      lv5.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(67, 0).cuboid(-2.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), ModelTransform.pivot(-3.0F, -21.0F, -9.5F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(58, 16).cuboid(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), ModelTransform.pivot(4.9F, 1.0F, 9.5F));
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(94, 16).cuboid(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), ModelTransform.pivot(-4.9F, 1.0F, 9.5F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(0, 0).cuboid(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), ModelTransform.pivot(4.9F, 1.0F, -10.5F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(0, 26).cuboid(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), ModelTransform.pivot(-4.9F, 1.0F, -10.5F));
      lv4.addChild("saddle", ModelPartBuilder.create().uv(74, 64).cuboid(-4.5F, -17.0F, -15.5F, 9.0F, 5.0F, 11.0F, lv3).uv(92, 114).cuboid(-3.5F, -20.0F, -15.5F, 7.0F, 3.0F, 11.0F, lv3).uv(0, 89).cuboid(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F, lv3), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      lv5.addChild("reins", ModelPartBuilder.create().uv(98, 42).cuboid(3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F).uv(84, 57).cuboid(-3.5F, -18.0F, -2.0F, 7.0F, 7.0F, 0.0F).uv(98, 42).cuboid(-3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      lv5.addChild("bridle", ModelPartBuilder.create().uv(60, 87).cuboid(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F, lv3).uv(21, 64).cuboid(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F, lv3).uv(50, 64).cuboid(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F, lv3).uv(74, 70).cuboid(2.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F).uv(74, 70).mirrored().cuboid(-3.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
      return TexturedModelData.of(lv, 128, 128);
   }

   public void setAngles(CamelEntity arg, float f, float g, float h, float i, float j) {
      this.getPart().traverse().forEach(ModelPart::resetTransform);
      this.setHeadAngles(arg, i, j, h);
      this.updateVisibleParts(arg);
      this.animateMovement(CamelAnimations.WALKING, f, g, 2.0F, 2.5F);
      this.updateAnimation(arg.sittingTransitionAnimationState, CamelAnimations.SITTING_TRANSITION, h, 1.0F);
      this.updateAnimation(arg.sittingAnimationState, CamelAnimations.SITTING, h, 1.0F);
      this.updateAnimation(arg.standingTransitionAnimationState, CamelAnimations.STANDING_TRANSITION, h, 1.0F);
      this.updateAnimation(arg.idlingAnimationState, CamelAnimations.IDLING, h, 1.0F);
      this.updateAnimation(arg.dashingAnimationState, CamelAnimations.DASHING, h, 1.0F);
   }

   private void setHeadAngles(CamelEntity entity, float headYaw, float headPitch, float animationProgress) {
      headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
      headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);
      if (entity.getJumpCooldown() > 0) {
         float i = animationProgress - (float)entity.age;
         float j = 45.0F * ((float)entity.getJumpCooldown() - i) / 55.0F;
         headPitch = MathHelper.clamp(headPitch + j, -25.0F, 70.0F);
      }

      this.head.yaw = headYaw * 0.017453292F;
      this.head.pitch = headPitch * 0.017453292F;
   }

   private void updateVisibleParts(CamelEntity camel) {
      boolean bl = camel.isSaddled();
      boolean bl2 = camel.hasPassengers();
      ModelPart[] var4 = this.saddleAndBridle;
      int var5 = var4.length;

      int var6;
      ModelPart lv;
      for(var6 = 0; var6 < var5; ++var6) {
         lv = var4[var6];
         lv.visible = bl;
      }

      var4 = this.reins;
      var5 = var4.length;

      for(var6 = 0; var6 < var5; ++var6) {
         lv = var4[var6];
         lv.visible = bl2 && bl;
      }

   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      if (this.child) {
         matrices.push();
         matrices.scale(0.45F, 0.45F, 0.45F);
         matrices.translate(0.0F, 1.834375F, 0.0F);
         this.getPart().render(matrices, vertices, light, overlay, red, green, blue, alpha);
         matrices.pop();
      } else {
         this.getPart().render(matrices, vertices, light, overlay, red, green, blue, alpha);
      }

   }

   public ModelPart getPart() {
      return this.root;
   }
}
