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
import net.minecraft.entity.passive.SheepEntity;

@Environment(EnvType.CLIENT)
public class SheepEntityModel extends QuadrupedEntityModel {
   private float headPitchModifier;

   public SheepEntityModel(ModelPart root) {
      super(root, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = QuadrupedEntityModel.getModelData(12, Dilation.NONE);
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F), ModelTransform.pivot(0.0F, 6.0F, -8.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(28, 8).cuboid(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F), ModelTransform.of(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public void animateModel(SheepEntity arg, float f, float g, float h) {
      super.animateModel(arg, f, g, h);
      this.head.pivotY = 6.0F + arg.getNeckAngle(h) * 9.0F;
      this.headPitchModifier = arg.getHeadAngle(h);
   }

   public void setAngles(SheepEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles(arg, f, g, h, i, j);
      this.head.pitch = this.headPitchModifier;
   }
}
