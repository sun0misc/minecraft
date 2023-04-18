package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class AxolotlEntityModel extends AnimalModel {
   public static final float MOVING_IN_WATER_LEG_PITCH = 1.8849558F;
   private final ModelPart tail;
   private final ModelPart leftHindLeg;
   private final ModelPart rightHindLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart topGills;
   private final ModelPart leftGills;
   private final ModelPart rightGills;

   public AxolotlEntityModel(ModelPart root) {
      super(true, 8.0F, 3.35F);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.head = this.body.getChild(EntityModelPartNames.HEAD);
      this.rightHindLeg = this.body.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = this.body.getChild(EntityModelPartNames.LEFT_HIND_LEG);
      this.rightFrontLeg = this.body.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = this.body.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
      this.tail = this.body.getChild(EntityModelPartNames.TAIL);
      this.topGills = this.head.getChild(EntityModelPartNames.TOP_GILLS);
      this.leftGills = this.head.getChild(EntityModelPartNames.LEFT_GILLS);
      this.rightGills = this.head.getChild(EntityModelPartNames.RIGHT_GILLS);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      ModelPartData lv3 = lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 11).cuboid(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).uv(2, 17).cuboid(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F), ModelTransform.pivot(0.0F, 20.0F, 5.0F));
      Dilation lv4 = new Dilation(0.001F);
      ModelPartData lv5 = lv3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 1).cuboid(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, lv4), ModelTransform.pivot(0.0F, 0.0F, -9.0F));
      ModelPartBuilder lv6 = ModelPartBuilder.create().uv(3, 37).cuboid(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, lv4);
      ModelPartBuilder lv7 = ModelPartBuilder.create().uv(0, 40).cuboid(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, lv4);
      ModelPartBuilder lv8 = ModelPartBuilder.create().uv(11, 40).cuboid(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, lv4);
      lv5.addChild(EntityModelPartNames.TOP_GILLS, lv6, ModelTransform.pivot(0.0F, -3.0F, -1.0F));
      lv5.addChild(EntityModelPartNames.LEFT_GILLS, lv7, ModelTransform.pivot(-4.0F, 0.0F, -1.0F));
      lv5.addChild(EntityModelPartNames.RIGHT_GILLS, lv8, ModelTransform.pivot(4.0F, 0.0F, -1.0F));
      ModelPartBuilder lv9 = ModelPartBuilder.create().uv(2, 13).cuboid(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, lv4);
      ModelPartBuilder lv10 = ModelPartBuilder.create().uv(2, 13).cuboid(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, lv4);
      lv3.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv10, ModelTransform.pivot(-3.5F, 1.0F, -1.0F));
      lv3.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv9, ModelTransform.pivot(3.5F, 1.0F, -1.0F));
      lv3.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv10, ModelTransform.pivot(-3.5F, 1.0F, -8.0F));
      lv3.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv9, ModelTransform.pivot(3.5F, 1.0F, -8.0F));
      lv3.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(2, 19).cuboid(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), ModelTransform.pivot(0.0F, 0.0F, 1.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   protected Iterable getHeadParts() {
      return ImmutableList.of();
   }

   protected Iterable getBodyParts() {
      return ImmutableList.of(this.body);
   }

   public void setAngles(AxolotlEntity arg, float f, float g, float h, float i, float j) {
      this.resetAngles(arg, i, j);
      if (arg.isPlayingDead()) {
         this.setPlayingDeadAngles(i);
         this.updateAnglesCache(arg);
      } else {
         boolean bl = g > 1.0E-5F || arg.getPitch() != arg.prevPitch || arg.getYaw() != arg.prevYaw;
         if (arg.isInsideWaterOrBubbleColumn()) {
            if (bl) {
               this.setMovingInWaterAngles(h, j);
            } else {
               this.setStandingInWaterAngles(h);
            }

            this.updateAnglesCache(arg);
         } else {
            if (arg.isOnGround()) {
               if (bl) {
                  this.setMovingOnGroundAngles(h, i);
               } else {
                  this.setStandingOnGroundAngles(h, i);
               }
            }

            this.updateAnglesCache(arg);
         }
      }
   }

   private void updateAnglesCache(AxolotlEntity axolotl) {
      Map map = axolotl.getModelAngles();
      map.put("body", this.getAngles(this.body));
      map.put("head", this.getAngles(this.head));
      map.put("right_hind_leg", this.getAngles(this.rightHindLeg));
      map.put("left_hind_leg", this.getAngles(this.leftHindLeg));
      map.put("right_front_leg", this.getAngles(this.rightFrontLeg));
      map.put("left_front_leg", this.getAngles(this.leftFrontLeg));
      map.put("tail", this.getAngles(this.tail));
      map.put("top_gills", this.getAngles(this.topGills));
      map.put("left_gills", this.getAngles(this.leftGills));
      map.put("right_gills", this.getAngles(this.rightGills));
   }

   private Vector3f getAngles(ModelPart part) {
      return new Vector3f(part.pitch, part.yaw, part.roll);
   }

   private void setAngles(ModelPart part, Vector3f angles) {
      part.setAngles(angles.x(), angles.y(), angles.z());
   }

   private void resetAngles(AxolotlEntity axolotl, float headYaw, float headPitch) {
      this.body.pivotX = 0.0F;
      this.head.pivotY = 0.0F;
      this.body.pivotY = 20.0F;
      Map map = axolotl.getModelAngles();
      if (map.isEmpty()) {
         this.body.setAngles(headPitch * 0.017453292F, headYaw * 0.017453292F, 0.0F);
         this.head.setAngles(0.0F, 0.0F, 0.0F);
         this.leftHindLeg.setAngles(0.0F, 0.0F, 0.0F);
         this.rightHindLeg.setAngles(0.0F, 0.0F, 0.0F);
         this.leftFrontLeg.setAngles(0.0F, 0.0F, 0.0F);
         this.rightFrontLeg.setAngles(0.0F, 0.0F, 0.0F);
         this.leftGills.setAngles(0.0F, 0.0F, 0.0F);
         this.rightGills.setAngles(0.0F, 0.0F, 0.0F);
         this.topGills.setAngles(0.0F, 0.0F, 0.0F);
         this.tail.setAngles(0.0F, 0.0F, 0.0F);
      } else {
         this.setAngles(this.body, (Vector3f)map.get("body"));
         this.setAngles(this.head, (Vector3f)map.get("head"));
         this.setAngles(this.leftHindLeg, (Vector3f)map.get("left_hind_leg"));
         this.setAngles(this.rightHindLeg, (Vector3f)map.get("right_hind_leg"));
         this.setAngles(this.leftFrontLeg, (Vector3f)map.get("left_front_leg"));
         this.setAngles(this.rightFrontLeg, (Vector3f)map.get("right_front_leg"));
         this.setAngles(this.leftGills, (Vector3f)map.get("left_gills"));
         this.setAngles(this.rightGills, (Vector3f)map.get("right_gills"));
         this.setAngles(this.topGills, (Vector3f)map.get("top_gills"));
         this.setAngles(this.tail, (Vector3f)map.get("tail"));
      }

   }

   private float lerpAngleDegrees(float start, float end) {
      return this.lerpAngleDegrees(0.05F, start, end);
   }

   private float lerpAngleDegrees(float delta, float start, float end) {
      return MathHelper.lerpAngleDegrees(delta, start, end);
   }

   private void setAngles(ModelPart part, float pitch, float yaw, float roll) {
      part.setAngles(this.lerpAngleDegrees(part.pitch, pitch), this.lerpAngleDegrees(part.yaw, yaw), this.lerpAngleDegrees(part.roll, roll));
   }

   private void setStandingOnGroundAngles(float animationProgress, float headYaw) {
      float h = animationProgress * 0.09F;
      float i = MathHelper.sin(h);
      float j = MathHelper.cos(h);
      float k = i * i - 2.0F * i;
      float l = j * j - 3.0F * i;
      this.head.pitch = this.lerpAngleDegrees(this.head.pitch, -0.09F * k);
      this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0F);
      this.head.roll = this.lerpAngleDegrees(this.head.roll, -0.2F);
      this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, -0.1F + 0.1F * k);
      this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, 0.6F + 0.05F * l);
      this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, -this.topGills.pitch);
      this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
      this.setAngles(this.leftHindLeg, 1.1F, 1.0F, 0.0F);
      this.setAngles(this.leftFrontLeg, 0.8F, 2.3F, -0.5F);
      this.copyLegAngles();
      this.body.pitch = this.lerpAngleDegrees(0.2F, this.body.pitch, 0.0F);
      this.body.yaw = this.lerpAngleDegrees(this.body.yaw, headYaw * 0.017453292F);
      this.body.roll = this.lerpAngleDegrees(this.body.roll, 0.0F);
   }

   private void setMovingOnGroundAngles(float animationProgress, float headYaw) {
      float h = animationProgress * 0.11F;
      float i = MathHelper.cos(h);
      float j = (i * i - 2.0F * i) / 5.0F;
      float k = 0.7F * i;
      this.head.pitch = this.lerpAngleDegrees(this.head.pitch, 0.0F);
      this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.09F * i);
      this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0F);
      this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, this.head.yaw);
      this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, 0.6F - 0.08F * (i * i + 2.0F * MathHelper.sin(h)));
      this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, -this.topGills.pitch);
      this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
      this.setAngles(this.leftHindLeg, 0.9424779F, 1.5F - j, -0.1F);
      this.setAngles(this.leftFrontLeg, 1.0995574F, 1.5707964F - k, 0.0F);
      this.setAngles(this.rightHindLeg, this.leftHindLeg.pitch, -1.0F - j, 0.0F);
      this.setAngles(this.rightFrontLeg, this.leftFrontLeg.pitch, -1.5707964F - k, 0.0F);
      this.body.pitch = this.lerpAngleDegrees(0.2F, this.body.pitch, 0.0F);
      this.body.yaw = this.lerpAngleDegrees(this.body.yaw, headYaw * 0.017453292F);
      this.body.roll = this.lerpAngleDegrees(this.body.roll, 0.0F);
   }

   private void setStandingInWaterAngles(float animationProgress) {
      float g = animationProgress * 0.075F;
      float h = MathHelper.cos(g);
      float i = MathHelper.sin(g) * 0.15F;
      this.body.pitch = this.lerpAngleDegrees(this.body.pitch, -0.15F + 0.075F * h);
      ModelPart var10000 = this.body;
      var10000.pivotY -= i;
      this.head.pitch = this.lerpAngleDegrees(this.head.pitch, -this.body.pitch);
      this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, 0.2F * h);
      this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, -0.3F * h - 0.19F);
      this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
      this.setAngles(this.leftHindLeg, 2.3561945F - h * 0.11F, 0.47123894F, 1.7278761F);
      this.setAngles(this.leftFrontLeg, 0.7853982F - h * 0.2F, 2.042035F, 0.0F);
      this.copyLegAngles();
      this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, 0.5F * h);
      this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0F);
      this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0F);
   }

   private void setMovingInWaterAngles(float animationProgress, float headPitch) {
      float h = animationProgress * 0.33F;
      float i = MathHelper.sin(h);
      float j = MathHelper.cos(h);
      float k = 0.13F * i;
      this.body.pitch = this.lerpAngleDegrees(0.1F, this.body.pitch, headPitch * 0.017453292F + k);
      this.head.pitch = -k * 1.8F;
      ModelPart var10000 = this.body;
      var10000.pivotY -= 0.45F * j;
      this.topGills.pitch = this.lerpAngleDegrees(this.topGills.pitch, -0.5F * i - 0.8F);
      this.leftGills.yaw = this.lerpAngleDegrees(this.leftGills.yaw, 0.3F * i + 0.9F);
      this.rightGills.yaw = this.lerpAngleDegrees(this.rightGills.yaw, -this.leftGills.yaw);
      this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, 0.3F * MathHelper.cos(h * 0.9F));
      this.setAngles(this.leftHindLeg, 1.8849558F, -0.4F * i, 1.5707964F);
      this.setAngles(this.leftFrontLeg, 1.8849558F, -0.2F * j - 0.1F, 1.5707964F);
      this.copyLegAngles();
      this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0F);
      this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0F);
   }

   private void setPlayingDeadAngles(float headYaw) {
      this.setAngles(this.leftHindLeg, 1.4137167F, 1.0995574F, 0.7853982F);
      this.setAngles(this.leftFrontLeg, 0.7853982F, 2.042035F, 0.0F);
      this.body.pitch = this.lerpAngleDegrees(this.body.pitch, -0.15F);
      this.body.roll = this.lerpAngleDegrees(this.body.roll, 0.35F);
      this.copyLegAngles();
      this.body.yaw = this.lerpAngleDegrees(this.body.yaw, headYaw * 0.017453292F);
      this.head.pitch = this.lerpAngleDegrees(this.head.pitch, 0.0F);
      this.head.yaw = this.lerpAngleDegrees(this.head.yaw, 0.0F);
      this.head.roll = this.lerpAngleDegrees(this.head.roll, 0.0F);
      this.tail.yaw = this.lerpAngleDegrees(this.tail.yaw, 0.0F);
      this.setAngles(this.topGills, 0.0F, 0.0F, 0.0F);
      this.setAngles(this.leftGills, 0.0F, 0.0F, 0.0F);
      this.setAngles(this.rightGills, 0.0F, 0.0F, 0.0F);
   }

   private void copyLegAngles() {
      this.setAngles(this.rightHindLeg, this.leftHindLeg.pitch, -this.leftHindLeg.yaw, -this.leftHindLeg.roll);
      this.setAngles(this.rightFrontLeg, this.leftFrontLeg.pitch, -this.leftFrontLeg.yaw, -this.leftFrontLeg.roll);
   }
}
