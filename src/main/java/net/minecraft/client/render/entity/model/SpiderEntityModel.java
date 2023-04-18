package net.minecraft.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SpiderEntityModel extends SinglePartEntityModel {
   private static final String BODY0 = "body0";
   private static final String BODY1 = "body1";
   private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
   private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
   private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
   private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightMiddleLeg;
   private final ModelPart leftMiddleLeg;
   private final ModelPart rightMiddleFrontLeg;
   private final ModelPart leftMiddleFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;

   public SpiderEntityModel(ModelPart root) {
      this.root = root;
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
      this.rightMiddleLeg = root.getChild("right_middle_hind_leg");
      this.leftMiddleLeg = root.getChild("left_middle_hind_leg");
      this.rightMiddleFrontLeg = root.getChild("right_middle_front_leg");
      this.leftMiddleFrontLeg = root.getChild("left_middle_front_leg");
      this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      int i = true;
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(32, 4).cuboid(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), ModelTransform.pivot(0.0F, 15.0F, -3.0F));
      lv2.addChild("body0", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.pivot(0.0F, 15.0F, 0.0F));
      lv2.addChild("body1", ModelPartBuilder.create().uv(0, 12).cuboid(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), ModelTransform.pivot(0.0F, 15.0F, 9.0F));
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(18, 0).cuboid(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
      ModelPartBuilder lv4 = ModelPartBuilder.create().uv(18, 0).mirrored().cuboid(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-4.0F, 15.0F, 2.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv4, ModelTransform.pivot(4.0F, 15.0F, 2.0F));
      lv2.addChild("right_middle_hind_leg", lv3, ModelTransform.pivot(-4.0F, 15.0F, 1.0F));
      lv2.addChild("left_middle_hind_leg", lv4, ModelTransform.pivot(4.0F, 15.0F, 1.0F));
      lv2.addChild("right_middle_front_leg", lv3, ModelTransform.pivot(-4.0F, 15.0F, 0.0F));
      lv2.addChild("left_middle_front_leg", lv4, ModelTransform.pivot(4.0F, 15.0F, 0.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-4.0F, 15.0F, -1.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv4, ModelTransform.pivot(4.0F, 15.0F, -1.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public ModelPart getPart() {
      return this.root;
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.yaw = headYaw * 0.017453292F;
      this.head.pitch = headPitch * 0.017453292F;
      float k = 0.7853982F;
      this.rightHindLeg.roll = -0.7853982F;
      this.leftHindLeg.roll = 0.7853982F;
      this.rightMiddleLeg.roll = -0.58119464F;
      this.leftMiddleLeg.roll = 0.58119464F;
      this.rightMiddleFrontLeg.roll = -0.58119464F;
      this.leftMiddleFrontLeg.roll = 0.58119464F;
      this.rightFrontLeg.roll = -0.7853982F;
      this.leftFrontLeg.roll = 0.7853982F;
      float l = -0.0F;
      float m = 0.3926991F;
      this.rightHindLeg.yaw = 0.7853982F;
      this.leftHindLeg.yaw = -0.7853982F;
      this.rightMiddleLeg.yaw = 0.3926991F;
      this.leftMiddleLeg.yaw = -0.3926991F;
      this.rightMiddleFrontLeg.yaw = -0.3926991F;
      this.leftMiddleFrontLeg.yaw = 0.3926991F;
      this.rightFrontLeg.yaw = -0.7853982F;
      this.leftFrontLeg.yaw = 0.7853982F;
      float n = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbDistance;
      float o = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + 3.1415927F) * 0.4F) * limbDistance;
      float p = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + 1.5707964F) * 0.4F) * limbDistance;
      float q = -(MathHelper.cos(limbAngle * 0.6662F * 2.0F + 4.712389F) * 0.4F) * limbDistance;
      float r = Math.abs(MathHelper.sin(limbAngle * 0.6662F + 0.0F) * 0.4F) * limbDistance;
      float s = Math.abs(MathHelper.sin(limbAngle * 0.6662F + 3.1415927F) * 0.4F) * limbDistance;
      float t = Math.abs(MathHelper.sin(limbAngle * 0.6662F + 1.5707964F) * 0.4F) * limbDistance;
      float u = Math.abs(MathHelper.sin(limbAngle * 0.6662F + 4.712389F) * 0.4F) * limbDistance;
      ModelPart var10000 = this.rightHindLeg;
      var10000.yaw += n;
      var10000 = this.leftHindLeg;
      var10000.yaw += -n;
      var10000 = this.rightMiddleLeg;
      var10000.yaw += o;
      var10000 = this.leftMiddleLeg;
      var10000.yaw += -o;
      var10000 = this.rightMiddleFrontLeg;
      var10000.yaw += p;
      var10000 = this.leftMiddleFrontLeg;
      var10000.yaw += -p;
      var10000 = this.rightFrontLeg;
      var10000.yaw += q;
      var10000 = this.leftFrontLeg;
      var10000.yaw += -q;
      var10000 = this.rightHindLeg;
      var10000.roll += r;
      var10000 = this.leftHindLeg;
      var10000.roll += -r;
      var10000 = this.rightMiddleLeg;
      var10000.roll += s;
      var10000 = this.leftMiddleLeg;
      var10000.roll += -s;
      var10000 = this.rightMiddleFrontLeg;
      var10000.roll += t;
      var10000 = this.leftMiddleFrontLeg;
      var10000.roll += -t;
      var10000 = this.rightFrontLeg;
      var10000.roll += u;
      var10000 = this.leftFrontLeg;
      var10000.roll += -u;
   }
}
