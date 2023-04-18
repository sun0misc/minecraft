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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SnowGolemEntityModel extends SinglePartEntityModel {
   private static final String UPPER_BODY = "upper_body";
   private final ModelPart root;
   private final ModelPart upperBody;
   private final ModelPart head;
   private final ModelPart leftArm;
   private final ModelPart rightArm;

   public SnowGolemEntityModel(ModelPart root) {
      this.root = root;
      this.head = root.getChild(EntityModelPartNames.HEAD);
      this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
      this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
      this.upperBody = root.getChild("upper_body");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      float f = 4.0F;
      Dilation lv3 = new Dilation(-0.5F);
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, lv3), ModelTransform.pivot(0.0F, 4.0F, 0.0F));
      ModelPartBuilder lv4 = ModelPartBuilder.create().uv(32, 0).cuboid(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, lv3);
      lv2.addChild(EntityModelPartNames.LEFT_ARM, lv4, ModelTransform.of(5.0F, 6.0F, 1.0F, 0.0F, 0.0F, 1.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_ARM, lv4, ModelTransform.of(-5.0F, 6.0F, -1.0F, 0.0F, 3.1415927F, -1.0F));
      lv2.addChild("upper_body", ModelPartBuilder.create().uv(0, 16).cuboid(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, lv3), ModelTransform.pivot(0.0F, 13.0F, 0.0F));
      lv2.addChild("lower_body", ModelPartBuilder.create().uv(0, 36).cuboid(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, lv3), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 64);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.head.yaw = headYaw * 0.017453292F;
      this.head.pitch = headPitch * 0.017453292F;
      this.upperBody.yaw = headYaw * 0.017453292F * 0.25F;
      float k = MathHelper.sin(this.upperBody.yaw);
      float l = MathHelper.cos(this.upperBody.yaw);
      this.leftArm.yaw = this.upperBody.yaw;
      this.rightArm.yaw = this.upperBody.yaw + 3.1415927F;
      this.leftArm.pivotX = l * 5.0F;
      this.leftArm.pivotZ = -k * 5.0F;
      this.rightArm.pivotX = -l * 5.0F;
      this.rightArm.pivotZ = k * 5.0F;
   }

   public ModelPart getPart() {
      return this.root;
   }

   public ModelPart getHead() {
      return this.head;
   }
}
