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
import net.minecraft.entity.mob.Hoglin;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class HoglinEntityModel extends AnimalModel {
   private static final float HEAD_PITCH_START = 0.87266463F;
   private static final float HEAD_PITCH_END = -0.34906584F;
   private final ModelPart head;
   private final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelPart body;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart mane;

   public HoglinEntityModel(ModelPart root) {
      super(true, 8.0F, 6.0F, 1.9F, 2.0F, 24.0F);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.mane = this.body.getChild(EntityModelPartNames.MANE);
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.rightEar = this.head.getChild(EntityModelPartNames.RIGHT_EAR);
      this.leftEar = this.head.getChild(EntityModelPartNames.LEFT_EAR);
      this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
      this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(1, 1).cuboid(-8.0F, -7.0F, -13.0F, 16.0F, 14.0F, 26.0F), ModelTransform.pivot(0.0F, 7.0F, 0.0F));
      lv3.addChild(EntityModelPartNames.MANE, ModelPartBuilder.create().uv(90, 33).cuboid(0.0F, 0.0F, -9.0F, 0.0F, 10.0F, 19.0F, new Dilation(0.001F)), ModelTransform.pivot(0.0F, -14.0F, -5.0F));
      ModelPartData lv4 = lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(61, 1).cuboid(-7.0F, -3.0F, -19.0F, 14.0F, 6.0F, 19.0F), ModelTransform.of(0.0F, 2.0F, -12.0F, 0.87266463F, 0.0F, 0.0F));
      lv4.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(1, 1).cuboid(-6.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F), ModelTransform.of(-6.0F, -2.0F, -3.0F, 0.0F, 0.0F, -0.6981317F));
      lv4.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(1, 6).cuboid(0.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F), ModelTransform.of(6.0F, -2.0F, -3.0F, 0.0F, 0.0F, 0.6981317F));
      lv4.addChild(EntityModelPartNames.RIGHT_HORN, ModelPartBuilder.create().uv(10, 13).cuboid(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F), ModelTransform.pivot(-7.0F, 2.0F, -12.0F));
      lv4.addChild(EntityModelPartNames.LEFT_HORN, ModelPartBuilder.create().uv(1, 13).cuboid(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F), ModelTransform.pivot(7.0F, 2.0F, -12.0F));
      int i = true;
      int j = true;
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(66, 42).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F), ModelTransform.pivot(-4.0F, 10.0F, -8.5F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(41, 42).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F), ModelTransform.pivot(4.0F, 10.0F, -8.5F));
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(21, 45).cuboid(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F), ModelTransform.pivot(-5.0F, 13.0F, 10.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(0, 45).cuboid(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F), ModelTransform.pivot(5.0F, 13.0F, 10.0F));
      return TexturedModelData.of(lv, 128, 64);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.body, this.rightFrontLeg, this.leftFrontLeg, this.rightHindLeg, this.leftHindLeg);
   }

   public void setAngles(MobEntity arg, float f, float g, float h, float i, float j) {
      this.rightEar.roll = -0.6981317F - g * MathHelper.sin(f);
      this.leftEar.roll = 0.6981317F + g * MathHelper.sin(f);
      this.head.yaw = i * 0.017453292F;
      int k = ((Hoglin)arg).getMovementCooldownTicks();
      float l = 1.0F - (float)MathHelper.abs(10 - 2 * k) / 10.0F;
      this.head.pitch = MathHelper.lerp(l, 0.87266463F, -0.34906584F);
      if (arg.isBaby()) {
         this.head.pivotY = MathHelper.lerp(l, 2.0F, 5.0F);
         this.mane.pivotZ = -3.0F;
      } else {
         this.head.pivotY = 2.0F;
         this.mane.pivotZ = -7.0F;
      }

      float m = 1.2F;
      this.rightFrontLeg.pitch = MathHelper.cos(f) * 1.2F * g;
      this.leftFrontLeg.pitch = MathHelper.cos(f + 3.1415927F) * 1.2F * g;
      this.rightHindLeg.pitch = this.leftFrontLeg.pitch;
      this.leftHindLeg.pitch = this.rightFrontLeg.pitch;
   }
}
