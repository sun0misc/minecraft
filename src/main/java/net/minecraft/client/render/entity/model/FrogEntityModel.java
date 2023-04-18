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
import net.minecraft.client.render.entity.animation.FrogAnimations;
import net.minecraft.entity.passive.FrogEntity;

@Environment(EnvType.CLIENT)
public class FrogEntityModel extends SinglePartEntityModel {
   private static final float field_39193 = 1.5F;
   private static final float field_42228 = 1.0F;
   private static final float field_42229 = 2.5F;
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart eyes;
   private final ModelPart tongue;
   private final ModelPart leftArm;
   private final ModelPart rightArm;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;
   private final ModelPart croakingBody;

   public FrogEntityModel(ModelPart root) {
      this.root = root.getChild(EntityModelPartNames.ROOT);
      this.body = this.root.getChild(EntityModelPartNames.BODY);
      this.head = this.body.getChild(EntityModelPartNames.HEAD);
      this.eyes = this.head.getChild(EntityModelPartNames.EYES);
      this.tongue = this.body.getChild(EntityModelPartNames.TONGUE);
      this.leftArm = this.body.getChild(EntityModelPartNames.LEFT_ARM);
      this.rightArm = this.body.getChild(EntityModelPartNames.RIGHT_ARM);
      this.leftLeg = this.root.getChild(EntityModelPartNames.LEFT_LEG);
      this.rightLeg = this.root.getChild(EntityModelPartNames.RIGHT_LEG);
      this.croakingBody = this.body.getChild(EntityModelPartNames.CROAKING_BODY);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.ROOT, ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
      ModelPartData lv4 = lv3.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(3, 1).cuboid(-3.5F, -2.0F, -8.0F, 7.0F, 3.0F, 9.0F).uv(23, 22).cuboid(-3.5F, -1.0F, -8.0F, 7.0F, 0.0F, 9.0F), ModelTransform.pivot(0.0F, -2.0F, 4.0F));
      ModelPartData lv5 = lv4.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(23, 13).cuboid(-3.5F, -1.0F, -7.0F, 7.0F, 0.0F, 9.0F).uv(0, 13).cuboid(-3.5F, -2.0F, -7.0F, 7.0F, 3.0F, 9.0F), ModelTransform.pivot(0.0F, -2.0F, -1.0F));
      ModelPartData lv6 = lv5.addChild(EntityModelPartNames.EYES, ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, 0.0F, 2.0F));
      lv6.addChild(EntityModelPartNames.RIGHT_EYE, ModelPartBuilder.create().uv(0, 0).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), ModelTransform.pivot(-1.5F, -3.0F, -6.5F));
      lv6.addChild(EntityModelPartNames.LEFT_EYE, ModelPartBuilder.create().uv(0, 5).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), ModelTransform.pivot(2.5F, -3.0F, -6.5F));
      lv4.addChild(EntityModelPartNames.CROAKING_BODY, ModelPartBuilder.create().uv(26, 5).cuboid(-3.5F, -0.1F, -2.9F, 7.0F, 2.0F, 3.0F, new Dilation(-0.1F)), ModelTransform.pivot(0.0F, -1.0F, -5.0F));
      ModelPartData lv7 = lv4.addChild(EntityModelPartNames.TONGUE, ModelPartBuilder.create().uv(17, 13).cuboid(-2.0F, 0.0F, -7.1F, 4.0F, 0.0F, 7.0F), ModelTransform.pivot(0.0F, -1.01F, 1.0F));
      ModelPartData lv8 = lv4.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(0, 32).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F), ModelTransform.pivot(4.0F, -1.0F, -6.5F));
      lv8.addChild(EntityModelPartNames.LEFT_HAND, ModelPartBuilder.create().uv(18, 40).cuboid(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(0.0F, 3.0F, -1.0F));
      ModelPartData lv9 = lv4.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(0, 38).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F), ModelTransform.pivot(-4.0F, -1.0F, -6.5F));
      lv9.addChild(EntityModelPartNames.RIGHT_HAND, ModelPartBuilder.create().uv(2, 40).cuboid(-4.0F, 0.01F, -5.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(0.0F, 3.0F, 0.0F));
      ModelPartData lv10 = lv3.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(14, 25).cuboid(-1.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F), ModelTransform.pivot(3.5F, -3.0F, 4.0F));
      lv10.addChild(EntityModelPartNames.LEFT_FOOT, ModelPartBuilder.create().uv(2, 32).cuboid(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(2.0F, 3.0F, 0.0F));
      ModelPartData lv11 = lv3.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(0, 25).cuboid(-2.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F), ModelTransform.pivot(-3.5F, -3.0F, 4.0F));
      lv11.addChild(EntityModelPartNames.RIGHT_FOOT, ModelPartBuilder.create().uv(18, 32).cuboid(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), ModelTransform.pivot(-2.0F, 3.0F, 0.0F));
      return TexturedModelData.of(lv, 48, 48);
   }

   public void setAngles(FrogEntity arg, float f, float g, float h, float i, float j) {
      this.getPart().traverse().forEach(ModelPart::resetTransform);
      this.updateAnimation(arg.longJumpingAnimationState, FrogAnimations.LONG_JUMPING, h);
      this.updateAnimation(arg.croakingAnimationState, FrogAnimations.CROAKING, h);
      this.updateAnimation(arg.usingTongueAnimationState, FrogAnimations.USING_TONGUE, h);
      if (arg.isInsideWaterOrBubbleColumn()) {
         this.animateMovement(FrogAnimations.SWIMMING, f, g, 1.0F, 2.5F);
      } else {
         this.animateMovement(FrogAnimations.WALKING, f, g, 1.5F, 2.5F);
      }

      this.updateAnimation(arg.idlingInWaterAnimationState, FrogAnimations.IDLING_IN_WATER, h);
      this.croakingBody.visible = arg.croakingAnimationState.isRunning();
   }

   public ModelPart getPart() {
      return this.root;
   }
}
