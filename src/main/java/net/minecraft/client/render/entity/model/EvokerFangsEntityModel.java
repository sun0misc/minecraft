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
public class EvokerFangsEntityModel extends SinglePartEntityModel {
   private static final String BASE = "base";
   private static final String UPPER_JAW = "upper_jaw";
   private static final String LOWER_JAW = "lower_jaw";
   private final ModelPart root;
   private final ModelPart base;
   private final ModelPart upperJaw;
   private final ModelPart lowerJaw;

   public EvokerFangsEntityModel(ModelPart root) {
      this.root = root;
      this.base = root.getChild("base");
      this.upperJaw = root.getChild("upper_jaw");
      this.lowerJaw = root.getChild("lower_jaw");
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild("base", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F), ModelTransform.pivot(-5.0F, 24.0F, -5.0F));
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(40, 0).cuboid(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
      lv2.addChild("upper_jaw", lv3, ModelTransform.pivot(1.5F, 24.0F, -4.0F));
      lv2.addChild("lower_jaw", lv3, ModelTransform.of(-1.5F, 24.0F, 4.0F, 0.0F, 3.1415927F, 0.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      float k = limbAngle * 2.0F;
      if (k > 1.0F) {
         k = 1.0F;
      }

      k = 1.0F - k * k * k;
      this.upperJaw.roll = 3.1415927F - k * 0.35F * 3.1415927F;
      this.lowerJaw.roll = 3.1415927F + k * 0.35F * 3.1415927F;
      float l = (limbAngle + MathHelper.sin(limbAngle * 2.7F)) * 0.6F * 12.0F;
      this.upperJaw.pivotY = 24.0F - l;
      this.lowerJaw.pivotY = this.upperJaw.pivotY;
      this.base.pivotY = this.upperJaw.pivotY;
   }

   public ModelPart getPart() {
      return this.root;
   }
}
