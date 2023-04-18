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
public class SheepWoolEntityModel extends QuadrupedEntityModel {
   private float headAngle;

   public SheepWoolEntityModel(ModelPart root) {
      super(root, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
   }

   public static TexturedModelData getTexturedModelData() {
      ModelData lv = new ModelData();
      ModelPartData lv2 = lv.getRoot();
      lv2.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.6F)), ModelTransform.pivot(0.0F, 6.0F, -8.0F));
      lv2.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(28, 8).cuboid(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new Dilation(1.75F)), ModelTransform.of(0.0F, 5.0F, 2.0F, 1.5707964F, 0.0F, 0.0F));
      ModelPartBuilder lv3 = ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new Dilation(0.5F));
      lv2.addChild(EntityModelPartNames.RIGHT_HIND_LEG, lv3, ModelTransform.pivot(-3.0F, 12.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.LEFT_HIND_LEG, lv3, ModelTransform.pivot(3.0F, 12.0F, 7.0F));
      lv2.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, lv3, ModelTransform.pivot(-3.0F, 12.0F, -5.0F));
      lv2.addChild(EntityModelPartNames.LEFT_FRONT_LEG, lv3, ModelTransform.pivot(3.0F, 12.0F, -5.0F));
      return TexturedModelData.of(lv, 64, 32);
   }

   public void animateModel(SheepEntity arg, float f, float g, float h) {
      super.animateModel(arg, f, g, h);
      this.head.pivotY = 6.0F + arg.getNeckAngle(h) * 9.0F;
      this.headAngle = arg.getHeadAngle(h);
   }

   public void setAngles(SheepEntity arg, float f, float g, float h, float i, float j) {
      super.setAngles(arg, f, g, h, i, j);
      this.head.pitch = this.headAngle;
   }
}
